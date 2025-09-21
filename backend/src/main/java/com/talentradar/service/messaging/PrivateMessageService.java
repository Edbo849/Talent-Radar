package com.talentradar.service.messaging;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.model.enums.MessageType;
import com.talentradar.model.messaging.ConversationParticipant;
import com.talentradar.model.messaging.PrivateConversation;
import com.talentradar.model.messaging.PrivateMessage;
import com.talentradar.model.user.User;
import com.talentradar.repository.messaging.ConversationParticipantRepository;
import com.talentradar.repository.messaging.PrivateConversationRepository;

import com.talentradar.repository.messaging.PrivateMessageRepository;

/**
 * Service responsible for managing private messages within conversations.
 * Handles message creation, retrieval, deletion, and read status management.
 */
@Service
@Transactional
public class PrivateMessageService {

    private static final Logger logger = LoggerFactory.getLogger(PrivateMessageService.class);

    @Autowired
    private PrivateMessageRepository messageRepository;

    @Autowired
    private PrivateConversationRepository conversationRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private MessageReadStatusService messageReadStatusService;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PrivateMessage sendMessage(Long conversationId, User sender, String messageText,
            MessageType messageType, String attachmentUrl, Long replyToMessageId) {

        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }
            if (sender == null) {
                throw new IllegalArgumentException("Sender is required");
            }
            if (messageText == null || messageText.trim().isEmpty()) {
                throw new IllegalArgumentException("Message text is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            // Verify sender is participant
            if (!participantRepository.existsByConversationAndUserAndIsActiveTrue(conversation, sender)) {
                throw new IllegalArgumentException("User is not a participant in this conversation");
            }

            PrivateMessage message = new PrivateMessage();
            message.setConversation(conversation);
            message.setSender(sender);
            message.setMessageText(messageText.trim());
            message.setMessageType(messageType != null ? messageType : MessageType.TEXT);
            message.setAttachmentUrl(attachmentUrl);

            // Handle reply
            if (replyToMessageId != null) {
                PrivateMessage replyToMessage = messageRepository.findById(replyToMessageId)
                        .orElseThrow(() -> new IllegalArgumentException("Reply message not found with ID: " + replyToMessageId));

                if (!replyToMessage.getConversation().getId().equals(conversationId)) {
                    throw new IllegalArgumentException("Reply message is not in the same conversation");
                }

                message.setReplyToMessage(replyToMessage);
            }

            PrivateMessage savedMessage = messageRepository.save(message);

            // Update conversation last message time
            conversation.setLastMessageAt(LocalDateTime.now());
            conversationRepository.save(conversation);

            // Update unread counts for other participants
            updateUnreadCounts(conversation, sender);

            // Mark as read for sender
            messageReadStatusService.markAsRead(savedMessage, sender);

            logger.info("Message sent in conversation {} by {}", conversationId, sender.getUsername());
            return savedMessage;

        } catch (IllegalArgumentException e) {
            logger.error("Error sending message in conversation {} by {}: {}",
                    conversationId,
                    sender != null ? sender.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to send message", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private void updateUnreadCounts(PrivateConversation conversation, User sender) {
        try {
            List<ConversationParticipant> participants = participantRepository
                    .findByConversationAndIsActiveTrue(conversation);

            for (ConversationParticipant participant : participants) {
                if (!participant.getUser().getId().equals(sender.getId())) {
                    participant.setUnreadCount(participant.getUnreadCount() + 1);
                    participantRepository.save(participant);
                }
            }

        } catch (Exception e) {
            logger.error("Error updating unread counts for conversation {}: {}",
                    conversation.getId(), e.getMessage());
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<PrivateMessage> getConversationMessages(Long conversationId, User requester, Pageable pageable) {
        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }
            if (requester == null) {
                throw new IllegalArgumentException("Requester is required");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            // Verify requester is participant
            if (!participantRepository.existsByConversationAndUserAndIsActiveTrue(conversation, requester)) {
                throw new IllegalArgumentException("User is not a participant in this conversation");
            }

            return messageRepository.findByConversationAndIsDeletedFalseOrderByCreatedAtDesc(conversation, pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting messages for conversation {} by {}: {}",
                    conversationId,
                    requester != null ? requester.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to retrieve conversation messages", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void markConversationAsRead(Long conversationId, User user) {
        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }
            if (user == null) {
                throw new IllegalArgumentException("User is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            // Verify user is participant
            ConversationParticipant participant = participantRepository
                    .findByConversationAndUser(conversation, user)
                    .orElseThrow(() -> new IllegalArgumentException("User is not a participant in this conversation"));

            // Reset unread count
            participant.setUnreadCount(0);
            participant.setLastReadAt(LocalDateTime.now());
            participantRepository.save(participant);

            // Mark all unread messages as read
            Page<PrivateMessage> unreadMessages = messageRepository
                    .findByConversationAndIsDeletedFalseOrderByCreatedAtDesc(conversation, Pageable.unpaged());

            for (PrivateMessage message : unreadMessages) {
                if (!message.getSender().getId().equals(user.getId())) {
                    messageReadStatusService.markAsRead(message, user);
                }
            }

            logger.info("Marked conversation {} as read for user {}", conversationId, user.getUsername());

        } catch (IllegalArgumentException e) {
            logger.error("Error marking conversation {} as read for user {}: {}",
                    conversationId,
                    user != null ? user.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to mark conversation as read", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteMessage(Long messageId, User requester) {
        try {
            if (messageId == null) {
                throw new IllegalArgumentException("Message ID is required");
            }
            if (requester == null) {
                throw new IllegalArgumentException("Requester is required");
            }

            PrivateMessage message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

            // Only sender can delete their message
            if (!message.getSender().getId().equals(requester.getId())) {
                throw new IllegalArgumentException("Only the message sender can delete this message");
            }

            message.setIsDeleted(true);
            messageRepository.save(message);

            logger.info("Message {} deleted by user {}", messageId, requester.getUsername());

        } catch (IllegalArgumentException e) {
            logger.error("Error deleting message {} by user {}: {}",
                    messageId,
                    requester != null ? requester.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to delete message", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public PrivateMessage getLatestMessage(Long conversationId) {
        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            return messageRepository.findTopByConversationAndIsDeletedFalseOrderByCreatedAtDesc(conversation);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting latest message for conversation {}: {}", conversationId, e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long conversationId, User user) {
        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }
            if (user == null) {
                throw new IllegalArgumentException("User is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            return messageRepository.countUnreadMessagesForUserInConversation(conversation, user.getId());

        } catch (IllegalArgumentException e) {
            logger.error("Error getting unread message count for conversation {} by user {}: {}",
                    conversationId,
                    user != null ? user.getUsername() : "null",
                    e.getMessage());
            return 0;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<PrivateMessage> searchMessages(Long conversationId, String searchTerm, User requester, Pageable pageable) {
        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                throw new IllegalArgumentException("Search term is required");
            }
            if (requester == null) {
                throw new IllegalArgumentException("Requester is required");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            // Verify requester is participant
            if (!participantRepository.existsByConversationAndUserAndIsActiveTrue(conversation, requester)) {
                throw new IllegalArgumentException("User is not a participant in this conversation");
            }

            return messageRepository.searchMessagesInConversation(conversation, searchTerm.trim(), pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error searching messages in conversation {} with term '{}' by user {}: {}",
                    conversationId, searchTerm,
                    requester != null ? requester.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to search messages", e);
        }
    }
}

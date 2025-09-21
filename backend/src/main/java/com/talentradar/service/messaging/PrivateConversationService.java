package com.talentradar.service.messaging;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.model.messaging.ConversationParticipant;
import com.talentradar.model.messaging.MessageReadStatus;
import com.talentradar.model.messaging.PrivateConversation;
import com.talentradar.model.user.User;
import com.talentradar.repository.messaging.ConversationParticipantRepository;
import com.talentradar.repository.messaging.MessageReadStatusRepository;
import com.talentradar.repository.messaging.PrivateConversationRepository;
import com.talentradar.repository.user.UserRepository;

/**
 * Service responsible for managing private conversations between users. Handles
 * conversation creation, participant management, and conversation operations.
 */
@Service
@Transactional
public class PrivateConversationService {

    private static final Logger logger = LoggerFactory.getLogger(PrivateConversationService.class);

    @Autowired
    private PrivateConversationRepository conversationRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private MessageReadStatusRepository messageReadStatusRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PrivateConversation createDirectConversation(User user1, User user2) {
        try {
            if (user1 == null || user2 == null) {
                throw new IllegalArgumentException("Both users are required for creating a conversation");
            }
            if (user1.getId().equals(user2.getId())) {
                throw new IllegalArgumentException("Cannot create conversation with the same user");
            }

            // Check if conversation already exists
            Optional<PrivateConversation> existing = conversationRepository
                    .findDirectConversationBetweenUsers(user1, user2);

            if (existing.isPresent()) {
                logger.debug("Direct conversation already exists between {} and {}",
                        user1.getUsername(), user2.getUsername());
                return existing.get();
            }

            // Create new conversation
            PrivateConversation conversation = new PrivateConversation();
            conversation.setCreatedBy(user1);
            conversation.setIsGroupConversation(false);
            conversation.setConversationName(getDisplayName(user1) + " & " + getDisplayName(user2));

            PrivateConversation savedConversation = conversationRepository.save(conversation);

            // Add participants
            addParticipant(savedConversation, user1);
            addParticipant(savedConversation, user2);

            logger.info("Created direct conversation between {} and {}",
                    user1.getUsername(), user2.getUsername());

            return savedConversation;

        } catch (IllegalArgumentException e) {
            logger.error("Error creating direct conversation between {} and {}: {}",
                    user1 != null ? user1.getUsername() : "null",
                    user2 != null ? user2.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to create direct conversation", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PrivateConversation createGroupConversation(User creator, String conversationName, List<Long> participantIds) {
        try {
            if (creator == null) {
                throw new IllegalArgumentException("Creator is required");
            }
            if (conversationName == null || conversationName.trim().isEmpty()) {
                throw new IllegalArgumentException("Conversation name is required");
            }
            if (participantIds == null || participantIds.isEmpty()) {
                throw new IllegalArgumentException("At least one participant is required");
            }

            PrivateConversation conversation = new PrivateConversation();
            conversation.setCreatedBy(creator);
            conversation.setIsGroupConversation(true);
            conversation.setConversationName(conversationName.trim());

            PrivateConversation savedConversation = conversationRepository.save(conversation);

            // Add creator as participant
            addParticipant(savedConversation, creator);

            // Add other participants
            int successfullyAdded = 0;
            for (Long userId : participantIds) {
                try {
                    if (!userId.equals(creator.getId())) { // Don't add creator twice
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
                        addParticipant(savedConversation, user);
                        successfullyAdded++;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to add user {} to conversation: {}", userId, e.getMessage());
                }
            }

            logger.info("Created group conversation '{}' with {} participants",
                    conversationName, successfullyAdded + 1);

            return savedConversation;

        } catch (IllegalArgumentException e) {
            logger.error("Error creating group conversation '{}' by {}: {}",
                    conversationName,
                    creator != null ? creator.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to create group conversation", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private void addParticipant(PrivateConversation conversation, User user) {
        // Check if already participant
        if (participantRepository.existsByConversationAndUserAndIsActiveTrue(conversation, user)) {
            logger.debug("User {} is already a participant in conversation {}",
                    user.getUsername(), conversation.getId());
            return;
        }

        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversation(conversation);
        participant.setUser(user);
        participant.setIsActive(true);
        participant.setUnreadCount(0);

        participantRepository.save(participant);
        logger.debug("Added user {} as participant to conversation {}",
                user.getUsername(), conversation.getId());
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void addParticipantToGroup(Long conversationId, Long userId, User requester) {
        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (requester == null) {
                throw new IllegalArgumentException("Requester is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            if (!conversation.getIsGroupConversation()) {
                throw new IllegalArgumentException("Cannot add participants to direct conversations");
            }

            // Check if requester is participant
            if (!participantRepository.existsByConversationAndUserAndIsActiveTrue(conversation, requester)) {
                throw new IllegalArgumentException("Only participants can add new members");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

            addParticipant(conversation, user);

            logger.info("Added user {} to conversation {} by {}",
                    user.getUsername(), conversationId, requester.getUsername());

        } catch (IllegalArgumentException e) {
            logger.error("Error adding user {} to conversation {} by {}: {}",
                    userId, conversationId,
                    requester != null ? requester.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to add participant to group", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void leaveConversation(Long conversationId, User user) {
        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }
            if (user == null) {
                throw new IllegalArgumentException("User is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            ConversationParticipant participant = participantRepository
                    .findByConversationAndUser(conversation, user)
                    .orElseThrow(() -> new IllegalArgumentException("User is not a participant in this conversation"));

            participant.setIsActive(false);
            participant.setLeftAt(LocalDateTime.now());

            participantRepository.save(participant);

            logger.info("User {} left conversation {}", user.getUsername(), conversationId);

        } catch (IllegalArgumentException e) {
            logger.error("Error leaving conversation {} by {}: {}", conversationId,
                    user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to leave conversation", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PrivateConversation> getUserConversations(User user) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User is required");
            }

            return conversationRepository.findConversationsForUser(user);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting conversations for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to retrieve user conversations", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PrivateConversation> searchUserConversations(User user, String searchTerm) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User is required");
            }
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getUserConversations(user);
            }

            return conversationRepository.searchConversationsForUser(user, searchTerm.trim());

        } catch (IllegalArgumentException e) {
            logger.error("Error searching conversations for user {} with term '{}': {}",
                    user != null ? user.getUsername() : "null", searchTerm, e.getMessage());
            throw new RuntimeException("Failed to search user conversations", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<PrivateConversation> findById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }

            return conversationRepository.findById(id);

        } catch (IllegalArgumentException e) {
            logger.error("Error finding conversation by ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isUserParticipant(Long conversationId, User user) {
        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }
            if (user == null) {
                throw new IllegalArgumentException("User is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            return participantRepository.existsByConversationAndUserAndIsActiveTrue(conversation, user);

        } catch (IllegalArgumentException e) {
            logger.error("Error checking if user {} is participant in conversation {}: {}",
                    user != null ? user.getUsername() : "null", conversationId, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<ConversationParticipant> getConversationParticipants(Long conversationId) {
        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            return participantRepository.findByConversationAndIsActiveTrue(conversation);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting participants for conversation {}: {}", conversationId, e.getMessage());
            throw new RuntimeException("Failed to retrieve conversation participants", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void updateConversationName(Long conversationId, String newName, User requester) {
        try {
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required");
            }
            if (newName == null || newName.trim().isEmpty()) {
                throw new IllegalArgumentException("Conversation name is required");
            }
            if (requester == null) {
                throw new IllegalArgumentException("Requester is required");
            }

            PrivateConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

            if (!conversation.getIsGroupConversation()) {
                throw new IllegalArgumentException("Cannot rename direct conversations");
            }

            // Check if requester is participant
            if (!participantRepository.existsByConversationAndUserAndIsActiveTrue(conversation, requester)) {
                throw new IllegalArgumentException("Only participants can rename conversations");
            }

            conversation.setConversationName(newName.trim());
            conversationRepository.save(conversation);

            logger.info("Conversation {} renamed to '{}' by {}",
                    conversationId, newName.trim(), requester.getUsername());

        } catch (IllegalArgumentException e) {
            logger.error("Error renaming conversation {} to '{}' by {}: {}",
                    conversationId, newName,
                    requester != null ? requester.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to update conversation name", e);
        }
    }

    /**
     * Retrieves the current read status of messages in a conversation.
     */
    public List<MessageReadStatus> getMessageReadStatus(Long messageId) {
        return messageReadStatusRepository.findByMessageId(messageId);
    }

    /**
     * Retrieves the current read status of messages in a conversation.
     */
    public List<MessageReadStatus> getConversationReadStatus(Long conversationId) {
        return messageReadStatusRepository.findByConversationId(conversationId);
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private String getDisplayName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        } else if (user.getLastName() != null) {
            return user.getLastName();
        } else {
            return user.getUsername();
        }
    }
}

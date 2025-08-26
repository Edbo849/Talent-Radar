package com.talentradar.controller.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.messaging.PrivateMessageDTO;
import com.talentradar.exception.MessageNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.enums.MessageType;
import com.talentradar.model.messaging.PrivateMessage;
import com.talentradar.model.user.User;
import com.talentradar.service.messaging.PrivateMessageService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for managing private messages within conversations. Provides
 * endpoints for sending, receiving, and managing private messages with proper
 * error handling and validation.
 */
@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:3000")
public class PrivateMessageController {

    private static final Logger logger = LoggerFactory.getLogger(PrivateMessageController.class);

    @Autowired
    private PrivateMessageService messageService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves paginated messages from a specific conversation.
     */
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Page<PrivateMessageDTO>> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Pageable pageable = PageRequest.of(page, size);

            logger.debug("Retrieving messages for conversation {} (page {}, size {})",
                    conversationId, page, size);

            Page<PrivateMessage> messages = messageService.getConversationMessages(conversationId, user, pageable);
            Page<PrivateMessageDTO> messageDTOs = messages.map(this::convertToDTO);

            logger.info("Retrieved {} messages for conversation {} for user {}",
                    messages.getTotalElements(), conversationId, user.getUsername());

            return ResponseEntity.ok(messageDTOs);
        } catch (UserNotFoundException e) {
            logger.warn("User not found when retrieving messages: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Access denied when retrieving messages: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error retrieving messages for conversation: {}", conversationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Searches for messages within a specific conversation.
     */
    @GetMapping("/conversation/{conversationId}/search")
    public ResponseEntity<Page<PrivateMessageDTO>> searchMessages(
            @PathVariable Long conversationId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            if (query == null || query.trim().isEmpty()) {
                logger.warn("Empty search query provided for conversation: {}", conversationId);
                return ResponseEntity.badRequest().build();
            }

            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Pageable pageable = PageRequest.of(page, size);

            logger.debug("Searching messages in conversation {} with query: {}", conversationId, query);

            Page<PrivateMessage> messages = messageService.searchMessages(conversationId, query, user, pageable);
            Page<PrivateMessageDTO> messageDTOs = messages.map(this::convertToDTO);

            logger.info("Found {} messages in conversation {} matching query: {}",
                    messages.getTotalElements(), conversationId, query);

            return ResponseEntity.ok(messageDTOs);
        } catch (UserNotFoundException e) {
            logger.warn("User not found when searching messages: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Access denied when searching messages: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error searching messages in conversation: {} with query: {}", conversationId, query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves the latest message from a specific conversation.
     */
    @GetMapping("/conversation/{conversationId}/latest")
    public ResponseEntity<PrivateMessageDTO> getLatestMessage(
            @PathVariable Long conversationId,
            HttpServletRequest request) {

        try {
            logger.debug("Retrieving latest message for conversation: {}", conversationId);

            PrivateMessage message = messageService.getLatestMessage(conversationId);

            if (message == null) {
                logger.debug("No messages found in conversation: {}", conversationId);
                return ResponseEntity.notFound().build();
            }

            logger.debug("Retrieved latest message from conversation: {}", conversationId);
            return ResponseEntity.ok(convertToDTO(message));
        } catch (Exception e) {
            logger.error("Error retrieving latest message for conversation: {}", conversationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Sends a new message in a conversation.
     */
    @PostMapping
    public ResponseEntity<PrivateMessageDTO> sendMessage(
            @Valid @RequestBody MessageSendRequest request,
            @RequestParam(required = false) Long senderId,
            HttpServletRequest httpRequest) {

        try {
            if (request.getMessageText() == null || request.getMessageText().trim().isEmpty()) {
                logger.warn("Empty message text provided");
                return ResponseEntity.badRequest().build();
            }

            // TODO: Get sender from authentication context
            User sender = userService.findById(senderId != null ? senderId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("Sender not found"));

            logger.info("User {} sending message to conversation {}",
                    sender.getUsername(), request.getConversationId());

            PrivateMessage message = messageService.sendMessage(
                    request.getConversationId(),
                    sender,
                    request.getMessageText(),
                    request.getMessageType(),
                    request.getAttachmentUrl(),
                    request.getReplyToMessageId()
            );

            logger.info("Successfully sent message with ID: {}", message.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(message));
        } catch (UserNotFoundException e) {
            logger.warn("User not found when sending message: {}", senderId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot send message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid message data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error sending message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Marks all messages in a conversation as read for the current user.
     */
    @PutMapping("/conversation/{conversationId}/mark-read")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable Long conversationId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            logger.debug("Marking conversation {} as read for user {}",
                    conversationId, user.getUsername());

            messageService.markConversationAsRead(conversationId, user);

            logger.info("Marked conversation {} as read for user {}",
                    conversationId, user.getUsername());

            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            logger.warn("User not found when marking conversation as read: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot mark conversation as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error marking conversation {} as read", conversationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a specific message.
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            logger.info("User {} attempting to delete message {}", user.getUsername(), messageId);

            messageService.deleteMessage(messageId, user);

            logger.info("Successfully deleted message {} by user {}", messageId, user.getUsername());
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            logger.warn("User not found when deleting message: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (MessageNotFoundException e) {
            logger.warn("Message not found when deleting: {}", messageId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot delete message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error deleting message: {}", messageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Converts a PrivateMessage entity to a PrivateMessageDTO for API
     * responses.
     */
    private PrivateMessageDTO convertToDTO(PrivateMessage message) {
        PrivateMessageDTO dto = new PrivateMessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getUsername());
        dto.setMessageText(message.getMessageText());
        dto.setMessageType(message.getMessageType().toString());
        dto.setAttachmentUrl(message.getAttachmentUrl());
        dto.setReplyToMessageId(message.getReplyToMessage() != null ? message.getReplyToMessage().getId() : null);
        dto.setIsDeleted(message.getIsDeleted());
        dto.setReadCount(message.getReadCount());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        return dto;
    }

    // Request DTO
    public static class MessageSendRequest {

        private Long conversationId;
        private String messageText;
        private MessageType messageType;
        private String attachmentUrl;
        private Long replyToMessageId;

        public Long getConversationId() {
            return conversationId;
        }

        public void setConversationId(Long conversationId) {
            this.conversationId = conversationId;
        }

        public String getMessageText() {
            return messageText;
        }

        public void setMessageText(String messageText) {
            this.messageText = messageText;
        }

        public MessageType getMessageType() {
            return messageType;
        }

        public void setMessageType(MessageType messageType) {
            this.messageType = messageType;
        }

        public String getAttachmentUrl() {
            return attachmentUrl;
        }

        public void setAttachmentUrl(String attachmentUrl) {
            this.attachmentUrl = attachmentUrl;
        }

        public Long getReplyToMessageId() {
            return replyToMessageId;
        }

        public void setReplyToMessageId(Long replyToMessageId) {
            this.replyToMessageId = replyToMessageId;
        }
    }
}

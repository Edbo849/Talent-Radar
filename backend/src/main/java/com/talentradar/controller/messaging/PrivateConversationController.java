package com.talentradar.controller.messaging;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.messaging.ConversationParticipantDTO;
import com.talentradar.dto.messaging.MessageReadStatusDTO;
import com.talentradar.dto.messaging.PrivateConversationDTO;
import com.talentradar.exception.ConversationNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.messaging.ConversationParticipant;
import com.talentradar.model.messaging.MessageReadStatus;
import com.talentradar.model.messaging.PrivateConversation;
import com.talentradar.model.user.User;
import com.talentradar.service.messaging.PrivateConversationService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for managing private conversations between users. Provides
 * endpoints for creating, retrieving, and managing private conversation threads
 * with proper error handling and validation.
 */
@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "http://localhost:3000")
public class PrivateConversationController {

    private static final Logger logger = LoggerFactory.getLogger(PrivateConversationController.class);

    @Autowired
    private PrivateConversationService conversationService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves all conversations for the current user.
     */
    @GetMapping
    public ResponseEntity<List<PrivateConversationDTO>> getUserConversations(
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            logger.debug("Retrieving conversations for user: {}", user.getUsername());

            List<PrivateConversation> conversations = conversationService.getUserConversations(user);
            List<PrivateConversationDTO> conversationDTOs = conversations.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} conversations for user {}",
                    conversations.size(), user.getUsername());

            return ResponseEntity.ok(conversationDTOs);
        } catch (UserNotFoundException e) {
            logger.warn("User not found when retrieving conversations: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving conversations for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Searches conversations for the current user based on query.
     */
    @GetMapping("/search")
    public ResponseEntity<List<PrivateConversationDTO>> searchUserConversations(
            @RequestParam String query,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            if (query == null || query.trim().isEmpty()) {
                logger.warn("Empty search query provided");
                return ResponseEntity.badRequest().build();
            }

            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            logger.debug("Searching conversations for user {} with query: {}",
                    user.getUsername(), query);

            List<PrivateConversation> conversations = conversationService.searchUserConversations(user, query);
            List<PrivateConversationDTO> conversationDTOs = conversations.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Found {} conversations for user {} matching query: {}",
                    conversations.size(), user.getUsername(), query);

            return ResponseEntity.ok(conversationDTOs);
        } catch (UserNotFoundException e) {
            logger.warn("User not found when searching conversations: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error searching conversations for user: {} with query: {}", userId, query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a specific conversation by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PrivateConversationDTO> getConversationById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            PrivateConversation conversation = conversationService.findById(id)
                    .orElseThrow(() -> new ConversationNotFoundException("Conversation not found with ID: " + id));

            // Verify user is participant
            if (!conversationService.isUserParticipant(id, user)) {
                logger.warn("User {} attempted to access conversation {} without permission",
                        user.getUsername(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            logger.debug("Retrieved conversation {} for user {}", id, user.getUsername());
            return ResponseEntity.ok(convertToDTO(conversation));
        } catch (UserNotFoundException | ConversationNotFoundException e) {
            logger.warn("Resource not found when retrieving conversation: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving conversation with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all participants in a specific conversation.
     */
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ConversationParticipantDTO>> getConversationParticipants(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Verify user is participant
            if (!conversationService.isUserParticipant(id, user)) {
                logger.warn("User {} attempted to access participants of conversation {} without permission",
                        user.getUsername(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<ConversationParticipant> participants = conversationService.getConversationParticipants(id);
            List<ConversationParticipantDTO> participantDTOs = participants.stream()
                    .map(this::convertParticipantToDTO)
                    .toList();

            logger.debug("Retrieved {} participants for conversation {}", participants.size(), id);
            return ResponseEntity.ok(participantDTOs);
        } catch (UserNotFoundException e) {
            logger.warn("User not found when retrieving participants: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Access denied when retrieving participants: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error retrieving participants for conversation: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates a new direct conversation between two users.
     */
    @PostMapping("/direct")
    public ResponseEntity<PrivateConversationDTO> createDirectConversation(
            @Valid @RequestBody DirectConversationRequest request,
            @RequestParam(required = false) Long userId,
            HttpServletRequest httpRequest) {

        try {
            // TODO: Get user from authentication context
            User user1 = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            User user2 = userService.findById(request.getOtherUserId())
                    .orElseThrow(() -> new UserNotFoundException("Other user not found with ID: " + request.getOtherUserId()));

            if (user1.getId().equals(user2.getId())) {
                logger.warn("User {} attempted to create conversation with themselves", user1.getUsername());
                return ResponseEntity.badRequest().build();
            }

            logger.info("Creating direct conversation between {} and {}",
                    user1.getUsername(), user2.getUsername());

            PrivateConversation conversation = conversationService.createDirectConversation(user1, user2);

            logger.info("Successfully created direct conversation with ID: {}", conversation.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(conversation));
        } catch (UserNotFoundException e) {
            logger.warn("User not found when creating direct conversation: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot create direct conversation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error creating direct conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates a new group conversation with multiple participants.
     */
    @PostMapping("/group")
    public ResponseEntity<PrivateConversationDTO> createGroupConversation(
            @Valid @RequestBody GroupConversationRequest request,
            @RequestParam(required = false) Long creatorId,
            HttpServletRequest httpRequest) {

        try {
            // TODO: Get creator from authentication context
            User creator = userService.findById(creatorId != null ? creatorId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("Creator not found"));

            if (request.getParticipantIds() == null || request.getParticipantIds().isEmpty()) {
                logger.warn("Empty participant list provided for group conversation");
                return ResponseEntity.badRequest().build();
            }

            logger.info("Creating group conversation '{}' with {} participants",
                    request.getConversationName(), request.getParticipantIds().size());

            PrivateConversation conversation = conversationService.createGroupConversation(
                    creator,
                    request.getConversationName(),
                    request.getParticipantIds()
            );

            logger.info("Successfully created group conversation with ID: {}", conversation.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(conversation));
        } catch (UserNotFoundException e) {
            logger.warn("User not found when creating group conversation: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid data for group conversation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating group conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Adds a new participant to an existing group conversation.
     */
    @PutMapping("/{id}/participants")
    public ResponseEntity<Void> addParticipantToGroup(
            @PathVariable Long id,
            @Valid @RequestBody AddParticipantRequest request,
            @RequestParam(required = false) Long requesterId,
            HttpServletRequest httpRequest) {

        try {
            // TODO: Get requester from authentication context
            User requester = userService.findById(requesterId != null ? requesterId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("Requester not found"));

            logger.info("User {} adding participant {} to conversation {}",
                    requester.getUsername(), request.getUserId(), id);

            conversationService.addParticipantToGroup(id, request.getUserId(), requester);

            logger.info("Successfully added participant {} to conversation {}",
                    request.getUserId(), id);

            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException | ConversationNotFoundException e) {
            logger.warn("Resource not found when adding participant: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot add participant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error adding participant to conversation: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Removes the current user from a conversation.
     */
    @PutMapping("/{id}/leave")
    public ResponseEntity<Void> leaveConversation(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            logger.info("User {} leaving conversation {}", user.getUsername(), id);

            conversationService.leaveConversation(id, user);

            logger.info("User {} successfully left conversation {}", user.getUsername(), id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException | ConversationNotFoundException e) {
            logger.warn("Resource not found when leaving conversation: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot leave conversation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error leaving conversation: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates the name of a group conversation.
     */
    @PutMapping("/{id}/name")
    public ResponseEntity<Void> updateConversationName(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNameRequest request,
            @RequestParam(required = false) Long requesterId,
            HttpServletRequest httpRequest) {

        try {
            if (request.getNewName() == null || request.getNewName().trim().isEmpty()) {
                logger.warn("Empty conversation name provided");
                return ResponseEntity.badRequest().build();
            }

            // TODO: Get requester from authentication context
            User requester = userService.findById(requesterId != null ? requesterId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("Requester not found"));

            logger.info("User {} updating conversation {} name to '{}'",
                    requester.getUsername(), id, request.getNewName());

            conversationService.updateConversationName(id, request.getNewName(), requester);

            logger.info("Successfully updated conversation {} name", id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException | ConversationNotFoundException e) {
            logger.warn("Resource not found when updating conversation name: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot update conversation name: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error updating conversation name: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves read status for messages in a conversation.
     */
    @GetMapping("/{id}/read-status")
    public ResponseEntity<List<MessageReadStatusDTO>> getMessageReadStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Long messageId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            // Verify user is participant
            if (!conversationService.isUserParticipant(id, user)) {
                logger.warn("User {} attempted to access read status of conversation {} without permission",
                        user.getUsername(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<MessageReadStatus> readStatuses;
            if (messageId != null) {
                readStatuses = conversationService.getMessageReadStatus(messageId);
            } else {
                readStatuses = conversationService.getConversationReadStatus(id);
            }

            List<MessageReadStatusDTO> readStatusDTOs = readStatuses.stream()
                    .map(this::convertReadStatusToDTO)
                    .toList();

            logger.debug("Retrieved {} read statuses for conversation {}", readStatuses.size(), id);
            return ResponseEntity.ok(readStatusDTOs);
        } catch (UserNotFoundException e) {
            logger.warn("User not found when retrieving read status: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving read status for conversation: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Checks if the current user is a participant in the conversation.
     */
    @GetMapping("/{id}/is-participant")
    public ResponseEntity<Boolean> isUserParticipant(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        try {
            // TODO: Get user from authentication context
            User user = userService.findById(userId != null ? userId : 1L)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            boolean isParticipant = conversationService.isUserParticipant(id, user);

            logger.debug("User {} participation in conversation {}: {}",
                    user.getUsername(), id, isParticipant);

            return ResponseEntity.ok(isParticipant);
        } catch (UserNotFoundException e) {
            logger.warn("User not found when checking participation: {}", userId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error checking user participation in conversation: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Converts a PrivateConversation entity to a PrivateConversationDTO for API
     * responses.
     */
    private PrivateConversationDTO convertToDTO(PrivateConversation conversation) {
        PrivateConversationDTO dto = new PrivateConversationDTO();
        dto.setId(conversation.getId());
        dto.setConversationName(conversation.getConversationName());
        dto.setCreatedById(conversation.getCreatedBy().getId());
        dto.setCreatedByName(conversation.getCreatedBy().getUsername());
        dto.setIsGroupConversation(conversation.getIsGroupConversation());
        dto.setLastMessageAt(conversation.getLastMessageAt());
        dto.setCreatedAt(conversation.getCreatedAt());
        return dto;
    }

    /**
     * Converts a ConversationParticipant entity to a ConversationParticipantDTO
     * for API responses.
     */
    private ConversationParticipantDTO convertParticipantToDTO(ConversationParticipant participant) {
        ConversationParticipantDTO dto = new ConversationParticipantDTO();
        dto.setId(participant.getId());
        dto.setUserId(participant.getUser().getId());
        dto.setUserName(participant.getUser().getUsername());

        if (participant.getUser().getProfileImageUrl() != null) {
            dto.setUserProfileImageUrl(participant.getUser().getProfileImageUrl());
        }

        dto.setJoinedAt(participant.getJoinedAt());
        dto.setLeftAt(participant.getLeftAt());
        dto.setLastReadAt(participant.getLastReadAt());
        dto.setUnreadCount(participant.getUnreadCount());
        dto.setIsActive(participant.getIsActive());
        return dto;
    }

    /**
     * Converts a MessageReadStatus entity to a MessageReadStatusDTO for API
     * responses.
     */
    private MessageReadStatusDTO convertReadStatusToDTO(MessageReadStatus readStatus) {
        MessageReadStatusDTO dto = new MessageReadStatusDTO();
        dto.setId(readStatus.getId());
        dto.setMessageId(readStatus.getMessage().getId());
        dto.setUserId(readStatus.getUser().getId());
        dto.setUserName(readStatus.getUser().getUsername());
        dto.setReadAt(readStatus.getReadAt());
        return dto;
    }

    // Request DTOs
    public static class DirectConversationRequest {

        private Long otherUserId;

        public Long getOtherUserId() {
            return otherUserId;
        }

        public void setOtherUserId(Long otherUserId) {
            this.otherUserId = otherUserId;
        }
    }

    public static class GroupConversationRequest {

        private String conversationName;
        private List<Long> participantIds;

        public String getConversationName() {
            return conversationName;
        }

        public void setConversationName(String conversationName) {
            this.conversationName = conversationName;
        }

        public List<Long> getParticipantIds() {
            return participantIds;
        }

        public void setParticipantIds(List<Long> participantIds) {
            this.participantIds = participantIds;
        }
    }

    public static class AddParticipantRequest {

        private Long userId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }

    public static class UpdateNameRequest {

        private String newName;

        public String getNewName() {
            return newName;
        }

        public void setNewName(String newName) {
            this.newName = newName;
        }
    }
}

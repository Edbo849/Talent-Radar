package com.talentradar.controller.discussion;

import java.util.List;

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

import com.talentradar.dto.discussion.DiscussionReplyDTO;
import com.talentradar.model.discussion.DiscussionReply;
import com.talentradar.model.user.User;
import com.talentradar.service.discussion.DiscussionReplyService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * REST controller for managing discussion thread replies. Provides endpoints
 * for creating, editing, and managing replies to discussion threads.
 */
@RestController
@RequestMapping("/api/discussions/replies")
@CrossOrigin(origins = "http://localhost:3000")
public class DiscussionReplyController {

    private static final Logger logger = LoggerFactory.getLogger(DiscussionReplyController.class);
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private DiscussionReplyService replyService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves all replies for a specific thread.
     */
    @GetMapping("/thread/{threadId}")
    public ResponseEntity<List<DiscussionReplyDTO>> getThreadReplies(@PathVariable @Positive Long threadId) {
        try {
            List<DiscussionReply> replies = replyService.getThreadReplies(threadId);
            List<DiscussionReplyDTO> replyDTOs = replies.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Retrieved {} replies for thread ID: {}", replies.size(), threadId);
            return ResponseEntity.ok(replyDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving replies for thread ID: {}", threadId, e);
            throw new RuntimeException("Failed to retrieve thread replies: " + e.getMessage());
        }
    }

    /**
     * Retrieves paginated replies for a specific thread.
     */
    @GetMapping("/thread/{threadId}/paginated")
    public ResponseEntity<Page<DiscussionReplyDTO>> getThreadRepliesPaginated(
            @PathVariable @Positive Long threadId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        try {
            // Validate page size
            if (size > MAX_PAGE_SIZE) {
                size = MAX_PAGE_SIZE;
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<DiscussionReply> replies = replyService.getThreadRepliesPaginated(threadId, pageable);
            Page<DiscussionReplyDTO> replyDTOs = replies.map(this::convertToDTO);

            logger.debug("Retrieved {} paginated replies for thread ID: {} (page {}, size {})",
                    replies.getTotalElements(), threadId, page, size);
            return ResponseEntity.ok(replyDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving paginated replies for thread ID: {}", threadId, e);
            throw new RuntimeException("Failed to retrieve paginated replies: " + e.getMessage());
        }
    }

    /**
     * Retrieves all replies to a specific reply (nested replies).
     */
    @GetMapping("/{replyId}/replies")
    public ResponseEntity<List<DiscussionReplyDTO>> getRepliesToReply(@PathVariable @Positive Long replyId) {
        try {
            List<DiscussionReply> replies = replyService.getRepliesToReply(replyId);
            List<DiscussionReplyDTO> replyDTOs = replies.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Retrieved {} nested replies for reply ID: {}", replies.size(), replyId);
            return ResponseEntity.ok(replyDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving nested replies for reply ID: {}", replyId, e);
            throw new RuntimeException("Failed to retrieve nested replies: " + e.getMessage());
        }
    }

    /**
     * Retrieves paginated replies by a specific author.
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<DiscussionReplyDTO>> getRepliesByAuthor(
            @PathVariable @Positive Long authorId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        try {
            User author = userService.findById(authorId)
                    .orElseThrow(() -> new RuntimeException("Author not found with ID: " + authorId));

            // Validate page size
            if (size > MAX_PAGE_SIZE) {
                size = MAX_PAGE_SIZE;
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<DiscussionReply> replies = replyService.getRepliesByAuthor(author, pageable);
            Page<DiscussionReplyDTO> replyDTOs = replies.map(this::convertToDTO);

            logger.debug("Retrieved {} replies by author: {} (ID: {})",
                    replies.getTotalElements(), author.getUsername(), authorId);
            return ResponseEntity.ok(replyDTOs);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Author not found with ID: {}", authorId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving replies by author ID: {}", authorId, e);
            throw new RuntimeException("Failed to retrieve replies by author: " + e.getMessage());
        }
    }

    /**
     * Retrieves a specific reply by its unique identifier.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiscussionReplyDTO> getReplyById(@PathVariable @Positive Long id) {
        try {
            DiscussionReply reply = replyService.getReplyById(id)
                    .orElseThrow(() -> new RuntimeException("Reply not found with ID: " + id));

            logger.debug("Retrieved reply ID: {}", id);
            return ResponseEntity.ok(convertToDTO(reply));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Reply not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving reply with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve reply: " + e.getMessage());
        }
    }

    /**
     * Creates a new reply to a discussion thread.
     */
    @PostMapping
    public ResponseEntity<DiscussionReplyDTO> createReply(
            @Valid @RequestBody ReplyCreateRequest request,
            HttpServletRequest httpRequest) {

        try {
            User author = userService.getCurrentUser(httpRequest);

            DiscussionReply reply = replyService.createReply(
                    request.getThreadId(),
                    author,
                    request.getContent(),
                    request.getParentReplyId()
            );

            logger.info("User {} created reply (ID: {}) on thread {}",
                    author.getUsername(), reply.getId(), request.getThreadId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(reply));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error creating reply", e);
            throw new RuntimeException("Failed to create reply: " + e.getMessage());
        }
    }

    /**
     * Updates an existing reply's content.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiscussionReplyDTO> updateReply(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ReplyUpdateRequest request,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);

            DiscussionReply reply = replyService.updateReply(
                    id,
                    requester,
                    request.getContent()
            );

            logger.info("User {} updated reply ID: {}", requester.getUsername(), id);
            return ResponseEntity.ok(convertToDTO(reply));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Reply not found for update with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to update reply with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error updating reply with ID: {}", id, e);
            throw new RuntimeException("Failed to update reply: " + e.getMessage());
        }
    }

    /**
     * Deletes a reply from the discussion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReply(
            @PathVariable @Positive Long id,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);
            replyService.deleteReply(id, requester);

            logger.info("User {} deleted reply ID: {}", requester.getUsername(), id);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Reply not found for deletion with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to delete reply with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting reply with ID: {}", id, e);
            throw new RuntimeException("Failed to delete reply: " + e.getMessage());
        }
    }

    /**
     * Votes on a reply (upvote or downvote).
     */
    @PostMapping("/{id}/vote")
    public ResponseEntity<Void> voteOnReply(
            @PathVariable @Positive Long id,
            @RequestParam boolean upvote,
            HttpServletRequest httpRequest) {

        try {
            User user = userService.getCurrentUser(httpRequest);

            // Get the reply first
            DiscussionReply reply = replyService.getReplyById(id)
                    .orElseThrow(() -> new RuntimeException("Reply not found with ID: " + id));

            // Call the correct method signature
            replyService.voteOnReply(reply, user, upvote);

            logger.debug("User {} {} reply ID: {}",
                    user.getUsername(), upvote ? "upvoted" : "downvoted", id);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Reply not found for voting with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error voting on reply with ID: {}", id, e);
            throw new RuntimeException("Failed to vote on reply: " + e.getMessage());
        }
    }

    /**
     * Features or unfeatures a reply (admin/moderator only).
     */
    @PutMapping("/{id}/feature")
    public ResponseEntity<Void> featureReply(
            @PathVariable @Positive Long id,
            @RequestParam boolean featured,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);

            // Call with correct method signature (only id and user)
            if (featured) {
                replyService.featureReply(id, requester);
            } else {
                replyService.unfeatureReply(id, requester);
            }

            logger.info("User {} {} reply ID: {}",
                    requester.getUsername(), featured ? "featured" : "unfeatured", id);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Reply not found for featuring with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to feature reply with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error featuring reply with ID: {}", id, e);
            throw new RuntimeException("Failed to feature reply: " + e.getMessage());
        }
    }

    /**
     * Reports a reply for inappropriate content.
     */
    @PostMapping("/{id}/report")
    public ResponseEntity<Void> reportReply(
            @PathVariable @Positive Long id,
            @RequestParam String reason,
            HttpServletRequest httpRequest) {

        try {
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Report reason cannot be empty");
            }

            User reporter = userService.getCurrentUser(httpRequest);

            // Get the reply first
            DiscussionReply reply = replyService.getReplyById(id)
                    .orElseThrow(() -> new RuntimeException("Reply not found with ID: " + id));

            // Call the correct method
            replyService.reportReply(reply, reporter, reason.trim());

            logger.info("User {} reported reply ID: {} for: {}",
                    reporter.getUsername(), id, reason);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid report request: {}", e.getMessage());
            throw new RuntimeException("Invalid report data: " + e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Reply not found for reporting with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error reporting reply with ID: {}", id, e);
            throw new RuntimeException("Failed to report reply: " + e.getMessage());
        }
    }

    /**
     * Converts a DiscussionReply entity to a DiscussionReplyDTO for API
     * responses.
     */
    private DiscussionReplyDTO convertToDTO(DiscussionReply reply) {
        DiscussionReplyDTO dto = new DiscussionReplyDTO();
        dto.setId(reply.getId());
        dto.setContent(reply.getContent());

        // Author information
        dto.setAuthorId(reply.getAuthor().getId());
        dto.setAuthorName(reply.getAuthor().getFullName());
        if (reply.getAuthor().getProfileImageUrl() != null) {
            dto.setAuthorProfileImageUrl(reply.getAuthor().getProfileImageUrl());
        }
        if (reply.getAuthor().getBadgeLevel() != null) {
            dto.setAuthorBadgeLevel(reply.getAuthor().getBadgeLevel().toString());
        }
        if (reply.getAuthor().getRole() != null) {
            dto.setAuthorRole(reply.getAuthor().getRole().toString());
        }

        // Thread and parent relationships
        dto.setThreadId(reply.getThread().getId());
        dto.setParentReplyId(reply.getParentReply() != null ? reply.getParentReply().getId() : null);

        // Voting metrics
        dto.setUpvotes(reply.getUpvotes());
        dto.setDownvotes(reply.getDownvotes());
        dto.setNetScore(reply.getNetScore());

        // Status flags
        dto.setIsFeatured(reply.getIsFeatured());
        dto.setIsDeleted(reply.getIsDeleted());

        // Timestamps
        dto.setCreatedAt(reply.getCreatedAt());
        dto.setUpdatedAt(reply.getUpdatedAt());
        if (reply.getDeletedAt() != null) {
            dto.setDeletedAt(reply.getDeletedAt());
        }

        return dto;
    }

    // Request DTOs
    public static class ReplyCreateRequest {

        private Long threadId;
        private String content;
        private Long parentReplyId;

        // Getters and setters
        public Long getThreadId() {
            return threadId;
        }

        public void setThreadId(Long threadId) {
            this.threadId = threadId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getParentReplyId() {
            return parentReplyId;
        }

        public void setParentReplyId(Long parentReplyId) {
            this.parentReplyId = parentReplyId;
        }
    }

    public static class ReplyUpdateRequest {

        private String content;

        // Getters and setters
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

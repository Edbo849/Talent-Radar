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

import com.talentradar.dto.discussion.DiscussionThreadDTO;
import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.enums.ThreadType;
import com.talentradar.model.player.Player;
import com.talentradar.model.user.User;
import com.talentradar.service.discussion.DiscussionCategoryService;
import com.talentradar.service.discussion.DiscussionThreadService;
import com.talentradar.service.player.PlayerService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * REST controller for managing discussion threads. Provides endpoints for
 * creating, editing, and managing discussion threads.
 */
@RestController
@RequestMapping("/api/discussions/threads")
@CrossOrigin(origins = "http://localhost:3000")
public class DiscussionThreadController {

    private static final Logger logger = LoggerFactory.getLogger(DiscussionThreadController.class);
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private DiscussionThreadService threadService;

    @Autowired
    private DiscussionCategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    /**
     * Retrieves all discussion threads with optional filtering and pagination.
     */
    @GetMapping
    public ResponseEntity<Page<DiscussionThreadDTO>> getAllThreads(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String threadType,
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        try {
            // Validate page size
            if (size > MAX_PAGE_SIZE) {
                size = MAX_PAGE_SIZE;
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<DiscussionThread> threads;

            // Apply filters
            if (categoryId != null) {
                // Validate category exists
                categoryService.getCategoryById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
                threads = threadService.getThreadsByCategory(categoryId, pageable);
            } else if (threadType != null) {
                ThreadType type = ThreadType.valueOf(threadType.toUpperCase());
                threads = threadService.getThreadsByThreadType(type, pageable);
            } else if (Boolean.TRUE.equals(pinned)) {
                threads = threadService.getPinnedThreads(pageable);
            } else {
                threads = threadService.getAllThreads(pageable);
            }

            Page<DiscussionThreadDTO> threadDTOs = threads.map(this::convertToDTO);
            logger.debug("Retrieved {} threads (page {}, size {})",
                    threads.getTotalElements(), page, size);
            return ResponseEntity.ok(threadDTOs);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid thread type parameter: {}", threadType);
            throw new RuntimeException("Invalid thread type: " + threadType);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Category not found with ID: {}", categoryId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving threads", e);
            throw new RuntimeException("Failed to retrieve threads: " + e.getMessage());
        }
    }

    /**
     * Retrieves a specific discussion thread by its unique identifier.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiscussionThreadDTO> getThreadById(
            @PathVariable @Positive Long id,
            HttpServletRequest request) {

        try {
            DiscussionThread thread = threadService.getThreadById(id)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + id));

            // Record view
            User user = userService.getCurrentUserOrNull(request);
            String ipAddress = getClientIpAddress(request);
            threadService.viewThread(id, user, ipAddress);

            logger.debug("Retrieved thread: {} (ID: {})", thread.getTitle(), id);
            return ResponseEntity.ok(convertToDTO(thread));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Thread not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving thread with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve thread: " + e.getMessage());
        }
    }

    /**
     * Retrieves trending discussion threads.
     */
    @GetMapping("/trending")
    public ResponseEntity<Page<DiscussionThreadDTO>> getTrendingThreads(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        try {
            // Validate page size
            if (size > MAX_PAGE_SIZE) {
                size = MAX_PAGE_SIZE;
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<DiscussionThread> threads = threadService.getTrendingThreads(pageable);
            Page<DiscussionThreadDTO> threadDTOs = threads.map(this::convertToDTO);

            logger.debug("Retrieved {} trending threads", threads.getTotalElements());
            return ResponseEntity.ok(threadDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving trending threads", e);
            throw new RuntimeException("Failed to retrieve trending threads: " + e.getMessage());
        }
    }

    /**
     * Retrieves featured discussion threads.
     */
    @GetMapping("/featured")
    public ResponseEntity<List<DiscussionThreadDTO>> getFeaturedThreads() {
        try {
            List<DiscussionThread> threads = threadService.getFeaturedThreads();
            List<DiscussionThreadDTO> threadDTOs = threads.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Retrieved {} featured threads", threads.size());
            return ResponseEntity.ok(threadDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving featured threads", e);
            throw new RuntimeException("Failed to retrieve featured threads: " + e.getMessage());
        }
    }

    /**
     * Searches for discussion threads based on query string.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DiscussionThreadDTO>> searchThreads(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        try {
            if (q == null || q.trim().isEmpty()) {
                throw new IllegalArgumentException("Search query cannot be empty");
            }

            // Validate page size
            if (size > MAX_PAGE_SIZE) {
                size = MAX_PAGE_SIZE;
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<DiscussionThread> threads = threadService.searchThreads(q.trim(), pageable);
            Page<DiscussionThreadDTO> threadDTOs = threads.map(this::convertToDTO);

            logger.debug("Search for '{}' returned {} threads", q, threads.getTotalElements());
            return ResponseEntity.ok(threadDTOs);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid search query: {}", e.getMessage());
            throw new RuntimeException("Invalid search query: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error searching threads with query: {}", q, e);
            throw new RuntimeException("Failed to search threads: " + e.getMessage());
        }
    }

    /**
     * Retrieves threads by a specific author.
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<DiscussionThreadDTO>> getThreadsByAuthor(
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
            Page<DiscussionThread> threads = threadService.getThreadsByAuthor(author, pageable);
            Page<DiscussionThreadDTO> threadDTOs = threads.map(this::convertToDTO);

            logger.debug("Retrieved {} threads by author: {} (ID: {})",
                    threads.getTotalElements(), author.getUsername(), authorId);
            return ResponseEntity.ok(threadDTOs);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Author not found with ID: {}", authorId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving threads by author ID: {}", authorId, e);
            throw new RuntimeException("Failed to retrieve threads by author: " + e.getMessage());
        }
    }

    /**
     * Retrieves threads related to a specific player.
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<Page<DiscussionThreadDTO>> getThreadsByPlayer(
            @PathVariable @Positive Long playerId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        try {
            Player player = playerService.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId));

            // Validate page size
            if (size > MAX_PAGE_SIZE) {
                size = MAX_PAGE_SIZE;
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<DiscussionThread> threads = threadService.getThreadsByPlayer(player, pageable);
            Page<DiscussionThreadDTO> threadDTOs = threads.map(this::convertToDTO);

            logger.debug("Retrieved {} threads for player: {} (ID: {})",
                    threads.getTotalElements(), player.getName(), playerId);
            return ResponseEntity.ok(threadDTOs);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Player not found with ID: {}", playerId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving threads by player ID: {}", playerId, e);
            throw new RuntimeException("Failed to retrieve threads by player: " + e.getMessage());
        }
    }

    /**
     * Creates a new discussion thread.
     */
    @PostMapping
    public ResponseEntity<DiscussionThreadDTO> createThread(
            @Valid @RequestBody ThreadCreateRequest request,
            HttpServletRequest httpRequest) {

        try {
            User author = userService.getCurrentUser(httpRequest);

            // Get category
            categoryService.getCategoryById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));

            // Get players if specified
            List<Player> players = null;
            if (request.getPlayerIds() != null && !request.getPlayerIds().isEmpty()) {
                players = request.getPlayerIds().stream()
                        .map(playerService::findById)
                        .filter(java.util.Optional::isPresent)
                        .map(java.util.Optional::get)
                        .toList();
            }

            ThreadType threadType;
            try {
                threadType = ThreadType.valueOf(request.getThreadType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid thread type: " + request.getThreadType());
            }

            DiscussionThread thread = threadService.createThread(
                    request.getCategoryId(), author, request.getTitle(), request.getContent(),
                    threadType, players
            );

            logger.info("User {} created thread: {} (ID: {})",
                    author.getUsername(), thread.getTitle(), thread.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(thread));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid thread creation request: {}", e.getMessage());
            throw new RuntimeException("Invalid thread data: " + e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error creating thread", e);
            throw new RuntimeException("Failed to create thread: " + e.getMessage());
        }
    }

    /**
     * Updates an existing discussion thread.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiscussionThreadDTO> updateThread(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ThreadUpdateRequest request,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);

            DiscussionThread thread = threadService.updateThread(
                    id, requester, request.getTitle(), request.getContent()
            );

            logger.info("User {} updated thread: {} (ID: {})",
                    requester.getUsername(), thread.getTitle(), id);
            return ResponseEntity.ok(convertToDTO(thread));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Thread not found for update with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to update thread with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error updating thread with ID: {}", id, e);
            throw new RuntimeException("Failed to update thread: " + e.getMessage());
        }
    }

    /**
     * Deletes a discussion thread.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteThread(
            @PathVariable @Positive Long id,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);
            threadService.deleteThread(id, requester);

            logger.info("User {} deleted thread with ID: {}", requester.getUsername(), id);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Thread not found for deletion with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to delete thread with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting thread with ID: {}", id, e);
            throw new RuntimeException("Failed to delete thread: " + e.getMessage());
        }
    }

    /**
     * Pins or unpins a discussion thread (moderator/admin only).
     */
    @PutMapping("/{id}/pin")
    public ResponseEntity<Void> pinThread(
            @PathVariable @Positive Long id,
            @RequestParam boolean pinned,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);

            // Call the correct method based on the pinned parameter
            if (pinned) {
                threadService.pinThread(id, requester);
            } else {
                threadService.unpinThread(id, requester);
            }

            logger.info("User {} {} thread ID: {}",
                    requester.getUsername(), pinned ? "pinned" : "unpinned", id);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Thread not found for pinning with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to pin thread with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error pinning thread with ID: {}", id, e);
            throw new RuntimeException("Failed to pin thread: " + e.getMessage());
        }
    }

    /**
     * Locks or unlocks a discussion thread (moderator/admin only).
     */
    @PutMapping("/{id}/lock")
    public ResponseEntity<Void> lockThread(
            @PathVariable @Positive Long id,
            @RequestParam boolean locked,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);

            // Call the correct method based on the locked parameter
            if (locked) {
                threadService.lockThread(id, requester);
            } else {
                threadService.unlockThread(id, requester);
            }

            logger.info("User {} {} thread ID: {}",
                    requester.getUsername(), locked ? "locked" : "unlocked", id);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Thread not found for locking with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to lock thread with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error locking thread with ID: {}", id, e);
            throw new RuntimeException("Failed to lock thread: " + e.getMessage());
        }
    }

    /**
     * Features or unfeatures a discussion thread (admin only).
     */
    @PutMapping("/{id}/feature")
    public ResponseEntity<Void> featureThread(
            @PathVariable @Positive Long id,
            @RequestParam boolean featured,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);

            // Call the correct method based on the featured parameter
            if (featured) {
                threadService.featureThread(id, requester);
            } else {
                threadService.unfeatureThread(id, requester);
            }

            logger.info("User {} {} thread ID: {}",
                    requester.getUsername(), featured ? "featured" : "unfeatured", id);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Thread not found for featuring with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to feature thread with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error featuring thread with ID: {}", id, e);
            throw new RuntimeException("Failed to feature thread: " + e.getMessage());
        }
    }

    /**
     * Converts a DiscussionThread entity to a DiscussionThreadDTO for API
     * responses.
     */
    private DiscussionThreadDTO convertToDTO(DiscussionThread thread) {
        DiscussionThreadDTO dto = new DiscussionThreadDTO();
        dto.setId(thread.getId());
        dto.setTitle(thread.getTitle());
        dto.setContent(thread.getContent());
        dto.setThreadType(thread.getThreadType());

        // Author information
        dto.setAuthorId(thread.getAuthor().getId());
        dto.setAuthorName(thread.getAuthor().getFullName());
        if (thread.getAuthor().getProfileImageUrl() != null) {
            dto.setAuthorProfileImageUrl(thread.getAuthor().getProfileImageUrl());
        }
        if (thread.getAuthor().getBadgeLevel() != null) {
            dto.setAuthorBadgeLevel(thread.getAuthor().getBadgeLevel().toString());
        }

        // Category information
        dto.setCategoryId(thread.getCategory().getId());
        dto.setCategoryName(thread.getCategory().getName());
        if (thread.getCategory().getIcon() != null) {
            dto.setCategoryIcon(thread.getCategory().getIcon());
        }
        if (thread.getCategory().getColor() != null) {
            dto.setCategoryColor(thread.getCategory().getColor());
        }

        // Metrics and status
        dto.setViewCount(thread.getViewCount());
        dto.setReplyCount(thread.getReplyCount());
        dto.setIsPinned(thread.getIsPinned());
        dto.setIsLocked(thread.getIsLocked());
        dto.setIsFeatured(thread.getIsFeatured());

        // Timestamps
        dto.setCreatedAt(thread.getCreatedAt());
        dto.setLastActivityAt(thread.getLastActivityAt());
        dto.setUpdatedAt(thread.getUpdatedAt());

        return dto;
    }

    /**
     * Extracts the client IP address from the HTTP request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Request DTOs
    public static class ThreadCreateRequest {

        private Long categoryId;
        private String title;
        private String content;
        private String threadType;
        private List<Long> playerIds;

        // Getters and setters
        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getThreadType() {
            return threadType;
        }

        public void setThreadType(String threadType) {
            this.threadType = threadType;
        }

        public List<Long> getPlayerIds() {
            return playerIds;
        }

        public void setPlayerIds(List<Long> playerIds) {
            this.playerIds = playerIds;
        }
    }

    public static class ThreadUpdateRequest {

        private String title;
        private String content;

        // Getters and setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

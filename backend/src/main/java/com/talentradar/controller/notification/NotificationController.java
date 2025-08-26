package com.talentradar.controller.notification;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.notification.NotificationDTO;
import com.talentradar.exception.NotificationNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.notification.Notification;
import com.talentradar.model.user.User;
import com.talentradar.service.notification.NotificationService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller for managing user notifications. Provides endpoints for
 * retrieving, marking as read, and managing user notifications with proper
 * error handling and validation.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves paginated notifications for the current user.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            User user = userService.getCurrentUser(request);
            Pageable pageable = PageRequest.of(page, size);

            logger.debug("Retrieving notifications for user {} (unreadOnly: {}, page: {}, size: {})",
                    user.getUsername(), unreadOnly, page, size);

            Page<Notification> notifications;
            if (unreadOnly) {
                notifications = notificationService.getUnreadNotifications(user, pageable);
            } else {
                notifications = notificationService.getUserNotifications(user, pageable);
            }

            Page<NotificationDTO> notificationDTOs = notifications.map(this::convertToDTO);

            logger.info("Retrieved {} notifications for user {}",
                    notificationDTOs.getTotalElements(), user.getUsername());

            return ResponseEntity.ok(notificationDTOs);
        } catch (UserNotFoundException e) {
            logger.warn("User not found when retrieving notifications");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid pagination parameters: page={}, size={}", page, size);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Marks a specific notification as read for the current user.
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid notification ID provided: {}", id);
                return ResponseEntity.badRequest().build();
            }

            User user = userService.getCurrentUser(request);

            logger.debug("User {} marking notification {} as read", user.getUsername(), id);

            Notification notification = notificationService.findById(id)
                    .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

            // Verify the notification belongs to the current user
            if (!notification.getUser().getId().equals(user.getId())) {
                logger.warn("User {} attempted to access notification {} belonging to another user",
                        user.getUsername(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            notificationService.markAsRead(notification, user);

            logger.info("Notification {} marked as read by user {}", id, user.getUsername());
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            logger.warn("User not found when marking notification as read");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (NotificationNotFoundException e) {
            logger.warn("Notification not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error marking notification {} as read", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Marks all notifications as read for the current user.
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest request) {
        try {
            User user = userService.getCurrentUser(request);

            logger.debug("User {} marking all notifications as read", user.getUsername());

            int markedCount = notificationService.markAllAsRead(user);

            logger.info("Marked {} notifications as read for user {}", markedCount, user.getUsername());
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            logger.warn("User not found when marking all notifications as read");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.error("Error marking all notifications as read", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves the count of unread notifications for the current user.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(HttpServletRequest request) {
        try {
            User user = userService.getCurrentUser(request);

            long count = notificationService.getUnreadNotificationCount(user);

            logger.debug("User {} has {} unread notifications", user.getUsername(), count);
            return ResponseEntity.ok(count);
        } catch (UserNotFoundException e) {
            logger.warn("User not found when getting unread count");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.error("Error getting unread notification count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a specific notification for the current user.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            if (id == null || id <= 0) {
                logger.warn("Invalid notification ID provided: {}", id);
                return ResponseEntity.badRequest().build();
            }

            User user = userService.getCurrentUser(request);

            logger.debug("User {} deleting notification {}", user.getUsername(), id);

            Notification notification = notificationService.findById(id)
                    .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

            // Verify the notification belongs to the current user
            if (!notification.getUser().getId().equals(user.getId())) {
                logger.warn("User {} attempted to delete notification {} belonging to another user",
                        user.getUsername(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            notificationService.deleteNotification(notification);

            logger.info("Notification {} deleted by user {}", id, user.getUsername());
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            logger.warn("User not found when deleting notification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (NotificationNotFoundException e) {
            logger.warn("Notification not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting notification {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Converts a Notification entity to a NotificationDTO for API responses.
     */
    private NotificationDTO convertToDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setNotificationType(notification.getNotificationType().toString());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setActionUrl(notification.getActionUrl());
        dto.setIsRead(notification.getIsRead());
        dto.setReadAt(notification.getReadAt());
        dto.setPriority(notification.getPriority());
        dto.setExpiresAt(notification.getExpiresAt());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUserId(notification.getUser() != null ? notification.getUser().getId() : null);
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setTriggeredByUserId(notification.getTriggeredBy() != null ? notification.getTriggeredBy().getId() : null);
        dto.setTriggeredByUsername(notification.getTriggeredBy() != null ? notification.getTriggeredBy().getUsername() : null);

        return dto;
    }
}

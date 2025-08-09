package com.talentradar.dto.notification;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Notification entities. Used to transfer notification
 * information between layers including notification content, user context, read
 * status, and related entity references.
 */
public class NotificationDTO {

    // Primary identifier
    private Long id;

    // Notification type (enum: PLAYER_UPDATE, REPLY, MENTION, etc.)
    private String notificationType;

    // Notification content
    private String title;
    private String message;

    // Action URL for navigation
    private String actionUrl;

    // Read status and timestamps
    private Boolean isRead;
    private LocalDateTime readAt;

    // Priority and expiration
    private Integer priority;
    private LocalDateTime expiresAt;

    // Timestamp fields
    private LocalDateTime createdAt;

    // User context
    private Long userId;

    // Related entity information
    private String relatedEntityType;
    private Long relatedEntityId;

    // Triggered by user information (derived fields)
    private Long triggeredByUserId;
    private String triggeredByUsername;

    // Constructors
    public NotificationDTO() {
    }

    public NotificationDTO(Long id, String notificationType, String title, String message, Boolean isRead) {
        this.id = id;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
    }

    // Helper methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isHighPriority() {
        return priority != null && priority >= 1;
    }

    public boolean isUrgent() {
        return priority != null && priority >= 2;
    }

    public String getPriorityDisplay() {
        if (priority == null || priority == 0) {
            return "Normal";
        }
        if (priority == 1) {
            return "High";
        }
        if (priority >= 2) {
            return "Urgent";
        }
        return "Normal";
    }

    public boolean hasRelatedEntity() {
        return relatedEntityType != null && relatedEntityId != null;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public Long getTriggeredByUserId() {
        return triggeredByUserId;
    }

    public void setTriggeredByUserId(Long triggeredByUserId) {
        this.triggeredByUserId = triggeredByUserId;
    }

    public String getTriggeredByUsername() {
        return triggeredByUsername;
    }

    public void setTriggeredByUsername(String triggeredByUsername) {
        this.triggeredByUsername = triggeredByUsername;
    }

    @Override
    public String toString() {
        return "NotificationDTO{"
                + "id=" + id
                + ", notificationType='" + notificationType + '\''
                + ", title='" + title + '\''
                + ", isRead=" + isRead
                + ", priority=" + priority
                + ", createdAt=" + createdAt
                + '}';
    }
}

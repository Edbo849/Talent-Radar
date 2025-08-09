package com.talentradar.dto.messaging;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for ConversationParticipant entities. Used to transfer
 * participant information between layers including user details, participation
 * metadata, and read status tracking.
 */
public class ConversationParticipantDTO {

    // Primary identifier
    private Long id;

    // User information (from User relationship)
    private Long userId;
    private String userName;
    private String userProfileImageUrl;

    // Participation metadata
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    // Read status tracking
    private LocalDateTime lastReadAt;
    private Integer unreadCount;

    // Participation status
    private Boolean isActive;

    // Constructors
    public ConversationParticipantDTO() {
    }

    public ConversationParticipantDTO(Long id, Long userId, String userName) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImageUrl() {
        return userProfileImageUrl;
    }

    public void setUserProfileImageUrl(String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }

    public LocalDateTime getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "ConversationParticipantDTO{"
                + "id=" + id
                + ", userId=" + userId
                + ", userName='" + userName + '\''
                + ", joinedAt=" + joinedAt
                + ", isActive=" + isActive
                + '}';
    }
}

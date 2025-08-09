package com.talentradar.dto.messaging;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for MessageReadStatus entities. Used to transfer message
 * read status information between layers including user read confirmations and
 * timestamps for message delivery tracking.
 */
public class MessageReadStatusDTO {

    // Primary identifier
    private Long id;

    // Message reference
    private Long messageId;

    // User information (from User relationship)
    private Long userId;
    private String userName;

    // Read timestamp
    private LocalDateTime readAt;

    // Constructors
    public MessageReadStatusDTO() {
    }

    public MessageReadStatusDTO(Long id, Long messageId, Long userId, String userName) {
        this.id = id;
        this.messageId = messageId;
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

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
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

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    @Override
    public String toString() {
        return "MessageReadStatusDTO{"
                + "id=" + id
                + ", messageId=" + messageId
                + ", userId=" + userId
                + ", userName='" + userName + '\''
                + ", readAt=" + readAt
                + '}';
    }
}

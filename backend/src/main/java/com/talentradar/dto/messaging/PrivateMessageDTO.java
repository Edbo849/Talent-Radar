package com.talentradar.dto.messaging;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for PrivateMessage entities. Used to transfer private
 * message information between layers including message content, sender details,
 * conversation references, and read status.
 */
public class PrivateMessageDTO {

    // Primary identifier
    private Long id;

    // Conversation reference
    private Long conversationId;

    // Sender information (from User relationship)
    private Long senderId;
    private String senderName;

    // Message content
    private String messageText;

    // Message type (enum: TEXT, IMAGE, FILE, etc.)
    private String messageType;

    // Optional attachment URL
    private String attachmentUrl;

    // Reply reference for threaded messaging
    private Long replyToMessageId;

    // Message status
    private Boolean isDeleted;

    // Read tracking
    private Integer readCount;

    // Timestamp fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public PrivateMessageDTO() {
    }

    public PrivateMessageDTO(Long id, String messageText, String senderName) {
        this.id = id;
        this.messageText = messageText;
        this.senderName = senderName;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount(Integer readCount) {
        this.readCount = readCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PrivateMessageDTO{"
                + "id=" + id
                + ", conversationId=" + conversationId
                + ", senderName='" + senderName + '\''
                + ", messageType='" + messageType + '\''
                + ", isDeleted=" + isDeleted
                + ", createdAt=" + createdAt
                + '}';
    }
}

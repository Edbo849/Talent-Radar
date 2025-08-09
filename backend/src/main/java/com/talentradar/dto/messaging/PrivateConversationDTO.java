package com.talentradar.dto.messaging;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for PrivateConversation entities. Used to transfer
 * conversation information between layers including conversation metadata,
 * participants, last message details, and unread counts.
 */
public class PrivateConversationDTO {

    // Primary identifier
    private Long id;

    // Conversation name/title
    private String conversationName;

    // Creator information (from User relationship)
    private Long createdById;
    private String createdByName;

    // Conversation type
    private Boolean isGroupConversation;

    // Activity tracking
    private LocalDateTime lastMessageAt;

    // Timestamp fields
    private LocalDateTime createdAt;

    // Related data (derived fields)
    // List of conversation participants
    private List<ConversationParticipantDTO> participants;

    // Last message in conversation
    private PrivateMessageDTO lastMessage;

    // Unread message count for current user
    private Integer unreadCount;

    // Constructors
    public PrivateConversationDTO() {
    }

    public PrivateConversationDTO(Long id, String conversationName, String createdByName) {
        this.id = id;
        this.conversationName = conversationName;
        this.createdByName = createdByName;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Boolean getIsGroupConversation() {
        return isGroupConversation;
    }

    public void setIsGroupConversation(Boolean isGroupConversation) {
        this.isGroupConversation = isGroupConversation;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Derived field getters and setters
    public List<ConversationParticipantDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ConversationParticipantDTO> participants) {
        this.participants = participants;
    }

    public PrivateMessageDTO getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(PrivateMessageDTO lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    @Override
    public String toString() {
        return "PrivateConversationDTO{"
                + "id=" + id
                + ", conversationName='" + conversationName + '\''
                + ", isGroupConversation=" + isGroupConversation
                + ", lastMessageAt=" + lastMessageAt
                + ", unreadCount=" + unreadCount
                + '}';
    }
}

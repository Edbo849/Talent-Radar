package com.talentradar.model.messaging;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.talentradar.model.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing private conversation containers between users. Manages
 * conversation metadata including participants, creation date, and last
 * activity for organising private messaging functionality.
 */
@Entity
@Table(name = "private_conversations")
public class PrivateConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_name", length = 200)
    private String conversationName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    @NotNull(message = "Creator is required")
    private User createdBy;

    @Column(name = "is_group_conversation")
    private Boolean isGroupConversation = false;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PrivateMessage> messages = new HashSet<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ConversationParticipant> participants = new HashSet<>();

    // Constructors
    public PrivateConversation() {
    }

    public PrivateConversation(User createdBy, String conversationName) {
        this.createdBy = createdBy;
        this.conversationName = conversationName;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastMessageAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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

    public Set<PrivateMessage> getMessages() {
        return messages;
    }

    public void setMessages(Set<PrivateMessage> messages) {
        this.messages = messages;
    }

    public Set<ConversationParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<ConversationParticipant> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrivateConversation that = (PrivateConversation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PrivateConversation{"
                + "id=" + id
                + ", conversationName='" + conversationName + '\''
                + ", isGroupConversation=" + isGroupConversation
                + ", createdAt=" + createdAt
                + '}';
    }
}

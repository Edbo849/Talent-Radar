package com.talentradar.model.messaging;

import java.time.LocalDateTime;

import com.talentradar.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity tracking read status of messages by conversation participants. Manages
 * message delivery confirmation and read receipts for enhanced messaging
 * experience and notification management.
 */
@Entity
@Table(name = "message_read_status")
public class MessageReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private PrivateMessage message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public MessageReadStatus() {
    }

    public MessageReadStatus(PrivateMessage message, User user, LocalDateTime readAt) {
        this.message = message;
        this.user = user;
        this.readAt = readAt;
    }

    public Long getId() {
        return id;
    }

    public PrivateMessage getMessage() {
        return message;
    }

    public void setMessage(PrivateMessage message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt
                = readAt;
    }
}

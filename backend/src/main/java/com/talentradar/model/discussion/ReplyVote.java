package com.talentradar.model.discussion;

import java.time.LocalDateTime;
import java.util.Objects;

import com.talentradar.model.enums.VoteType;
import com.talentradar.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing votes on discussion replies. Enables community-driven
 * quality assessment of replies through upvoting and downvoting mechanisms.
 */
@Entity
@Table(name = "reply_votes")
public class ReplyVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id", nullable = false)
    @NotNull(message = "Reply is required")
    private DiscussionReply reply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    @NotNull(message = "Vote type is required")
    private VoteType voteType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public ReplyVote() {
    }

    public ReplyVote(DiscussionReply reply, User user, VoteType voteType) {
        this.reply = reply;
        this.user = user;
        this.voteType = voteType;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isUpvote() {
        return voteType == VoteType.UPVOTE;
    }

    public boolean isDownvote() {
        return voteType == VoteType.DOWNVOTE;
    }

    public boolean isVoteByUser(Long userId) {
        return user != null && user.getId().equals(userId);
    }

    public boolean isVoteOnReply(Long replyId) {
        return reply != null && reply.getId().equals(replyId);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiscussionReply getReply() {
        return reply;
    }

    public void setReply(DiscussionReply reply) {
        this.reply = reply;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReplyVote replyVote = (ReplyVote) o;
        return Objects.equals(id, replyVote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ReplyVote{"
                + "id=" + id
                + ", voteType=" + voteType
                + ", createdAt=" + createdAt
                + '}';
    }
}

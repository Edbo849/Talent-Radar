package com.talentradar.model.poll;

import java.time.LocalDateTime;
import java.util.Objects;

import com.talentradar.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing individual user votes on poll options. Tracks voting
 * participation ensuring one vote per user per poll and enabling vote tracking
 * for analytics and result calculation.
 */
@Entity
@Table(name = "poll_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"poll_id", "user_id"}),
    @UniqueConstraint(columnNames = {"poll_id", "ip_address"})
})
public class PollVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    @NotNull(message = "Poll is required")
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    @NotNull(message = "Poll option is required")
    private PollOption pollOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public PollVote() {
    }

    public PollVote(Poll poll, PollOption pollOption, User user) {
        this.poll = poll;
        this.pollOption = pollOption;
        this.user = user;
        this.isAnonymous = false;
    }

    public PollVote(Poll poll, PollOption pollOption, String ipAddress, String userAgent) {
        this.poll = poll;
        this.pollOption = pollOption;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isAnonymous = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isUserVote() {
        return user != null;
    }

    public boolean isAnonymousVote() {
        return user == null || Boolean.TRUE.equals(isAnonymous);
    }

    public String getVoterDisplayName() {
        if (isAnonymousVote()) {
            return "Anonymous";
        }
        return user != null ? user.getDisplayName() : "Unknown";
    }

    public boolean isVoteFromUser(Long userId) {
        return user != null && user.getId().equals(userId);
    }

    public boolean isVoteFromIP(String ip) {
        return ipAddress != null && ipAddress.equals(ip);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public PollOption getPollOption() {
        return pollOption;
    }

    public void setPollOption(PollOption pollOption) {
        this.pollOption = pollOption;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
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
        PollVote pollVote = (PollVote) o;
        return Objects.equals(id, pollVote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PollVote{"
                + "id=" + id
                + ", pollId=" + (poll != null ? poll.getId() : null)
                + ", optionId=" + (pollOption != null ? pollOption.getId() : null)
                + ", isAnonymous=" + isAnonymous
                + ", createdAt=" + createdAt
                + '}';
    }
}

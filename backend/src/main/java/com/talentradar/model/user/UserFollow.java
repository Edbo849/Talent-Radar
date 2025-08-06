package com.talentradar.model.user;

import java.time.LocalDateTime;
import java.util.Objects;

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
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing user following relationships. Enables users to follow
 * other users and create social networks within the Talent Radar community for
 * content discovery and engagement.
 */
@Entity
@Table(name = "user_follows")
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    @NotNull(message = "Follower is required")
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    @NotNull(message = "Following user is required")
    private User following;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public UserFollow() {
    }

    public UserFollow(User follower, User following) {
        this.follower = follower;
        this.following = following;
        this.notificationEnabled = true;
    }

    public UserFollow(User follower, User following, Boolean notificationEnabled) {
        this.follower = follower;
        this.following = following;
        this.notificationEnabled = notificationEnabled;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isFollowing(Long userId) {
        return following != null && following.getId().equals(userId);
    }

    public boolean isFollowedBy(Long userId) {
        return follower != null && follower.getId().equals(userId);
    }

    public boolean isSelfFollow() {
        return follower != null && following != null
                && follower.getId().equals(following.getId());
    }

    public boolean shouldNotify() {
        return Boolean.TRUE.equals(notificationEnabled);
    }

    public String getFollowerDisplayName() {
        return follower != null ? follower.getDisplayName() : "Unknown User";
    }

    public String getFollowingDisplayName() {
        return following != null ? following.getDisplayName() : "Unknown User";
    }

    public boolean isMutualWith(UserFollow otherFollow) {
        return otherFollow != null
                && this.follower.getId().equals(otherFollow.following.getId())
                && this.following.getId().equals(otherFollow.follower.getId());
    }

    // Check if this follow relationship involves a specific user
    public boolean involvesUser(Long userId) {
        return (follower != null && follower.getId().equals(userId))
                || (following != null && following.getId().equals(userId));
    }

    // Check if this is a follow relationship between two specific users
    public boolean isBetweenUsers(Long userId1, Long userId2) {
        return (follower != null && following != null)
                && ((follower.getId().equals(userId1) && following.getId().equals(userId2))
                || (follower.getId().equals(userId2) && following.getId().equals(userId1)));
    }

    public boolean isRecentFollow() {
        if (createdAt == null) {
            return false;
        }
        return createdAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowing() {
        return following;
    }

    public void setFollowing(User following) {
        this.following = following;
    }

    public Boolean getNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
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
        UserFollow that = (UserFollow) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserFollow{"
                + "id=" + id
                + ", followerId=" + (follower != null ? follower.getId() : null)
                + ", followingId=" + (following != null ? following.getId() : null)
                + ", notificationEnabled=" + notificationEnabled
                + ", createdAt=" + createdAt
                + '}';
    }
}

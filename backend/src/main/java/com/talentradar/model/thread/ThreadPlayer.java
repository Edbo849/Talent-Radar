package com.talentradar.model.thread;

import java.time.LocalDateTime;
import java.util.Objects;

import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.enums.ThreadType;
import com.talentradar.model.player.Player;
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
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing the association between discussion threads and players.
 * Links specific players to relevant discussion threads for organised
 * player-centric conversations and content discovery.
 */
@Entity
@Table(name = "thread_players")
public class ThreadPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    @NotNull(message = "Discussion thread is required")
    private DiscussionThread thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    @NotNull(message = "Player is required")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id")
    private User addedByUser;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "context_notes")
    private String contextNotes;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public ThreadPlayer() {
    }

    public ThreadPlayer(DiscussionThread thread, Player player) {
        this.thread = thread;
        this.player = player;
    }

    public ThreadPlayer(DiscussionThread thread, Player player, User addedByUser) {
        this.thread = thread;
        this.player = player;
        this.addedByUser = addedByUser;
    }

    public ThreadPlayer(DiscussionThread thread, Player player, User addedByUser, Integer displayOrder) {
        this.thread = thread;
        this.player = player;
        this.addedByUser = addedByUser;
        this.displayOrder = displayOrder;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean isAddedByUser(Long userId) {
        return addedByUser != null && addedByUser.getId().equals(userId);
    }

    public boolean canBeRemovedByUser(User user) {
        // Can be removed by the user who added it, thread author, or moderators
        if (addedByUser != null && addedByUser.getId().equals(user.getId())) {
            return true;
        }
        if (thread.getAuthor().getId().equals(user.getId())) {
            return true;
        }
        return user.canModerate();
    }

    public String getPlayerDisplayName() {
        return player != null ? player.getFullName() : "Unknown Player";
    }

    public String getThreadTitle() {
        return thread != null ? thread.getTitle() : "Unknown Thread";
    }

    public boolean isRelevantForComparison() {
        return thread != null && thread.getThreadType() == ThreadType.PLAYER_COMPARISON;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiscussionThread getThread() {
        return thread;
    }

    public void setThread(DiscussionThread thread) {
        this.thread = thread;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public User getAddedByUser() {
        return addedByUser;
    }

    public void setAddedByUser(User addedByUser) {
        this.addedByUser = addedByUser;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getContextNotes() {
        return contextNotes;
    }

    public void setContextNotes(String contextNotes) {
        this.contextNotes = contextNotes;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
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
        ThreadPlayer that = (ThreadPlayer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ThreadPlayer{"
                + "id=" + id
                + ", threadId=" + (thread != null ? thread.getId() : null)
                + ", playerId=" + (player != null ? player.getId() : null)
                + ", displayOrder=" + displayOrder
                + ", isPrimary=" + isPrimary
                + ", createdAt=" + createdAt
                + '}';
    }
}

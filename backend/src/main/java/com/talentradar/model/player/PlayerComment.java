package com.talentradar.model.player;

import java.time.LocalDateTime;
import java.util.Objects;

import com.talentradar.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing user comments and discussions about players. Enables
 * community engagement through player-specific comments, replies, and threaded
 * discussions with voting capabilities.
 */
@Entity
@Table(name = "player_comments")
public class PlayerComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    @NotNull(message = "Player is required")
    private Player player;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull(message = "Author is required")
    private User author;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private PlayerComment parentComment;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Content is required")
    private String content;

    @Column(nullable = false)
    private Integer upvotes = 0;

    @Column(nullable = false)
    private Integer downvotes = 0;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PlayerComment() {
    }

    public PlayerComment(Player player, User author, String content) {
        this.player = player;
        this.author = author;
        this.content = content;
    }

    public PlayerComment(Player player, User author, String content, PlayerComment parentComment) {
        this.player = player;
        this.author = author;
        this.content = content;
        this.parentComment = parentComment;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementUpvotes() {
        this.upvotes++;
    }

    public void decrementUpvotes() {
        if (this.upvotes > 0) {
            this.upvotes--;
        }
    }

    public void incrementDownvotes() {
        this.downvotes++;
    }

    public void decrementDownvotes() {
        if (this.downvotes > 0) {
            this.downvotes--;
        }
    }

    public int getNetScore() {
        return upvotes - downvotes;
    }

    public boolean canBeEditedBy(User user) {
        return author.getId().equals(user.getId()) || user.canModerate();
    }

    public boolean canBeDeletedBy(User user) {
        return author.getId().equals(user.getId()) || user.canModerate();
    }

    public boolean isReply() {
        return parentComment != null;
    }

    public boolean isRecentlyPosted() {
        return createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public PlayerComment getParentComment() {
        return parentComment;
    }

    public void setParentComment(PlayerComment parentComment) {
        this.parentComment = parentComment;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    public Integer getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayerComment that = (PlayerComment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PlayerComment{"
                + "id=" + id
                + ", player=" + (player != null ? player.getName() : "null")
                + ", author=" + (author != null ? author.getUsername() : "null")
                + ", upvotes=" + upvotes
                + ", downvotes=" + downvotes
                + '}';
    }
}

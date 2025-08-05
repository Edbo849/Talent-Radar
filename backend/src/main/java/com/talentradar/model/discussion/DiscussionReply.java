package com.talentradar.model.discussion;

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
 * Entity representing replies to discussion threads. Allows users to respond to
 * discussion topics and create threaded conversations with nested reply
 * structures.
 */
@Entity
@Table(name = "discussion_replies")
public class DiscussionReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "thread_id", nullable = false)
    @NotNull(message = "Thread is required")
    private DiscussionThread thread;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull(message = "Author is required")
    private User author;

    @ManyToOne
    @JoinColumn(name = "parent_reply_id")
    private DiscussionReply parentReply;

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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public DiscussionReply() {
    }

    public DiscussionReply(DiscussionThread thread, User author, String content) {
        this.thread = thread;
        this.author = author;
        this.content = content;
    }

    public DiscussionReply(DiscussionThread thread, User author, String content, DiscussionReply parentReply) {
        this.thread = thread;
        this.author = author;
        this.content = content;
        this.parentReply = parentReply;
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

    public Integer getNetScore() {
        int up = Objects.requireNonNullElse(upvotes, 0);
        int down = Objects.requireNonNullElse(downvotes, 0);
        return up - down;
    }

    public int getTotalVotes() {
        return upvotes + downvotes;
    }

    public double getUpvoteRatio() {
        int total = getTotalVotes();
        if (total == 0) {
            return 0.0;
        }
        return (double) upvotes / total;
    }

    public boolean isControversial() {
        int total = getTotalVotes();
        if (total < 10) {
            return false; // Need minimum votes to be considered controversial

        }
        double ratio = getUpvoteRatio();
        return ratio >= 0.4 && ratio <= 0.6; // Close to 50/50 split
    }

    public boolean canBeEditedBy(User user) {
        return author.getId().equals(user.getId()) || user.canModerate();
    }

    public boolean canBeDeletedBy(User user) {
        return author.getId().equals(user.getId()) || user.canModerate();
    }

    public boolean isReply() {
        return parentReply != null;
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

    public DiscussionThread getThread() {
        return thread;
    }

    public void setThread(DiscussionThread thread) {
        this.thread = thread;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public DiscussionReply getParentReply() {
        return parentReply;
    }

    public void setParentReply(DiscussionReply parentReply) {
        this.parentReply = parentReply;
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

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiscussionReply that = (DiscussionReply) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DiscussionReply{"
                + "id=" + id
                + ", thread=" + (thread != null ? thread.getTitle() : "null")
                + ", author=" + (author != null ? author.getUsername() : "null")
                + ", upvotes=" + upvotes
                + ", downvotes=" + downvotes
                + '}';
    }
}

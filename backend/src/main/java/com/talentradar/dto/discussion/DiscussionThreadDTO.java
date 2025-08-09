package com.talentradar.dto.discussion;

import java.time.LocalDateTime;
import java.util.List;

import com.talentradar.model.enums.ThreadType;

/**
 * Data Transfer Object for DiscussionThread entities. Used to transfer
 * discussion thread information between layers including content, author
 * details, category information, engagement metrics, and associated player
 * references.
 */
public class DiscussionThreadDTO {

    // Primary identifier
    private Long id;

    // Thread title (required, max 200 characters)
    private String title;

    // Thread content (required, text field)
    private String content;

    // Type of thread (enum: GENERAL, PLAYER_ANALYSIS, etc.)
    private ThreadType threadType;

    // Author information (from User relationship)
    private Long authorId;
    private String authorName;
    private String authorProfileImageUrl;
    private String authorBadgeLevel;

    // Category information (from DiscussionCategory relationship)
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;

    // Engagement metrics
    private Integer viewCount;
    private Integer replyCount;

    // Thread status flags
    private Boolean isPinned;
    private Boolean isLocked;
    private Boolean isFeatured;

    // Timestamp fields
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime updatedAt;

    // Derived fields (not directly from model)
    // Associated player information (derived from relationships or tags)
    private List<Long> playerIds;
    private List<String> playerNames;

    // Constructors
    public DiscussionThreadDTO() {
    }

    public DiscussionThreadDTO(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ThreadType getThreadType() {
        return threadType;
    }

    public void setThreadType(ThreadType threadType) {
        this.threadType = threadType;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorProfileImageUrl() {
        return authorProfileImageUrl;
    }

    public void setAuthorProfileImageUrl(String authorProfileImageUrl) {
        this.authorProfileImageUrl = authorProfileImageUrl;
    }

    public String getAuthorBadgeLevel() {
        return authorBadgeLevel;
    }

    public void setAuthorBadgeLevel(String authorBadgeLevel) {
        this.authorBadgeLevel = authorBadgeLevel;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
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

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Derived field getters and setters
    public List<Long> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<Long> playerIds) {
        this.playerIds = playerIds;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(List<String> playerNames) {
        this.playerNames = playerNames;
    }

    @Override
    public String toString() {
        return "DiscussionThreadDTO{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", threadType=" + threadType
                + ", authorName='" + authorName + '\''
                + ", categoryName='" + categoryName + '\''
                + ", viewCount=" + viewCount
                + ", replyCount=" + replyCount
                + '}';
    }
}

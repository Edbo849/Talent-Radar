package com.talentradar.dto.discussion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for DiscussionReply entities. Used to transfer reply
 * information between layers including content, author details, voting metrics,
 * threading information, and user interaction state.
 */
public class DiscussionReplyDTO {

    // Primary identifier
    private Long id;

    // Reply content (required, text field)
    private String content;

    // Author information (from User relationship)
    private Long authorId;
    private String authorName;
    private String authorProfileImageUrl;
    private String authorBadgeLevel;
    private String authorRole;

    // Thread and parent relationships
    private Long threadId;
    private Long parentReplyId;

    // Voting metrics
    private Integer upvotes;
    private Integer downvotes;
    private Integer netScore;

    // Status flags
    private Boolean isFeatured;
    private Boolean isDeleted;

    // User interaction state (derived fields)
    private Boolean hasUserVoted;
    private Boolean userVoteIsUpvote;

    // Timestamp fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // Nested reply structure (derived fields)
    private List<DiscussionReplyDTO> childReplies;

    // Constructors
    public DiscussionReplyDTO() {
    }

    public DiscussionReplyDTO(Long id, String content, String authorName) {
        this.id = id;
        this.content = content;
        this.authorName = authorName;
    }

    // Helper methods
    public Integer getNetScore() {
        // If netScore is already calculated, return it
        if (netScore != null) {
            return netScore;
        }

        // Calculate on-the-fly if not set
        int upvoteCount = Objects.requireNonNullElse(upvotes, 0);
        int downvoteCount = Objects.requireNonNullElse(downvotes, 0);
        return upvoteCount - downvoteCount;
    }

    public boolean hasChildReplies() {
        return childReplies != null && !childReplies.isEmpty();
    }

    public int getChildReplyCount() {
        return childReplies != null ? childReplies.size() : 0;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Long getParentReplyId() {
        return parentReplyId;
    }

    public void setParentReplyId(Long parentReplyId) {
        this.parentReplyId = parentReplyId;
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

    public void setNetScore(Integer netScore) {
        this.netScore = netScore;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Boolean getHasUserVoted() {
        return hasUserVoted;
    }

    public void setHasUserVoted(Boolean hasUserVoted) {
        this.hasUserVoted = hasUserVoted;
    }

    public Boolean getUserVoteIsUpvote() {
        return userVoteIsUpvote;
    }

    public void setUserVoteIsUpvote(Boolean userVoteIsUpvote) {
        this.userVoteIsUpvote = userVoteIsUpvote;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<DiscussionReplyDTO> getChildReplies() {
        return childReplies;
    }

    public void setChildReplies(List<DiscussionReplyDTO> childReplies) {
        this.childReplies = childReplies;
    }

    @Override
    public String toString() {
        return "DiscussionReplyDTO{"
                + "id=" + id
                + ", authorName='" + authorName + '\''
                + ", threadId=" + threadId
                + ", parentReplyId=" + parentReplyId
                + ", upvotes=" + upvotes
                + ", downvotes=" + downvotes
                + ", netScore=" + getNetScore()
                + '}';
    }
}

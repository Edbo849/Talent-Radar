package com.talentradar.dto.player;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data Transfer Object for PlayerComment entities. Used to transfer player
 * comment information between layers including content, author details, voting
 * metrics, and user interaction context.
 */
public class PlayerCommentDTO {

    // Primary identifier
    private Long id;

    // Player context
    private Long playerId;
    private String playerName;

    // Author information
    private Long authorId;
    private String authorName;
    private String authorProfileImage;

    // Comment content
    private String content;

    // Voting metrics
    private Integer upvotes;
    private Integer downvotes;

    // Status flags
    private Boolean isFeatured;
    private Boolean isDeleted;

    // Threading support
    private Long parentCommentId;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User interaction context (derived field)
    private String userVoteType; // UPVOTE, DOWNVOTE, or null

    // Constructors
    public PlayerCommentDTO() {
    }

    public PlayerCommentDTO(Long id, String content, String authorName) {
        this.id = id;
        this.content = content;
        this.authorName = authorName;
    }

    // Helper methods
    public int getNetScore() {
        int up = Objects.requireNonNullElse(upvotes, 0);
        int down = Objects.requireNonNullElse(downvotes, 0);
        return up - down;
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public boolean hasUserVoted() {
        return userVoteType != null;
    }

    public boolean isRecentlyPosted() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
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

    public String getAuthorProfileImage() {
        return authorProfileImage;
    }

    public void setAuthorProfileImage(String authorProfileImage) {
        this.authorProfileImage = authorProfileImage;
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

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
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

    public String getUserVoteType() {
        return userVoteType;
    }

    public void setUserVoteType(String userVoteType) {
        this.userVoteType = userVoteType;
    }

    @Override
    public String toString() {
        return "PlayerCommentDTO{"
                + "id=" + id
                + ", playerName='" + playerName + '\''
                + ", authorName='" + authorName + '\''
                + ", upvotes=" + upvotes
                + ", downvotes=" + downvotes
                + ", isReply=" + isReply()
                + '}';
    }
}

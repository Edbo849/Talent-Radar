package com.talentradar.dto.player;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for PlayerCommentVote entities. Used to transfer comment
 * vote information between layers including vote type, user information, and
 * timestamps.
 */
public class PlayerCommentVoteDTO {

    // Primary identifier
    private Long id;

    // Comment reference
    private Long commentId;

    // User information
    private Long userId;
    private String userName;

    // Vote information
    private String voteType; // UPVOTE or DOWNVOTE

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public PlayerCommentVoteDTO() {
    }

    public PlayerCommentVoteDTO(Long id, Long commentId, Long userId, String voteType) {
        this.id = id;
        this.commentId = commentId;
        this.userId = userId;
        this.voteType = voteType;
    }

    // Helper methods
    public boolean isUpvote() {
        return "UPVOTE".equals(voteType);
    }

    public boolean isDownvote() {
        return "DOWNVOTE".equals(voteType);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getVoteType() {
        return voteType;
    }

    public void setVoteType(String voteType) {
        this.voteType = voteType;
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
    public String toString() {
        return "PlayerCommentVoteDTO{"
                + "id=" + id
                + ", commentId=" + commentId
                + ", userName='" + userName + '\''
                + ", voteType='" + voteType + '\''
                + '}';
    }
}

package com.talentradar.dto.player;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating new PlayerComment entities. Used to
 * transfer comment creation data from client to server with validation
 * constraints matching the model requirements.
 */
public class PlayerCommentCreateDTO {

    // Required fields
    // Player reference (required)
    @NotNull(message = "Player ID is required")
    private Long playerId;

    // Comment content (required)
    @NotBlank(message = "Comment content is required")
    private String content;

    // Optional threading support
    private Long parentCommentId; // For replies

    // Constructors
    public PlayerCommentCreateDTO() {
    }

    public PlayerCommentCreateDTO(Long playerId, String content) {
        this.playerId = playerId;
        this.content = content;
    }

    public PlayerCommentCreateDTO(Long playerId, String content, Long parentCommentId) {
        this.playerId = playerId;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

    // Helper methods
    public boolean isReply() {
        return parentCommentId != null;
    }

    // Getters and setters
    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    @Override
    public String toString() {
        return "PlayerCommentCreateDTO{"
                + "playerId=" + playerId
                + ", contentLength=" + (content != null ? content.length() : 0)
                + ", isReply=" + isReply()
                + '}';
    }
}

package com.talentradar.dto.player;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for PlayerRating entities. Used to transfer player
 * rating information between layers including rating values, category context,
 * user information, and metadata.
 */
public class PlayerRatingDTO {

    // Primary identifier
    private Long id;

    // Player context
    private Long playerId;
    private String playerName;

    // User context
    private Long userId;
    private String userName;
    private String userRole;

    // Rating category context
    private Long categoryId;
    private String categoryName;

    // Rating information
    private BigDecimal rating;
    private String notes;
    private String positionContext;
    private Long matchContext;

    // Status
    private Boolean isActive;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public PlayerRatingDTO() {
    }

    public PlayerRatingDTO(Long id, String playerName, String categoryName, BigDecimal rating) {
        this.id = id;
        this.playerName = playerName;
        this.categoryName = categoryName;
        this.rating = rating;
    }

    // Helper methods
    public boolean isRecent() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }

    public String getRatingDisplay() {
        return rating != null ? rating.toString() + "/10" : "Not rated";
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

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
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

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPositionContext() {
        return positionContext;
    }

    public void setPositionContext(String positionContext) {
        this.positionContext = positionContext;
    }

    public Long getMatchContext() {
        return matchContext;
    }

    public void setMatchContext(Long matchContext) {
        this.matchContext = matchContext;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
        return "PlayerRatingDTO{"
                + "id=" + id
                + ", playerName='" + playerName + '\''
                + ", categoryName='" + categoryName + '\''
                + ", rating=" + rating
                + ", userName='" + userName + '\''
                + '}';
    }
}

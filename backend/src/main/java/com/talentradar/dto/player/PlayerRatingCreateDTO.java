package com.talentradar.dto.player;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating new PlayerRating entities. Used to transfer
 * player rating creation data from client to server with validation constraints
 * matching the model requirements.
 */
public class PlayerRatingCreateDTO {

    // Required fields
    // Rating category (required)
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    // Rating value (required, 1.0 to 10.0)
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "10.0", message = "Rating cannot exceed 10.0")
    private BigDecimal rating;

    // Optional context and notes
    private String notes;
    private String positionContext;
    private Long matchContext;

    // Constructors
    public PlayerRatingCreateDTO() {
    }

    public PlayerRatingCreateDTO(Long categoryId, BigDecimal rating) {
        this.categoryId = categoryId;
        this.rating = rating;
    }

    // Getters and setters
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    @Override
    public String toString() {
        return "PlayerRatingCreateDTO{"
                + "categoryId=" + categoryId
                + ", rating=" + rating
                + ", positionContext='" + positionContext + '\''
                + '}';
    }
}

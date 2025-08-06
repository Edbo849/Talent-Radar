package com.talentradar.model.player;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.talentradar.model.scouting.RatingCategory;
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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing user ratings and evaluations of players. Allows users to
 * rate players on various aspects of performance and provides aggregated rating
 * data for talent assessment.
 */
@Entity
@Table(name = "player_ratings")
public class PlayerRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    @NotNull(message = "Player is required")
    private Player player;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Rating category is required")
    private RatingCategory category;

    @Column(nullable = false, precision = 3, scale = 1)
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "10.0", message = "Rating cannot exceed 10.0")
    private BigDecimal rating;

    @Column(name = "position_context", length = 10)
    private String positionContext;

    @Column(name = "match_context")
    private Long matchContext;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PlayerRating() {
    }

    public PlayerRating(Player player, User user, RatingCategory category, BigDecimal rating) {
        this.player = player;
        this.user = user;
        this.category = category;
        this.rating = rating;
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

    public boolean canBeUpdatedBy(User user) {
        return this.user.getId().equals(user.getId());
    }

    public boolean isRecent() {
        return createdAt.isAfter(LocalDateTime.now().minusDays(30));
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RatingCategory getCategory() {
        return category;
    }

    public void setCategory(RatingCategory category) {
        this.category = category;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayerRating that = (PlayerRating) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PlayerRating{"
                + "id=" + id
                + ", player=" + (player != null ? player.getName() : "null")
                + ", category=" + (category != null ? category.getName() : "null")
                + ", rating=" + rating
                + '}';
    }
}

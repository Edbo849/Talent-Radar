package com.talentradar.model.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entity representing personalized player recommendations for users. Tracks
 * algorithm-generated or manual player suggestions based on user preferences,
 * viewing history, and scouting interests.
 */
@Entity
@Table(name = "user_recommendations")
public class UserRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommender_id", nullable = false)
    private User recommender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_user_id", nullable = false)
    private User recommendedUser;

    @Column(name = "recommendation_text", columnDefinition = "TEXT")
    private String recommendationText;

    @Column(name = "skill_area", length = 100)
    private String skillArea;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserRecommendation() {
    }

    public UserRecommendation(User recommender, User recommendedUser, String recommendationText, String skillArea, Boolean isPublic, LocalDateTime createdAt) {
        this.recommender = recommender;
        this.recommendedUser = recommendedUser;
        this.recommendationText = recommendationText;
        this.skillArea = skillArea;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
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

    public Long getId() {
        return id;
    }

    public User getRecommender() {
        return recommender;
    }

    public void setRecommender(User recommender) {
        this.recommender = recommender;
    }

    public User getRecommendedUser() {
        return recommendedUser;
    }

    public void setRecommendedUser(User recommendedUser) {
        this.recommendedUser = recommendedUser;
    }

    public String getRecommendationText() {
        return recommendationText;
    }

    public void setRecommendationText(String recommendationText) {
        this.recommendationText = recommendationText;
    }

    public String getSkillArea() {
        return skillArea;
    }

    public void setSkillArea(String skillArea) {
        this.skillArea = skillArea;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
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
}

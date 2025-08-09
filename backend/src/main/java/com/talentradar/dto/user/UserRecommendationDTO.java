package com.talentradar.dto.user;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for UserRecommendation entity responses. Contains
 * recommendation information including recommender and recommended user
 * details. Used for displaying user recommendations with full context and
 * metadata.
 */
public class UserRecommendationDTO {

    // Core identification fields
    private Long id;
    private Long recommenderId;
    private String recommenderName;
    private Long recommendedUserId;
    private String recommendedUserName;

    // Recommendation content fields
    private String recommendationText;
    private String skillArea;

    // Configuration fields
    private Boolean isPublic;

    // Timestamp fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* Constructors */
    public UserRecommendationDTO() {
    }

    public UserRecommendationDTO(Long id, String recommendationText, String skillArea) {
        this.id = id;
        this.recommendationText = recommendationText;
        this.skillArea = skillArea;
    }

    /* Getters and Setters */
    // Core identification getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecommenderId() {
        return recommenderId;
    }

    public void setRecommenderId(Long recommenderId) {
        this.recommenderId = recommenderId;
    }

    public String getRecommenderName() {
        return recommenderName;
    }

    public void setRecommenderName(String recommenderName) {
        this.recommenderName = recommenderName;
    }

    public Long getRecommendedUserId() {
        return recommendedUserId;
    }

    public void setRecommendedUserId(Long recommendedUserId) {
        this.recommendedUserId = recommendedUserId;
    }

    public String getRecommendedUserName() {
        return recommendedUserName;
    }

    public void setRecommendedUserName(String recommendedUserName) {
        this.recommendedUserName = recommendedUserName;
    }

    // Recommendation content getters and setters
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

    // Configuration getters and setters
    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    // Timestamp getters and setters
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

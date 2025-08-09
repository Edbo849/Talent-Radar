package com.talentradar.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating new UserRecommendation entities. Contains
 * validation rules and required fields for recommendation creation. Excludes
 * system-generated fields like ID, timestamps, and recommender information.
 */
public class UserRecommendationCreateDTO {

    // Target user identification field
    @NotNull(message = "Recommended user ID is required")
    private Long recommendedUserId;

    // Recommendation content fields
    @NotBlank(message = "Recommendation text is required")
    @Size(max = 1000, message = "Recommendation text must not exceed 1000 characters")
    private String recommendationText;

    @Size(max = 100, message = "Skill area must not exceed 100 characters")
    private String skillArea;

    // Configuration fields
    private Boolean isPublic = false;

    /* Constructors */
    public UserRecommendationCreateDTO() {
    }

    public UserRecommendationCreateDTO(Long recommendedUserId, String recommendationText) {
        this.recommendedUserId = recommendedUserId;
        this.recommendationText = recommendationText;
    }

    /* Getters and Setters */
    // Target user identification getters and setters
    public Long getRecommendedUserId() {
        return recommendedUserId;
    }

    public void setRecommendedUserId(Long recommendedUserId) {
        this.recommendedUserId = recommendedUserId;
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
}

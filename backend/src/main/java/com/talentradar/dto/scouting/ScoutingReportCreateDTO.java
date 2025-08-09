package com.talentradar.dto.scouting;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating new ScoutingReport entities. Contains
 * validation rules and all required fields for scouting report creation.
 * Matches the ScoutingReport model structure with appropriate constraints.
 */
public class ScoutingReportCreateDTO {

    // Core report identification
    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Report content is required")
    private String content;

    // Match context information
    private LocalDate matchDate;

    @Size(max = 100, message = "Opponent club must not exceed 100 characters")
    private String opponentClub;

    // Rating fields with validation matching model constraints
    @Min(value = 1, message = "Overall rating must be at least 1")
    @Max(value = 10, message = "Overall rating must not exceed 10")
    private Integer overallRating;

    @Min(value = 1, message = "Technical rating must be at least 1")
    @Max(value = 10, message = "Technical rating must not exceed 10")
    private Integer technicalRating;

    @Min(value = 1, message = "Physical rating must be at least 1")
    @Max(value = 10, message = "Physical rating must not exceed 10")
    private Integer physicalRating;

    @Min(value = 1, message = "Mental rating must be at least 1")
    @Max(value = 10, message = "Mental rating must not exceed 10")
    private Integer mentalRating;

    // Report configuration and detailed analysis
    private Boolean isPublic = false;
    private String strengths;
    private String weaknesses;
    private String recommendations;

    /* Constructors */
    public ScoutingReportCreateDTO() {
    }

    public ScoutingReportCreateDTO(Long playerId, String title, String content) {
        this.playerId = playerId;
        this.title = title;
        this.content = content;
    }

    /* Getters and Setters */
    // Core report identification getters and setters
    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Match context information getters and setters
    public LocalDate getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDate matchDate) {
        this.matchDate = matchDate;
    }

    public String getOpponentClub() {
        return opponentClub;
    }

    public void setOpponentClub(String opponentClub) {
        this.opponentClub = opponentClub;
    }

    // Rating fields getters and setters
    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public Integer getTechnicalRating() {
        return technicalRating;
    }

    public void setTechnicalRating(Integer technicalRating) {
        this.technicalRating = technicalRating;
    }

    public Integer getPhysicalRating() {
        return physicalRating;
    }

    public void setPhysicalRating(Integer physicalRating) {
        this.physicalRating = physicalRating;
    }

    public Integer getMentalRating() {
        return mentalRating;
    }

    public void setMentalRating(Integer mentalRating) {
        this.mentalRating = mentalRating;
    }

    // Report configuration and detailed analysis getters and setters
    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(String weaknesses) {
        this.weaknesses = weaknesses;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
}

package com.talentradar.dto.scouting;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for ScoutingReport entities. Used to transfer
 * comprehensive scouting report information between layers including report
 * content, ratings, player context, and scout information.
 */
public class ScoutingReportDTO {

    // Primary identifier
    private Long id;

    // Player context
    private Long playerId;
    private String playerName;

    // Scout information
    private Long scoutId;
    private String scoutName;

    // Report content
    private String title;
    private String content;

    // Match context
    private LocalDate matchDate;
    private String opponentClub;

    // Rating information
    private Integer overallRating;
    private Integer technicalRating;
    private Integer physicalRating;
    private Integer mentalRating;

    // Report status and visibility
    private String status;
    private Boolean isPublic;

    // Additional assessments
    private String strengths;
    private String weaknesses;
    private String recommendations;

    // Timestamp fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ScoutingReportDTO() {
    }

    public ScoutingReportDTO(Long id, String title, String playerName, String scoutName) {
        this.id = id;
        this.title = title;
        this.playerName = playerName;
        this.scoutName = scoutName;
    }

    // Helper methods
    public Double getAverageRating() {
        if (technicalRating == null || physicalRating == null || mentalRating == null) {
            return null;
        }
        return (technicalRating + physicalRating + mentalRating) / 3.0;
    }

    public boolean isDraft() {
        return "DRAFT".equals(status);
    }

    public boolean isPublished() {
        return "PUBLISHED".equals(status);
    }

    public boolean hasMatchContext() {
        return matchDate != null || (opponentClub != null && !opponentClub.trim().isEmpty());
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

    public Long getScoutId() {
        return scoutId;
    }

    public void setScoutId(Long scoutId) {
        this.scoutId = scoutId;
    }

    public String getScoutName() {
        return scoutName;
    }

    public void setScoutName(String scoutName) {
        this.scoutName = scoutName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
        return "ScoutingReportDTO{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", playerName='" + playerName + '\''
                + ", scoutName='" + scoutName + '\''
                + ", status='" + status + '\''
                + ", overallRating=" + overallRating
                + '}';
    }
}

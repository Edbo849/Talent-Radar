package com.talentradar.dto.scouting;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for RatingCategory entity responses. Contains all
 * category information including metadata and optional statistics. Used for
 * returning rating category data to clients with usage metrics.
 */
public class RatingCategoryDTO {

    // Core identification fields
    private Long id;
    private String name;
    private String description;

    // Position-specific configuration
    private Boolean positionSpecific;
    private List<String> applicablePositions;

    // Timestamp fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional statistics fields
    private Long usageCount;
    private Double averageRating;

    /* Constructors */
    public RatingCategoryDTO() {
    }

    public RatingCategoryDTO(Long id, String name, String description, Boolean positionSpecific) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.positionSpecific = positionSpecific;
    }

    /* Getters and Setters */
    // Core identification getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Position-specific configuration getters and setters
    public Boolean getPositionSpecific() {
        return positionSpecific;
    }

    public void setPositionSpecific(Boolean positionSpecific) {
        this.positionSpecific = positionSpecific;
    }

    public List<String> getApplicablePositions() {
        return applicablePositions;
    }

    public void setApplicablePositions(List<String> applicablePositions) {
        this.applicablePositions = applicablePositions;
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

    // Statistics getters and setters
    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}

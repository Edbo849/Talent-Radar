package com.talentradar.dto.scouting;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating new RatingCategory entities. Contains
 * validation rules and required fields for category creation. Excludes
 * system-generated fields like ID and timestamps.
 */
public class RatingCategoryCreateDTO {

    // Core category information
    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    // Position-specific configuration
    private Boolean positionSpecific = false;
    private List<String> applicablePositions;

    /* Constructors */
    public RatingCategoryCreateDTO() {
    }

    public RatingCategoryCreateDTO(String name, String description, Boolean positionSpecific) {
        this.name = name;
        this.description = description;
        this.positionSpecific = positionSpecific;
    }

    /* Getters and Setters */
    // Core category information getters and setters
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
}

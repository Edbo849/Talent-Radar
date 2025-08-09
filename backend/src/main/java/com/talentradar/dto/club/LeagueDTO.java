package com.talentradar.dto.club;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for League entities. Used to transfer football league
 * information between layers including name, country, season details, and
 * competition type.
 */
public class LeagueDTO {

    // Primary identifier
    private Long id;

    // External API identifier (from API-Football)
    private Integer externalId;

    // League name (required, max 100 characters)
    private String name;

    // Country where league is held
    private CountryDTO country;

    // URL to league logo image
    private String logoUrl;

    // Season year for this league instance
    private Integer season;

    // Type of competition (e.g., "League", "Cup")
    private String type;

    // Timestamp when league was created
    private LocalDateTime createdAt;

    // Timestamp when league was last updated
    private LocalDateTime updatedAt;

    // Constructors
    public LeagueDTO() {
    }

    public LeagueDTO(Long id, String name, CountryDTO country, Integer season) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.season = season;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CountryDTO getCountry() {
        return country;
    }

    public void setCountry(CountryDTO country) {
        this.country = country;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

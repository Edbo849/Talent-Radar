package com.talentradar.dto.club;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Club entities. Used to transfer football club
 * information between layers including basic details, location, stadium
 * information, and derived statistics.
 */
public class ClubDTO {

    // Primary identifier
    private Long id;

    // External API identifier (from API-Football)
    private Integer externalId;

    // Club name (required, max 100 characters)
    private String name;

    // URL to club logo image
    private String logoUrl;

    // Country where club is based (relationship with Country entity)
    private CountryDTO country;

    // Year the club was founded
    private Integer founded;

    // Whether the club is currently active
    private Boolean isActive;

    // Short name or abbreviation (max 10 characters)
    private String shortName;

    // City where club is located (max 50 characters)
    private String city;

    // Stadium name (max 100 characters)
    private String stadium;

    // Stadium capacity
    private Integer stadiumCapacity;

    // Club website URL (max 200 characters)
    private String websiteUrl;

    // Timestamp when club was created
    private LocalDateTime createdAt;

    // Timestamp when club was last updated
    private LocalDateTime updatedAt;

    // Derived fields (not directly from model)
    // Number of players in the club (calculated field)
    private Integer playerCount;

    // Current league information (derived from relationships)
    private Long currentLeagueId;
    private String currentLeagueName;

    // Constructors
    public ClubDTO() {
    }

    public ClubDTO(Long id, String name, CountryDTO country, String logoUrl) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.logoUrl = logoUrl;
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public CountryDTO getCountry() {
        return country;
    }

    public void setCountry(CountryDTO country) {
        this.country = country;
    }

    public Integer getFounded() {
        return founded;
    }

    public void setFounded(Integer founded) {
        this.founded = founded;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStadium() {
        return stadium;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public Integer getStadiumCapacity() {
        return stadiumCapacity;
    }

    public void setStadiumCapacity(Integer stadiumCapacity) {
        this.stadiumCapacity = stadiumCapacity;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
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

    // Derived field getters and setters
    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public Long getCurrentLeagueId() {
        return currentLeagueId;
    }

    public void setCurrentLeagueId(Long currentLeagueId) {
        this.currentLeagueId = currentLeagueId;
    }

    public String getCurrentLeagueName() {
        return currentLeagueName;
    }

    public void setCurrentLeagueName(String currentLeagueName) {
        this.currentLeagueName = currentLeagueName;
    }
}

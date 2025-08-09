package com.talentradar.dto.player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for Player entities. Used to transfer comprehensive
 * player information between layers including personal details, physical
 * attributes, career statistics, club information, and derived metrics for UI
 * presentation.
 */
public class PlayerDTO {

    // Primary identifier
    private Long id;

    // External API identifier
    private Integer externalId;

    // Personal information
    private String name;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Integer age;
    private String birthPlace;
    private String birthCountry;
    private String nationality;

    // Physical attributes
    private String position;
    private String preferredFoot;
    private Integer heightCm;
    private Integer weightKg;
    private String photoUrl;

    // Status flags
    private Boolean isInjured;
    private Boolean isActive;
    private Boolean isEligibleForU21;

    // Additional profile information
    private String bio;
    private Integer jerseyNumber;
    private Long marketValue;
    private LocalDate contractExpires;

    // Trending and view metrics
    private Double trendingScore;
    private Integer totalViews;
    private Integer weeklyViews;
    private Integer monthlyViews;

    // Current club information (derived from currentClub relationship)
    private Long currentClubId;
    private String currentClubName;
    private String currentClubLogoUrl;
    private String currentClubCountry;

    // Statistics summary (latest season - derived fields)
    private Integer appearances;
    private Integer goals;
    private Integer assists;
    private Integer minutesPlayed;
    private Double rating;

    // Social metrics (derived fields)
    private Integer commentsCount;
    private Integer ratingsCount;
    private Double averageRating;
    private Integer scoutingReportsCount;

    // User context (derived fields)
    private Boolean hasUserRated;
    private Boolean hasUserCommented;
    private Boolean isFollowedByUser;

    // Timestamps
    private LocalDateTime lastActivity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Lists for detailed view (derived fields)
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> expertiseAreas;

    // Constructors
    public PlayerDTO() {
    }

    public PlayerDTO(Long id, String name, Integer age, String position) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.position = position;
    }

    // Helper methods
    public String getFullName() {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        if (firstName != null) {
            return firstName;
        }
        if (lastName != null) {
            return lastName;
        }
        return "Unknown Player";
    }

    public String getDisplayPosition() {
        return position != null ? position : "Unknown";
    }

    public String getHeightDisplay() {
        return heightCm != null ? heightCm + " cm" : "Unknown";
    }

    public String getWeightDisplay() {
        return weightKg != null ? weightKg + " kg" : "Unknown";
    }

    public String getNationalityDisplay() {
        return nationality != null ? nationality : "Unknown";
    }

    public boolean isTrending() {
        return trendingScore != null && trendingScore > 0;
    }

    public String getTrendingStatus() {
        if (trendingScore == null || trendingScore <= 0) {
            return "Not Trending";
        }
        if (trendingScore >= 80) {
            return "Viral";
        }
        if (trendingScore >= 60) {
            return "Hot";
        }
        if (trendingScore >= 40) {
            return "Rising";
        }
        return "Trending";
    }

    public Double getGoalsPerGame() {
        if (appearances == null || appearances == 0 || goals == null) {
            return 0.0;
        }
        return (double) goals / appearances;
    }

    public Double getAssistsPerGame() {
        if (appearances == null || appearances == 0 || assists == null) {
            return 0.0;
        }
        return (double) assists / appearances;
    }

    public Integer getTotalGoalContributions() {
        int g = Objects.requireNonNullElse(goals, 0);
        int a = Objects.requireNonNullElse(assists, 0);
        return g + a;
    }

    public String getMarketValueDisplay() {
        if (marketValue == null || marketValue == 0) {
            return "Unknown";
        }
        if (marketValue >= 1000000) {
            return String.format("€%.1fM", marketValue / 1000000.0);
        }
        if (marketValue >= 1000) {
            return String.format("€%.0fK", marketValue / 1000.0);
        }
        return "€" + marketValue;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getBirthCountry() {
        return birthCountry;
    }

    public void setBirthCountry(String birthCountry) {
        this.birthCountry = birthCountry;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPreferredFoot() {
        return preferredFoot;
    }

    public void setPreferredFoot(String preferredFoot) {
        this.preferredFoot = preferredFoot;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Integer heightCm) {
        this.heightCm = heightCm;
    }

    public Integer getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Integer weightKg) {
        this.weightKg = weightKg;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Boolean getIsInjured() {
        return isInjured;
    }

    public void setIsInjured(Boolean isInjured) {
        this.isInjured = isInjured;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsEligibleForU21() {
        return isEligibleForU21;
    }

    public void setIsEligibleForU21(Boolean isEligibleForU21) {
        this.isEligibleForU21 = isEligibleForU21;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public Long getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Long marketValue) {
        this.marketValue = marketValue;
    }

    public LocalDate getContractExpires() {
        return contractExpires;
    }

    public void setContractExpires(LocalDate contractExpires) {
        this.contractExpires = contractExpires;
    }

    public Double getTrendingScore() {
        return trendingScore;
    }

    public void setTrendingScore(Double trendingScore) {
        this.trendingScore = trendingScore;
    }

    public Integer getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Integer totalViews) {
        this.totalViews = totalViews;
    }

    public Integer getWeeklyViews() {
        return weeklyViews;
    }

    public void setWeeklyViews(Integer weeklyViews) {
        this.weeklyViews = weeklyViews;
    }

    public Integer getMonthlyViews() {
        return monthlyViews;
    }

    public void setMonthlyViews(Integer monthlyViews) {
        this.monthlyViews = monthlyViews;
    }

    public Long getCurrentClubId() {
        return currentClubId;
    }

    public void setCurrentClubId(Long currentClubId) {
        this.currentClubId = currentClubId;
    }

    public String getCurrentClubName() {
        return currentClubName;
    }

    public void setCurrentClubName(String currentClubName) {
        this.currentClubName = currentClubName;
    }

    public String getCurrentClubLogoUrl() {
        return currentClubLogoUrl;
    }

    public void setCurrentClubLogoUrl(String currentClubLogoUrl) {
        this.currentClubLogoUrl = currentClubLogoUrl;
    }

    public String getCurrentClubCountry() {
        return currentClubCountry;
    }

    public void setCurrentClubCountry(String currentClubCountry) {
        this.currentClubCountry = currentClubCountry;
    }

    public Integer getAppearances() {
        return appearances;
    }

    public void setAppearances(Integer appearances) {
        this.appearances = appearances;
    }

    public Integer getGoals() {
        return goals;
    }

    public void setGoals(Integer goals) {
        this.goals = goals;
    }

    public Integer getAssists() {
        return assists;
    }

    public void setAssists(Integer assists) {
        this.assists = assists;
    }

    public Integer getMinutesPlayed() {
        return minutesPlayed;
    }

    public void setMinutesPlayed(Integer minutesPlayed) {
        this.minutesPlayed = minutesPlayed;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Integer getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(Integer ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getScoutingReportsCount() {
        return scoutingReportsCount;
    }

    public void setScoutingReportsCount(Integer scoutingReportsCount) {
        this.scoutingReportsCount = scoutingReportsCount;
    }

    public Boolean getHasUserRated() {
        return hasUserRated;
    }

    public void setHasUserRated(Boolean hasUserRated) {
        this.hasUserRated = hasUserRated;
    }

    public Boolean getHasUserCommented() {
        return hasUserCommented;
    }

    public void setHasUserCommented(Boolean hasUserCommented) {
        this.hasUserCommented = hasUserCommented;
    }

    public Boolean getIsFollowedByUser() {
        return isFollowedByUser;
    }

    public void setIsFollowedByUser(Boolean isFollowedByUser) {
        this.isFollowedByUser = isFollowedByUser;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
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

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses;
    }

    public List<String> getExpertiseAreas() {
        return expertiseAreas;
    }

    public void setExpertiseAreas(List<String> expertiseAreas) {
        this.expertiseAreas = expertiseAreas;
    }

    @Override
    public String toString() {
        return "PlayerDTO{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", age=" + age
                + ", position='" + position + '\''
                + ", currentClubName='" + currentClubName + '\''
                + '}';
    }
}

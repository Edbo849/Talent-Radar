package com.talentradar.model.player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.talentradar.model.club.Club;
import com.talentradar.model.scouting.ScoutingReport;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entity representing a football player in the Talent Radar system. This class
 * stores comprehensive player information including personal details, physical
 * attributes, career statistics, and relationships to clubs, injuries, and
 * transfers. Players can be tracked across different seasons and clubs with
 * detailed performance metrics.
 */
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true)
    private Integer externalId;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Player name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Column(name = "birth_place", length = 100)
    private String birthPlace;

    @Column(name = "birth_country", length = 50)
    private String birthCountry;

    @Column(length = 50)
    private String nationality;

    @Column(length = 50)
    private String position;

    @Column(name = "height_cm")
    @Min(value = 150, message = "Height must be at least 150cm")
    @Max(value = 220, message = "Height must not exceed 220cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    @Min(value = 40, message = "Weight must be at least 40kg")
    @Max(value = 120, message = "Weight must not exceed 120kg")
    private Integer weightKg;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "is_injured")
    private Boolean isInjured = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "trending_score")
    private Double trendingScore = 0.0;

    @Column(name = "total_views")
    private Integer totalViews = 0;

    @Column(name = "weekly_views")
    private Integer weeklyViews = 0;

    @Column(name = "monthly_views")
    private Integer monthlyViews = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_club_id")
    private Club currentClub;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ScoutingReport> scoutingReports = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlayerStatistic> statistics = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PlayerInjury> injuries = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PlayerTransfer> transfers = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PlayerTrophy> trophies = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PlayerSidelined> sidelinedPeriods = new HashSet<>();

    // Constructors
    public Player() {
    }

    public Player(String name, LocalDate dateOfBirth) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
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

    // Helper methods
    public String getFullName() {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }

        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            fullName.append(firstName);
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (fullName.length() > 0) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }

        return fullName.length() > 0 ? fullName.toString() : "Unknown Player";
    }

    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public boolean isEligibleForU21() {
        return getAge() <= 21;
    }

    // Getters and Setters
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

    public Set<ScoutingReport> getScoutingReports() {
        return scoutingReports;
    }

    public void setScoutingReports(Set<ScoutingReport> scoutingReports) {
        this.scoutingReports = scoutingReports;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }

    public List<PlayerStatistic> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<PlayerStatistic> statistics) {
        this.statistics = statistics;
    }

    public Set<PlayerInjury> getInjuries() {
        return injuries;
    }

    public void setInjuries(Set<PlayerInjury> injuries) {
        this.injuries = injuries;
    }

    public Set<PlayerTransfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(Set<PlayerTransfer> transfers) {
        this.transfers = transfers;
    }

    public Set<PlayerTrophy> getTrophies() {
        return trophies;
    }

    public void setTrophies(Set<PlayerTrophy> trophies) {
        this.trophies = trophies;
    }

    public Set<PlayerSidelined> getSidelinedPeriods() {
        return sidelinedPeriods;
    }

    public void setSidelinedPeriods(Set<PlayerSidelined> sidelinedPeriods) {
        this.sidelinedPeriods = sidelinedPeriods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Player player = (Player) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Player{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", age=" + getAge()
                + ", currentClub=" + currentClub
                + '}';
    }

}

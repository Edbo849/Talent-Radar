package com.talentradar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true)
    private Integer externalId;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Name is required")
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ScoutingReport> scoutingReports = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PlayerStatistic> statistics = new HashSet<>();

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
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public String getFullName() {
        return name != null ? name : (firstName + " " + lastName);
    }

    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
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

    public Set<ScoutingReport> getScoutingReports() {
        return scoutingReports;
    }

    public void setScoutingReports(Set<ScoutingReport> scoutingReports) {
        this.scoutingReports = scoutingReports;
    }

    public Set<PlayerStatistic> getStatistics() {
        return statistics;
    }

    public void setStatistics(Set<PlayerStatistic> statistics) {
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
                + '}';
    }
}

package com.talentradar.model.club;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.talentradar.model.player.PlayerStatistic;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entity representing football clubs in the system. Stores club information
 * including name, logo, founding details, and relationships to players,
 * leagues, and countries.
 */
@Entity
@Table(name = "clubs")
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true)
    private Integer externalId;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Club name is required")
    @Size(max = 100, message = "Club name must not exceed 100 characters")
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "founded_year")
    private Integer founded;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "short_name", length = 10)
    private String shortName;

    @Column(name = "city", length = 50)
    private String city;

    @Column(length = 100)
    private String stadium;

    @Column(name = "stadium_capacity")
    private Integer stadiumCapacity;

    @Column(name = "is_national")
    private Boolean isNational = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PlayerStatistic> playerStatistics = new HashSet<>();

    // Constructors
    public Club() {
    }

    public Club(String name) {
        this.name = name;
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
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

    public Boolean getIsNational() {
        return isNational;
    }

    public void setIsNational(Boolean isNational) {
        this.isNational = isNational;
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

    public Set<PlayerStatistic> getPlayerStatistics() {
        return playerStatistics;
    }

    public void setPlayerStatistics(Set<PlayerStatistic> playerStatistics) {
        this.playerStatistics = playerStatistics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Club club = (Club) o;
        return Objects.equals(id, club.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Club{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", country='" + country + '\''
                + '}';
    }

}

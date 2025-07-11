package com.talentradar.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "player_statistics")
public class PlayerStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    @NotNull(message = "Player is required")
    private Player player;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Season is required")
    private String season; // e.g., "2023-24"

    @Column(name = "competition", length = 100)
    private String competition; // e.g., "Premier League", "Champions League"

    @Column(name = "appearances")
    @Min(value = 0, message = "Appearances cannot be negative")
    private Integer appearances = 0;

    @Column(name = "goals")
    @Min(value = 0, message = "Goals cannot be negative")
    private Integer goals = 0;

    @Column(name = "assists")
    @Min(value = 0, message = "Assists cannot be negative")
    private Integer assists = 0;

    @Column(name = "minutes_played")
    @Min(value = 0, message = "Minutes played cannot be negative")
    private Integer minutesPlayed = 0;

    @Column(name = "yellow_cards")
    @Min(value = 0, message = "Yellow cards cannot be negative")
    private Integer yellowCards = 0;

    @Column(name = "red_cards")
    @Min(value = 0, message = "Red cards cannot be negative")
    private Integer redCards = 0;

    @Column(name = "clean_sheets")
    @Min(value = 0, message = "Clean sheets cannot be negative")
    private Integer cleanSheets = 0; // For goalkeepers

    @Column(name = "saves")
    @Min(value = 0, message = "Saves cannot be negative")
    private Integer saves = 0; // For goalkeepers

    @Column(name = "pass_accuracy")
    @Min(value = 0, message = "Pass accuracy cannot be negative")
    private Double passAccuracy; // Percentage

    @Column(name = "shots_on_target")
    @Min(value = 0, message = "Shots on target cannot be negative")
    private Integer shotsOnTarget = 0;

    @Column(name = "total_shots")
    @Min(value = 0, message = "Total shots cannot be negative")
    private Integer totalShots = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public PlayerStatistic() {
    }

    public PlayerStatistic(Player player, String season) {
        this.player = player;
        this.season = season;
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
    public Double getGoalsPerGame() {
        if (appearances == null || appearances == 0) {
            return 0.0;
        }
        return (double) goals / appearances;
    }

    public Double getAssistsPerGame() {
        if (appearances == null || appearances == 0) {
            return 0.0;
        }
        return (double) assists / appearances;
    }

    public Double getMinutesPerGame() {
        if (appearances == null || appearances == 0) {
            return 0.0;
        }
        return (double) minutesPlayed / appearances;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getCompetition() {
        return competition;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
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

    public Integer getYellowCards() {
        return yellowCards;
    }

    public void setYellowCards(Integer yellowCards) {
        this.yellowCards = yellowCards;
    }

    public Integer getRedCards() {
        return redCards;
    }

    public void setRedCards(Integer redCards) {
        this.redCards = redCards;
    }

    public Integer getCleanSheets() {
        return cleanSheets;
    }

    public void setCleanSheets(Integer cleanSheets) {
        this.cleanSheets = cleanSheets;
    }

    public Integer getSaves() {
        return saves;
    }

    public void setSaves(Integer saves) {
        this.saves = saves;
    }

    public Double getPassAccuracy() {
        return passAccuracy;
    }

    public void setPassAccuracy(Double passAccuracy) {
        this.passAccuracy = passAccuracy;
    }

    public Integer getShotsOnTarget() {
        return shotsOnTarget;
    }

    public void setShotsOnTarget(Integer shotsOnTarget) {
        this.shotsOnTarget = shotsOnTarget;
    }

    public Integer getTotalShots() {
        return totalShots;
    }

    public void setTotalShots(Integer totalShots) {
        this.totalShots = totalShots;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayerStatistic that = (PlayerStatistic) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PlayerStatistic{"
                + "id=" + id
                + ", season='" + season + '\''
                + ", goals=" + goals
                + ", assists=" + assists
                + ", appearances=" + appearances
                + '}';
    }
}

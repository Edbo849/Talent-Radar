package com.talentradar.model.player;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.talentradar.model.club.Club;
import com.talentradar.model.club.League;

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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing detailed performance statistics for a player in a
 * specific season. Tracks comprehensive match statistics including goals,
 * assists, passing accuracy, defensive actions, and advanced metrics.
 * Statistics are associated with specific clubs and leagues for accurate
 * historical tracking.
 */
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    @Column(nullable = false)
    @NotNull(message = "Season is required")
    private Integer season;

    // Basic game stats
    @Column(name = "appearances")
    @Min(value = 0, message = "Appearances cannot be negative")
    private Integer appearances = 0;

    @Column(name = "lineups")
    @Min(value = 0, message = "Lineups cannot be negative")
    private Integer lineups = 0;

    @Column(name = "minutes_played")
    @Min(value = 0, message = "Minutes played cannot be negative")
    private Integer minutesPlayed = 0;

    @Column(length = 50)
    private String position;

    @Column(precision = 4, scale = 2)
    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @DecimalMax(value = "10.0", message = "Rating cannot exceed 10.0")
    private BigDecimal rating;

    @Column(name = "is_captain")
    private Boolean isCaptain = false;

    // Goals and assists
    @Column(name = "goals")
    @Min(value = 0, message = "Goals cannot be negative")
    private Integer goals = 0;

    @Column(name = "assists")
    @Min(value = 0, message = "Assists cannot be negative")
    private Integer assists = 0;

    @Column(name = "goals_conceded")
    @Min(value = 0, message = "Goals conceded cannot be negative")
    private Integer goalsConceded = 0;

    @Column(name = "saves")
    @Min(value = 0, message = "Saves cannot be negative")
    private Integer saves = 0;

    // Shots
    @Column(name = "shots_total")
    @Min(value = 0, message = "Total shots cannot be negative")
    private Integer shotsTotal = 0;

    @Column(name = "shots_on_target")
    @Min(value = 0, message = "Shots on target cannot be negative")
    private Integer shotsOnTarget = 0;

    // Passes
    @Column(name = "passes_total")
    @Min(value = 0, message = "Total passes cannot be negative")
    private Integer passesTotal = 0;

    @Column(name = "passes_key")
    @Min(value = 0, message = "Key passes cannot be negative")
    private Integer passesKey = 0;

    // Defensive stats
    @Column(name = "tackles_total")
    @Min(value = 0, message = "Total tackles cannot be negative")
    private Integer tacklesTotal = 0;

    @Column(name = "tackles_blocks")
    @Min(value = 0, message = "Tackle blocks cannot be negative")
    private Integer tacklesBlocks = 0;

    @Column(name = "interceptions")
    @Min(value = 0, message = "Interceptions cannot be negative")
    private Integer interceptions = 0;

    // Dribbles
    @Column(name = "dribbles_attempts")
    @Min(value = 0, message = "Dribble attempts cannot be negative")
    private Integer dribblesAttempts = 0;

    @Column(name = "dribbles_success")
    @Min(value = 0, message = "Successful dribbles cannot be negative")
    private Integer dribblesSuccess = 0;

    // Fouls and cards
    @Column(name = "fouls_drawn")
    @Min(value = 0, message = "Fouls drawn cannot be negative")
    private Integer foulsDrawn = 0;

    @Column(name = "fouls_committed")
    @Min(value = 0, message = "Fouls committed cannot be negative")
    private Integer foulsCommitted = 0;

    @Column(name = "yellow_cards")
    @Min(value = 0, message = "Yellow cards cannot be negative")
    private Integer yellowCards = 0;

    @Column(name = "red_cards")
    @Min(value = 0, message = "Red cards cannot be negative")
    private Integer redCards = 0;

    // Penalties
    @Column(name = "penalties_won")
    @Min(value = 0, message = "Penalties won cannot be negative")
    private Integer penaltiesWon = 0;

    @Column(name = "penalties_scored")
    @Min(value = 0, message = "Penalties scored cannot be negative")
    private Integer penaltiesScored = 0;

    @Column(name = "penalties_missed")
    @Min(value = 0, message = "Penalties missed cannot be negative")
    private Integer penaltiesMissed = 0;

    // Substitutions
    @Column(name = "substitutes_in")
    @Min(value = 0, message = "Substitutes in cannot be negative")
    private Integer substitutesIn = 0;

    @Column(name = "substitutes_out")
    @Min(value = 0, message = "Substitutes out cannot be negative")
    private Integer substitutesOut = 0;

    @Column(name = "substitutes_bench")
    @Min(value = 0, message = "Substitutes bench cannot be negative")
    private Integer substitutesBench = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public PlayerStatistic() {
    }

    public PlayerStatistic(Player player, Club club, League league, Integer season) {
        this.player = player;
        this.club = club;
        this.league = league;
        this.season = season;
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

    public Double getDribbleSuccessRate() {
        if (dribblesAttempts == null || dribblesAttempts == 0) {
            return 0.0;
        }
        return (double) dribblesSuccess / dribblesAttempts * 100;
    }

    public Double getShotAccuracy() {
        if (shotsTotal == null || shotsTotal == 0) {
            return 0.0;
        }
        return (double) shotsOnTarget / shotsTotal * 100;
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

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public Integer getAppearances() {
        return appearances;
    }

    public void setAppearances(Integer appearances) {
        this.appearances = appearances;
    }

    public Integer getLineups() {
        return lineups;
    }

    public void setLineups(Integer lineups) {
        this.lineups = lineups;
    }

    public Integer getMinutesPlayed() {
        return minutesPlayed;
    }

    public void setMinutesPlayed(Integer minutesPlayed) {
        this.minutesPlayed = minutesPlayed;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Boolean getIsCaptain() {
        return isCaptain;
    }

    public void setIsCaptain(Boolean isCaptain) {
        this.isCaptain = isCaptain;
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

    public Integer getGoalsConceded() {
        return goalsConceded;
    }

    public void setGoalsConceded(Integer goalsConceded) {
        this.goalsConceded = goalsConceded;
    }

    public Integer getSaves() {
        return saves;
    }

    public void setSaves(Integer saves) {
        this.saves = saves;
    }

    public Integer getShotsTotal() {
        return shotsTotal;
    }

    public void setShotsTotal(Integer shotsTotal) {
        this.shotsTotal = shotsTotal;
    }

    public Integer getShotsOnTarget() {
        return shotsOnTarget;
    }

    public void setShotsOnTarget(Integer shotsOnTarget) {
        this.shotsOnTarget = shotsOnTarget;
    }

    public Integer getPassesTotal() {
        return passesTotal;
    }

    public void setPassesTotal(Integer passesTotal) {
        this.passesTotal = passesTotal;
    }

    public Integer getPassesKey() {
        return passesKey;
    }

    public void setPassesKey(Integer passesKey) {
        this.passesKey = passesKey;
    }

    public Integer getTacklesTotal() {
        return tacklesTotal;
    }

    public void setTacklesTotal(Integer tacklesTotal) {
        this.tacklesTotal = tacklesTotal;
    }

    public Integer getTacklesBlocks() {
        return tacklesBlocks;
    }

    public void setTacklesBlocks(Integer tacklesBlocks) {
        this.tacklesBlocks = tacklesBlocks;
    }

    public Integer getInterceptions() {
        return interceptions;
    }

    public void setInterceptions(Integer interceptions) {
        this.interceptions = interceptions;
    }

    public Integer getDribblesAttempts() {
        return dribblesAttempts;
    }

    public void setDribblesAttempts(Integer dribblesAttempts) {
        this.dribblesAttempts = dribblesAttempts;
    }

    public Integer getDribblesSuccess() {
        return dribblesSuccess;
    }

    public void setDribblesSuccess(Integer dribblesSuccess) {
        this.dribblesSuccess = dribblesSuccess;
    }

    public Integer getFoulsDrawn() {
        return foulsDrawn;
    }

    public void setFoulsDrawn(Integer foulsDrawn) {
        this.foulsDrawn = foulsDrawn;
    }

    public Integer getFoulsCommitted() {
        return foulsCommitted;
    }

    public void setFoulsCommitted(Integer foulsCommitted) {
        this.foulsCommitted = foulsCommitted;
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

    public Integer getPenaltiesWon() {
        return penaltiesWon;
    }

    public void setPenaltiesWon(Integer penaltiesWon) {
        this.penaltiesWon = penaltiesWon;
    }

    public Integer getPenaltiesScored() {
        return penaltiesScored;
    }

    public void setPenaltiesScored(Integer penaltiesScored) {
        this.penaltiesScored = penaltiesScored;
    }

    public Integer getPenaltiesMissed() {
        return penaltiesMissed;
    }

    public void setPenaltiesMissed(Integer penaltiesMissed) {
        this.penaltiesMissed = penaltiesMissed;
    }

    public Integer getSubstitutesIn() {
        return substitutesIn;
    }

    public void setSubstitutesIn(Integer substitutesIn) {
        this.substitutesIn = substitutesIn;
    }

    public Integer getSubstitutesOut() {
        return substitutesOut;
    }

    public void setSubstitutesOut(Integer substitutesOut) {
        this.substitutesOut = substitutesOut;
    }

    public Integer getSubstitutesBench() {
        return substitutesBench;
    }

    public void setSubstitutesBench(Integer substitutesBench) {
        this.substitutesBench = substitutesBench;
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
                + ", season=" + season
                + ", goals=" + goals
                + ", assists=" + assists
                + ", appearances=" + appearances
                + '}';
    }
}

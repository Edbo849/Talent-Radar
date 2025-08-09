package com.talentradar.dto.player;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for PlayerStatistic entities. Used to transfer
 * comprehensive player statistics between layers including performance metrics,
 * club and league context, and calculated fields.
 */
public class PlayerStatisticDTO {

    // Primary identifier
    private Long id;

    // Player context
    private Long playerId;
    private String playerName;

    // Club context
    private Long clubId;
    private String clubName;
    private String clubLogoUrl;

    // League context
    private Long leagueId;
    private String leagueName;

    // Season
    private Integer season;

    // Basic game statistics
    private Integer appearances;
    private Integer lineups;
    private Integer minutesPlayed;
    private String position;
    private BigDecimal rating;
    private Boolean isCaptain;

    // Goals and assists
    private Integer goals;
    private Integer assists;
    private Integer goalsConceded;
    private Integer saves;

    // Shooting statistics
    private Integer shotsTotal;
    private Integer shotsOnTarget;

    // Passing statistics
    private Integer passesTotal;
    private Integer passesKey;
    private BigDecimal passAccuracy;

    // Defensive statistics
    private Integer tacklesTotal;
    private Integer tacklesBlocks;
    private Integer interceptions;

    // Dribbling statistics
    private Integer dribblesAttempts;
    private Integer dribblesSuccess;

    // Disciplinary statistics
    private Integer foulsDrawn;
    private Integer foulsCommitted;
    private Integer yellowCards;
    private Integer redCards;

    // Penalty statistics
    private Integer penaltiesWon;
    private Integer penaltiesScored;
    private Integer penaltiesMissed;

    // Substitution statistics
    private Integer substitutesIn;
    private Integer substitutesOut;
    private Integer substitutesBench;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields (derived from statistics)
    private Double goalsPerGame;
    private Double assistsPerGame;
    private Double minutesPerGame;
    private Double dribbleSuccessRate;
    private Double shotAccuracy;

    // Constructors
    public PlayerStatisticDTO() {
    }

    public PlayerStatisticDTO(Long id, String playerName, Integer season) {
        this.id = id;
        this.playerName = playerName;
        this.season = season;
    }

    // Helper methods
    public void calculateDerivedStats() {
        // Calculate goals per game
        if (appearances != null && appearances > 0 && goals != null) {
            this.goalsPerGame = (double) goals / appearances;
        }

        // Calculate assists per game
        if (appearances != null && appearances > 0 && assists != null) {
            this.assistsPerGame = (double) assists / appearances;
        }

        // Calculate minutes per game
        if (appearances != null && appearances > 0 && minutesPlayed != null) {
            this.minutesPerGame = (double) minutesPlayed / appearances;
        }

        // Calculate dribble success rate
        if (dribblesAttempts != null && dribblesAttempts > 0 && dribblesSuccess != null) {
            this.dribbleSuccessRate = ((double) dribblesSuccess / dribblesAttempts) * 100;
        }

        // Calculate shot accuracy
        if (shotsTotal != null && shotsTotal > 0 && shotsOnTarget != null) {
            this.shotAccuracy = ((double) shotsOnTarget / shotsTotal) * 100;
        }
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getClubLogoUrl() {
        return clubLogoUrl;
    }

    public void setClubLogoUrl(String clubLogoUrl) {
        this.clubLogoUrl = clubLogoUrl;
    }

    public Long getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
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

    public BigDecimal getPassAccuracy() {
        return passAccuracy;
    }

    public void setPassAccuracy(BigDecimal passAccuracy) {
        this.passAccuracy = passAccuracy;
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

    // Calculated field getters and setters
    public Double getGoalsPerGame() {
        return goalsPerGame;
    }

    public void setGoalsPerGame(Double goalsPerGame) {
        this.goalsPerGame = goalsPerGame;
    }

    public Double getAssistsPerGame() {
        return assistsPerGame;
    }

    public void setAssistsPerGame(Double assistsPerGame) {
        this.assistsPerGame = assistsPerGame;
    }

    public Double getMinutesPerGame() {
        return minutesPerGame;
    }

    public void setMinutesPerGame(Double minutesPerGame) {
        this.minutesPerGame = minutesPerGame;
    }

    public Double getDribbleSuccessRate() {
        return dribbleSuccessRate;
    }

    public void setDribbleSuccessRate(Double dribbleSuccessRate) {
        this.dribbleSuccessRate = dribbleSuccessRate;
    }

    public Double getShotAccuracy() {
        return shotAccuracy;
    }

    public void setShotAccuracy(Double shotAccuracy) {
        this.shotAccuracy = shotAccuracy;
    }

    @Override
    public String toString() {
        return "PlayerStatisticDTO{"
                + "id=" + id
                + ", playerName='" + playerName + '\''
                + ", season=" + season
                + ", goals=" + goals
                + ", assists=" + assists
                + ", appearances=" + appearances
                + '}';
    }
}

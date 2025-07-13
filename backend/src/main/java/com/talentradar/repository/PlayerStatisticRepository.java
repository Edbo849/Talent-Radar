package com.talentradar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.talentradar.model.Club;
import com.talentradar.model.League;
import com.talentradar.model.Player;
import com.talentradar.model.PlayerStatistic;

@Repository
public interface PlayerStatisticRepository extends JpaRepository<PlayerStatistic, Long> {

    // Find all statistics for a specific player
    List<PlayerStatistic> findByPlayer(Player player);

    // Find statistics for a player in a specific season
    List<PlayerStatistic> findByPlayerAndSeason(Player player, Integer season);

    // Find specific statistic by player, club, league and season
    Optional<PlayerStatistic> findByPlayerAndClubAndLeagueAndSeason(Player player, Club club, League league, Integer season);

    // Find all statistics for a specific season
    List<PlayerStatistic> findBySeason(Integer season);

    // Find statistics for a club in a specific season
    List<PlayerStatistic> findByClubAndSeason(Club club, Integer season);

    // Find statistics for a league in a specific season
    List<PlayerStatistic> findByLeagueAndSeason(League league, Integer season);

    // Find top goal scorers in a season
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.season = :season ORDER BY ps.goals DESC")
    List<PlayerStatistic> findTopGoalScorersBySeason(Integer season);

    // Find top assist providers in a season
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.season = :season ORDER BY ps.assists DESC")
    List<PlayerStatistic> findTopAssistProvidersBySeason(Integer season);

    // Find players with most appearances in a season
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.season = :season ORDER BY ps.appearances DESC")
    List<PlayerStatistic> findMostActivePlayersBySeason(Integer season);
}

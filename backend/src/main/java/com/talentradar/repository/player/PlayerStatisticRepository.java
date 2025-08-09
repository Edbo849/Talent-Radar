package com.talentradar.repository.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.club.Club;
import com.talentradar.model.club.League;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerStatistic;

/**
 * Repository interface for managing PlayerStatistic entities. Provides data
 * access operations for player statistics management, performance analysis,
 * season tracking, and leaderboard queries.
 */
@Repository
public interface PlayerStatisticRepository extends JpaRepository<PlayerStatistic, Long> {

    /* Basic player statistics */
    // Find all statistics for a specific player
    List<PlayerStatistic> findByPlayer(Player player);

    // Find all statistics for a specific player ordered by season
    List<PlayerStatistic> findByPlayerOrderBySeasonDesc(Player player);

    // Find statistics for a player in a specific season
    List<PlayerStatistic> findByPlayerAndSeason(Player player, Integer season);

    // Find statistics for a player and club ordered by season
    List<PlayerStatistic> findByPlayerAndClubOrderBySeasonDesc(Player player, Club club);

    /* Season-based finder methods */
    // Find all statistics for a specific season
    List<PlayerStatistic> findBySeason(Integer season);

    // Find statistics for multiple seasons
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.player = :player AND ps.season IN :seasons ORDER BY ps.season DESC")
    List<PlayerStatistic> findByPlayerAndSeasonIn(@Param("player") Player player, @Param("seasons") List<Integer> seasons);

    /* Club-based finder methods */
    // Find statistics for a club in a specific season
    List<PlayerStatistic> findByClubAndSeason(Club club, Integer season);

    // Find statistics by club and season ordered by goals
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.club = :club AND ps.season = :season ORDER BY ps.goals DESC")
    List<PlayerStatistic> findByClubAndSeasonOrderByGoalsDesc(@Param("club") Club club, @Param("season") Integer season);

    /* League-based finder methods */
    // Find statistics for a league in a specific season
    List<PlayerStatistic> findByLeagueAndSeason(League league, Integer season);

    // Find statistics by league and season ordered by goals
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.league = :league AND ps.season = :season ORDER BY ps.goals DESC")
    List<PlayerStatistic> findByLeagueAndSeasonOrderByGoalsDesc(@Param("league") League league, @Param("season") Integer season);

    /* Position-based finder methods */
    // Find players by position
    List<PlayerStatistic> findByPositionOrderBySeasonDesc(String position);

    // Find statistics by position and season ordered by goals
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.position = :position AND ps.season = :season ORDER BY ps.goals DESC")
    Page<PlayerStatistic> findByPositionAndSeasonOrderByGoalsDesc(@Param("position") String position, @Param("season") Integer season, Pageable pageable);

    /* Specific statistic queries */
    // Find specific statistic by player, club, league and season
    Optional<PlayerStatistic> findByPlayerAndClubAndLeagueAndSeason(Player player, Club club, League league, Integer season);

    // Additional useful methods
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.player = :player AND ps.club = :club AND ps.season = :season")
    Optional<PlayerStatistic> findByPlayerAndClubAndSeason(@Param("player") Player player, @Param("club") Club club, @Param("season") Integer season);

    /* Top performers - Goal scoring */
    // Find top goal scorers in a season (with pagination)
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.season = :season ORDER BY ps.goals DESC")
    Page<PlayerStatistic> findTopGoalScorersBySeason(@Param("season") Integer season, Pageable pageable);

    // Find top goal scorers in a season (without pagination - for existing method)
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.season = :season ORDER BY ps.goals DESC")
    List<PlayerStatistic> findTopGoalScorersBySeason(@Param("season") Integer season);

    // Find top scorers across all seasons
    @Query("SELECT ps FROM PlayerStatistic ps ORDER BY ps.goals DESC")
    Page<PlayerStatistic> findTopGoalScorersAllTime(Pageable pageable);

    /* Top performers - Assists */
    // Find top assist providers in a season (with pagination)
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.season = :season ORDER BY ps.assists DESC")
    Page<PlayerStatistic> findTopAssistProvidersBySeason(@Param("season") Integer season, Pageable pageable);

    // Find top assist providers in a season (without pagination - for existing method)
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.season = :season ORDER BY ps.assists DESC")
    List<PlayerStatistic> findTopAssistProvidersBySeason(@Param("season") Integer season);

    /* Top performers - Activity and ratings */
    // Find most active players in a season (with pagination)
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.season = :season ORDER BY ps.appearances DESC")
    Page<PlayerStatistic> findMostActivePlayersBySeason(@Param("season") Integer season, Pageable pageable);

    // Find most active players in a season (without pagination - for existing method)
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.season = :season ORDER BY ps.appearances DESC")
    List<PlayerStatistic> findMostActivePlayersBySeason(@Param("season") Integer season);

    // Find players with highest ratings in a season
    @Query("SELECT ps FROM PlayerStatistic ps WHERE ps.season = :season AND ps.rating IS NOT NULL ORDER BY ps.rating DESC")
    Page<PlayerStatistic> findTopRatedPlayersBySeason(@Param("season") Integer season, Pageable pageable);

    /* Career and aggregation queries */
    // Find career statistics summary for a player
    @Query("SELECT SUM(ps.goals), SUM(ps.assists), SUM(ps.appearances), SUM(ps.minutesPlayed) FROM PlayerStatistic ps WHERE ps.player = :player")
    Object[] findCareerSummaryByPlayer(@Param("player") Player player);

    // Find players who played for multiple clubs
    @Query("SELECT ps.player FROM PlayerStatistic ps GROUP BY ps.player HAVING COUNT(DISTINCT ps.club) > 1")
    List<Player> findPlayersWithMultipleClubs();
}

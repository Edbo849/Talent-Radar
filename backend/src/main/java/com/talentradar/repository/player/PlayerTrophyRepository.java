package com.talentradar.repository.player;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerTrophy;

/**
 * Repository interface for managing PlayerTrophy entities. Provides data access
 * operations for trophy management, achievement tracking, success analytics,
 * and competitive analysis.
 */
@Repository
public interface PlayerTrophyRepository extends JpaRepository<PlayerTrophy, Long> {

    /* Basic trophy finder methods */
    // Find all trophies for a specific player
    List<PlayerTrophy> findByPlayer(Player player);

    // Find all trophies for a player ordered by season
    List<PlayerTrophy> findByPlayerOrderBySeasonDesc(Player player);

    /* League-based finder methods */
    // Find trophies by league name
    List<PlayerTrophy> findByLeagueName(String leagueName);

    // Find trophies by league name for a specific player
    List<PlayerTrophy> findByPlayerAndLeagueName(Player player, String leagueName);

    // Find trophies by league name containing (case-insensitive)
    List<PlayerTrophy> findByLeagueNameContainingIgnoreCase(String leagueName);

    // Find unique leagues where player has trophies
    @Query("SELECT DISTINCT pt.leagueName FROM PlayerTrophy pt WHERE pt.player = :player")
    List<String> findUniqueLeaguesForPlayer(@Param("player") Player player);

    /* Country-based finder methods */
    // Find trophies by country
    List<PlayerTrophy> findByCountry(String country);

    // Find trophies by country for a specific player
    List<PlayerTrophy> findByPlayerAndCountry(Player player, String country);

    // Find unique countries where player has trophies
    @Query("SELECT DISTINCT pt.country FROM PlayerTrophy pt WHERE pt.player = :player")
    List<String> findUniqueCountriesForPlayer(@Param("player") Player player);

    /* Season-based finder methods */
    // Find trophies by season
    List<PlayerTrophy> findBySeason(String season);

    // Find trophies by season for a specific player
    List<PlayerTrophy> findByPlayerAndSeason(Player player, String season);

    // Find trophies in multiple seasons
    @Query("SELECT pt FROM PlayerTrophy pt WHERE pt.season IN :seasons")
    List<PlayerTrophy> findBySeasonIn(@Param("seasons") List<String> seasons);

    // Find recent trophies (last N seasons)
    @Query("SELECT pt FROM PlayerTrophy pt WHERE pt.season >= :fromSeason ORDER BY pt.season DESC")
    List<PlayerTrophy> findRecentTrophies(@Param("fromSeason") String fromSeason);

    /* Place/Position-based finder methods */
    // Find trophies by place/position
    List<PlayerTrophy> findByPlace(String place);

    // Find trophies by place for a specific player
    List<PlayerTrophy> findByPlayerAndPlace(Player player, String place);

    // Find first place trophies (winners)
    @Query("SELECT pt FROM PlayerTrophy pt WHERE pt.place = '1st' OR pt.place = 'Winner' OR pt.place = 'Champions'")
    List<PlayerTrophy> findWinnerTrophies();

    // Find first place trophies for a specific player
    @Query("SELECT pt FROM PlayerTrophy pt WHERE pt.player = :player AND (pt.place = '1st' OR pt.place = 'Winner' OR pt.place = 'Champions')")
    List<PlayerTrophy> findWinnerTrophiesForPlayer(@Param("player") Player player);

    /* Multi-player queries */
    // Find trophies for multiple players
    @Query("SELECT pt FROM PlayerTrophy pt WHERE pt.player IN :players")
    List<PlayerTrophy> findByPlayerIn(@Param("players") List<Player> players);

    /* Count methods */
    // Count trophies for a player
    long countByPlayer(Player player);

    // Count winner trophies for a player
    @Query("SELECT COUNT(pt) FROM PlayerTrophy pt WHERE pt.player = :player AND (pt.place = '1st' OR pt.place = 'Winner' OR pt.place = 'Champions')")
    long countWinnerTrophiesForPlayer(@Param("player") Player player);

    /* Existence checks */
    // Check if player has won specific trophy
    boolean existsByPlayerAndLeagueNameAndPlace(Player player, String leagueName, String place);

    /* Analytics and statistics */
    // Find players with most trophies
    @Query("SELECT pt.player, COUNT(pt) FROM PlayerTrophy pt GROUP BY pt.player ORDER BY COUNT(pt) DESC")
    List<Object[]> findPlayersWithMostTrophies();

    // Find most successful leagues (most trophies awarded)
    @Query("SELECT pt.leagueName, COUNT(pt) FROM PlayerTrophy pt GROUP BY pt.leagueName ORDER BY COUNT(pt) DESC")
    List<Object[]> findMostSuccessfulLeagues();

    // Find most successful countries (most trophies awarded)
    @Query("SELECT pt.country, COUNT(pt) FROM PlayerTrophy pt GROUP BY pt.country ORDER BY COUNT(pt) DESC")
    List<Object[]> findMostSuccessfulCountries();

    // Find trophy distribution by place
    @Query("SELECT pt.place, COUNT(pt) FROM PlayerTrophy pt GROUP BY pt.place ORDER BY COUNT(pt) DESC")
    List<Object[]> findTrophyDistributionByPlace();

    // Find seasonal trophy distribution
    @Query("SELECT pt.season, COUNT(pt) FROM PlayerTrophy pt GROUP BY pt.season ORDER BY pt.season DESC")
    List<Object[]> findSeasonalTrophyDistribution();
}

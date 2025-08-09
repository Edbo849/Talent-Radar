package com.talentradar.repository.player;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerSidelined;

/**
 * Repository interface for managing PlayerSidelined entities. Provides data
 * access operations for tracking sidelined periods, suspension management,
 * availability monitoring, and team planning.
 */
@Repository
public interface PlayerSidelinedRepository extends JpaRepository<PlayerSidelined, Long> {

    /* Basic sidelined finder methods */
    // Find all sidelined periods for a specific player
    List<PlayerSidelined> findByPlayer(Player player);

    // Find all sidelined periods for a player ordered by start date
    List<PlayerSidelined> findByPlayerOrderByStartDateDesc(Player player);

    /* Type-based finder methods */
    // Find sidelined periods by type
    List<PlayerSidelined> findByType(String type);

    // Find sidelined periods by type for a specific player
    List<PlayerSidelined> findByPlayerAndType(Player player, String type);

    /* Current status queries */
    // Find current sidelined periods (no end date or end date in future)
    @Query("SELECT ps FROM PlayerSidelined ps WHERE ps.endDate IS NULL OR ps.endDate >= CURRENT_DATE")
    List<PlayerSidelined> findCurrentSidelinedPeriods();

    // Find current sidelined periods for a specific player
    @Query("SELECT ps FROM PlayerSidelined ps WHERE ps.player = :player AND (ps.endDate IS NULL OR ps.endDate >= CURRENT_DATE)")
    List<PlayerSidelined> findCurrentSidelinedPeriodsForPlayer(@Param("player") Player player);

    // Find players currently sidelined
    @Query("SELECT DISTINCT ps.player FROM PlayerSidelined ps WHERE ps.endDate IS NULL OR ps.endDate >= CURRENT_DATE")
    List<Player> findCurrentlySidelinedPlayers();

    // Check if player is currently sidelined
    @Query("SELECT COUNT(ps) > 0 FROM PlayerSidelined ps WHERE ps.player = :player AND (ps.endDate IS NULL OR ps.endDate >= CURRENT_DATE)")
    boolean isPlayerCurrentlySidelined(@Param("player") Player player);

    /* Active vs completed status */
    // Find active sidelined periods (no end date)
    @Query("SELECT ps FROM PlayerSidelined ps WHERE ps.endDate IS NULL")
    List<PlayerSidelined> findActiveSidelinedPeriods();

    // Find completed sidelined periods
    @Query("SELECT ps FROM PlayerSidelined ps WHERE ps.endDate IS NOT NULL AND ps.endDate < CURRENT_DATE")
    List<PlayerSidelined> findCompletedSidelinedPeriods();

    /* Time-based finder methods */
    // Find sidelined periods between dates
    @Query("SELECT ps FROM PlayerSidelined ps WHERE ps.startDate >= :startDate AND ps.startDate <= :endDate")
    List<PlayerSidelined> findByStartDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find sidelined periods that ended in a specific period
    @Query("SELECT ps FROM PlayerSidelined ps WHERE ps.endDate >= :startDate AND ps.endDate <= :endDate")
    List<PlayerSidelined> findByEndDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find recent sidelined periods (within specified days)
    @Query("SELECT ps FROM PlayerSidelined ps WHERE ps.startDate >= :date ORDER BY ps.startDate DESC")
    List<PlayerSidelined> findRecentSidelinedPeriods(@Param("date") LocalDate date);

    /* Duration analysis */
    // Find long-term sidelined periods (more than specified days)
    @Query("SELECT ps FROM PlayerSidelined ps WHERE DATEDIFF(COALESCE(ps.endDate, CURRENT_DATE), ps.startDate) > :days")
    List<PlayerSidelined> findLongTermSidelinedPeriods(@Param("days") int days);

    /* Overlap analysis */
    // Find overlapping sidelined periods for a player
    @Query("SELECT ps FROM PlayerSidelined ps WHERE ps.player = :player AND ps.startDate <= :endDate AND (ps.endDate IS NULL OR ps.endDate >= :startDate)")
    List<PlayerSidelined> findOverlappingPeriods(@Param("player") Player player, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /* Count methods */
    // Count total sidelined periods for a player
    long countByPlayer(Player player);

    /* Analytics and statistics */
    // Find most common sidelined types
    @Query("SELECT ps.type, COUNT(ps) FROM PlayerSidelined ps GROUP BY ps.type ORDER BY COUNT(ps) DESC")
    List<Object[]> findMostCommonSidelinedTypes();
}

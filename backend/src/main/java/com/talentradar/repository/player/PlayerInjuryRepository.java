package com.talentradar.repository.player;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.club.Club;
import com.talentradar.model.club.League;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerInjury;

/**
 * Repository interface for managing PlayerInjury entities. Provides data access
 * operations for injury tracking, recovery monitoring, club statistics, and
 * injury prevention analysis.
 */
@Repository
public interface PlayerInjuryRepository extends JpaRepository<PlayerInjury, Long> {

    /* Basic injury finder methods */
    // Find all injuries for a specific player
    List<PlayerInjury> findByPlayer(Player player);

    // Find all injuries for a specific player ordered by start date
    List<PlayerInjury> findByPlayerOrderByStartDateDesc(Player player);

    /* Status-based finder methods */

 /* Type-based finder methods */
    // Find injuries by type
    List<PlayerInjury> findByInjuryType(String injuryType);

    // Find injuries by reason
    List<PlayerInjury> findByReasonContainingIgnoreCase(String reason);

    /* Club and league finder methods */
    // Find injuries by club
    List<PlayerInjury> findByClub(Club club);

    // Find injuries by league
    List<PlayerInjury> findByLeague(League league);

    // Find injuries for players in a specific club and season
    @Query("SELECT pi FROM PlayerInjury pi WHERE pi.club = :club AND pi.startDate >= :seasonStart AND pi.startDate <= :seasonEnd")
    List<PlayerInjury> findByClubAndSeason(@Param("club") Club club, @Param("seasonStart") LocalDate seasonStart, @Param("seasonEnd") LocalDate seasonEnd);

    /* Fixture-based finder methods */
    // Find injuries by fixture ID
    List<PlayerInjury> findByFixtureId(Integer fixtureId);

    // Find recent injuries (within specified days)
    @Query("SELECT pi FROM PlayerInjury pi WHERE pi.startDate >= :date ORDER BY pi.startDate DESC")
    List<PlayerInjury> findRecentInjuries(@Param("date") LocalDate date);

    /* Long-term and severity analysis */
    // Find long-term injuries (more than specified days)
    @Query("SELECT pi FROM PlayerInjury pi WHERE  DATEDIFF(CURRENT_DATE, pi.startDate) > :days")
    List<PlayerInjury> findLongTermInjuries(@Param("days") int days);

    /* Analytics and statistics */
    // Find most common injury types
    @Query("SELECT pi.injuryType, COUNT(pi) FROM PlayerInjury pi GROUP BY pi.injuryType ORDER BY COUNT(pi) DESC")
    List<Object[]> findMostCommonInjuryTypes();
}

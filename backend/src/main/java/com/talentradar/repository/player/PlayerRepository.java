package com.talentradar.repository.player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.club.Club;
import com.talentradar.model.player.Player;

/**
 * Repository interface for managing Player entities. Provides data access
 * operations for player management, search functionality, age-based filtering,
 * and club associations.
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /* Basic finder methods */
    // Find player by external API ID (from API-Football)
    Optional<Player> findByExternalId(Integer externalId);

    /* Search methods */
    // Search players by name (case-insensitive)
    List<Player> findByNameContainingIgnoreCase(String name);

    // Search with pagination
    Page<Player> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Search by first name or last name with pagination
    @Query("SELECT p FROM Player p WHERE "
            + "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Player> findByFirstNameOrLastNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Search by first name or last name without pagination
    @Query("SELECT p FROM Player p WHERE "
            + "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Player> findByFirstNameOrLastNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    /* Status-based finder methods */
    // Find active players only
    List<Player> findByIsActiveTrue();

    // Active players with pagination
    Page<Player> findByIsActiveTrue(Pageable pageable);

    // Count active players
    long countByIsActiveTrue();

    /* Nationality-based finder methods */
    // Find players by nationality
    List<Player> findByNationality(String nationality);

    // Nationality and active status with pagination
    Page<Player> findByNationalityAndIsActiveTrue(String nationality, Pageable pageable);

    // Find players by birth country
    List<Player> findByBirthCountry(String birthCountry);

    /* Position-based finder methods */
    // Position-based queries
    List<Player> findByPositionAndIsActiveTrue(String position);

    // Find players by postition and active
    Page<Player> findByPositionAndIsActiveTrue(String position, Pageable pageable);

    // Find players by position and nationality
    Page<Player> findByPositionAndNationalityAndIsActiveTrue(String position, String nationality, Pageable pageable);

    /* Age and date-based finder methods */
    // Find all U21 players (custom query)
    @Query("SELECT p FROM Player p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) <= 21")
    List<Player> findU21Players();

    // Find all U21 players with pagination
    @Query("SELECT p FROM Player p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) <= 21 AND p.isActive = true")
    Page<Player> findU21PlayersPage(Pageable pageable);

    // Count U21 players (used in your service)
    @Query("SELECT COUNT(p) FROM Player p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) <= 21")
    long countByU21Eligible();

    // Find active players born after a certain date
    @Query("SELECT p FROM Player p WHERE p.dateOfBirth >= :minDate AND p.isActive = true")
    List<Player> findByDateOfBirthAfterAndIsActiveTrue(LocalDate minDate);

    // Age range query
    @Query("SELECT p FROM Player p WHERE (YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth)) BETWEEN :minAge AND :maxAge AND p.isActive = true")
    List<Player> findByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);

    // Find players created after a certain date with
    @Query("SELECT p FROM Player p WHERE p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<Player> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("since") LocalDateTime since, Pageable pageable);


    /* Club-based finder methods */
    // Find current players by club (those with active statistics)
    @Query("SELECT DISTINCT ps.player FROM PlayerStatistic ps WHERE ps.club = :club AND ps.player.isActive = true ORDER BY ps.player.name")
    List<Player> findCurrentPlayersByClub(@Param("club") Club club);

    // Find current players by club and position
    @Query("SELECT DISTINCT ps.player FROM PlayerStatistic ps WHERE ps.club = :club AND ps.position = :position AND ps.player.isActive = true ORDER BY ps.player.name")
    List<Player> findCurrentPlayersByClubAndPosition(@Param("club") Club club, @Param("position") String position);

    // Count current players by club
    @Query("SELECT COUNT(DISTINCT ps.player) FROM PlayerStatistic ps WHERE ps.club = :club AND ps.player.isActive = true")
    long countCurrentPlayersByClub(@Param("club") Club club);

    /* Trending and popularity methods */
    // Trending players
    Page<Player> findByTrendingScoreGreaterThanOrderByTrendingScoreDesc(Double minScore, Pageable pageable);

    // Find trending players by weekly views
    @Query("SELECT DISTINCT pv.player FROM PlayerView pv "
            + "WHERE pv.createdAt >= :weekAgo AND pv.player.isActive = true "
            + "GROUP BY pv.player "
            + "ORDER BY COUNT(pv) DESC")
    Page<Player> findTrendingPlayersByWeeklyViews(@Param("weekAgo") LocalDateTime weekAgo, Pageable pageable);
}

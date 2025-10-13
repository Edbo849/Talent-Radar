package com.talentradar.repository.scouting;

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

import com.talentradar.model.enums.ReportStatus;
import com.talentradar.model.player.Player;
import com.talentradar.model.scouting.ScoutingReport;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing ScoutingReport entities. Provides data
 * access operations for scouting report management, player analysis, scout
 * tracking, and report analytics.
 */
@Repository
public interface ScoutingReportRepository extends JpaRepository<ScoutingReport, Long> {

    /* Basic player-based finder methods */
    // Find all reports for a specific player
    List<ScoutingReport> findByPlayer(Player player);

    // Find all reports for a specific player with pagination
    Page<ScoutingReport> findByPlayer(Player player, Pageable pageable);

    // Find all reports by player ordered by creation date
    Page<ScoutingReport> findByPlayerOrderByCreatedAtDesc(Player player, Pageable pageable);

    // Find reports by scout and player
    List<ScoutingReport> findByScoutAndPlayer(User scout, Player player);

    // Find reports by player and scout
    List<ScoutingReport> findByPlayerAndScout(Player player, User scout);

    /* Scout-based finder methods */
    // Find all reports by a specific scout
    List<ScoutingReport> findByScout(User scout);

    // Find all reports by a specific scout with pagination
    Page<ScoutingReport> findByScout(User scout, Pageable pageable);

    // Find reports by scout ordered by creation date
    Page<ScoutingReport> findByScoutOrderByCreatedAtDesc(User scout, Pageable pageable);

    // Find reports by scout and status
    List<ScoutingReport> findByScoutAndStatus(User scout, ReportStatus status);

    /* Status-based finder methods */
    // Find reports by status
    List<ScoutingReport> findByStatus(ReportStatus status);

    // Find reports by status with pagination
    Page<ScoutingReport> findByStatus(ReportStatus status, Pageable pageable);

    // Find reports by player and status
    List<ScoutingReport> findByPlayerAndStatus(Player player, ReportStatus status);

    /* Public reports methods */
    // Find public reports
    Page<ScoutingReport> findByIsPublicTrue(Pageable pageable);

    // Find all public reports
    List<ScoutingReport> findByIsPublicTrue();

    // Find public reports for a specific player
    Page<ScoutingReport> findByPlayerAndIsPublicTrue(Player player, Pageable pageable);

    // Find public reports for a player ordered by creation date
    Page<ScoutingReport> findByPlayerAndIsPublicTrueOrderByCreatedAtDesc(Player player, Pageable pageable);

    // Find public reports by status
    Page<ScoutingReport> findByIsPublicTrueAndStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    // Find reports by public status ordered by creation date
    Page<ScoutingReport> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    /* Rating-based finder methods */
    // Find reports by overall rating
    List<ScoutingReport> findByOverallRating(Integer overallRating);

    // Find reports with rating above threshold
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.overallRating >= :minRating")
    List<ScoutingReport> findByOverallRatingGreaterThanEqual(@Param("minRating") Integer minRating);

    // Find reports with rating in range
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.overallRating >= :minRating AND sr.overallRating <= :maxRating")
    List<ScoutingReport> findByOverallRatingBetween(@Param("minRating") Integer minRating, @Param("maxRating") Integer maxRating);

    // Find public reports by rating range
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.isPublic = true AND sr.overallRating >= :minRating AND sr.overallRating <= :maxRating")
    List<ScoutingReport> findByOverallRatingBetweenAndIsPublicTrue(@Param("minRating") Integer minRating, @Param("maxRating") Integer maxRating);

    // Find reports by technical rating range
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.technicalRating >= :minRating AND sr.technicalRating <= :maxRating")
    List<ScoutingReport> findByTechnicalRatingBetween(@Param("minRating") Integer minRating, @Param("maxRating") Integer maxRating);

    // Find reports by physical rating range
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.physicalRating >= :minRating AND sr.physicalRating <= :maxRating")
    List<ScoutingReport> findByPhysicalRatingBetween(@Param("minRating") Integer minRating, @Param("maxRating") Integer maxRating);

    // Find reports by mental rating range
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.mentalRating >= :minRating AND sr.mentalRating <= :maxRating")
    List<ScoutingReport> findByMentalRatingBetween(@Param("minRating") Integer minRating, @Param("maxRating") Integer maxRating);

    /* Time-based finder methods */
    // Find reports by match date
    List<ScoutingReport> findByMatchDate(LocalDate matchDate);

    // Find public reports by match date
    List<ScoutingReport> findByMatchDateAndIsPublicTrue(java.time.LocalDate matchDate);

    // Find reports between match dates
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.matchDate >= :startDate AND sr.matchDate <= :endDate")
    List<ScoutingReport> findByMatchDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find recent reports (within specified days)
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.createdAt >= :date ORDER BY sr.createdAt DESC")
    List<ScoutingReport> findRecentReports(@Param("date") LocalDateTime date);

    // Find reports updated after specific date
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.updatedAt >= :date")
    List<ScoutingReport> findRecentlyUpdated(@Param("date") LocalDateTime date);

    // Find public reports by date range
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.isPublic = true AND sr.createdAt >= :startDate AND sr.createdAt <= :endDate")
    List<ScoutingReport> findByCreatedAtBetweenAndIsPublicTrue(@Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /* Opponent-based finder methods */
    // Find reports by opponent club
    List<ScoutingReport> findByOpponentClub(String opponentClub);

    // Find reports by opponent club containing (case-insensitive)
    List<ScoutingReport> findByOpponentClubContainingIgnoreCase(String opponentClub);

    // Find public reports by opponent club
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.isPublic = true AND LOWER(sr.opponentClub) LIKE LOWER(CONCAT('%', :clubName, '%'))")
    List<ScoutingReport> findByOpponentClubContainingIgnoreCaseAndIsPublicTrue(@Param("clubName") String clubName);

    /* Search methods */
    // Search reports by title
    List<ScoutingReport> findByTitleContainingIgnoreCase(String title);

    // Search reports by content
    List<ScoutingReport> findByContentContainingIgnoreCase(String content);

    // Search reports by title or content
    @Query("SELECT sr FROM ScoutingReport sr WHERE LOWER(sr.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(sr.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<ScoutingReport> searchByTitleOrContent(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Full text search for public reports
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.isPublic = true AND "
            + "(LOWER(sr.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(sr.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(sr.player.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(sr.scout.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
            + "LOWER(sr.scout.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ScoutingReport> findByFullTextSearchAndIsPublicTrue(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find reports with specific strengths
    List<ScoutingReport> findByStrengthsContainingIgnoreCase(String strengths);

    // Find reports with specific weaknesses
    List<ScoutingReport> findByWeaknessesContainingIgnoreCase(String weaknesses);

    // Find reports with recommendations
    List<ScoutingReport> findByRecommendationsContainingIgnoreCase(String recommendations);

    /* Top-rated and featured reports */
    // Find top-rated reports
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.isPublic = true ORDER BY sr.overallRating DESC")
    List<ScoutingReport> findTopRatedReports(Pageable pageable);

    // Find recent public reports (top N)
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.isPublic = true ORDER BY sr.createdAt DESC")
    List<ScoutingReport> findTopNByIsPublicTrueOrderByCreatedAtDesc(@Param("limit") int limit);

    // Find top-rated public reports (top N)
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.isPublic = true AND sr.overallRating IS NOT NULL ORDER BY sr.overallRating DESC")
    List<ScoutingReport> findTopNByIsPublicTrueOrderByOverallRatingDesc(@Param("limit") int limit);

    /* Existence check methods */
    // Check if scout has already reported on player
    boolean existsByScoutAndPlayer(User scout, Player player);

    /* Count methods */
    // Count reports for a player
    long countByPlayer(Player player);

    // Count public reports for a player
    long countByPlayerAndIsPublicTrue(Player player);

    // Count reports by scout
    long countByScout(User scout);

    // Count public reports by scout
    long countByScoutAndIsPublicTrue(User scout);

    // Count reports by status
    long countByStatus(ReportStatus status);

    // Count reports by scout and status
    long countByScoutAndStatus(User scout, ReportStatus status);

    /* Analytics and statistics methods */
    // Find average rating for a player
    @Query("SELECT AVG(sr.overallRating) FROM ScoutingReport sr WHERE sr.player = :player AND sr.isPublic = true")
    Optional<Double> findAverageRatingForPlayer(@Param("player") Player player);

    // Get average rating for a player from public reports
    @Query("SELECT AVG(sr.overallRating) FROM ScoutingReport sr WHERE sr.player = :player AND sr.isPublic = true AND sr.overallRating IS NOT NULL")
    Double getAverageOverallRatingByPlayerAndIsPublicTrue(@Param("player") Player player);

    // Find average technical rating for a player
    @Query("SELECT AVG(sr.technicalRating) FROM ScoutingReport sr WHERE sr.player = :player AND sr.isPublic = true")
    Optional<Double> findAverageTechnicalRatingForPlayer(@Param("player") Player player);

    // Find average physical rating for a player
    @Query("SELECT AVG(sr.physicalRating) FROM ScoutingReport sr WHERE sr.player = :player AND sr.isPublic = true")
    Optional<Double> findAveragePhysicalRatingForPlayer(@Param("player") Player player);

    // Find average mental rating for a player
    @Query("SELECT AVG(sr.mentalRating) FROM ScoutingReport sr WHERE sr.player = :player AND sr.isPublic = true")
    Optional<Double> findAverageMentalRatingForPlayer(@Param("player") Player player);

    // Find most active scouts
    @Query("SELECT sr.scout, COUNT(sr) FROM ScoutingReport sr GROUP BY sr.scout ORDER BY COUNT(sr) DESC")
    List<Object[]> findMostActiveScouts();

    // Find most scouted players
    @Query("SELECT sr.player, COUNT(sr) FROM ScoutingReport sr WHERE sr.isPublic = true GROUP BY sr.player ORDER BY COUNT(sr) DESC")
    List<Object[]> findMostScoutedPlayers();

    // Get statistics
    @Query("SELECT COUNT(sr), AVG(sr.overallRating), MAX(sr.overallRating), MIN(sr.overallRating) FROM ScoutingReport sr WHERE sr.isPublic = true")
    Object[] getPublicReportsStatistics();

    /* Administrative methods */
    // Find reports that need review (draft status for too long)
    @Query("SELECT sr FROM ScoutingReport sr WHERE sr.status = 'DRAFT' AND sr.createdAt < :cutoffDate")
    List<ScoutingReport> findDraftReportsOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}

package com.talentradar.repository.player;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.enums.UserRole;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerRating;
import com.talentradar.model.scouting.RatingCategory;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing PlayerRating entities. Provides data access
 * operations for player rating management, category-based analysis, expert
 * evaluations, and rating analytics.
 */
@Repository
public interface PlayerRatingRepository extends JpaRepository<PlayerRating, Long> {

    /* Basic rating finder methods */
    // Find user's rating for a specific player and category
    Optional<PlayerRating> findByPlayerAndUserAndCategoryAndIsActiveTrue(Player player, User user, RatingCategory category);

    // Finds a rating by player, user, and category.
    Optional<PlayerRating> findByPlayerIdAndUserIdAndCategoryId(Long playerId, Long userId, Long categoryId);

    // Get all ratings for a player
    List<PlayerRating> findByPlayerAndIsActiveTrueOrderByCreatedAtDesc(Player player);

    // Finds all ratings for a specific player.
    Page<PlayerRating> findByPlayerId(Long playerId, Pageable pageable);

    // Get all active ratings by a user
    List<PlayerRating> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);

    /* Category-based finder methods */
    // Get all ratings for a player in a specific category
    List<PlayerRating> findByPlayerAndCategoryAndIsActiveTrueOrderByCreatedAtDesc(Player player, RatingCategory category);

    // Returns true if any PlayerRating exists for the given category ID (used for category usage validation)
    boolean existsByCategoryId(Long categoryId);

    /* Average rating calculations */
    // Calculate average rating for a player in a specific category
    @Query("SELECT AVG(pr.rating) FROM PlayerRating pr WHERE pr.player = :player AND pr.category = :category AND pr.isActive = true")
    Optional<BigDecimal> findAverageRatingByPlayerAndCategory(@Param("player") Player player, @Param("category") RatingCategory category);

    // Calculate average rating for a player in a specific category by user role
    @Query("SELECT AVG(pr.rating) FROM PlayerRating pr JOIN pr.user u WHERE pr.player = :player AND pr.category = :category AND pr.isActive = true AND u.role = :userRole")
    Optional<BigDecimal> findAverageRatingByPlayerAndCategoryAndUserRole(@Param("player") Player player, @Param("category") RatingCategory category, @Param("userRole") UserRole userRole);

    //Gets average ratings for a player grouped by category.
    @Query("SELECT r.category.id, r.category.name, AVG(r.rating), COUNT(r) "
            + "FROM PlayerRating r "
            + "WHERE r.player.id = :playerId "
            + "GROUP BY r.category.id, r.category.name "
            + "ORDER BY r.category.name")
    List<Object[]> findAverageRatingsByPlayerIdGroupedByCategory(@Param("playerId") Long playerId);

    /* Count methods */
    // Count ratings for a player in a specific category
    @Query("SELECT COUNT(pr) FROM PlayerRating pr WHERE pr.player = :player AND pr.category = :category AND pr.isActive = true")
    Long countRatingsByPlayerAndCategory(@Param("player") Player player, @Param("category") RatingCategory category);

    /* Top performers and leaderboards */
    // Get top-rated players in a category
    @Query("SELECT pr.player, AVG(pr.rating) as avgRating FROM PlayerRating pr WHERE pr.category = :category AND pr.isActive = true GROUP BY pr.player HAVING COUNT(pr) >= :minRatings ORDER BY avgRating DESC")
    Page<Object[]> findTopRatedPlayersByCategory(@Param("category") RatingCategory category, @Param("minRatings") Long minRatings, Pageable pageable);

    /* Time-based queries */
    // Get recent ratings (last 30 days)
    List<PlayerRating> findByCreatedAtAfterAndIsActiveTrueOrderByCreatedAtDesc(LocalDateTime since);

    // Get rating history for a player and category
    @Query("SELECT pr FROM PlayerRating pr WHERE pr.player = :player AND pr.category = :category ORDER BY pr.createdAt DESC")
    List<PlayerRating> findRatingHistoryByPlayerAndCategory(@Param("player") Player player, @Param("category") RatingCategory category);

    // Check if user has rated player in category recently (within 7 days)
    @Query("SELECT COUNT(pr) > 0 FROM PlayerRating pr WHERE pr.player = :player AND pr.user = :user AND pr.category = :category AND pr.createdAt >= :since")
    boolean hasUserRatedRecentlyInCategory(@Param("player") Player player, @Param("user") User user, @Param("category") RatingCategory category, @Param("since") LocalDateTime since);

    /* Expert ratings */
    // Get expert ratings (from scouts and coaches)
    @Query("SELECT pr FROM PlayerRating pr JOIN pr.user u WHERE pr.player = :player AND pr.category = :category AND pr.isActive = true AND (u.role = 'SCOUT' OR u.role = 'COACH') ORDER BY pr.createdAt DESC")
    List<PlayerRating> findExpertRatingsByPlayerAndCategory(@Param("player") Player player, @Param("category") RatingCategory category);
}

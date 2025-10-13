package com.talentradar.repository.player;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerView;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing PlayerView entities. Provides data access
 * operations for view tracking, analytics, trending analysis, and user behavior
 * monitoring.
 */
@Repository
public interface PlayerViewRepository extends JpaRepository<PlayerView, Long> {

    /* Basic view finder methods */
    // Basic queries
    List<PlayerView> findByPlayer(Player player);

    // Find views with pagination
    Page<PlayerView> findByPlayer(Player player, Pageable pageable);

    // Find views by user with pagination and order by createdAt descending
    Page<PlayerView> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Find views by user
    List<PlayerView> findByUser(User user);

    // Find views by player and user
    List<PlayerView> findByPlayerAndUser(Player player, User user);

    /* Time-based queries */
    // Find views after specific time
    List<PlayerView> findByCreatedAtAfter(LocalDateTime since);

    // Find player views after specific time
    List<PlayerView> findByPlayerAndCreatedAtAfter(Player player, LocalDateTime since);

    // For counting views between two dates
    long countByPlayerAndCreatedAtBetween(Player player, LocalDateTime start, LocalDateTime end);

    // Find player views after specific time
    @Query("SELECT pv FROM PlayerView pv WHERE pv.player = :player AND pv.viewedAt >= :since")
    List<PlayerView> findByPlayerAndViewedAtAfter(@Param("player") Player player, @Param("since") LocalDateTime since);

    /* Ordered and paginated queries */
    // For finding recent views with pagination (ordered by createdAt)
    Page<PlayerView> findByPlayerOrderByCreatedAtDesc(Player player, Pageable pageable);

    // Recent views
    List<PlayerView> findTop10ByPlayerOrderByCreatedAtDesc(Player player);

    // User's recent views
    List<PlayerView> findTop20ByUserOrderByCreatedAtDesc(User user);

    /* Count methods */
    // Count total views for a player
    long countByPlayer(Player player);

    // Count views by player after specific time
    long countByPlayerAndCreatedAtAfter(Player player, LocalDateTime since);

    // Count views by user
    long countByUser(User user);

    // Count views after specific time
    long countByCreatedAtAfter(LocalDateTime since);

    // For counting anonymous views
    long countByPlayerAndUserIsNull(Player player);

    /* Trending and popularity analysis */
    // Trending and popular players
    @Query("SELECT pv.player, COUNT(pv) as viewCount FROM PlayerView pv WHERE pv.createdAt >= :since GROUP BY pv.player ORDER BY COUNT(pv) DESC")
    List<Object[]> findMostViewedPlayersSinceList(@Param("since") LocalDateTime since);

    @Query("SELECT pv.player, COUNT(pv) as viewCount FROM PlayerView pv GROUP BY pv.player ORDER BY COUNT(pv) DESC")
    Page<Object[]> findMostViewedPlayersAllTime(Pageable pageable);

    @Query("SELECT pv.player, COUNT(pv) as viewCount FROM PlayerView pv WHERE pv.createdAt >= :since GROUP BY pv.player ORDER BY COUNT(pv) DESC")
    Page<Object[]> findMostViewedPlayersSince(@Param("since") LocalDateTime since, Pageable pageable);

    /* User behavior analysis */
    // Count distinct players viewed by user
    @Query("SELECT COUNT(DISTINCT pv.player) FROM PlayerView pv WHERE pv.user = :user")
    long countDistinctPlayersByUser(@Param("user") User user);

    // Count distinct users who viewed a player
    @Query("SELECT COUNT(DISTINCT pv.user) FROM PlayerView pv WHERE pv.player = :player")
    long countDistinctUsersByPlayer(@Param("player") Player player);

    /* IP-based tracking (for anonymous users) */
    // Find views by IP address
    List<PlayerView> findByIpAddress(String ipAddress);

    // Count views by player and IP
    long countByPlayerAndIpAddress(Player player, String ipAddress);

    @Query("SELECT COUNT(pv) FROM PlayerView pv WHERE pv.player = :player AND pv.ipAddress = :ip AND pv.createdAt >= :since")
    long countByPlayerAndIpAddressAndCreatedAtAfter(@Param("player") Player player,
            @Param("ip") String ipAddress,
            @Param("since") LocalDateTime since);

    /* Duration-based queries */
    // Get average view duration for player
    @Query("SELECT AVG(pv.viewDurationSeconds) FROM PlayerView pv WHERE pv.player = :player")
    Double getAverageViewDurationForPlayer(@Param("player") Player player);

    // Find views with minimum duration
    @Query("SELECT pv FROM PlayerView pv WHERE pv.viewDurationSeconds >= :minDuration")
    List<PlayerView> findByViewDurationSecondsGreaterThanEqual(@Param("minDuration") Integer minDuration);

    /* Referrer analysis */
    // Find top referrers
    @Query("SELECT pv.referrerUrl, COUNT(pv) FROM PlayerView pv WHERE pv.referrerUrl IS NOT NULL GROUP BY pv.referrerUrl ORDER BY COUNT(pv) DESC")
    List<Object[]> findTopReferrers();

    /* Weekly/Daily trends */
    // Find daily views for player
    @Query("SELECT DATE(pv.createdAt) as viewDate, COUNT(pv) as viewCount FROM PlayerView pv WHERE pv.player = :player AND pv.createdAt >= :since GROUP BY DATE(pv.createdAt) ORDER BY viewDate")
    List<Object[]> findDailyViewsForPlayer(@Param("player") Player player, @Param("since") LocalDateTime since);

    // Find weekly views for player
    @Query("SELECT WEEK(pv.createdAt) as viewWeek, COUNT(pv) as viewCount FROM PlayerView pv WHERE pv.player = :player AND pv.createdAt >= :since GROUP BY WEEK(pv.createdAt) ORDER BY viewWeek")
    List<Object[]> findWeeklyViewsForPlayer(@Param("player") Player player, @Param("since") LocalDateTime since);

    /* Anonymous vs registered user views */
    // Count anonymous views by player
    @Query("SELECT COUNT(pv) FROM PlayerView pv WHERE pv.player = :player AND pv.user IS NULL")
    long countAnonymousViewsByPlayer(@Param("player") Player player);

    // Count registered user views by player
    @Query("SELECT COUNT(pv) FROM PlayerView pv WHERE pv.player = :player AND pv.user IS NOT NULL")
    long countRegisteredViewsByPlayer(@Param("player") Player player);

    /* Player engagement metrics */
    // Get player engagement metrics
    @Query("SELECT p, COUNT(pv) as viewCount, COUNT(DISTINCT pv.user) as uniqueUsers "
            + "FROM PlayerView pv JOIN pv.player p "
            + "WHERE pv.createdAt >= :since "
            + "GROUP BY p "
            + "ORDER BY viewCount DESC")
    List<Object[]> findPlayerEngagementMetrics(@Param("since") LocalDateTime since);

    /* Existence checks */
    // Checks if a recent view exists by user.
    boolean existsByPlayerAndUserAndViewedAtAfter(Player player, User user, LocalDateTime after);

    // Checks if a recent view exists by IP address.
    boolean existsByPlayerAndIpAddressAndViewedAtAfter(Player player, String ipAddress, LocalDateTime after);

    /* Additional count methods with IDs */
    // Counts views after a specific date.
    long countByPlayerAndViewedAtAfter(Player player, LocalDateTime after);

    // Counts distinct users who viewed a player.
    @Query("SELECT COUNT(DISTINCT v.user.id) FROM PlayerView v WHERE v.player.id = :playerId AND v.user IS NOT NULL")
    long countDistinctUsersByPlayerId(@Param("playerId") Long playerId);

    // Counts distinct anonymous IPs that viewed a player.
    @Query("SELECT COUNT(DISTINCT v.ipAddress) FROM PlayerView v WHERE v.player.id = :playerId AND v.user IS NULL")
    long countDistinctAnonymousIpsByPlayerId(@Param("playerId") Long playerId);

    /* Daily analytics with IDs */
    // Gets daily view counts for a player in the last week. 
    @Query("SELECT DATE(v.viewedAt), COUNT(v) FROM PlayerView v "
            + "WHERE v.player = :player AND v.viewedAt >= :since "
            + "GROUP BY DATE(v.viewedAt) ORDER BY DATE(v.viewedAt)")
    List<Object[]> findDailyViewsForPlayerLastWeek(@Param("player") Player player, @Param("since") LocalDateTime since);

    // Gets top viewing countries for a player (placeholder - requires geolocation).
    @Query("SELECT 'Unknown' as country, COUNT(v) FROM PlayerView v WHERE v.player.id = :playerId GROUP BY 1")
    List<Object[]> findTopViewingCountriesForPlayer(@Param("playerId") Long playerId);

    /* User-specific analytics */
    // Gets recent views by a user.
    @Query(value = "SELECT * FROM player_views v WHERE v.user_id = :userId ORDER BY v.viewed_at DESC LIMIT :limit", nativeQuery = true)
    List<PlayerView> findRecentViewsByUser(@Param("userId") Long userId, @Param("limit") int limit);

    // Counts distinct players viewed by a user.
    @Query("SELECT COUNT(DISTINCT v.player.id) FROM PlayerView v WHERE v.user.id = :userId")
    long countDistinctPlayersByUserId(@Param("userId") Long userId);

    // Counts views by user after a specific date.
    long countByUserAndViewedAtAfter(User user, LocalDateTime after);

    // Gets most viewed players by a user.
    @Query("SELECT v.player.id, v.player.name, COUNT(v) FROM PlayerView v "
            + "WHERE v.user.id = :userId "
            + "GROUP BY v.player.id, v.player.name "
            + "ORDER BY COUNT(v) DESC")
    List<Object[]> findMostViewedPlayersByUser(@Param("userId") Long userId);

    // Gets daily view activity for a user.
    @Query("SELECT DATE(v.viewedAt), COUNT(v) FROM PlayerView v "
            + "WHERE v.user.id = :userId AND v.viewedAt >= :since "
            + "GROUP BY DATE(v.viewedAt) ORDER BY DATE(v.viewedAt)")
    List<Object[]> findDailyViewActivityForUser(@Param("userId") Long userId);

    /* Cleanup methods */
    // Cleanup queries for old data
    @Modifying
    @Query("DELETE FROM PlayerView pv WHERE pv.createdAt < :cutoffDate")
    void deleteViewsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}

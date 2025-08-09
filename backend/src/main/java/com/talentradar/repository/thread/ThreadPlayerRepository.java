package com.talentradar.repository.thread;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.player.Player;
import com.talentradar.model.thread.ThreadPlayer;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing ThreadPlayer entities. Provides data access
 * operations for thread-player associations, discussion tracking, player
 * popularity analysis, and content discovery.
 */
@Repository
public interface ThreadPlayerRepository extends JpaRepository<ThreadPlayer, Long> {

    /* Basic association methods */
    // Find associations by thread
    List<ThreadPlayer> findByThread(DiscussionThread thread);

    // Find associations by player
    List<ThreadPlayer> findByPlayer(Player player);

    // Find specific thread-player association
    Optional<ThreadPlayer> findByThreadAndPlayer(DiscussionThread thread, Player player);

    // Check if player is associated with thread
    boolean existsByThreadAndPlayer(DiscussionThread thread, Player player);

    /* Thread and player lookup methods */
    // Get players for a thread
    @Query("SELECT tp.player FROM ThreadPlayer tp WHERE tp.thread = :thread ORDER BY tp.createdAt ASC")
    List<Player> findPlayersByThread(@Param("thread") DiscussionThread thread);

    // Get threads for a player
    @Query("SELECT tp.thread FROM ThreadPlayer tp WHERE tp.player = :player ORDER BY tp.createdAt DESC")
    List<DiscussionThread> findThreadsByPlayer(@Param("player") Player player);

    // Get threads for a player with pagination
    @Query("SELECT tp.thread FROM ThreadPlayer tp WHERE tp.player = :player ORDER BY tp.createdAt DESC")
    Page<DiscussionThread> findThreadsByPlayer(@Param("player") Player player, Pageable pageable);

    // Find associations by player ordered by creation date
    List<ThreadPlayer> findByPlayerOrderByCreatedAtDesc(Player player);

    /* User-based methods */
    // Find associations by user who added players
    List<ThreadPlayer> findByAddedByUser(User user);

    // Find threads where user has added players
    @Query("SELECT DISTINCT tp.thread FROM ThreadPlayer tp WHERE tp.addedByUser = :user")
    List<DiscussionThread> findThreadsWhereUserAddedPlayers(@Param("user") User user);

    // Find most added players by user
    @Query("SELECT tp.player, COUNT(tp) FROM ThreadPlayer tp WHERE tp.addedByUser = :user GROUP BY tp.player ORDER BY COUNT(tp) DESC")
    List<Object[]> findMostAddedPlayersByUser(@Param("user") User user);

    /* Recent activity methods */
    // Find recent player associations
    List<ThreadPlayer> findTop20ByOrderByCreatedAtDesc();

    // Find recent discussions for player
    @Query("SELECT tp FROM ThreadPlayer tp WHERE tp.player = :player AND tp.createdAt >= :since ORDER BY tp.createdAt DESC")
    List<ThreadPlayer> findRecentDiscussionsForPlayer(@Param("player") Player player, @Param("since") java.time.LocalDateTime since);

    /* Thread filtering methods */
    // Get threads discussing multiple specific players
    @Query("SELECT tp.thread FROM ThreadPlayer tp WHERE tp.player IN :players GROUP BY tp.thread HAVING COUNT(DISTINCT tp.player) = :playerCount")
    List<DiscussionThread> findThreadsWithAllPlayers(@Param("players") List<Player> players, @Param("playerCount") Long playerCount);

    // Find comparison threads (threads with exactly 2 players)
    @Query("SELECT tp.thread FROM ThreadPlayer tp GROUP BY tp.thread HAVING COUNT(tp.player) = 2")
    List<DiscussionThread> findComparisonThreads();

    // Find threads with specific number of players
    @Query("SELECT tp.thread FROM ThreadPlayer tp GROUP BY tp.thread HAVING COUNT(tp.player) = :count")
    List<DiscussionThread> findThreadsWithPlayerCount(@Param("count") Long count);

    /* Player attribute-based filtering */
    // Find threads involving players from specific positions
    @Query("SELECT DISTINCT tp.thread FROM ThreadPlayer tp WHERE tp.player.position = :position")
    List<DiscussionThread> findThreadsByPlayerPosition(@Param("position") String position);

    // Find threads involving players from specific clubs
    @Query("SELECT DISTINCT tp.thread FROM ThreadPlayer tp WHERE tp.player.currentClub.id = :clubId")
    List<DiscussionThread> findThreadsByPlayerClub(@Param("clubId") Long clubId);

    // Find threads involving players from specific nationalities
    @Query("SELECT DISTINCT tp.thread FROM ThreadPlayer tp WHERE tp.player.nationality = :nationality")
    List<DiscussionThread> findThreadsByPlayerNationality(@Param("nationality") String nationality);

    /* Analytics and popularity methods */
    // Find most discussed players
    @Query("SELECT tp.player, COUNT(tp.thread) as threadCount FROM ThreadPlayer tp GROUP BY tp.player ORDER BY COUNT(tp.thread) DESC")
    List<Object[]> findMostDiscussedPlayers();

    // Find most discussed players (distinct threads)
    @Query("SELECT tp.player, COUNT(DISTINCT tp.thread) as threadCount FROM ThreadPlayer tp GROUP BY tp.player ORDER BY COUNT(DISTINCT tp.thread) DESC")
    List<Object[]> findMostDiscussedPlayersDistinct();

    // Find trending player discussions (recent activity)
    @Query("SELECT tp.player, COUNT(tp) as recentMentions "
            + "FROM ThreadPlayer tp "
            + "WHERE tp.createdAt >= :since "
            + "GROUP BY tp.player "
            + "ORDER BY COUNT(tp) DESC")
    List<Object[]> findTrendingPlayerDiscussions(@Param("since") java.time.LocalDateTime since);

    /* Player co-occurrence analysis */
    // Find players discussed together (co-occurrence)
    @Query("SELECT tp1.player, tp2.player, COUNT(tp1.thread) as coOccurrence "
            + "FROM ThreadPlayer tp1 JOIN ThreadPlayer tp2 ON tp1.thread = tp2.thread "
            + "WHERE tp1.player.id < tp2.player.id "
            + "GROUP BY tp1.player, tp2.player "
            + "ORDER BY COUNT(tp1.thread) DESC")
    List<Object[]> findPlayerCoOccurrences();

    // Find popular player combinations (pairs)
    @Query("SELECT tp1.player, tp2.player, COUNT(DISTINCT tp1.thread) as frequency "
            + "FROM ThreadPlayer tp1 "
            + "JOIN ThreadPlayer tp2 ON tp1.thread = tp2.thread "
            + "WHERE tp1.player.id < tp2.player.id "
            + "GROUP BY tp1.player, tp2.player "
            + "HAVING COUNT(DISTINCT tp1.thread) > 1 "
            + "ORDER BY COUNT(DISTINCT tp1.thread) DESC")
    List<Object[]> findPopularPlayerCombinations();

    /* Count methods */
    // Count associations by player
    long countByPlayer(Player player);

    // Count associations by thread
    long countByThread(DiscussionThread thread);

    // Count associations by user
    long countByAddedByUser(User user);

    /* Statistical methods */
    // Get average players per thread
    @Query("SELECT AVG(subquery.playerCount) FROM (SELECT COUNT(tp.player) as playerCount FROM ThreadPlayer tp GROUP BY tp.thread) subquery")
    Double getAveragePlayersPerThread();

    // Count threads with players
    @Query("SELECT COUNT(DISTINCT tp.thread) FROM ThreadPlayer tp")
    long countThreadsWithPlayers();

    // Count players in discussions
    @Query("SELECT COUNT(DISTINCT tp.player) FROM ThreadPlayer tp")
    long countPlayersInDiscussions();

    /* Deletion methods */
    // Remove player from thread
    void deleteByThreadAndPlayer(DiscussionThread thread, Player player);

    // Remove all players from thread
    void deleteByThread(DiscussionThread thread);

    // Remove all thread associations for a player
    void deleteByPlayer(Player player);
}

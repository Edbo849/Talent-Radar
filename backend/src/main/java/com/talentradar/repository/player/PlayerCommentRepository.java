package com.talentradar.repository.player;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerComment;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing PlayerComment entities. Provides data
 * access operations for comment management, reply handling, voting systems, and
 * user engagement tracking.
 */
@Repository
public interface PlayerCommentRepository extends JpaRepository<PlayerComment, Long> {

    /* Basic comment finder methods */
    // Find comments for a specific player (top-level comments only)
    Page<PlayerComment> findByPlayerAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(Player player, Pageable pageable);

    // Find replies to a specific comment
    List<PlayerComment> findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(PlayerComment parentComment);

    /* Author-based finder methods */
    // Find comments by author
    Page<PlayerComment> findByAuthorAndIsDeletedFalseOrderByCreatedAtDesc(User author, Pageable pageable);

    // Find comments by author
    List<PlayerComment> findByAuthorAndIsDeletedFalseOrderByCreatedAtDesc(User author);

    /* Rating and popularity methods */
    // Find top-rated comments for a player
    @Query("SELECT pc FROM PlayerComment pc WHERE pc.player = :player AND pc.parentComment IS NULL AND pc.isDeleted = false ORDER BY (pc.upvotes - pc.downvotes) DESC, pc.createdAt DESC")
    Page<PlayerComment> findTopRatedCommentsByPlayer(@Param("player") Player player, Pageable pageable);

    // Find most upvoted comments for a player
    @Query("SELECT pc FROM PlayerComment pc WHERE pc.player = :player AND pc.isDeleted = false ORDER BY pc.upvotes DESC")
    List<PlayerComment> findMostUpvotedComments(@Param("player") Player player);

    // Find comments with high engagement (votes)
    @Query("SELECT pc FROM PlayerComment pc WHERE pc.isDeleted = false AND (pc.upvotes + pc.downvotes) >= :minVotes ORDER BY (pc.upvotes - pc.downvotes) DESC")
    Page<PlayerComment> findHighEngagementComments(@Param("minVotes") Integer minVotes, Pageable pageable);

    // Find comments with most replies
    @Query("SELECT pc FROM PlayerComment pc WHERE pc.isDeleted = false AND pc.parentComment IS NULL ORDER BY SIZE(pc.replies) DESC")
    List<PlayerComment> findCommentsWithMostReplies();

    /* Featured comment methods */
    // Find featured comments
    Page<PlayerComment> findByIsFeaturedTrueAndIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // Find featured comments for a player
    List<PlayerComment> findByPlayerAndIsFeaturedTrueAndIsDeletedFalseOrderByCreatedAtDesc(Player player);

    // Find all featured comments (across all players)
    List<PlayerComment> findByIsFeaturedTrueAndIsDeletedFalseOrderByCreatedAtDesc();

    /* Time-based finder methods */
    // Find recent comments for a player
    List<PlayerComment> findByPlayerAndCreatedAtAfterAndIsDeletedFalseOrderByCreatedAtDesc(Player player, LocalDateTime since);

    // Find recent comments (within specified days)
    @Query("SELECT pc FROM PlayerComment pc WHERE pc.isDeleted = false AND pc.createdAt >= :since ORDER BY pc.createdAt DESC")
    List<PlayerComment> findRecentComments(@Param("since") java.time.LocalDateTime since);

    /* Search methods */
    // Search comments by content
    @Query("SELECT pc FROM PlayerComment pc WHERE pc.isDeleted = false AND LOWER(pc.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<PlayerComment> searchCommentsByContent(@Param("searchTerm") String searchTerm);

    // Search comments by content
    @Query("SELECT pc FROM PlayerComment pc WHERE MATCH(pc.content) AGAINST(:searchTerm IN NATURAL LANGUAGE MODE) AND pc.isDeleted = false")
    Page<PlayerComment> findByContentSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    /* Count methods */
    // Count comments for a player
    Long countByPlayerAndIsDeletedFalse(Player player);

    // Count comments by author
    Long countByAuthorAndIsDeletedFalse(User author);

    /* Existence check methods */
    // Check if user has commented on a player
    boolean existsByPlayerAndAuthorAndIsDeletedFalse(Player player, User author);

    /* Activity and engagement analytics */
    // Find most active commenters for a player
    @Query("SELECT pc.author, COUNT(pc) as commentCount FROM PlayerComment pc WHERE pc.player = :player AND pc.isDeleted = false GROUP BY pc.author ORDER BY commentCount DESC")
    Page<Object[]> findMostActiveCommentersByPlayer(@Param("player") Player player, Pageable pageable);
}

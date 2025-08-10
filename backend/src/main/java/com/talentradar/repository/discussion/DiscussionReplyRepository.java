package com.talentradar.repository.discussion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.discussion.DiscussionReply;
import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing DiscussionReply entities. Provides data
 * access operations for reply management, thread-based queries, nested reply
 * handling, and user activity tracking.
 */
@Repository
public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {

    /* Basic thread reply methods */
    // Find all replies by thread
    List<DiscussionReply> findByThread(DiscussionThread thread);

    // Find all replies by thread with pagination
    Page<DiscussionReply> findByThread(DiscussionThread thread, Pageable pageable);

    // Find all replies by thread ordered by creation time
    List<DiscussionReply> findByThreadOrderByCreatedAtAsc(DiscussionThread thread);

    // Find all replies by thread ordered by creation time with pagination
    Page<DiscussionReply> findByThreadOrderByCreatedAtAsc(DiscussionThread thread, Pageable pageable);

    /* Non-deleted reply methods */
    // Find non-deleted replies by thread
    List<DiscussionReply> findByThreadAndIsDeletedFalse(DiscussionThread thread);

    // Find non-deleted replies by thread with pagination
    Page<DiscussionReply> findByThreadAndIsDeletedFalseOrderByCreatedAtAsc(DiscussionThread thread, Pageable pageable);

    /* Top-level reply methods (no parent) */
    // Find top-level replies by thread
    List<DiscussionReply> findByThreadAndParentReplyIsNull(DiscussionThread thread);

    // Find top-level replies by thread with pagination
    Page<DiscussionReply> findByThreadAndParentReplyIsNullOrderByCreatedAtAsc(DiscussionThread thread, Pageable pageable);

    // Find non-deleted top-level replies by thread
    List<DiscussionReply> findByThreadAndParentReplyIsNullAndIsDeletedFalse(DiscussionThread thread);

    // Find non-deleted top-level replies by thread with pagination
    Page<DiscussionReply> findByThreadAndParentReplyIsNullAndIsDeletedFalseOrderByCreatedAtAsc(DiscussionThread thread, Pageable pageable);

    /* Child reply methods (nested) */
    // Find child replies by parent
    List<DiscussionReply> findByParentReply(DiscussionReply parentReply);

    // Find child replies by parent ordered by creation time
    List<DiscussionReply> findByParentReplyOrderByCreatedAtAsc(DiscussionReply parentReply);

    // Find non-deleted child replies by parent
    List<DiscussionReply> findByParentReplyAndIsDeletedFalseOrderByCreatedAtAsc(DiscussionReply parentReply);

    /* Author-based reply methods */
    // Find all replies by author
    List<DiscussionReply> findByAuthor(User author);

    // Find all replies by author with pagination
    Page<DiscussionReply> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    // Find non-deleted replies by author with pagination
    Page<DiscussionReply> findByAuthorAndIsDeletedFalseOrderByCreatedAtDesc(User author, Pageable pageable);

    // Find replies by thread and author
    List<DiscussionReply> findByThreadAndAuthor(DiscussionThread thread, User author);

    /* Popular reply methods (by votes) */
    // Find replies by thread ordered by net votes
    @Query("SELECT dr FROM DiscussionReply dr WHERE dr.thread = :thread AND dr.isDeleted = false ORDER BY (dr.upvotes - dr.downvotes) DESC, dr.createdAt ASC")
    List<DiscussionReply> findByThreadOrderByNetVotesDesc(@Param("thread") DiscussionThread thread);

    // Find replies by thread ordered by net votes with pagination
    @Query("SELECT dr FROM DiscussionReply dr WHERE dr.thread = :thread AND dr.isDeleted = false ORDER BY (dr.upvotes - dr.downvotes) DESC, dr.createdAt ASC")
    Page<DiscussionReply> findByThreadOrderByNetVotesDesc(@Param("thread") DiscussionThread thread, Pageable pageable);

    // Find top 10 most upvoted replies
    List<DiscussionReply> findTop10ByIsDeletedFalseOrderByUpvotesDesc();

    // Find most upvoted replies since specific time
    @Query("SELECT dr FROM DiscussionReply dr WHERE dr.isDeleted = false AND dr.createdAt >= :since ORDER BY dr.upvotes DESC")
    List<DiscussionReply> findMostUpvotedSince(@Param("since") LocalDateTime since, Pageable pageable);

    /* Featured reply methods */
    // Find featured replies by thread
    List<DiscussionReply> findByThreadAndIsFeaturedTrue(DiscussionThread thread);

    // Find all featured replies
    List<DiscussionReply> findByIsFeaturedTrueOrderByCreatedAtDesc();

    // Find non-deleted featured replies by thread
    List<DiscussionReply> findByThreadAndIsFeaturedTrueAndIsDeletedFalse(DiscussionThread thread);

    // Find non-deleted featured replies by thread ordered by creation time
    List<DiscussionReply> findByThreadAndIsFeaturedTrueAndIsDeletedFalseOrderByCreatedAtDesc(DiscussionThread thread);

    /* Time-based reply methods */
    // Find replies created after specific time
    List<DiscussionReply> findByCreatedAtAfter(LocalDateTime since);

    // Find replies by thread created after specific time
    List<DiscussionReply> findByThreadAndCreatedAtAfter(DiscussionThread thread, LocalDateTime since);

    // Find replies by author created after specific time
    List<DiscussionReply> findByAuthorAndCreatedAtAfter(User author, LocalDateTime since);

    // Find recent replies
    @Query("SELECT dr FROM DiscussionReply dr WHERE dr.isDeleted = false ORDER BY dr.createdAt DESC")
    List<DiscussionReply> findRecentReplies(Pageable pageable);

    /* Search methods */
    // Search replies by content
    @Query("SELECT dr FROM DiscussionReply dr WHERE dr.isDeleted = false AND LOWER(dr.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<DiscussionReply> searchReplies(@Param("query") String query);

    // Search replies in specific thread
    @Query("SELECT dr FROM DiscussionReply dr WHERE dr.thread = :thread AND dr.isDeleted = false AND LOWER(dr.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<DiscussionReply> searchRepliesInThread(@Param("thread") DiscussionThread thread, @Param("query") String query);

    /* Activity and social methods */
    // Find recent replies by followed users
    @Query("SELECT dr FROM DiscussionReply dr WHERE dr.author IN "
            + "(SELECT uf.following FROM UserFollow uf WHERE uf.follower = :user) "
            + "AND dr.isDeleted = false ORDER BY dr.createdAt DESC")
    List<DiscussionReply> findRecentRepliesByFollowedUsers(@Param("user") User user, Pageable pageable);

    // Find last reply time for thread
    @Query("SELECT MAX(dr.createdAt) FROM DiscussionReply dr WHERE dr.thread = :thread AND dr.isDeleted = false")
    LocalDateTime findLastReplyTimeForThread(@Param("thread") DiscussionThread thread);

    // Find user's most active threads
    @Query("SELECT dr.thread, COUNT(dr) FROM DiscussionReply dr WHERE dr.author = :user AND dr.isDeleted = false GROUP BY dr.thread ORDER BY COUNT(dr) DESC")
    List<Object[]> findUserMostActiveThreads(@Param("user") User user);

    /* Count methods */
    // Count replies by thread
    long countByThread(DiscussionThread thread);

    // Count non-deleted replies by thread
    long countByThreadAndIsDeletedFalse(DiscussionThread thread);

    // Count replies by author
    long countByAuthor(User author);

    // Count non-deleted replies by author
    long countByAuthorAndIsDeletedFalse(User author);

    // Count replies by parent reply
    long countByParentReply(DiscussionReply parentReply);

    // Count non-deleted replies
    long countByIsDeletedFalse();

    /* Special analysis methods */
    // Find deeply nested replies (replies to replies)
    @Query("SELECT dr FROM DiscussionReply dr WHERE dr.parentReply IS NOT NULL AND dr.parentReply.parentReply IS NOT NULL")
    List<DiscussionReply> findDeeplyNestedReplies();

    // Find unique participants in thread
    @Query("SELECT DISTINCT dr.author FROM DiscussionReply dr WHERE dr.thread = :thread AND dr.isDeleted = false")
    List<User> findUniqueParticipants(@Param("thread") DiscussionThread thread);

    // Count unique participants in thread
    @Query("SELECT COUNT(DISTINCT dr.author) FROM DiscussionReply dr WHERE dr.thread = :thread AND dr.isDeleted = false")
    long countUniqueParticipants(@Param("thread") DiscussionThread thread);

    /* Statistical methods */
    // Get daily reply counts
    @Query("SELECT DATE(dr.createdAt), COUNT(dr) FROM DiscussionReply dr WHERE dr.createdAt >= :since AND dr.isDeleted = false GROUP BY DATE(dr.createdAt) ORDER BY DATE(dr.createdAt)")
    List<Object[]> getDailyReplyCounts(@Param("since") LocalDateTime since);

    // Find most active users
    @Query("SELECT dr.author, COUNT(dr) FROM DiscussionReply dr WHERE dr.isDeleted = false GROUP BY dr.author ORDER BY COUNT(dr) DESC")
    List<Object[]> findMostActiveUsers();

    /* Bulk operation methods */
    // Mark all replies as deleted by user
    @Modifying
    @Query("UPDATE DiscussionReply dr SET dr.isDeleted = true WHERE dr.author = :user")
    int markAllRepliesAsDeletedByUser(@Param("user") User user);

    // Mark all replies as deleted by thread
    @Modifying
    @Query("UPDATE DiscussionReply dr SET dr.isDeleted = true WHERE dr.thread = :thread")
    int markAllRepliesAsDeletedByThread(@Param("thread") DiscussionThread thread);

    // Delete old deleted replies
    @Query("DELETE FROM DiscussionReply dr WHERE dr.isDeleted = true AND dr.createdAt < :cutoffDate")
    @Modifying
    int deleteOldDeletedReplies(@Param("cutoffDate") LocalDateTime cutoffDate);
}

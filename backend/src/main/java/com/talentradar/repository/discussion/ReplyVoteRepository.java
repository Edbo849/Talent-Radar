package com.talentradar.repository.discussion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.discussion.DiscussionReply;
import com.talentradar.model.discussion.ReplyVote;
import com.talentradar.model.enums.VoteType;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing ReplyVote entities. Provides data access
 * operations for vote management, user voting patterns, statistical analysis,
 * and vote aggregation.
 */
@Repository
public interface ReplyVoteRepository extends JpaRepository<ReplyVote, Long> {

    /* Basic vote finder methods */
    // Get user's vote on a specific reply
    Optional<ReplyVote> findByReplyAndUser(DiscussionReply reply, User user);

    // Get all votes for a reply
    List<ReplyVote> findByReply(DiscussionReply reply);

    // Get all votes by a user
    List<ReplyVote> findByUser(User user);

    /* Vote existence methods */
    // Check if user has voted on a reply
    boolean existsByReplyAndUser(DiscussionReply reply, User user);

    /* Vote type filtering methods */
    // Get votes by type for a reply
    List<ReplyVote> findByReplyAndVoteType(DiscussionReply reply, VoteType voteType);

    // Count votes by type for a reply
    long countByReplyAndVoteType(DiscussionReply reply, VoteType voteType);

    // Count user votes by type
    @Query("SELECT COUNT(rv) FROM ReplyVote rv WHERE rv.user = :user AND rv.voteType = :voteType")
    long countUserVotesByType(@Param("user") User user, @Param("voteType") VoteType voteType);

    /* Time-based vote methods */
    // Get recent votes
    List<ReplyVote> findByCreatedAtAfter(LocalDateTime since);

    // Get user's recent votes
    List<ReplyVote> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime since);

    /* Statistical and aggregation methods */
    // Most upvoted replies
    @Query("SELECT rv.reply, COUNT(rv) as voteCount FROM ReplyVote rv WHERE rv.voteType = 'UPVOTE' GROUP BY rv.reply ORDER BY COUNT(rv) DESC")
    List<Object[]> findMostUpvotedReplies();

    // Most active voters
    @Query("SELECT rv.user, COUNT(rv) as voteCount FROM ReplyVote rv GROUP BY rv.user ORDER BY COUNT(rv) DESC")
    List<Object[]> findMostActiveVoters();

    // Vote statistics for a reply
    @Query("SELECT rv.voteType, COUNT(rv) FROM ReplyVote rv WHERE rv.reply = :reply GROUP BY rv.voteType")
    List<Object[]> getVoteStatisticsForReply(@Param("reply") DiscussionReply reply);

    // Net score for replies (upvotes - downvotes)
    @Query("SELECT rv.reply, "
            + "SUM(CASE WHEN rv.voteType = 'UPVOTE' THEN 1 ELSE 0 END) - "
            + "SUM(CASE WHEN rv.voteType = 'DOWNVOTE' THEN 1 ELSE 0 END) as netScore "
            + "FROM ReplyVote rv GROUP BY rv.reply ORDER BY netScore DESC")
    List<Object[]> getRepliesByNetScore();

    // Get controversial replies (high vote count but close upvote/downvote ratio)
    @Query("SELECT rv.reply, "
            + "COUNT(rv) as totalVotes, "
            + "SUM(CASE WHEN rv.voteType = 'UPVOTE' THEN 1 ELSE 0 END) as upvotes, "
            + "SUM(CASE WHEN rv.voteType = 'DOWNVOTE' THEN 1 ELSE 0 END) as downvotes "
            + "FROM ReplyVote rv "
            + "GROUP BY rv.reply "
            + "HAVING COUNT(rv) >= :minVotes "
            + "ORDER BY (ABS(SUM(CASE WHEN rv.voteType = 'UPVOTE' THEN 1 ELSE 0 END) - "
            + "SUM(CASE WHEN rv.voteType = 'DOWNVOTE' THEN 1 ELSE 0 END)) / COUNT(rv)) ASC")
    List<Object[]> findControversialReplies(@Param("minVotes") int minVotes);

    // Daily vote statistics
    @Query("SELECT DATE(rv.createdAt), rv.voteType, COUNT(rv) FROM ReplyVote rv WHERE rv.createdAt >= :since GROUP BY DATE(rv.createdAt), rv.voteType ORDER BY DATE(rv.createdAt)")
    List<Object[]> getDailyVoteStatistics(@Param("since") LocalDateTime since);

    /* Deletion methods */
    // Delete all votes for a reply
    void deleteByReply(DiscussionReply reply);

    // Delete all votes by a user
    void deleteByUser(User user);
}

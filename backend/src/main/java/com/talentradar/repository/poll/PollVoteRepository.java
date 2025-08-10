package com.talentradar.repository.poll;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.poll.Poll;
import com.talentradar.model.poll.PollOption;
import com.talentradar.model.poll.PollVote;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing PollVote entities. Provides data access
 * operations for vote management, participation tracking, analytics, and fraud
 * prevention.
 */
@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {

    /* Basic vote finder methods */
    // Find vote by poll and user
    Optional<PollVote> findByPollAndUser(Poll poll, User user);

    // Find all votes for a specific poll
    List<PollVote> findByPoll(Poll poll);

    // Find all votes by a specific user
    List<PollVote> findByUser(User user);

    // Find votes for a specific poll option
    List<PollVote> findByPollOption(PollOption pollOption);

    /* Existence check methods */
    // Check if an IP address has voted on a poll (for anonymous polls)
    boolean existsByPollAndIpAddress(Poll poll, String ipAddress);

    // Check if user has voted on a poll
    boolean existsByPollAndUser(Poll poll, User user);

    /* Count methods */
    // Count votes for a specific poll
    long countByPoll(Poll poll);

    // Count votes for a specific poll option
    long countByPollOption(PollOption pollOption);

    // Count votes by a specific user
    long countByUser(User user);

    /* Paginated finder methods */
    // Find votes by poll with pagination
    Page<PollVote> findByPoll(Poll poll, Pageable pageable);

    // Find votes by user with pagination
    Page<PollVote> findByUser(User user, Pageable pageable);

    /* Time-based and recent activity */
    // Find recent votes across all polls
    @Query("SELECT pv FROM PollVote pv ORDER BY pv.createdAt DESC")
    List<PollVote> findRecentVotes(Pageable pageable);

    // Find votes within date range
    @Query("SELECT pv FROM PollVote pv WHERE pv.createdAt >= :startDate AND pv.createdAt <= :endDate")
    List<PollVote> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /* Analytics and aggregation methods */
    // Get vote counts per option for a poll
    @Query("SELECT pv.pollOption, COUNT(pv) FROM PollVote pv WHERE pv.poll = :poll GROUP BY pv.pollOption")
    List<Object[]> getVoteCountsByOption(@Param("poll") Poll poll);

    // Find votes for polls created by a specific user
    @Query("SELECT pv FROM PollVote pv WHERE pv.poll.author = :user")
    List<PollVote> findVotesForPollsCreatedBy(@Param("user") User user);

    // Get most active voters
    @Query("SELECT pv.user, COUNT(pv) FROM PollVote pv GROUP BY pv.user ORDER BY COUNT(pv) DESC")
    List<Object[]> findMostActiveVoters(Pageable pageable);

    /* Deletion methods */
    // Delete votes for a specific poll
    void deleteByPoll(Poll poll);

    // Delete votes by a specific user
    void deleteByUser(User user);
}

package com.talentradar.repository.poll;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.enums.PollType;
import com.talentradar.model.player.Player;
import com.talentradar.model.poll.Poll;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing Poll entities. Provides data access
 * operations for poll management, voting analytics, status filtering, and
 * engagement tracking.
 */
@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {

    /* Basic entity-based finder methods */
    // Find polls by thread ordered by creation date
    List<Poll> findByThreadOrderByCreatedAtDesc(DiscussionThread thread);

    // Find polls by player with pagination
    Page<Poll> findByPlayerOrderByCreatedAtDesc(Player player, Pageable pageable);

    // Find polls by author with pagination
    Page<Poll> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);


    /* Active polls */
    // Find active polls ordered by creation date
    @Query("SELECT p FROM Poll p WHERE p.isActive = true AND (p.expiresAt IS NULL OR p.expiresAt > CURRENT_TIMESTAMP) ORDER BY p.createdAt DESC")
    Page<Poll> findActivePollsOrderByCreatedAtDesc(Pageable pageable);

    // Find active polls ordered by creation date
    Page<Poll> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    // Find active polls ordered by total votes
    Page<Poll> findByIsActiveTrueOrderByTotalVotesDesc(Pageable pageable);

    /* Poll type filtering */
    // Find polls by type and active status
    Page<Poll> findByPollTypeAndIsActiveTrueOrderByCreatedAtDesc(PollType pollType, Pageable pageable);

    /* Expired polls */
    // Find expired polls ordered by creation date
    @Query("SELECT p FROM Poll p WHERE p.isActive = false OR (p.expiresAt IS NOT NULL AND p.expiresAt < CURRENT_TIMESTAMP) ORDER BY p.createdAt DESC")
    Page<Poll> findExpiredPollsOrderByCreatedAtDesc(Pageable pageable);

    /* Time-based finder methods */
    // Find recent active polls
    List<Poll> findByCreatedAtAfterAndIsActiveTrueOrderByCreatedAtDesc(LocalDateTime since);

    // Find polls expiring before specific date
    @Query("SELECT p FROM Poll p WHERE p.isActive = true AND p.expiresAt IS NOT NULL AND p.expiresAt BETWEEN CURRENT_TIMESTAMP AND :endDate ORDER BY p.expiresAt ASC")
    List<Poll> findExpiringPollsBefore(@Param("endDate") LocalDateTime endDate);

    /* Popular and featured polls */
    // Find most popular active polls by vote count
    @Query("SELECT p FROM Poll p WHERE p.isActive = true ORDER BY p.totalVotes DESC, p.createdAt DESC")
    Page<Poll> findMostPopularPolls(Pageable pageable);

    // Find featured polls with minimum vote threshold
    @Query("SELECT p FROM Poll p WHERE p.isActive = true AND p.totalVotes >= :minVotes ORDER BY p.totalVotes DESC")
    Page<Poll> findFeaturedPolls(@Param("minVotes") Integer minVotes, Pageable pageable);

    /* User voting analytics */
    // Find polls voted by user
    @Query("SELECT v.pollOption.id FROM PollVote v WHERE v.poll.id = :pollId AND v.user.id = :userId")
    List<Long> findVotedOptionIdsByPollAndUser(@Param("pollId") Long pollId, @Param("userId") Long userId);

    // Find polls voted by IP address
    @Query("SELECT v.pollOption.id FROM PollVote v WHERE v.poll.id = :pollId AND v.ipAddress = :ipAddress")
    List<Long> findVotedOptionIdsByPollAndIpAddress(@Param("pollId") Long pollId, @Param("ipAddress") String ipAddress);

    /* Count methods */
    // Count active polls by author
    Long countByAuthorAndIsActiveTrue(User author);
}

package com.talentradar.repository.poll;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.poll.Poll;
import com.talentradar.model.poll.PollOption;

/**
 * Repository interface for managing PollOption entities. Provides data access
 * operations for poll option management, vote counting, and winner
 * determination.
 */
@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    /* Basic finder methods */
    // Find options for a poll ordered by display order
    List<PollOption> findByPollOrderByDisplayOrderAsc(Poll poll);

    /* Result analysis methods */
    // Find winning option for a poll (highest vote count)
    @Query("SELECT po FROM PollOption po WHERE po.poll = :poll ORDER BY po.voteCount DESC LIMIT 1")
    PollOption findWinningOptionByPoll(@Param("poll") Poll poll);

    /* Vote counting methods */
    // Get total votes for all options in a poll
    @Query("SELECT SUM(po.voteCount) FROM PollOption po WHERE po.poll = :poll")
    Long getTotalVotesByPoll(@Param("poll") Poll poll);
}

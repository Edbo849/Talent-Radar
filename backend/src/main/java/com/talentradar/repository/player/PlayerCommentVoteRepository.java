package com.talentradar.repository.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.talentradar.model.enums.VoteType;
import com.talentradar.model.player.PlayerComment;
import com.talentradar.model.player.PlayerCommentVote;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing PlayerCommentVote entities. Provides data
 * access operations for vote management, user voting patterns, and comment
 * engagement tracking.
 */
@Repository
public interface PlayerCommentVoteRepository extends JpaRepository<PlayerCommentVote, Long> {

    /* Basic vote finder methods */
    // Find vote by comment and user
    Optional<PlayerCommentVote> findByCommentAndUser(PlayerComment comment, User user);

    // Get all votes for a comment
    List<PlayerCommentVote> findByComment(PlayerComment comment);

    // Get votes by user
    List<PlayerCommentVote> findByUser(User user);

    /* Existence check methods */
    // Check if user has voted on comment
    boolean existsByCommentAndUser(PlayerComment comment, User user);

    /* Vote counting methods */
    // Count votes by type for a comment
    long countByCommentAndVoteType(PlayerComment comment, VoteType voteType);

    /* Vote management methods */
    // Delete vote by comment and user
    void deleteByCommentAndUser(PlayerComment comment, User user);
}

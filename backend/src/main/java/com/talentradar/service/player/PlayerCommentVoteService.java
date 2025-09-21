package com.talentradar.service.player;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.model.enums.VoteType;
import com.talentradar.model.player.PlayerComment;
import com.talentradar.model.player.PlayerCommentVote;
import com.talentradar.model.user.User;
import com.talentradar.repository.player.PlayerCommentRepository;
import com.talentradar.repository.player.PlayerCommentVoteRepository;

/**
 * Service responsible for managing player comment votes. Handles vote creation,
 * updates, removal, and vote count management.
 */
@Service
@Transactional
public class PlayerCommentVoteService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCommentVoteService.class);

    @Autowired
    private PlayerCommentVoteRepository voteRepository;

    @Autowired
    private PlayerCommentRepository commentRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PlayerCommentVote voteOnComment(Long commentId, User user, VoteType voteType) {
        try {
            if (commentId == null) {
                throw new IllegalArgumentException("Comment ID cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (voteType == null) {
                throw new IllegalArgumentException("Vote type cannot be null");
            }

            PlayerComment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

            // Check if user already voted
            Optional<PlayerCommentVote> existingVote = voteRepository.findByCommentAndUser(comment, user);

            if (existingVote.isPresent()) {
                PlayerCommentVote vote = existingVote.get();
                if (vote.getVoteType() == voteType) {
                    // Remove vote if same type
                    voteRepository.delete(vote);
                    updateCommentVoteCounts(comment);
                    logger.info("Removed {} vote from comment {} by user {}", voteType, commentId, user.getUsername());
                    return null;
                } else {
                    // Update vote type
                    vote.setVoteType(voteType);
                    PlayerCommentVote savedVote = voteRepository.save(vote);
                    updateCommentVoteCounts(comment);
                    logger.info("Updated vote to {} on comment {} by user {}", voteType, commentId, user.getUsername());
                    return savedVote;
                }
            } else {
                // Create new vote
                PlayerCommentVote vote = new PlayerCommentVote();
                vote.setComment(comment);
                vote.setUser(user);
                vote.setVoteType(voteType);
                PlayerCommentVote savedVote = voteRepository.save(vote);
                updateCommentVoteCounts(comment);
                logger.info("Created {} vote on comment {} by user {}", voteType, commentId, user.getUsername());
                return savedVote;
            }

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for voting on comment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error voting on comment {} by user {}: {}",
                    commentId, user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to vote on comment", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void removeVote(Long commentId, User user) {
        try {
            if (commentId == null) {
                throw new IllegalArgumentException("Comment ID cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            PlayerComment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

            voteRepository.deleteByCommentAndUser(comment, user);
            updateCommentVoteCounts(comment);

            logger.info("Removed vote from comment {} by user {}", commentId, user.getUsername());

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for removing vote: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error removing vote from comment {} by user {}: {}",
                    commentId, user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to remove vote", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean hasUserVoted(Long commentId, User user) {
        try {
            if (commentId == null) {
                throw new IllegalArgumentException("Comment ID cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            PlayerComment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

            return voteRepository.existsByCommentAndUser(comment, user);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for checking user vote: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error checking if user {} voted on comment {}: {}",
                    user != null ? user.getUsername() : "null", commentId, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<VoteType> getUserVoteType(Long commentId, User user) {
        try {
            if (commentId == null) {
                throw new IllegalArgumentException("Comment ID cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            PlayerComment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

            return voteRepository.findByCommentAndUser(comment, user)
                    .map(PlayerCommentVote::getVoteType);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting user vote type: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting vote type for comment {} by user {}: {}",
                    commentId, user != null ? user.getUsername() : "null", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private void updateCommentVoteCounts(PlayerComment comment) {
        try {
            long upvotes = voteRepository.countByCommentAndVoteType(comment, VoteType.UPVOTE);
            long downvotes = voteRepository.countByCommentAndVoteType(comment, VoteType.DOWNVOTE);

            comment.setUpvotes((int) upvotes);
            comment.setDownvotes((int) downvotes);
            commentRepository.save(comment);

            logger.info("Updated vote counts for comment {}: {} upvotes, {} downvotes",
                    comment.getId(), upvotes, downvotes);

        } catch (Exception e) {
            logger.error("Error updating vote counts for comment {}: {}",
                    comment != null ? comment.getId() : "null", e.getMessage());
        }
    }
}

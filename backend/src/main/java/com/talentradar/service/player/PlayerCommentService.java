package com.talentradar.service.player;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.model.enums.VoteType;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerComment;
import com.talentradar.model.player.PlayerCommentVote;
import com.talentradar.model.user.User;
import com.talentradar.repository.player.PlayerCommentRepository;
import com.talentradar.repository.player.PlayerCommentVoteRepository;
import com.talentradar.repository.player.PlayerRepository;
import com.talentradar.service.notification.NotificationService;

/**
 * Service responsible for managing player comments. Handles comment creation,
 * voting, moderation, and retrieval operations.
 */
@Service
@Transactional
public class PlayerCommentService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCommentService.class);

    @Autowired
    private PlayerCommentRepository commentRepository;

    @Autowired
    private PlayerCommentVoteRepository voteRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PlayerComment save(PlayerComment comment) {
        try {
            if (comment == null) {
                throw new IllegalArgumentException("Comment cannot be null");
            }
            return commentRepository.save(comment);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid comment data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error saving comment: {}", e.getMessage());
            throw new RuntimeException("Failed to save comment", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PlayerComment createComment(Long playerId, User author, String content, Long parentCommentId) {
        try {
            if (playerId == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }
            if (author == null) {
                throw new IllegalArgumentException("Author cannot be null");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("Comment content cannot be null or empty");
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

            PlayerComment parentComment = null;
            if (parentCommentId != null) {
                parentComment = commentRepository.findById(parentCommentId)
                        .orElseThrow(() -> new IllegalArgumentException("Parent comment not found with ID: " + parentCommentId));
            }

            PlayerComment comment = new PlayerComment();
            comment.setPlayer(player);
            comment.setAuthor(author);
            comment.setContent(content.trim());
            comment.setParentComment(parentComment);

            PlayerComment savedComment = commentRepository.save(comment);

            // Notify if it's a reply to another comment
            if (parentComment != null && !parentComment.getAuthor().getId().equals(author.getId())) {
                try {
                    notificationService.notifyOfCommentReply(parentComment, savedComment);
                } catch (Exception e) {
                    logger.warn("Failed to send comment reply notification: {}", e.getMessage());
                }
            }

            logger.info("Created comment for player {} by user {}", player.getName(), author.getUsername());
            return savedComment;

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for creating comment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating comment for player {} by user {}: {}",
                    playerId, author != null ? author.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to create comment", e);
        }
    }

    /**
     * Updates an existing comment's content.
     */
    public PlayerComment updateComment(Long commentId, User user, String newContent) {
        try {
            if (commentId == null) {
                throw new IllegalArgumentException("Comment ID cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (newContent == null || newContent.trim().isEmpty()) {
                throw new IllegalArgumentException("New content cannot be null or empty");
            }

            PlayerComment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

            // Check permissions
            if (!comment.getAuthor().getId().equals(user.getId()) && !user.canModerate()) {
                throw new IllegalStateException("User not authorized to update this comment");
            }

            // Check if comment is deleted
            if (comment.getIsDeleted()) {
                throw new IllegalStateException("Cannot update deleted comment");
            }

            comment.setContent(newContent.trim());
            PlayerComment updatedComment = commentRepository.save(comment);

            logger.info("Updated comment {} by user {}", commentId, user.getUsername());
            return updatedComment;

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error updating comment: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating comment {} by user {}: {}", commentId, user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to update comment", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<PlayerComment> getPlayerComments(Long playerId, Pageable pageable) {
        try {
            if (playerId == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

            return commentRepository.findByPlayerAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(player, pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting player comments: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving comments for player {}: {}", playerId, e.getMessage());
            throw new RuntimeException("Failed to retrieve player comments", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerComment> getCommentReplies(Long parentCommentId) {
        try {
            if (parentCommentId == null) {
                throw new IllegalArgumentException("Parent comment ID cannot be null");
            }

            PlayerComment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + parentCommentId));

            return commentRepository.findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(parentComment);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting comment replies: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving replies for comment {}: {}", parentCommentId, e.getMessage());
            throw new RuntimeException("Failed to retrieve comment replies", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void voteOnComment(Long commentId, User user, VoteType voteType) {
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

            Optional<PlayerCommentVote> existingVoteOpt = voteRepository.findByCommentAndUser(comment, user);

            if (existingVoteOpt.isPresent()) {
                PlayerCommentVote existingVote = existingVoteOpt.get();

                if (existingVote.getVoteType() == voteType) {
                    // Remove vote if same type
                    voteRepository.delete(existingVote);
                    updateCommentVoteCounts(comment);
                    logger.info("Removed {} vote from comment {} by user {}", voteType, commentId, user.getUsername());
                    return;
                } else {
                    // Update vote type
                    existingVote.setVoteType(voteType);
                    voteRepository.save(existingVote);
                    logger.info("Updated vote to {} on comment {} by user {}", voteType, commentId, user.getUsername());
                }
            } else {
                // Create new vote
                PlayerCommentVote vote = new PlayerCommentVote();
                vote.setComment(comment);
                vote.setUser(user);
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                logger.info("Added {} vote to comment {} by user {}", voteType, commentId, user.getUsername());
            }

            updateCommentVoteCounts(comment);

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
    public void deleteComment(Long commentId, User user) {
        try {
            if (commentId == null) {
                throw new IllegalArgumentException("Comment ID cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            PlayerComment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

            // Check permissions
            if (!comment.getAuthor().getId().equals(user.getId()) && !user.canModerate()) {
                throw new IllegalStateException("Cannot delete this comment");
            }

            comment.setIsDeleted(true);
            commentRepository.save(comment);

            logger.info("Deleted comment {} by user {}", commentId, user.getUsername());

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error deleting comment: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deleting comment {} by user {}: {}",
                    commentId, user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to delete comment", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void featureComment(Long commentId, User user) {
        try {
            if (commentId == null) {
                throw new IllegalArgumentException("Comment ID cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            if (!user.canModerate()) {
                throw new IllegalStateException("Only moderators can feature comments");
            }

            PlayerComment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

            comment.setIsFeatured(true);
            commentRepository.save(comment);

            logger.info("Featured comment {} by moderator {}", commentId, user.getUsername());

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error featuring comment: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error featuring comment {} by user {}: {}",
                    commentId, user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to feature comment", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<PlayerComment> getCommentById(Long commentId) {
        try {
            if (commentId == null) {
                logger.warn("Attempted to find comment with null ID");
                return Optional.empty();
            }
            return commentRepository.findById(commentId);
        } catch (Exception e) {
            logger.error("Error finding comment by ID {}: {}", commentId, e.getMessage());
            return Optional.empty();
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
    @Transactional(readOnly = true)
    public List<PlayerComment> getFeaturedComments(Long playerId) {
        try {
            if (playerId == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player not found with ID: " + playerId));

            return commentRepository.findByPlayerAndIsFeaturedTrueAndIsDeletedFalseOrderByCreatedAtDesc(player);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting featured comments: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving featured comments for player {}: {}", playerId, e.getMessage());
            throw new RuntimeException("Failed to retrieve featured comments", e);
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

            logger.debug("Updated vote counts for comment {}: {} upvotes, {} downvotes",
                    comment.getId(), upvotes, downvotes);

        } catch (Exception e) {
            logger.error("Error updating vote counts for comment {}: {}",
                    comment != null ? comment.getId() : "null", e.getMessage());
        }
    }
}

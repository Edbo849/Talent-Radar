package com.talentradar.service.discussion;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.model.discussion.DiscussionReply;
import com.talentradar.model.discussion.ReplyVote;
import com.talentradar.model.enums.VoteType;
import com.talentradar.model.user.User;
import com.talentradar.repository.discussion.DiscussionReplyRepository;
import com.talentradar.repository.discussion.ReplyVoteRepository;

@Service
@Transactional
public class ReplyVoteService {

    private static final Logger logger = LoggerFactory.getLogger(ReplyVoteService.class);

    @Autowired
    private ReplyVoteRepository replyVoteRepository;

    @Autowired
    private DiscussionReplyRepository replyRepository;

    /**
     * Processes a vote on a reply, handling vote creation, update, or removal.
     */
    public ReplyVote voteOnReply(Long replyId, User user, VoteType voteType) {
        if (replyId == null) {
            throw new IllegalArgumentException("Reply ID cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (voteType == null) {
            throw new IllegalArgumentException("Vote type cannot be null");
        }

        try {
            DiscussionReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new RuntimeException("Reply not found with ID: " + replyId));

            // Check if user already voted
            Optional<ReplyVote> existingVote = replyVoteRepository.findByReplyAndUser(reply, user);

            if (existingVote.isPresent()) {
                ReplyVote vote = existingVote.get();
                if (vote.getVoteType() == voteType) {
                    // Remove vote if same type
                    if (vote.getVoteType() == VoteType.UPVOTE) {
                        reply.decrementUpvotes();
                    } else {
                        reply.decrementDownvotes();
                    }
                    replyVoteRepository.delete(vote);
                    replyRepository.save(reply);

                    logger.info("Removed {} vote from reply {} by user {}",
                            voteType, replyId, user.getUsername());
                    return null;
                } else {
                    // Update vote type
                    // Decrement old vote count
                    if (vote.getVoteType() == VoteType.UPVOTE) {
                        reply.decrementUpvotes();
                    } else {
                        reply.decrementDownvotes();
                    }

                    // Update and increment new vote count
                    vote.setVoteType(voteType);
                    if (voteType == VoteType.UPVOTE) {
                        reply.incrementUpvotes();
                    } else {
                        reply.incrementDownvotes();
                    }

                    ReplyVote savedVote = replyVoteRepository.save(vote);
                    replyRepository.save(reply);

                    logger.info("Changed vote from {} to {} on reply {} by user {}",
                            vote.getVoteType() == VoteType.UPVOTE ? VoteType.DOWNVOTE : VoteType.UPVOTE,
                            voteType, replyId, user.getUsername());
                    return savedVote;
                }
            } else {
                // Create new vote
                ReplyVote vote = new ReplyVote();
                vote.setReply(reply);
                vote.setUser(user);
                vote.setVoteType(voteType);

                if (voteType == VoteType.UPVOTE) {
                    reply.incrementUpvotes();
                } else {
                    reply.incrementDownvotes();
                }

                ReplyVote savedVote = replyVoteRepository.save(vote);
                replyRepository.save(reply);

                logger.info("Added {} vote to reply {} by user {}",
                        voteType, replyId, user.getUsername());
                return savedVote;
            }
        } catch (Exception e) {
            logger.error("Error voting on reply {}: {}", replyId, e.getMessage(), e);
            throw new RuntimeException("Failed to vote on reply: " + e.getMessage(), e);
        }
    }

    /**
     * Removes a user's vote from a reply.
     */
    public void removeVote(Long replyId, User user) {
        if (replyId == null) {
            throw new IllegalArgumentException("Reply ID cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            DiscussionReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new RuntimeException("Reply not found with ID: " + replyId));

            Optional<ReplyVote> existingVote = replyVoteRepository.findByReplyAndUser(reply, user);

            if (existingVote.isPresent()) {
                ReplyVote vote = existingVote.get();

                // Decrement the appropriate counter
                if (vote.getVoteType() == VoteType.UPVOTE) {
                    reply.decrementUpvotes();
                } else {
                    reply.decrementDownvotes();
                }

                replyVoteRepository.delete(vote);
                replyRepository.save(reply);

                logger.info("Removed vote from reply {} by user {}", replyId, user.getUsername());
            }
        } catch (Exception e) {
            logger.error("Error removing vote from reply {}: {}", replyId, e.getMessage(), e);
            throw new RuntimeException("Failed to remove vote: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a user has voted on a specific reply.
     */
    @Transactional(readOnly = true)
    public boolean hasUserVoted(Long replyId, User user) {
        if (replyId == null) {
            throw new IllegalArgumentException("Reply ID cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            DiscussionReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new RuntimeException("Reply not found with ID: " + replyId));

            return replyVoteRepository.existsByReplyAndUser(reply, user);
        } catch (Exception e) {
            logger.error("Error checking if user voted on reply {}: {}", replyId, e.getMessage(), e);
            throw new RuntimeException("Failed to check vote status: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the type of vote a user has cast on a reply.
     */
    @Transactional(readOnly = true)
    public Optional<VoteType> getUserVoteType(Long replyId, User user) {
        if (replyId == null) {
            throw new IllegalArgumentException("Reply ID cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            DiscussionReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new RuntimeException("Reply not found with ID: " + replyId));

            return replyVoteRepository.findByReplyAndUser(reply, user)
                    .map(ReplyVote::getVoteType);
        } catch (Exception e) {
            logger.error("Error getting user vote type for reply {}: {}", replyId, e.getMessage(), e);
            throw new RuntimeException("Failed to get vote type: " + e.getMessage(), e);
        }
    }

    /**
     * Counts the number of upvotes for a reply.
     */
    @Transactional(readOnly = true)
    public long getUpvoteCount(Long replyId) {
        if (replyId == null) {
            throw new IllegalArgumentException("Reply ID cannot be null");
        }

        try {
            DiscussionReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new RuntimeException("Reply not found with ID: " + replyId));

            return replyVoteRepository.countByReplyAndVoteType(reply, VoteType.UPVOTE);
        } catch (Exception e) {
            logger.error("Error counting upvotes for reply {}: {}", replyId, e.getMessage(), e);
            throw new RuntimeException("Failed to count upvotes: " + e.getMessage(), e);
        }
    }

    /**
     * Counts the number of downvotes for a reply.
     */
    @Transactional(readOnly = true)
    public long getDownvoteCount(Long replyId) {
        if (replyId == null) {
            throw new IllegalArgumentException("Reply ID cannot be null");
        }

        try {
            DiscussionReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new RuntimeException("Reply not found with ID: " + replyId));

            return replyVoteRepository.countByReplyAndVoteType(reply, VoteType.DOWNVOTE);
        } catch (Exception e) {
            logger.error("Error counting downvotes for reply {}: {}", replyId, e.getMessage(), e);
            throw new RuntimeException("Failed to count downvotes: " + e.getMessage(), e);
        }
    }
}

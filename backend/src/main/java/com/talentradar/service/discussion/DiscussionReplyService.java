package com.talentradar.service.discussion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.DiscussionReplyNotFoundException;
import com.talentradar.exception.DiscussionThreadNotFoundException;
import com.talentradar.model.discussion.DiscussionReply;
import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.discussion.ReplyVote;
import com.talentradar.model.enums.VoteType;
import com.talentradar.model.user.User;
import com.talentradar.repository.discussion.DiscussionReplyRepository;
import com.talentradar.repository.discussion.DiscussionThreadRepository;
import com.talentradar.repository.discussion.ReplyVoteRepository;
import com.talentradar.service.notification.NotificationService;

/**
 * Service layer for managing discussion replies. Provides business logic for
 * reply creation, modification, voting, and moderation operations.
 */
@Service
@Transactional
public class DiscussionReplyService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussionReplyService.class);

    @Autowired
    private DiscussionReplyRepository replyRepository;

    @Autowired
    private DiscussionThreadRepository threadRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReplyVoteRepository replyVoteRepository;

    /**
     * Creates a new reply to a discussion thread.
     */
    public DiscussionReply createReply(Long threadId, User author, String content, Long parentReplyId) {
        try {
            if (threadId == null) {
                throw new IllegalArgumentException("Thread ID cannot be null");
            }
            if (author == null) {
                throw new IllegalArgumentException("Author cannot be null");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("Reply content cannot be null or empty");
            }

            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new DiscussionThreadNotFoundException("Thread not found with ID: " + threadId));

            if (thread.getIsLocked()) {
                throw new IllegalStateException("Cannot reply to locked thread");
            }

            DiscussionReply parentReply = null;
            if (parentReplyId != null) {
                parentReply = replyRepository.findById(parentReplyId)
                        .orElseThrow(() -> new DiscussionReplyNotFoundException("Parent reply not found with ID: " + parentReplyId));

                // Ensure parent reply belongs to the same thread
                if (!parentReply.getThread().getId().equals(threadId)) {
                    throw new IllegalArgumentException("Parent reply does not belong to this thread");
                }
            }

            DiscussionReply reply = new DiscussionReply(thread, author, content.trim(), parentReply);
            DiscussionReply savedReply = replyRepository.save(reply);

            // Update thread statistics
            thread.incrementReplyCount();
            thread.updateLastActivity();
            threadRepository.save(thread);

            // Notify thread author and parent reply author
            notificationService.notifyOfNewReply(thread, savedReply, parentReply);

            logger.info("Created new reply for thread {} by user: {}", threadId, author.getUsername());
            return savedReply;

        } catch (DiscussionThreadNotFoundException | DiscussionReplyNotFoundException e) {
            logger.error("Entity not found while creating reply: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid reply creation parameters: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error creating discussion reply: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create discussion reply", e);
        }
    }

    /**
     * Votes on a reply (upvote or downvote).
     */
    public void voteOnReply(DiscussionReply reply, User user, boolean isUpvote) {
        try {
            if (reply == null) {
                throw new IllegalArgumentException("Reply cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            // Check if user has already voted on this reply
            Optional<ReplyVote> existingVoteOpt = replyVoteRepository.findByReplyAndUser(reply, user);

            if (existingVoteOpt.isPresent()) {
                // Update existing vote
                ReplyVote existingVote = existingVoteOpt.get();
                VoteType newVoteType = isUpvote ? VoteType.UPVOTE : VoteType.DOWNVOTE;

                if (existingVote.getVoteType() != newVoteType) {
                    // Change vote type - first undo the old vote
                    if (existingVote.getVoteType() == VoteType.UPVOTE) {
                        reply.decrementUpvotes();
                    } else {
                        reply.decrementDownvotes();
                    }

                    // Then apply the new vote
                    if (newVoteType == VoteType.UPVOTE) {
                        reply.incrementUpvotes();
                    } else {
                        reply.incrementDownvotes();
                    }

                    existingVote.setVoteType(newVoteType);
                    replyVoteRepository.save(existingVote);
                } else {
                    // If same vote type, remove the vote
                    if (existingVote.getVoteType() == VoteType.UPVOTE) {
                        reply.decrementUpvotes();
                    } else {
                        reply.decrementDownvotes();
                    }
                    replyVoteRepository.delete(existingVote);
                }
            } else {
                // Create new vote
                ReplyVote vote = new ReplyVote(reply, user, isUpvote ? VoteType.UPVOTE : VoteType.DOWNVOTE);
                replyVoteRepository.save(vote);

                if (isUpvote) {
                    reply.incrementUpvotes();
                } else {
                    reply.incrementDownvotes();
                }
            }

            replyRepository.save(reply);
            logger.debug("User {} {} reply {}", user.getUsername(), isUpvote ? "upvoted" : "downvoted", reply.getId());

        } catch (IllegalArgumentException e) {
            logger.error("Invalid vote parameters: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error voting on reply: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to vote on reply", e);
        }
    }

    /**
     * Reports a reply for inappropriate content.
     */
    public void reportReply(DiscussionReply reply, User reporter, String reason) {
        try {
            if (reply == null) {
                throw new IllegalArgumentException("Reply cannot be null");
            }
            if (reporter == null) {
                throw new IllegalArgumentException("Reporter cannot be null");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Report reason cannot be empty");
            }

            // Create a report notification for moderators
            notificationService.notifyModeratorsOfReport(reply, reporter, reason.trim());

            logger.info("Reply {} reported by {} for: {}", reply.getId(), reporter.getUsername(), reason.trim());

        } catch (IllegalArgumentException e) {
            logger.error("Invalid report parameters: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error reporting reply: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to report reply", e);
        }
    }

    /**
     * Retrieves all replies for a specific thread.
     */
    @Transactional(readOnly = true)
    public List<DiscussionReply> getThreadReplies(Long threadId) {
        try {
            if (threadId == null) {
                throw new IllegalArgumentException("Thread ID cannot be null");
            }

            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new DiscussionThreadNotFoundException("Thread not found with ID: " + threadId));

            return replyRepository.findByThreadAndParentReplyIsNullAndIsDeletedFalse(thread);

        } catch (DiscussionThreadNotFoundException e) {
            logger.error("Thread not found for replies retrieval: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid thread ID: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving thread replies: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve thread replies", e);
        }
    }

    /**
     * Retrieves paginated replies for a specific thread.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionReply> getThreadRepliesPaginated(Long threadId, Pageable pageable) {
        try {
            if (threadId == null) {
                throw new IllegalArgumentException("Thread ID cannot be null");
            }

            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new DiscussionThreadNotFoundException("Thread not found with ID: " + threadId));

            return replyRepository.findByThreadAndParentReplyIsNullAndIsDeletedFalseOrderByCreatedAtAsc(thread, pageable);

        } catch (DiscussionThreadNotFoundException e) {
            logger.error("Thread not found for paginated replies: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid thread ID: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving paginated thread replies: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve paginated thread replies", e);
        }
    }

    /**
     * Retrieves all replies to a specific parent reply.
     */
    @Transactional(readOnly = true)
    public List<DiscussionReply> getRepliesToReply(Long parentReplyId) {
        try {
            if (parentReplyId == null) {
                throw new IllegalArgumentException("Parent reply ID cannot be null");
            }

            DiscussionReply parentReply = replyRepository.findById(parentReplyId)
                    .orElseThrow(() -> new DiscussionReplyNotFoundException("Reply not found with ID: " + parentReplyId));

            return replyRepository.findByParentReplyAndIsDeletedFalseOrderByCreatedAtAsc(parentReply);

        } catch (DiscussionReplyNotFoundException e) {
            logger.error("Parent reply not found: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parent reply ID: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving replies to reply: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve replies to reply", e);
        }
    }

    /**
     * Retrieves paginated replies by a specific author.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionReply> getRepliesByAuthor(User author, Pageable pageable) {
        try {
            if (author == null) {
                throw new IllegalArgumentException("Author cannot be null");
            }
            logger.debug("Retrieving replies by author: {}", author.getUsername());
            return replyRepository.findByAuthorAndIsDeletedFalseOrderByCreatedAtDesc(author, pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid author parameter: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving replies by author: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve replies by author", e);
        }
    }

    /**
     * Retrieves a specific reply by its unique identifier.
     */
    @Transactional(readOnly = true)
    public Optional<DiscussionReply> getReplyById(Long replyId) {
        try {
            if (replyId == null) {
                throw new IllegalArgumentException("Reply ID cannot be null");
            }
            logger.debug("Finding reply by ID: {}", replyId);
            return replyRepository.findById(replyId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid reply ID: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error finding reply by ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find reply by ID", e);
        }
    }

    /**
     * Updates the content of an existing reply.
     */
    public DiscussionReply updateReply(Long replyId, User requester, String content) {
        try {
            if (replyId == null) {
                throw new IllegalArgumentException("Reply ID cannot be null");
            }
            if (requester == null) {
                throw new IllegalArgumentException("Requester cannot be null");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("Reply content cannot be null or empty");
            }

            DiscussionReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new DiscussionReplyNotFoundException("Reply not found with ID: " + replyId));

            // Check permissions - user can edit their own reply
            if (!reply.getAuthor().getId().equals(requester.getId()) && !requester.canModerate()) {
                throw new IllegalStateException("Cannot edit this reply");
            }

            // Check if thread is locked
            if (reply.getThread().getIsLocked() && !requester.canModerate()) {
                throw new IllegalStateException("Cannot edit reply in locked thread");
            }

            reply.setContent(content.trim());
            DiscussionReply updatedReply = replyRepository.save(reply);
            logger.info("Updated reply {} by user: {}", replyId, requester.getUsername());
            return updatedReply;

        } catch (DiscussionReplyNotFoundException e) {
            logger.error("Reply not found for update: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid reply update parameters: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating reply: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update reply", e);
        }
    }

    /**
     * Deletes a reply by marking it as deleted.
     */
    public void deleteReply(Long replyId, User requester) {
        try {
            if (replyId == null) {
                throw new IllegalArgumentException("Reply ID cannot be null");
            }
            if (requester == null) {
                throw new IllegalArgumentException("Requester cannot be null");
            }

            DiscussionReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new DiscussionReplyNotFoundException("Reply not found with ID: " + replyId));

            // Check permissions
            if (!reply.getAuthor().getId().equals(requester.getId()) && !requester.canModerate()) {
                throw new IllegalStateException("Cannot delete this reply");
            }

            reply.setIsDeleted(true);
            reply.setDeletedAt(LocalDateTime.now());
            replyRepository.save(reply);

            // Update thread reply count
            DiscussionThread thread = reply.getThread();
            thread.decrementReplyCount();
            threadRepository.save(thread);

            logger.info("Deleted reply {} by user: {}", replyId, requester.getUsername());

        } catch (DiscussionReplyNotFoundException e) {
            logger.error("Reply not found for deletion: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Cannot delete reply: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deleting reply: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete reply", e);
        }
    }

    /**
     * Features a reply for highlighting.
     */
    public void featureReply(Long replyId, User requester) {
        try {
            if (replyId == null) {
                throw new IllegalArgumentException("Reply ID cannot be null");
            }
            if (requester == null) {
                throw new IllegalArgumentException("Requester cannot be null");
            }
            if (!requester.canModerate()) {
                throw new IllegalStateException("Only moderators can feature replies");
            }

            DiscussionReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new DiscussionReplyNotFoundException("Reply not found with ID: " + replyId));

            reply.setIsFeatured(true);
            replyRepository.save(reply);
            logger.info("Featured reply {} by moderator: {}", replyId, requester.getUsername());

        } catch (DiscussionReplyNotFoundException e) {
            logger.error("Reply not found for featuring: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Cannot feature reply: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error featuring reply: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to feature reply", e);
        }
    }

    /**
     * Unfeatures a previously featured reply.
     */
    public void unfeatureReply(Long replyId, User requester) {
        try {
            if (replyId == null) {
                throw new IllegalArgumentException("Reply ID cannot be null");
            }
            if (requester == null) {
                throw new IllegalArgumentException("Requester cannot be null");
            }
            if (!requester.canModerate()) {
                throw new IllegalStateException("Only moderators can unfeature replies");
            }

            DiscussionReply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new DiscussionReplyNotFoundException("Reply not found with ID: " + replyId));

            reply.setIsFeatured(false);
            replyRepository.save(reply);
            logger.info("Unfeatured reply {} by moderator: {}", replyId, requester.getUsername());

        } catch (DiscussionReplyNotFoundException e) {
            logger.error("Reply not found for unfeaturing: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Cannot unfeature reply: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error unfeaturing reply: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to unfeature reply", e);
        }
    }

    /**
     * Retrieves featured replies for a specific thread.
     */
    @Transactional(readOnly = true)
    public List<DiscussionReply> getFeaturedReplies(Long threadId) {
        try {
            if (threadId == null) {
                throw new IllegalArgumentException("Thread ID cannot be null");
            }

            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new DiscussionThreadNotFoundException("Thread not found with ID: " + threadId));

            return replyRepository.findByThreadAndIsFeaturedTrueAndIsDeletedFalseOrderByCreatedAtDesc(thread);

        } catch (DiscussionThreadNotFoundException e) {
            logger.error("Thread not found for featured replies: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid thread ID: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving featured replies: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve featured replies", e);
        }
    }

    /**
     * Counts the number of replies in a specific thread.
     */
    @Transactional(readOnly = true)
    public long getReplyCountByThread(Long threadId) {
        try {
            if (threadId == null) {
                throw new IllegalArgumentException("Thread ID cannot be null");
            }

            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new DiscussionThreadNotFoundException("Thread not found with ID: " + threadId));

            return replyRepository.countByThreadAndIsDeletedFalse(thread);

        } catch (DiscussionThreadNotFoundException e) {
            logger.error("Thread not found for reply count: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid thread ID: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error counting replies by thread: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to count replies by thread", e);
        }
    }

    /**
     * Counts the number of replies by a specific author.
     */
    @Transactional(readOnly = true)
    public long getReplyCountByAuthor(User author) {
        try {
            if (author == null) {
                throw new IllegalArgumentException("Author cannot be null");
            }
            return replyRepository.countByAuthorAndIsDeletedFalse(author);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid author parameter: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error counting replies by author: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to count replies by author", e);
        }
    }

    /**
     * Retrieves the most recent replies across all threads.
     */
    @Transactional(readOnly = true)
    public List<DiscussionReply> getRecentReplies(int limit) {
        try {
            if (limit <= 0 || limit > 100) {
                throw new IllegalArgumentException("Limit must be between 1 and 100");
            }
            logger.debug("Retrieving {} recent replies", limit);
            return replyRepository.findRecentReplies(PageRequest.of(0, limit));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid limit parameter: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving recent replies: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve recent replies", e);
        }
    }
}

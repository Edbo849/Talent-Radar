package com.talentradar.service.poll;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.PollNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.enums.PollType;
import com.talentradar.model.player.Player;
import com.talentradar.model.poll.Poll;
import com.talentradar.model.poll.PollOption;
import com.talentradar.model.poll.PollVote;
import com.talentradar.model.user.User;
import com.talentradar.repository.poll.PollOptionRepository;
import com.talentradar.repository.poll.PollRepository;
import com.talentradar.repository.poll.PollVoteRepository;
import com.talentradar.service.notification.NotificationService;

/**
 * Service responsible for managing poll operations. Handles poll creation,
 * voting, retrieval, and lifecycle management.
 */
@Service
@Transactional
public class PollService {

    private static final Logger logger = LoggerFactory.getLogger(PollService.class);

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollOptionRepository pollOptionRepository;

    @Autowired
    private PollVoteRepository pollVoteRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public Poll createPoll(User author, String question, String description, PollType pollType,
            List<String> options, DiscussionThread thread, Player player,
            LocalDateTime expiresAt, boolean isAnonymous) {
        try {
            if (author == null) {
                throw new UserNotFoundException("Author cannot be null");
            }
            if (question == null || question.trim().isEmpty()) {
                throw new IllegalArgumentException("Poll question cannot be null or empty");
            }
            if (pollType == null) {
                throw new IllegalArgumentException("Poll type cannot be null");
            }
            if (options == null || options.isEmpty()) {
                throw new IllegalArgumentException("Poll must have at least one option");
            }
            if (options.size() < 2) {
                throw new IllegalArgumentException("Poll must have at least two options");
            }

            // Validate options content
            for (String option : options) {
                if (option == null || option.trim().isEmpty()) {
                    throw new IllegalArgumentException("Poll options cannot be null or empty");
                }
            }

            Poll poll = new Poll(author, question.trim());
            poll.setDescription(description != null ? description.trim() : null);
            poll.setPollType(pollType);
            poll.setThread(thread);
            poll.setPlayer(player);
            poll.setExpiresAt(expiresAt);
            poll.setIsAnonymous(isAnonymous);

            Poll savedPoll = pollRepository.save(poll);

            // Create poll options
            for (int i = 0; i < options.size(); i++) {
                try {
                    PollOption option = new PollOption(savedPoll, options.get(i).trim());
                    option.setDisplayOrder(i);
                    pollOptionRepository.save(option);
                } catch (Exception e) {
                    logger.error("Error creating poll option {}: {}", i, e.getMessage());
                    throw new RuntimeException("Failed to create poll option", e);
                }
            }

            // Notify followers
            try {
                notificationService.notifyUserFollowersOfNewPoll(author, savedPoll);
            } catch (Exception e) {
                logger.warn("Failed to send poll notification: {}", e.getMessage());
            }

            logger.info("Created poll '{}' by user {}", question, author.getUsername());
            return savedPoll;

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error creating poll: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error creating poll by user {}: {}",
                    author != null ? author.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to create poll", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PollVote vote(Poll poll, PollOption option, User user, String ipAddress) {
        try {
            if (poll == null) {
                throw new PollNotFoundException("Poll cannot be null");
            }
            if (option == null) {
                throw new IllegalArgumentException("Poll option cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            // Validate option belongs to poll
            if (!option.getPoll().getId().equals(poll.getId())) {
                throw new IllegalArgumentException("Option does not belong to the specified poll");
            }

            // Check if poll is still active
            if (!poll.canBeVotedBy(user)) {
                throw new IllegalStateException("This poll is not accepting votes");
            }

            // Check if user has already voted
            if (poll.getIsAnonymous()) {
                if (ipAddress != null && pollVoteRepository.existsByPollAndIpAddress(poll, ipAddress)) {
                    throw new IllegalStateException("You have already voted on this poll");
                }
            } else {
                if (pollVoteRepository.existsByPollAndUser(poll, user)) {
                    throw new IllegalStateException("You have already voted on this poll");
                }
            }

            // Create vote
            PollVote vote = new PollVote();
            vote.setPoll(poll);
            vote.setPollOption(option);
            vote.setUser(poll.getIsAnonymous() ? null : user);
            vote.setIpAddress(ipAddress);

            PollVote savedVote = pollVoteRepository.save(vote);

            // Update counts
            option.incrementVoteCount();
            poll.incrementTotalVotes();

            pollOptionRepository.save(option);
            pollRepository.save(poll);

            logger.info("User {} voted on poll {} for option {}",
                    user.getUsername(), poll.getId(), option.getId());
            return savedVote;

        } catch (PollNotFoundException | UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error voting on poll: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error processing vote for user {} on poll {}: {}",
                    user != null ? user.getUsername() : "null",
                    poll != null ? poll.getId() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to process vote", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void changeVote(Poll poll, PollOption newOption, User user, String ipAddress) {
        try {
            if (poll == null) {
                throw new PollNotFoundException("Poll cannot be null");
            }
            if (newOption == null) {
                throw new IllegalArgumentException("New poll option cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            // Validate option belongs to poll
            if (!newOption.getPoll().getId().equals(poll.getId())) {
                throw new IllegalArgumentException("Option does not belong to the specified poll");
            }

            PollVote existingVote;

            if (poll.getIsAnonymous()) {
                // For anonymous polls, we can't easily change votes
                throw new IllegalStateException("Cannot change vote on anonymous poll");
            } else {
                existingVote = pollVoteRepository.findByPollAndUser(poll, user)
                        .orElseThrow(() -> new IllegalStateException("No existing vote found"));
            }

            // Update counts for old option
            PollOption oldOption = existingVote.getPollOption();
            oldOption.decrementVoteCount();
            pollOptionRepository.save(oldOption);

            // Update vote to new option
            existingVote.setPollOption(newOption);
            pollVoteRepository.save(existingVote);

            // Update counts for new option
            newOption.incrementVoteCount();
            pollOptionRepository.save(newOption);

            logger.info("User {} changed vote on poll {} from option {} to option {}",
                    user.getUsername(), poll.getId(), oldOption.getId(), newOption.getId());

        } catch (PollNotFoundException | UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error changing vote: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error changing vote for user {} on poll {}: {}",
                    user != null ? user.getUsername() : "null",
                    poll != null ? poll.getId() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to change vote", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Poll> getActivePolls(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }
            return pollRepository.findActivePollsOrderByCreatedAtDesc(pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting active polls: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting active polls: {}", e.getMessage());
            throw new RuntimeException("Failed to get active polls", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Poll> getPollsByPlayer(Player player, Pageable pageable) {
        try {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }
            return pollRepository.findByPlayerOrderByCreatedAtDesc(player, pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting polls by player: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting polls for player {}: {}",
                    player != null ? player.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to get polls by player", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Poll> getMostPopularPolls(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }
            return pollRepository.findMostPopularPolls(pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting popular polls: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting most popular polls: {}", e.getMessage());
            throw new RuntimeException("Failed to get most popular polls", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PollOption> getPollOptions(Poll poll) {
        try {
            if (poll == null) {
                throw new PollNotFoundException("Poll cannot be null");
            }
            return pollOptionRepository.findByPollOrderByDisplayOrderAsc(poll);
        } catch (PollNotFoundException e) {
            logger.error("Error getting poll options: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting options for poll {}: {}",
                    poll != null ? poll.getId() : "null", e.getMessage());
            throw new RuntimeException("Failed to get poll options", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public PollOption getWinningOption(Poll poll) {
        try {
            if (poll == null) {
                throw new PollNotFoundException("Poll cannot be null");
            }
            return pollOptionRepository.findWinningOptionByPoll(poll);
        } catch (PollNotFoundException e) {
            logger.error("Error getting winning option: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting winning option for poll {}: {}",
                    poll != null ? poll.getId() : "null", e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean hasUserVoted(Poll poll, User user, String ipAddress) {
        try {
            if (poll == null) {
                throw new PollNotFoundException("Poll cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            if (poll.getIsAnonymous()) {
                return ipAddress != null && pollVoteRepository.existsByPollAndIpAddress(poll, ipAddress);
            } else {
                return pollVoteRepository.existsByPollAndUser(poll, user);
            }
        } catch (PollNotFoundException | UserNotFoundException e) {
            logger.error("Error checking if user voted: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error checking vote status for user {} on poll {}: {}",
                    user != null ? user.getUsername() : "null",
                    poll != null ? poll.getId() : "null",
                    e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<Poll> findById(Long pollId) {
        try {
            if (pollId == null) {
                logger.warn("Attempted to find poll with null ID");
                return Optional.empty();
            }
            return pollRepository.findById(pollId);
        } catch (Exception e) {
            logger.error("Error finding poll by ID {}: {}", pollId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<PollOption> findOptionById(Long optionId) {
        try {
            if (optionId == null) {
                logger.warn("Attempted to find poll option with null ID");
                return Optional.empty();
            }
            return pollOptionRepository.findById(optionId);
        } catch (Exception e) {
            logger.error("Error finding poll option by ID {}: {}", optionId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the IDs of options that a user has voted for in a specific
     * poll.
     */
    @Transactional(readOnly = true)
    public List<Long> getUserVotedOptionIds(Poll poll, User user, String ipAddress) {
        try {
            if (poll == null) {
                throw new PollNotFoundException("Poll cannot be null");
            }
            if (poll.getIsAnonymous()) {
                if (ipAddress == null) {
                    throw new IllegalArgumentException("IP address is required for anonymous polls");
                }
                return pollRepository.findVotedOptionIdsByPollAndIpAddress(poll.getId(), ipAddress);
            } else {
                if (user == null) {
                    throw new UserNotFoundException("User cannot be null");
                }
                return pollRepository.findVotedOptionIdsByPollAndUser(poll.getId(), user.getId());
            }
        } catch (PollNotFoundException | UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting user voted option IDs: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error getting voted option IDs for poll {}: {}", poll != null ? poll.getId() : "null", e.getMessage());
            throw new RuntimeException("Failed to get user voted option IDs", e);
        }
    }

    /**
     * Closes a poll and prevents further voting.
     */
    public void closePoll(Poll poll, User user) {
        try {
            if (poll == null) {
                throw new PollNotFoundException("Poll cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            if (!poll.canBeEditedBy(user)) {
                throw new IllegalStateException("Only poll creator or moderators can close polls");
            }

            poll.setIsActive(false);
            pollRepository.save(poll);

            logger.info("Poll {} closed by user {}", poll.getId(), user.getUsername());

        } catch (PollNotFoundException | UserNotFoundException | IllegalStateException e) {
            logger.error("Error closing poll: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error closing poll {} by user {}: {}",
                    poll != null ? poll.getId() : "null",
                    user != null ? user.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to close poll", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void expirePolls() {
        try {
            // Called by scheduled task to close expired polls
            List<Poll> expiringPolls = pollRepository.findExpiringPollsBefore(LocalDateTime.now());

            int expiredCount = 0;
            for (Poll poll : expiringPolls) {
                try {
                    poll.setIsActive(false);
                    pollRepository.save(poll);
                    expiredCount++;
                } catch (Exception e) {
                    logger.error("Error expiring poll {}: {}", poll.getId(), e.getMessage());
                }
            }

            if (expiredCount > 0) {
                logger.info("Expired {} polls", expiredCount);
            }

        } catch (Exception e) {
            logger.error("Error expiring polls: {}", e.getMessage());
            throw new RuntimeException("Failed to expire polls", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Poll> getPollsByAuthor(User author, Pageable pageable) {
        try {
            if (author == null) {
                throw new UserNotFoundException("Author cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }
            return pollRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Invalid parameters for getting polls by author: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error getting polls for author {}: {}",
                    author != null ? author.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to get polls by author", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Poll> getExpiredPolls(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }
            return pollRepository.findExpiredPollsOrderByCreatedAtDesc(pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting expired polls: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting expired polls: {}", e.getMessage());
            throw new RuntimeException("Failed to get expired polls", e);
        }
    }
}

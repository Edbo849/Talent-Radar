package com.talentradar.service.poll;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.PollNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.poll.Poll;
import com.talentradar.model.poll.PollOption;
import com.talentradar.model.poll.PollVote;
import com.talentradar.model.user.User;
import com.talentradar.repository.poll.PollOptionRepository;
import com.talentradar.repository.poll.PollRepository;
import com.talentradar.repository.poll.PollVoteRepository;

/**
 * Service responsible for managing poll vote operations. Handles voting, vote
 * changes, removal, and vote analytics.
 */
@Service
@Transactional
public class PollVoteService {

    private static final Logger logger = LoggerFactory.getLogger(PollVoteService.class);

    @Autowired
    private PollVoteRepository pollVoteRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollOptionRepository pollOptionRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PollVote voteOnPoll(Long pollId, Long optionId, User user, String ipAddress) {
        try {
            if (pollId == null) {
                throw new PollNotFoundException("Poll ID cannot be null");
            }
            if (optionId == null) {
                throw new IllegalArgumentException("Option ID cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            Poll poll = pollRepository.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            PollOption option = pollOptionRepository.findById(optionId)
                    .orElseThrow(() -> new IllegalArgumentException("Poll option not found with ID: " + optionId));

            // Validate option belongs to poll
            if (!option.getPoll().getId().equals(pollId)) {
                throw new IllegalArgumentException("Option does not belong to the specified poll");
            }

            // Check if poll is still active
            if (poll.isExpired()) {
                throw new IllegalStateException("Cannot vote on expired poll");
            }

            if (!poll.getIsActive()) {
                throw new IllegalStateException("Cannot vote on inactive poll");
            }

            // Check if user already voted on this poll
            Optional<PollVote> existingVote = pollVoteRepository.findByPollAndUser(poll, user);

            if (existingVote.isPresent()) {
                PollVote vote = existingVote.get();
                PollOption oldOption = vote.getPollOption();

                if (oldOption.getId().equals(optionId)) {
                    // Same option - remove vote
                    oldOption.decrementVoteCount();
                    poll.decrementTotalVotes();
                    pollVoteRepository.delete(vote);

                    pollOptionRepository.save(oldOption);
                    pollRepository.save(poll);

                    logger.info("Removed vote from poll {} by user {}", pollId, user.getUsername());
                    return null;
                } else {
                    // Different option - update vote
                    oldOption.decrementVoteCount();
                    option.incrementVoteCount();

                    vote.setPollOption(option);

                    PollVote savedVote = pollVoteRepository.save(vote);
                    pollOptionRepository.save(oldOption);
                    pollOptionRepository.save(option);

                    logger.info("Updated vote on poll {} by user {} to option {}",
                            pollId, user.getUsername(), optionId);
                    return savedVote;
                }
            } else {
                // New vote
                PollVote vote = new PollVote();
                vote.setPoll(poll);
                vote.setPollOption(option);
                vote.setUser(user);
                vote.setIpAddress(ipAddress);

                option.incrementVoteCount();
                poll.incrementTotalVotes();

                PollVote savedVote = pollVoteRepository.save(vote);
                pollOptionRepository.save(option);
                pollRepository.save(poll);

                logger.info("Created new vote on poll {} by user {} for option {}",
                        pollId, user.getUsername(), optionId);
                return savedVote;
            }

        } catch (PollNotFoundException | UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error voting on poll: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error processing vote for user {} on poll {}: {}",
                    user != null ? user.getUsername() : "null", pollId, e.getMessage());
            throw new RuntimeException("Failed to process vote", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void removeVote(Long pollId, User user) {
        try {
            if (pollId == null) {
                throw new PollNotFoundException("Poll ID cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            Poll poll = pollRepository.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            Optional<PollVote> existingVote = pollVoteRepository.findByPollAndUser(poll, user);

            if (existingVote.isPresent()) {
                PollVote vote = existingVote.get();
                PollOption option = vote.getPollOption();

                option.decrementVoteCount();
                poll.decrementTotalVotes();

                pollVoteRepository.delete(vote);
                pollOptionRepository.save(option);
                pollRepository.save(poll);

                logger.info("Removed vote from poll {} by user {}", pollId, user.getUsername());
            } else {
                logger.warn("Attempted to remove non-existent vote from poll {} by user {}",
                        pollId, user.getUsername());
            }

        } catch (PollNotFoundException | UserNotFoundException e) {
            logger.error("Error removing vote: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error removing vote for user {} on poll {}: {}",
                    user != null ? user.getUsername() : "null", pollId, e.getMessage());
            throw new RuntimeException("Failed to remove vote", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean hasUserVoted(Long pollId, User user) {
        try {
            if (pollId == null) {
                throw new PollNotFoundException("Poll ID cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            Poll poll = pollRepository.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            return pollVoteRepository.existsByPollAndUser(poll, user);

        } catch (PollNotFoundException | UserNotFoundException e) {
            logger.error("Error checking if user voted: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error checking vote status for user {} on poll {}: {}",
                    user != null ? user.getUsername() : "null", pollId, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<PollOption> getUserVotedOption(Long pollId, User user) {
        try {
            if (pollId == null) {
                throw new PollNotFoundException("Poll ID cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            Poll poll = pollRepository.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            return pollVoteRepository.findByPollAndUser(poll, user)
                    .map(PollVote::getPollOption);

        } catch (PollNotFoundException | UserNotFoundException e) {
            logger.error("Error getting user voted option: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error getting voted option for user {} on poll {}: {}",
                    user != null ? user.getUsername() : "null", pollId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PollVote> getVotesForPoll(Long pollId) {
        try {
            if (pollId == null) {
                throw new PollNotFoundException("Poll ID cannot be null");
            }

            Poll poll = pollRepository.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            return pollVoteRepository.findByPoll(poll);

        } catch (PollNotFoundException e) {
            logger.error("Error getting votes for poll: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting votes for poll {}: {}", pollId, e.getMessage());
            throw new RuntimeException("Failed to get votes for poll", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PollVote> getVotesByUser(User user) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            return pollVoteRepository.findByUser(user);

        } catch (UserNotFoundException e) {
            logger.error("Error getting votes by user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting votes for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to get votes by user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PollVote changeVote(Long pollId, Long newOptionId, User user) {
        try {
            if (pollId == null) {
                throw new PollNotFoundException("Poll ID cannot be null");
            }
            if (newOptionId == null) {
                throw new IllegalArgumentException("New option ID cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            Poll poll = pollRepository.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            PollOption newOption = pollOptionRepository.findById(newOptionId)
                    .orElseThrow(() -> new IllegalArgumentException("Poll option not found with ID: " + newOptionId));

            // Validate option belongs to poll
            if (!newOption.getPoll().getId().equals(pollId)) {
                throw new IllegalArgumentException("Option does not belong to the specified poll");
            }

            // Check if poll is still active
            if (poll.isExpired() || !poll.getIsActive()) {
                throw new IllegalStateException("Cannot change vote on inactive or expired poll");
            }

            PollVote existingVote = pollVoteRepository.findByPollAndUser(poll, user)
                    .orElseThrow(() -> new IllegalStateException("No existing vote found"));

            PollOption oldOption = existingVote.getPollOption();

            // If same option, just return existing vote
            if (oldOption.getId().equals(newOptionId)) {
                return existingVote;
            }

            // Update counts
            oldOption.decrementVoteCount();
            newOption.incrementVoteCount();

            // Update vote
            existingVote.setPollOption(newOption);

            PollVote savedVote = pollVoteRepository.save(existingVote);
            pollOptionRepository.save(oldOption);
            pollOptionRepository.save(newOption);

            logger.info("Changed vote on poll {} by user {} from option {} to option {}",
                    pollId, user.getUsername(), oldOption.getId(), newOptionId);

            return savedVote;

        } catch (PollNotFoundException | UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error changing vote: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error changing vote for user {} on poll {}: {}",
                    user != null ? user.getUsername() : "null", pollId, e.getMessage());
            throw new RuntimeException("Failed to change vote", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Long getVoteCountForOption(Long optionId) {
        try {
            if (optionId == null) {
                throw new IllegalArgumentException("Option ID cannot be null");
            }

            PollOption option = pollOptionRepository.findById(optionId)
                    .orElseThrow(() -> new IllegalArgumentException("Poll option not found with ID: " + optionId));

            return pollVoteRepository.countByPollOption(option);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting vote count for option: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting vote count for option {}: {}", optionId, e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Long getTotalVoteCountForPoll(Long pollId) {
        try {
            if (pollId == null) {
                throw new PollNotFoundException("Poll ID cannot be null");
            }

            Poll poll = pollRepository.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            return pollVoteRepository.countByPoll(poll);

        } catch (PollNotFoundException e) {
            logger.error("Error getting total vote count for poll: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting total vote count for poll {}: {}", pollId, e.getMessage());
            return 0L;
        }
    }
}

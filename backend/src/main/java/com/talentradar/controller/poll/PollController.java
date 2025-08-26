package com.talentradar.controller.poll;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.poll.PollCreateDTO;
import com.talentradar.dto.poll.PollDTO;
import com.talentradar.dto.poll.PollOptionDTO;
import com.talentradar.dto.poll.PollVoteDTO;
import com.talentradar.exception.PlayerNotFoundException;
import com.talentradar.exception.PollNotFoundException;
import com.talentradar.exception.PollOptionNotFoundException;
import com.talentradar.exception.ThreadNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.enums.PollType;
import com.talentradar.model.player.Player;
import com.talentradar.model.poll.Poll;
import com.talentradar.model.poll.PollOption;
import com.talentradar.model.poll.PollVote;
import com.talentradar.model.user.User;
import com.talentradar.service.discussion.DiscussionThreadService;
import com.talentradar.service.player.PlayerService;
import com.talentradar.service.poll.PollService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for managing polls and voting functionality. Provides
 * endpoints for creating, retrieving, and participating in community polls
 * related to players, teams, transfers, and other football-related topics.
 */
@RestController
@RequestMapping("/api/polls")
@CrossOrigin(origins = "http://localhost:3000")
public class PollController {

    private static final Logger logger = LoggerFactory.getLogger(PollController.class);

    @Autowired
    private PollService pollService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private DiscussionThreadService discussionThreadService;

    /**
     * Retrieves paginated polls with optional filtering.
     */
    @GetMapping
    public ResponseEntity<Page<PollDTO>> getPolls(
            @RequestParam(defaultValue = "active") String filter,
            @RequestParam(required = false) Long playerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Poll> polls;
            User currentUser = userService.getCurrentUserOrNull(request);

            if (playerId != null) {
                Player player = playerService.findById(playerId)
                        .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));
                polls = pollService.getPollsByPlayer(player, pageable);
            } else if ("popular".equals(filter)) {
                polls = pollService.getMostPopularPolls(pageable);
            } else {
                polls = pollService.getActivePolls(pageable);
            }

            Page<PollDTO> pollDTOs = polls.map(poll -> convertToDTO(poll, currentUser, request));
            return ResponseEntity.ok(pollDTOs);

        } catch (PlayerNotFoundException e) {
            logger.error("Player not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving polls: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve polls", e);
        }
    }

    /**
     * Creates a new poll.
     */
    @PostMapping
    public ResponseEntity<PollDTO> createPoll(
            @Valid @RequestBody PollCreateDTO createDTO,
            HttpServletRequest request) {

        try {
            User author = userService.getCurrentUser(request);

            // Get related entities if specified
            DiscussionThread thread = null;
            if (createDTO.getThreadId() != null) {
                thread = discussionThreadService.getThreadById(createDTO.getThreadId())
                        .orElseThrow(() -> new ThreadNotFoundException("Thread not found with ID: " + createDTO.getThreadId()));
            }

            Player player = null;
            if (createDTO.getPlayerId() != null) {
                player = playerService.findById(createDTO.getPlayerId())
                        .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + createDTO.getPlayerId()));
            }

            LocalDateTime expiresAt = null;
            if (createDTO.getExpiresInHours() != null && createDTO.getExpiresInHours() > 0) {
                expiresAt = LocalDateTime.now().plusHours(createDTO.getExpiresInHours());
            }
            Boolean isAnonymous = createDTO.getIsAnonymous();
            Poll poll = pollService.createPoll(
                    author,
                    createDTO.getQuestion(),
                    createDTO.getDescription(),
                    PollType.valueOf(createDTO.getPollType().toUpperCase()),
                    createDTO.getOptions(),
                    thread,
                    player,
                    expiresAt,
                    isAnonymous != null ? isAnonymous : false
            );

            logger.info("Created new poll with ID: {} by user: {}", poll.getId(), author.getUsername());
            return ResponseEntity.ok(convertToDTO(poll, author, request));

        } catch (UserNotFoundException | PlayerNotFoundException | ThreadNotFoundException e) {
            logger.error("Entity not found while creating poll: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid poll type: {}", e.getMessage());
            throw new RuntimeException("Invalid poll type specified", e);
        } catch (Exception e) {
            logger.error("Error creating poll: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create poll", e);
        }
    }

    /**
     * Retrieves a specific poll by ID.
     */
    @GetMapping("/{pollId}")
    public ResponseEntity<PollDTO> getPoll(
            @PathVariable Long pollId,
            HttpServletRequest request) {

        try {
            Poll poll = pollService.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            User currentUser = userService.getCurrentUserOrNull(request);
            return ResponseEntity.ok(convertToDTO(poll, currentUser, request));

        } catch (PollNotFoundException e) {
            logger.error("Poll not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving poll: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve poll", e);
        }
    }

    /**
     * Submits a vote on a poll option.
     */
    @PostMapping("/{pollId}/vote")
    public ResponseEntity<PollVoteDTO> vote(
            @PathVariable Long pollId,
            @RequestParam Long optionId,
            HttpServletRequest request) {

        try {
            Poll poll = pollService.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            PollOption option = pollService.findOptionById(optionId)
                    .orElseThrow(() -> new PollOptionNotFoundException("Poll option not found with ID: " + optionId));

            // Validate option belongs to poll
            if (!option.getPoll().getId().equals(pollId)) {
                throw new IllegalArgumentException("Option does not belong to the specified poll");
            }

            User user = userService.getCurrentUserOrNull(request);
            String ipAddress = getClientIpAddress(request);

            // Check if user has already voted
            if (pollService.hasUserVoted(poll, user, ipAddress)) {
                logger.warn("User {} has already voted on poll {}",
                        user != null ? user.getUsername() : ipAddress, pollId);
                throw new IllegalStateException("You have already voted on this poll");
            }

            PollVote vote = pollService.vote(poll, option, user, ipAddress);

            logger.info("Vote recorded on poll {} by user {} for option {}",
                    pollId, user != null ? user.getUsername() : "anonymous", optionId);

            return ResponseEntity.ok(convertVoteToDTO(vote));

        } catch (PollNotFoundException | PollOptionNotFoundException e) {
            logger.error("Entity not found while voting: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid vote attempt: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving poll results: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve poll results", e);
        }
    }

    /**
     * Retrieves detailed poll results including vote counts and percentages.
     */
    @GetMapping("/{pollId}/results")
    public ResponseEntity<PollDTO> getPollResults(
            @PathVariable Long pollId,
            HttpServletRequest request) {

        try {
            Poll poll = pollService.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            User currentUser = userService.getCurrentUserOrNull(request);
            PollDTO pollDTO = convertToDTO(poll, currentUser, request);

            // Include detailed results with vote counts and percentages
            List<PollOption> options = pollService.getPollOptions(poll);
            List<PollOptionDTO> optionDTOs = options.stream()
                    .map(option -> {
                        PollOptionDTO dto = convertOptionToDTO(option);
                        dto.calculatePercentage(poll.getTotalVotes());
                        return dto;
                    })
                    .toList();

            pollDTO.setOptions(optionDTOs);
            return ResponseEntity.ok(pollDTO);

        } catch (PollNotFoundException e) {
            logger.error("Poll not found for results: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving poll results: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve poll results", e);
        }
    }

    /**
     * Closes an active poll.
     */
    @PostMapping("/{pollId}/close")
    public ResponseEntity<Void> closePoll(
            @PathVariable Long pollId,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);
            Poll poll = pollService.findById(pollId)
                    .orElseThrow(() -> new PollNotFoundException("Poll not found with ID: " + pollId));

            pollService.closePoll(poll, user);

            logger.info("Poll {} closed by user {}", pollId, user.getUsername());
            return ResponseEntity.ok().build();

        } catch (UserNotFoundException | PollNotFoundException e) {
            logger.error("Entity not found while closing poll: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Unauthorised poll closure attempt: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error closing poll: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to close poll", e);
        }
    }

    /**
     * Extracts client IP address from HTTP request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Converts Poll entity to PollDTO for API responses.
     */
    private PollDTO convertToDTO(Poll poll, User currentUser, HttpServletRequest request) {
        PollDTO dto = new PollDTO();
        dto.setId(poll.getId());
        dto.setQuestion(poll.getQuestion());
        dto.setDescription(poll.getDescription());
        dto.setPollType(poll.getPollType().toString());
        dto.setAuthorId(poll.getAuthor().getId());
        dto.setAuthorName(poll.getAuthor().getFullName());
        dto.setAuthorProfileImageUrl(poll.getAuthor().getProfileImageUrl());
        dto.setAuthorBadgeLevel(poll.getAuthor().getBadgeLevel() != null ? poll.getAuthor().getBadgeLevel().toString() : null);
        dto.setThreadId(poll.getThread() != null ? poll.getThread().getId() : null);
        dto.setThreadTitle(poll.getThread() != null ? poll.getThread().getTitle() : null);
        dto.setPlayerId(poll.getPlayer() != null ? poll.getPlayer().getId() : null);
        dto.setPlayerName(poll.getPlayer() != null ? poll.getPlayer().getName() : null);
        dto.setIsAnonymous(poll.getIsAnonymous());
        dto.setIsActive(poll.getIsActive());
        dto.setTotalVotes(poll.getTotalVotes());
        dto.setExpiresAt(poll.getExpiresAt());
        dto.setCreatedAt(poll.getCreatedAt());
        dto.setUpdatedAt(poll.getUpdatedAt());

        if (currentUser != null || poll.getIsAnonymous()) {
            String ipAddress = getClientIpAddress(request);
            dto.setHasUserVoted(pollService.hasUserVoted(poll, currentUser, ipAddress));
            dto.setCanUserVote(poll.canBeVotedBy(currentUser) && !dto.getHasUserVoted());
            dto.setUserVotedOptionIds(pollService.getUserVotedOptionIds(poll, currentUser, ipAddress));
        } else {
            dto.setHasUserVoted(false);
            dto.setCanUserVote(false);
            dto.setUserVotedOptionIds(null);
        }

        if (poll.getOptions() != null) {
            List<PollOptionDTO> optionDTOs = poll.getOptions().stream()
                    .map(option -> {
                        PollOptionDTO optionDTO = convertOptionToDTO(option);
                        optionDTO.setHasUserVoted(dto.getUserVotedOptionIds() != null && dto.getUserVotedOptionIds().contains(option.getId()));
                        optionDTO.calculatePercentage(poll.getTotalVotes());
                        optionDTO.setCreatedAt(option.getCreatedAt());
                        return optionDTO;
                    })
                    .toList();
            dto.setOptions(optionDTOs);
        }

        return dto;
    }

    /**
     * Converts PollOption entity to PollOptionDTO for API responses.
     */
    private PollOptionDTO convertOptionToDTO(PollOption option) {
        PollOptionDTO dto = new PollOptionDTO();
        dto.setId(option.getId());
        dto.setOptionText(option.getOptionText());
        dto.setVoteCount(option.getVoteCount());
        dto.setPercentage(option.getVotePercentage());
        dto.setDisplayOrder(option.getDisplayOrder());
        dto.setCreatedAt(option.getCreatedAt());
        return dto;
    }

    /**
     * Converts PollVote entity to PollVoteDTO for API responses.
     */
    private PollVoteDTO convertVoteToDTO(PollVote vote) {
        PollVoteDTO dto = new PollVoteDTO();
        dto.setId(vote.getId());
        dto.setPollId(vote.getPoll().getId());
        dto.setOptionId(vote.getPollOption().getId());
        dto.setOptionText(vote.getPollOption().getOptionText());
        dto.setUserId(vote.getUser() != null ? vote.getUser().getId() : null);
        dto.setUsername(vote.getUser() != null ? vote.getUser().getUsername() : null);
        dto.setVoterName(vote.getUser() != null ? vote.getUser().getFullName() : null);
        dto.setIpAddress(vote.getIsAnonymous() ? vote.getIpAddress() : null);
        dto.setUserAgent(vote.getUserAgent());
        dto.setIsAnonymous(vote.getIsAnonymous());
        dto.setCreatedAt(vote.getCreatedAt());
        return dto;
    }
}

package com.talentradar.service.player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.dto.player.PlayerRatingDTO;
import com.talentradar.exception.PlayerNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.enums.UserRole;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerRating;
import com.talentradar.model.scouting.RatingCategory;
import com.talentradar.model.user.User;
import com.talentradar.repository.player.PlayerRatingRepository;
import com.talentradar.repository.scouting.RatingCategoryRepository;
import com.talentradar.service.notification.NotificationService;

/**
 * Service responsible for managing player ratings and rating analytics. Handles
 * rating creation, updates, aggregation, and rating-based queries.
 */
@Service
@Transactional
public class PlayerRatingService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerRatingService.class);

    @Autowired
    private PlayerRatingRepository playerRatingRepository;

    @Autowired
    private RatingCategoryRepository ratingCategoryRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PlayerRating saveRating(Player player, User user, RatingCategory category,
            BigDecimal rating, String notes) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            if (category == null) {
                throw new IllegalArgumentException("Rating category cannot be null");
            }
            if (rating == null || rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(BigDecimal.valueOf(10)) > 0) {
                throw new IllegalArgumentException("Rating must be between 0 and 10");
            }

            logger.debug("Saving rating for player {} by user {} in category {}",
                    player.getName(), user.getUsername(), category.getName());

            // Check if user already has a rating for this player and category
            Optional<PlayerRating> existingRating = playerRatingRepository
                    .findByPlayerIdAndUserIdAndCategoryId(player.getId(), user.getId(), category.getId());

            PlayerRating playerRating;
            if (existingRating.isPresent()) {
                // Update existing rating
                playerRating = existingRating.get();
                playerRating.setRating(rating);
                playerRating.setNotes(notes);
                playerRating.setUpdatedAt(LocalDateTime.now());

                logger.info("Updated existing rating with ID: {}", playerRating.getId());
            } else {
                // Create new rating
                playerRating = new PlayerRating();
                playerRating.setPlayer(player);
                playerRating.setUser(user);
                playerRating.setCategory(category);
                playerRating.setRating(rating);
                playerRating.setNotes(notes);
                playerRating.setCreatedAt(LocalDateTime.now());
                playerRating.setUpdatedAt(LocalDateTime.now());

                logger.info("Created new rating for player {} by user {}", player.getName(), user.getUsername());
            }

            return playerRatingRepository.save(playerRating);

        } catch (PlayerNotFoundException | UserNotFoundException | IllegalArgumentException e) {
            logger.error("Invalid parameters for saving rating: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error saving rating for player {} by user {}: {}",
                    player != null ? player.getName() : "null",
                    user != null ? user.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to save rating", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PlayerRating updateRatingByUser(Long ratingId, BigDecimal rating, String notes, User user) {
        try {
            if (ratingId == null) {
                throw new IllegalArgumentException("Rating ID cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            if (rating == null || rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(BigDecimal.valueOf(10)) > 0) {
                throw new IllegalArgumentException("Rating must be between 0 and 10");
            }

            logger.debug("User {} updating rating with ID: {}", user.getUsername(), ratingId);

            PlayerRating playerRating = playerRatingRepository.findById(ratingId)
                    .orElseThrow(() -> new IllegalArgumentException("Rating not found with ID: " + ratingId));

            // Verify the rating belongs to the current user
            if (!playerRating.getUser().getId().equals(user.getId())) {
                throw new IllegalStateException("User can only update their own ratings");
            }

            playerRating.setRating(rating);
            playerRating.setNotes(notes);
            playerRating.setUpdatedAt(LocalDateTime.now());

            PlayerRating savedRating = playerRatingRepository.save(playerRating);

            logger.info("Successfully updated rating with ID: {}", ratingId);
            return savedRating;

        } catch (UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error updating rating: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating rating {} by user {}: {}",
                    ratingId, user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to update rating", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerRatingDTO> getAverageRatingsByPlayer(Long playerId) {
        try {
            if (playerId == null) {
                throw new PlayerNotFoundException("Player ID cannot be null");
            }

            logger.debug("Calculating average ratings for player: {}", playerId);

            // Get all ratings for the player grouped by category
            List<Object[]> averageResults = playerRatingRepository.findAverageRatingsByPlayerIdGroupedByCategory(playerId);

            List<PlayerRatingDTO> averageRatings = averageResults.stream()
                    .map(result -> {
                        PlayerRatingDTO dto = new PlayerRatingDTO();
                        dto.setPlayerId(playerId);
                        dto.setCategoryId((Long) result[0]);
                        dto.setCategoryName((String) result[1]);
                        dto.setRating((BigDecimal) result[2]);
                        return dto;
                    })
                    .collect(Collectors.toList());

            logger.info("Retrieved average ratings for {} categories for player {}", averageRatings.size(), playerId);
            return averageRatings;

        } catch (PlayerNotFoundException e) {
            logger.error("Player not found for getting average ratings: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting average ratings for player {}: {}", playerId, e.getMessage());
            throw new RuntimeException("Failed to get average ratings", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public PlayerRating findByPlayerUserAndCategory(Long playerId, Long userId, Long categoryId) {
        try {
            if (playerId == null || userId == null || categoryId == null) {
                throw new IllegalArgumentException("Player ID, User ID, and Category ID cannot be null");
            }

            logger.debug("Finding rating for player {} by user {} in category {}", playerId, userId, categoryId);

            Optional<PlayerRating> rating = playerRatingRepository.findByPlayerIdAndUserIdAndCategoryId(playerId, userId, categoryId);

            if (rating.isPresent()) {
                logger.debug("Found rating with ID: {}", rating.get().getId());
            } else {
                logger.debug("No rating found for player {} by user {} in category {}", playerId, userId, categoryId);
            }

            return rating.orElse(null);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for finding rating: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding rating for player {} by user {} in category {}: {}",
                    playerId, userId, categoryId, e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PlayerRating createOrUpdateRating(Player player, User user, RatingCategory category,
            BigDecimal rating, String notes, String positionContext) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            if (category == null) {
                throw new IllegalArgumentException("Rating category cannot be null");
            }
            if (rating == null || rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(BigDecimal.valueOf(10)) > 0) {
                throw new IllegalArgumentException("Rating must be between 0 and 10");
            }

            // Check if user has rated this player in this category recently
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            if (playerRatingRepository.hasUserRatedRecentlyInCategory(player, user, category, oneWeekAgo)) {
                throw new IllegalStateException("You can only rate a player in each category once per week");
            }

            // Deactivate any existing rating by this user for this player/category
            Optional<PlayerRating> existingRating = playerRatingRepository
                    .findByPlayerAndUserAndCategoryAndIsActiveTrue(player, user, category);

            if (existingRating.isPresent()) {
                existingRating.get().setIsActive(false);
                playerRatingRepository.save(existingRating.get());
            }

            // Create new rating
            PlayerRating newRating = new PlayerRating(player, user, category, rating);
            newRating.setNotes(notes);
            newRating.setPositionContext(positionContext);

            PlayerRating savedRating = playerRatingRepository.save(newRating);

            // Send notification to followers of the user
            try {
                notificationService.notifyUserFollowersOfNewRating(user, player, category, rating);
            } catch (Exception e) {
                logger.warn("Failed to send rating notification: {}", e.getMessage());
            }

            logger.info("Created rating for player {} by user {} in category {}",
                    player.getName(), user.getUsername(), category.getName());
            return savedRating;

        } catch (PlayerNotFoundException | UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error creating or updating rating: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error creating or updating rating for player {} by user {}: {}",
                    player != null ? player.getName() : "null",
                    user != null ? user.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to create or update rating", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPlayerRatingSummary(Player player) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }

            Map<String, Object> summary = new HashMap<>();
            List<RatingCategory> categories = ratingCategoryRepository.findAll();

            Map<String, BigDecimal> averageRatings = new HashMap<>();
            Map<String, Long> ratingCounts = new HashMap<>();
            Map<String, BigDecimal> expertRatings = new HashMap<>();

            for (RatingCategory category : categories) {
                try {
                    // Overall average
                    Optional<BigDecimal> avgRating = playerRatingRepository
                            .findAverageRatingByPlayerAndCategory(player, category);
                    if (avgRating.isPresent()) {
                        averageRatings.put(category.getName(), avgRating.get().setScale(1, RoundingMode.HALF_UP));
                    }

                    // Rating count
                    Long count = playerRatingRepository.countRatingsByPlayerAndCategory(player, category);
                    ratingCounts.put(category.getName(), count);

                    // Expert ratings (scouts and coaches)
                    Optional<BigDecimal> expertAvg = playerRatingRepository
                            .findAverageRatingByPlayerAndCategoryAndUserRole(player, category, UserRole.SCOUT);
                    if (expertAvg.isPresent()) {
                        expertRatings.put(category.getName(), expertAvg.get().setScale(1, RoundingMode.HALF_UP));
                    }
                } catch (Exception e) {
                    logger.warn("Error processing rating summary for category {}: {}", category.getName(), e.getMessage());
                }
            }

            summary.put("averageRatings", averageRatings);
            summary.put("ratingCounts", ratingCounts);
            summary.put("expertRatings", expertRatings);
            summary.put("totalRatings", averageRatings.values().stream()
                    .filter(rating -> rating.compareTo(BigDecimal.ZERO) > 0)
                    .count());

            logger.info("Generated rating summary for player {}", player.getName());
            return summary;

        } catch (PlayerNotFoundException e) {
            logger.error("Player not found for rating summary: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error generating rating summary for player {}: {}",
                    player != null ? player.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to generate rating summary", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<PlayerRating> findByPlayerId(Long playerId, Pageable pageable) {
        try {
            if (playerId == null) {
                throw new PlayerNotFoundException("Player ID cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            logger.debug("Finding ratings for player {} with pagination", playerId);
            return playerRatingRepository.findByPlayerId(playerId, pageable);

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Invalid parameters for finding ratings by player: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error finding ratings for player {}: {}", playerId, e.getMessage());
            throw new RuntimeException("Failed to find ratings by player", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerRating> getRatingHistory(Player player, RatingCategory category) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }
            if (category == null) {
                throw new IllegalArgumentException("Category cannot be null");
            }

            return playerRatingRepository.findRatingHistoryByPlayerAndCategory(player, category);

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Invalid parameters for getting rating history: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error getting rating history for player {} in category {}: {}",
                    player != null ? player.getName() : "null",
                    category != null ? category.getName() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to get rating history", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerRating> getExpertRatings(Player player, RatingCategory category) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }
            if (category == null) {
                throw new IllegalArgumentException("Category cannot be null");
            }

            return playerRatingRepository.findExpertRatingsByPlayerAndCategory(player, category);

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Invalid parameters for getting expert ratings: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error getting expert ratings for player {} in category {}: {}",
                    player != null ? player.getName() : "null",
                    category != null ? category.getName() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to get expert ratings", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Object[]> getTopRatedPlayers(RatingCategory category, Long minRatings, Pageable pageable) {
        try {
            if (category == null) {
                throw new IllegalArgumentException("Category cannot be null");
            }
            if (minRatings == null || minRatings < 0) {
                throw new IllegalArgumentException("Minimum ratings must be non-negative");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return playerRatingRepository.findTopRatedPlayersByCategory(category, minRatings, pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting top rated players: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting top rated players for category {}: {}",
                    category != null ? category.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to get top rated players", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerRating> getUserRatings(User user) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            return playerRatingRepository.findByUserAndIsActiveTrueOrderByCreatedAtDesc(user);

        } catch (UserNotFoundException e) {
            logger.error("User not found for getting ratings: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting ratings for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to get user ratings", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean canUserRate(Player player, User user, RatingCategory category) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            if (category == null) {
                throw new IllegalArgumentException("Category cannot be null");
            }

            // Check if category is applicable for player's position
            if (category.getPositionSpecific() && !category.isApplicableForPosition(player.getPosition())) {
                return false;
            }

            // Check if user has rated recently
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            return !playerRatingRepository.hasUserRatedRecentlyInCategory(player, user, category, oneWeekAgo);

        } catch (PlayerNotFoundException | UserNotFoundException | IllegalArgumentException e) {
            logger.error("Invalid parameters for checking if user can rate: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error checking if user {} can rate player {} in category {}: {}",
                    user != null ? user.getUsername() : "null",
                    player != null ? player.getName() : "null",
                    category != null ? category.getName() : "null",
                    e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteRating(Long ratingId, User user) {
        try {
            if (ratingId == null) {
                throw new IllegalArgumentException("Rating ID cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            PlayerRating rating = playerRatingRepository.findById(ratingId)
                    .orElseThrow(() -> new IllegalArgumentException("Rating not found"));

            if (!rating.canBeUpdatedBy(user)) {
                throw new IllegalStateException("You can only delete your own ratings");
            }

            rating.setIsActive(false);
            playerRatingRepository.save(rating);

            logger.info("Deleted rating {} by user {}", ratingId, user.getUsername());

        } catch (UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error deleting rating: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deleting rating {} by user {}: {}",
                    ratingId, user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to delete rating", e);
        }
    }
}

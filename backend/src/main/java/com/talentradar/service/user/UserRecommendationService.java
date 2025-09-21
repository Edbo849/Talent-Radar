package com.talentradar.service.user;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.user.User;
import com.talentradar.model.user.UserRecommendation;
import com.talentradar.repository.user.UserRecommendationRepository;
import com.talentradar.repository.user.UserRepository;

/**
 * Service responsible for managing user recommendations. Handles recommendation
 * creation, retrieval, search, and management operations.
 */
@Service
@Transactional
public class UserRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(UserRecommendationService.class);

    @Autowired
    private UserRecommendationRepository recommendationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public UserRecommendation createRecommendation(User recommender, Long recommendedUserId,
            String recommendationText, String skillArea, Boolean isPublic) {
        try {
            if (recommender == null) {
                throw new UserNotFoundException("Recommender cannot be null");
            }
            if (recommendedUserId == null) {
                throw new IllegalArgumentException("Recommended user ID cannot be null");
            }
            if (recommendationText == null || recommendationText.trim().isEmpty()) {
                throw new IllegalArgumentException("Recommendation text cannot be null or empty");
            }

            User recommendedUser = userRepository.findById(recommendedUserId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + recommendedUserId));

            // Prevent self-recommendations
            if (recommender.getId().equals(recommendedUserId)) {
                throw new IllegalArgumentException("Users cannot recommend themselves");
            }

            // Check if recommendation already exists
            if (recommendationRepository.existsByRecommenderAndRecommendedUser(recommender, recommendedUser)) {
                throw new IllegalStateException("Recommendation already exists for this user pair");
            }

            UserRecommendation recommendation = new UserRecommendation();
            recommendation.setRecommender(recommender);
            recommendation.setRecommendedUser(recommendedUser);
            recommendation.setRecommendationText(recommendationText.trim());
            recommendation.setSkillArea(skillArea != null ? skillArea.trim() : null);
            recommendation.setIsPublic(isPublic != null ? isPublic : false);

            UserRecommendation saved = recommendationRepository.save(recommendation);
            logger.info("Created recommendation from {} for {}",
                    recommender.getUsername(), recommendedUser.getUsername());

            return saved;

        } catch (UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error creating recommendation: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error creating recommendation for user {} by {}: {}",
                    recommendedUserId, recommender != null ? recommender.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to create recommendation", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<UserRecommendation> getRecommendationsForUser(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            return recommendationRepository.findByRecommendedUser(user);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting recommendations for user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting recommendations for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get recommendations for user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<UserRecommendation> getPublicRecommendationsForUser(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            return recommendationRepository.findByRecommendedUserAndIsPublicTrue(user);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting public recommendations for user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting public recommendations for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get public recommendations for user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<UserRecommendation> getPublicRecommendationsForUser(Long userId, Pageable pageable) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            return recommendationRepository.findByRecommendedUserAndIsPublicTrue(user, pageable);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting paginated public recommendations for user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting paginated public recommendations for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get paginated public recommendations for user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<UserRecommendation> getRecommendationsByUser(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            return recommendationRepository.findByRecommender(user);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting recommendations by user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting recommendations by user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get recommendations by user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<UserRecommendation> searchRecommendations(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                throw new IllegalArgumentException("Search term cannot be null or empty");
            }

            return recommendationRepository.searchPublicRecommendations(searchTerm.trim());

        } catch (IllegalArgumentException e) {
            logger.error("Error searching recommendations: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error searching recommendations with term '{}': {}", searchTerm, e.getMessage());
            throw new RuntimeException("Failed to search recommendations", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<UserRecommendation> findById(Long id) {
        try {
            if (id == null) {
                logger.warn("Attempted to find recommendation with null ID");
                return Optional.empty();
            }

            return recommendationRepository.findById(id);

        } catch (RuntimeException e) {
            logger.error("Runtime error finding recommendation by ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public UserRecommendation updateRecommendation(Long id, User requester, String recommendationText,
            String skillArea, Boolean isPublic) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Recommendation ID cannot be null");
            }
            if (requester == null) {
                throw new UserNotFoundException("Requester cannot be null");
            }

            UserRecommendation recommendation = recommendationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Recommendation not found with ID: " + id));

            // Only recommender can update
            if (!recommendation.getRecommender().getId().equals(requester.getId())) {
                throw new IllegalStateException("Only the recommender can update this recommendation");
            }

            // Update fields if provided
            if (recommendationText != null) {
                if (recommendationText.trim().isEmpty()) {
                    throw new IllegalArgumentException("Recommendation text cannot be empty");
                }
                recommendation.setRecommendationText(recommendationText.trim());
            }
            if (skillArea != null) {
                recommendation.setSkillArea(skillArea.trim());
            }
            if (isPublic != null) {
                recommendation.setIsPublic(isPublic);
            }

            UserRecommendation saved = recommendationRepository.save(recommendation);
            logger.info("Updated recommendation {} by user {}", id, requester.getUsername());

            return saved;

        } catch (UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error updating recommendation: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error updating recommendation {} by user {}: {}",
                    id, requester != null ? requester.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to update recommendation", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteRecommendation(Long id, User requester) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Recommendation ID cannot be null");
            }
            if (requester == null) {
                throw new UserNotFoundException("Requester cannot be null");
            }

            UserRecommendation recommendation = recommendationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Recommendation not found with ID: " + id));

            // Only recommender or recommended user can delete
            if (!recommendation.getRecommender().getId().equals(requester.getId())
                    && !recommendation.getRecommendedUser().getId().equals(requester.getId())) {
                throw new IllegalStateException("Only the recommender or recommended user can delete this recommendation");
            }

            recommendationRepository.delete(recommendation);
            logger.info("Deleted recommendation with ID: {} by user {}", id, requester.getUsername());

        } catch (UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error deleting recommendation: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error deleting recommendation {} by user {}: {}",
                    id, requester != null ? requester.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to delete recommendation", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getRecommendationCount(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            return recommendationRepository.countByRecommendedUser(user);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting recommendation count: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting recommendation count for user {}: {}", userId, e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getPublicRecommendationCount(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            return recommendationRepository.countByRecommendedUserAndIsPublicTrue(user);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting public recommendation count: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting public recommendation count for user {}: {}", userId, e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<UserRecommendation> getRecommendationsBySkillArea(String skillArea, Pageable pageable) {
        try {
            if (skillArea == null || skillArea.trim().isEmpty()) {
                throw new IllegalArgumentException("Skill area cannot be null or empty");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return recommendationRepository.findBySkillAreaContainingIgnoreCaseAndIsPublicTrue(skillArea.trim(), pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting recommendations by skill area: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting recommendations by skill area '{}': {}", skillArea, e.getMessage());
            throw new RuntimeException("Failed to get recommendations by skill area", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<String> getTopSkillAreas(int limit) {
        try {
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be positive");
            }
            if (limit > 100) {
                throw new IllegalArgumentException("Limit cannot exceed 100");
            }

            return recommendationRepository.findTopSkillAreas(limit);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting top skill areas: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting top skill areas with limit {}: {}", limit, e.getMessage());
            throw new RuntimeException("Failed to get top skill areas", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<UserRecommendation> getRecentPublicRecommendations(int limit) {
        try {
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be positive");
            }
            if (limit > 100) {
                throw new IllegalArgumentException("Limit cannot exceed 100");
            }

            return recommendationRepository.findTopNByIsPublicTrueOrderByCreatedAtDesc(limit);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting recent public recommendations: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting recent public recommendations with limit {}: {}", limit, e.getMessage());
            throw new RuntimeException("Failed to get recent public recommendations", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean hasUserRecommended(User recommender, User recommendedUser) {
        try {
            if (recommender == null) {
                throw new UserNotFoundException("Recommender cannot be null");
            }
            if (recommendedUser == null) {
                throw new UserNotFoundException("Recommended user cannot be null");
            }

            return recommendationRepository.existsByRecommenderAndRecommendedUser(recommender, recommendedUser);

        } catch (UserNotFoundException e) {
            logger.error("Error checking if user has recommended: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error checking recommendation existence between {} and {}: {}",
                    recommender != null ? recommender.getUsername() : "null",
                    recommendedUser != null ? recommendedUser.getUsername() : "null",
                    e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<UserRecommendation> getAllPublicRecommendations(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return recommendationRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting all public recommendations: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting all public recommendations: {}", e.getMessage());
            throw new RuntimeException("Failed to get all public recommendations", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getTotalRecommendationCount() {
        try {
            return recommendationRepository.count();
        } catch (RuntimeException e) {
            logger.error("Runtime error getting total recommendation count: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getTotalPublicRecommendationCount() {
        try {
            return recommendationRepository.countByIsPublicTrue();
        } catch (RuntimeException e) {
            logger.error("Runtime error getting total public recommendation count: {}", e.getMessage());
            return 0L;
        }
    }
}

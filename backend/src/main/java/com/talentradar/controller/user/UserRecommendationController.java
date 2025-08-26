package com.talentradar.controller.user;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.user.UserRecommendationCreateDTO;
import com.talentradar.dto.user.UserRecommendationDTO;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.exception.UserRecommendationNotFoundException;
import com.talentradar.model.user.User;
import com.talentradar.model.user.UserRecommendation;
import com.talentradar.service.user.UserRecommendationService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for managing user recommendations and endorsements. Provides
 * endpoints for creating, retrieving, and managing professional recommendations
 * between users.
 */
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "http://localhost:3000")
public class UserRecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(UserRecommendationController.class);

    @Autowired
    private UserRecommendationService recommendationService;

    @Autowired
    private UserService userService;

    /**
     * Creates a new user recommendation.
     */
    @PostMapping
    public ResponseEntity<UserRecommendationDTO> createRecommendation(
            @Valid @RequestBody UserRecommendationCreateDTO createDTO,
            HttpServletRequest request) {

        try {
            User currentUser = userService.getCurrentUser(request);

            if (currentUser.getId().equals(createDTO.getRecommendedUserId())) {
                throw new IllegalArgumentException("Cannot recommend yourself");
            }

            UserRecommendation recommendation = recommendationService.createRecommendation(
                    currentUser,
                    createDTO.getRecommendedUserId(),
                    createDTO.getRecommendationText(),
                    createDTO.getSkillArea(),
                    createDTO.getIsPublic()
            );

            logger.info("User recommendation created by {} for user ID: {}",
                    currentUser.getUsername(), createDTO.getRecommendedUserId());
            return ResponseEntity.ok(convertToDTO(recommendation));

        } catch (UserNotFoundException e) {
            logger.error("User not found while creating recommendation: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid recommendation data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating user recommendation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create recommendation", e);
        }
    }

    /**
     * Retrieves public recommendations for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserRecommendationDTO>> getUserRecommendations(@PathVariable Long userId) {
        try {
            List<UserRecommendation> recommendations = recommendationService.getPublicRecommendationsForUser(userId);
            List<UserRecommendationDTO> dtos = recommendations.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} recommendations for user ID: {}", recommendations.size(), userId);
            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            logger.error("Error retrieving user recommendations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user recommendations", e);
        }
    }

    /**
     * Retrieves paginated public recommendations for a specific user.
     */
    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<UserRecommendationDTO>> getUserRecommendationsPaginated(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserRecommendation> recommendations = recommendationService.getPublicRecommendationsForUser(userId, pageable);
            Page<UserRecommendationDTO> dtos = recommendations.map(this::convertToDTO);

            logger.info("Retrieved {} paginated recommendations for user ID: {}",
                    recommendations.getNumberOfElements(), userId);
            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            logger.error("Error retrieving paginated user recommendations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve paginated user recommendations", e);
        }
    }

    /**
     * Retrieves recommendations given by the current user.
     */
    @GetMapping("/my-given")
    public ResponseEntity<List<UserRecommendationDTO>> getMyGivenRecommendations(HttpServletRequest request) {
        try {
            User currentUser = userService.getCurrentUser(request);
            List<UserRecommendation> recommendations = recommendationService.getRecommendationsByUser(currentUser.getId());
            List<UserRecommendationDTO> dtos = recommendations.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} given recommendations for user: {}",
                    recommendations.size(), currentUser.getUsername());
            return ResponseEntity.ok(dtos);

        } catch (UserNotFoundException e) {
            logger.error("User not found while retrieving given recommendations: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving given recommendations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve given recommendations", e);
        }
    }

    /**
     * Retrieves recommendations received by the current user.
     */
    @GetMapping("/my-received")
    public ResponseEntity<List<UserRecommendationDTO>> getMyReceivedRecommendations(HttpServletRequest request) {
        try {
            User currentUser = userService.getCurrentUser(request);
            List<UserRecommendation> recommendations = recommendationService.getRecommendationsForUser(currentUser.getId());
            List<UserRecommendationDTO> dtos = recommendations.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} received recommendations for user: {}",
                    recommendations.size(), currentUser.getUsername());
            return ResponseEntity.ok(dtos);

        } catch (UserNotFoundException e) {
            logger.error("User not found while retrieving received recommendations: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving received recommendations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve received recommendations", e);
        }
    }

    /**
     * Searches recommendations based on query parameters.
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserRecommendationDTO>> searchRecommendations(@RequestParam String q) {
        try {
            if (q == null || q.trim().isEmpty()) {
                throw new IllegalArgumentException("Search query is required");
            }

            List<UserRecommendation> recommendations = recommendationService.searchRecommendations(q);
            List<UserRecommendationDTO> dtos = recommendations.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Found {} recommendations for query: {}", recommendations.size(), q);
            return ResponseEntity.ok(dtos);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid search query: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching recommendations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search recommendations", e);
        }
    }

    /**
     * Updates an existing recommendation.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserRecommendationDTO> updateRecommendation(
            @PathVariable Long id,
            @Valid @RequestBody UserRecommendationCreateDTO updateDTO,
            HttpServletRequest request) {

        try {
            User currentUser = userService.getCurrentUser(request);
            UserRecommendation recommendation = recommendationService.updateRecommendation(
                    id,
                    currentUser,
                    updateDTO.getRecommendationText(),
                    updateDTO.getSkillArea(),
                    updateDTO.getIsPublic()
            );

            logger.info("Recommendation updated by user: {}", currentUser.getUsername());
            return ResponseEntity.ok(convertToDTO(recommendation));

        } catch (UserNotFoundException | UserRecommendationNotFoundException e) {
            logger.error("Entity not found while updating recommendation: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid recommendation update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating recommendation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update recommendation", e);
        }
    }

    /**
     * Deletes a recommendation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id, HttpServletRequest request) {
        try {
            User currentUser = userService.getCurrentUser(request);
            recommendationService.deleteRecommendation(id, currentUser);

            logger.info("Recommendation deleted by user: {}", currentUser.getUsername());
            return ResponseEntity.ok().build();

        } catch (UserNotFoundException | UserRecommendationNotFoundException e) {
            logger.error("Entity not found while deleting recommendation: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Unauthorised recommendation deletion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting recommendation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete recommendation", e);
        }
    }

    /**
     * Converts UserRecommendation entity to UserRecommendationDTO.
     */
    private UserRecommendationDTO convertToDTO(UserRecommendation recommendation) {
        UserRecommendationDTO dto = new UserRecommendationDTO();
        dto.setId(recommendation.getId());
        dto.setRecommenderId(recommendation.getRecommender().getId());
        dto.setRecommenderName(recommendation.getRecommender().getDisplayName());
        dto.setRecommendedUserId(recommendation.getRecommendedUser().getId());
        dto.setRecommendedUserName(recommendation.getRecommendedUser().getDisplayName());
        dto.setRecommendationText(recommendation.getRecommendationText());
        dto.setSkillArea(recommendation.getSkillArea());
        dto.setIsPublic(recommendation.getIsPublic());
        dto.setCreatedAt(recommendation.getCreatedAt());
        dto.setUpdatedAt(recommendation.getUpdatedAt());
        return dto;
    }
}

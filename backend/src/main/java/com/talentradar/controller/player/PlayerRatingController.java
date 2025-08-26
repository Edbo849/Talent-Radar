package com.talentradar.controller.player;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

import com.talentradar.dto.player.PlayerRatingCreateDTO;
import com.talentradar.dto.player.PlayerRatingDTO;
import com.talentradar.exception.PlayerNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerRating;
import com.talentradar.model.scouting.RatingCategory;
import com.talentradar.model.user.User;
import com.talentradar.service.player.PlayerRatingService;
import com.talentradar.service.player.PlayerService;
import com.talentradar.service.scouting.RatingCategoryService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for managing player ratings and evaluations. Provides
 * endpoints for creating, retrieving, and managing player ratings across
 * different categories with proper error handling and validation.
 */
@RestController
@RequestMapping("/api/players/{playerId}/ratings")
@CrossOrigin(origins = "http://localhost:3000")
public class PlayerRatingController {

    private static final Logger logger = LoggerFactory.getLogger(PlayerRatingController.class);

    @Autowired
    private PlayerRatingService ratingService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserService userService;

    @Autowired
    private RatingCategoryService categoryService;

    /**
     * Retrieves all ratings for a specific player.
     */
    @GetMapping
    public ResponseEntity<Page<PlayerRatingDTO>> getPlayerRatings(
            @PathVariable Long playerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            HttpServletRequest request) {

        try {
            Pageable pageable = PageRequest.of(page, size);

            logger.debug("Retrieving ratings for player {} (page: {}, size: {}, category: {})",
                    playerId, page, size, categoryId);

            Page<PlayerRating> ratings = ratingService.findByPlayerId(playerId, pageable);
            Page<PlayerRatingDTO> ratingDTOs = ratings.map(this::convertToDTO);

            logger.info("Retrieved {} ratings for player {}", ratingDTOs.getTotalElements(), playerId);
            return ResponseEntity.ok(ratingDTOs);
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when retrieving ratings: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving ratings for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates a new rating for a specific player.
     */
    @PostMapping
    public ResponseEntity<PlayerRatingDTO> createRating(
            @PathVariable Long playerId,
            @Valid @RequestBody PlayerRatingCreateDTO createDTO,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);
            Player player = playerService.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));
            RatingCategory category = categoryService.findById(createDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Rating category not found"));

            logger.info("User {} creating rating for player {} in category {}",
                    user.getUsername(), player.getName(), category.getName());

            PlayerRating rating = ratingService.saveRating(
                    player,
                    user,
                    category,
                    createDTO.getRating(),
                    createDTO.getNotes()
            );

            logger.info("Successfully created rating with ID: {}", rating.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(rating));
        } catch (UserNotFoundException e) {
            logger.warn("User not found when creating rating");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.warn("Invalid data when creating rating: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot create rating: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error creating rating for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates an existing rating.
     */
    @PutMapping("/{ratingId}")
    public ResponseEntity<PlayerRatingDTO> updateRating(
            @PathVariable Long playerId,
            @PathVariable Long ratingId,
            @Valid @RequestBody PlayerRatingCreateDTO updateDTO,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);

            logger.info("User {} updating rating {} for player {}", user.getUsername(), ratingId, playerId);

            PlayerRating rating = ratingService.updateRatingByUser(
                    ratingId,
                    updateDTO.getRating(),
                    updateDTO.getNotes(),
                    user
            );

            logger.info("Successfully updated rating: {}", ratingId);
            return ResponseEntity.ok(convertToDTO(rating));
        } catch (UserNotFoundException e) {
            logger.warn("User not found when updating rating");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot update rating: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error updating rating: {}", ratingId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a specific rating.
     */
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable Long playerId,
            @PathVariable Long ratingId,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);

            logger.info("User {} deleting rating {} for player {}", user.getUsername(), ratingId, playerId);

            ratingService.deleteRating(ratingId, user);

            logger.info("Successfully deleted rating: {}", ratingId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            logger.warn("User not found when deleting rating");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot delete rating: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error deleting rating: {}", ratingId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves average ratings for a player by category.
     */
    @GetMapping("/average")
    public ResponseEntity<List<PlayerRatingDTO>> getAverageRatings(
            @PathVariable Long playerId,
            HttpServletRequest request) {

        try {
            logger.debug("Retrieving average ratings for player: {}", playerId);

            List<PlayerRatingDTO> averageRatings = ratingService.getAverageRatingsByPlayer(playerId);

            logger.info("Retrieved average ratings for {} categories for player {}",
                    averageRatings.size(), playerId);

            return ResponseEntity.ok(averageRatings);
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when retrieving average ratings: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving average ratings for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves user's own rating for a specific player and category.
     */
    @GetMapping("/my-rating")
    public ResponseEntity<PlayerRatingDTO> getUserRating(
            @PathVariable Long playerId,
            @RequestParam Long categoryId,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);

            logger.debug("Retrieving user {} rating for player {} in category {}",
                    user.getUsername(), playerId, categoryId);

            PlayerRating rating = ratingService.findByPlayerUserAndCategory(playerId, user.getId(), categoryId);
            if (rating == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(convertToDTO(rating));
        } catch (UserNotFoundException e) {
            logger.warn("User not found when retrieving user rating");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.error("Error retrieving user rating for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Converts a PlayerRating entity to a PlayerRatingDTO for API responses.
     */
    private PlayerRatingDTO convertToDTO(PlayerRating rating) {
        if (rating == null) {
            return null;
        }

        PlayerRatingDTO dto = new PlayerRatingDTO();
        dto.setId(rating.getId());
        dto.setPlayerId(rating.getPlayer().getId());
        dto.setPlayerName(rating.getPlayer().getName());
        dto.setUserId(rating.getUser().getId());
        dto.setUserName(rating.getUser().getUsername());
        dto.setUserRole(rating.getUser().getRole().toString());
        dto.setCategoryId(rating.getCategory().getId());
        dto.setCategoryName(rating.getCategory().getName());
        dto.setRating(rating.getRating());
        dto.setNotes(rating.getNotes());
        dto.setPositionContext(rating.getPositionContext());
        dto.setMatchContext(rating.getMatchContext());
        dto.setIsActive(rating.getIsActive());
        dto.setCreatedAt(rating.getCreatedAt());
        dto.setUpdatedAt(rating.getUpdatedAt());

        return dto;
    }
}

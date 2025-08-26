package com.talentradar.controller.scouting;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.talentradar.dto.scouting.RatingCategoryCreateDTO;
import com.talentradar.dto.scouting.RatingCategoryDTO;
import com.talentradar.exception.RatingCategoryNotFoundException;
import com.talentradar.model.scouting.RatingCategory;
import com.talentradar.service.scouting.RatingCategoryService;

import jakarta.validation.Valid;

/**
 * REST controller for managing rating categories used in player evaluations.
 * Provides endpoints for creating, retrieving, and managing rating categories
 * for scouting reports.
 */
@RestController
@RequestMapping("/api/rating-categories")
@CrossOrigin(origins = "http://localhost:3000")
public class RatingCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(RatingCategoryController.class);

    @Autowired
    private RatingCategoryService ratingCategoryService;

    /**
     * Retrieves all rating categories.
     */
    @GetMapping
    public ResponseEntity<List<RatingCategoryDTO>> getAllCategories() {
        try {
            List<RatingCategory> categories = ratingCategoryService.findAll();
            List<RatingCategoryDTO> categoryDTOs = categories.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} rating categories", categories.size());
            return ResponseEntity.ok(categoryDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving rating categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve rating categories", e);
        }
    }

    /**
     * Retrieves rating categories applicable to a specific position.
     */
    @GetMapping("/by-position")
    public ResponseEntity<List<RatingCategoryDTO>> getCategoriesByPosition(@RequestParam String position) {
        try {
            if (position == null || position.trim().isEmpty()) {
                throw new IllegalArgumentException("Position parameter is required");
            }

            List<RatingCategory> categories = ratingCategoryService.findCategoriesForPosition(position);
            List<RatingCategoryDTO> categoryDTOs = categories.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} rating categories for position: {}", categories.size(), position);
            return ResponseEntity.ok(categoryDTOs);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid position parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving rating categories by position: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve rating categories by position", e);
        }
    }

    /**
     * Retrieves a specific rating category by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RatingCategoryDTO> getCategory(@PathVariable Long id) {
        try {
            RatingCategory category = ratingCategoryService.findById(id)
                    .orElseThrow(() -> new RatingCategoryNotFoundException("Rating category not found with ID: " + id));

            logger.info("Retrieved rating category with ID: {}", id);
            return ResponseEntity.ok(convertToDTO(category));

        } catch (RatingCategoryNotFoundException e) {
            logger.error("Rating category not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving rating category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve rating category", e);
        }
    }

    /**
     * Creates a new rating category.
     */
    @PostMapping
    public ResponseEntity<RatingCategoryDTO> createCategory(@Valid @RequestBody RatingCategoryCreateDTO createDTO) {
        try {
            RatingCategory category = ratingCategoryService.createCategory(
                    createDTO.getName(),
                    createDTO.getDescription(),
                    Boolean.TRUE.equals(createDTO.getPositionSpecific()),
                    createDTO.getApplicablePositions()
            );

            logger.info("Created new rating category with ID: {} and name: {}", category.getId(), category.getName());
            return ResponseEntity.ok(convertToDTO(category));

        } catch (IllegalArgumentException e) {
            logger.error("Invalid rating category data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating rating category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create rating category", e);
        }
    }

    /**
     * Updates an existing rating category.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RatingCategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody RatingCategoryCreateDTO updateDTO) {

        try {
            RatingCategory category = ratingCategoryService.updateCategory(
                    id,
                    updateDTO.getName(),
                    updateDTO.getDescription(),
                    Boolean.TRUE.equals(updateDTO.getPositionSpecific()),
                    updateDTO.getApplicablePositions()
            );

            logger.info("Updated rating category with ID: {}", id);
            return ResponseEntity.ok(convertToDTO(category));

        } catch (RatingCategoryNotFoundException e) {
            logger.error("Rating category not found for update: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid rating category update data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating rating category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update rating category", e);
        }
    }

    /**
     * Deletes a rating category.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            if (!ratingCategoryService.canDeleteCategory(id)) {
                logger.warn("Cannot delete rating category with ID: {} - category is in use", id);
                throw new IllegalStateException("Cannot delete rating category that is currently in use");
            }

            ratingCategoryService.deleteCategory(id);
            logger.info("Deleted rating category with ID: {}", id);
            return ResponseEntity.ok().build();

        } catch (RatingCategoryNotFoundException e) {
            logger.error("Rating category not found for deletion: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Cannot delete rating category: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting rating category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete rating category", e);
        }
    }

    /**
     * Converts RatingCategory entity to RatingCategoryDTO.
     */
    private RatingCategoryDTO convertToDTO(RatingCategory category) {
        RatingCategoryDTO dto = new RatingCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setPositionSpecific(category.getPositionSpecific());
        dto.setApplicablePositions(category.getApplicablePositions());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
}

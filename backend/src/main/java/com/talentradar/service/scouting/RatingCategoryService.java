package com.talentradar.service.scouting;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.RatingCategoryNotFoundException;
import com.talentradar.model.scouting.RatingCategory;
import com.talentradar.repository.player.PlayerRatingRepository;
import com.talentradar.repository.scouting.RatingCategoryRepository;

/**
 * Service responsible for managing rating categories. Handles rating category
 * CRUD operations, position-specific queries, and validation.
 */
@Service
@Transactional
public class RatingCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(RatingCategoryService.class);

    @Autowired
    private RatingCategoryRepository ratingCategoryRepository;

    @Autowired
    private PlayerRatingRepository playerRatingRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<RatingCategory> findAll() {
        try {
            return ratingCategoryRepository.findAll();
        } catch (Exception e) {
            logger.error("Error finding all rating categories: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve rating categories", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<RatingCategory> findById(Long id) {
        try {
            if (id == null) {
                logger.warn("Attempted to find rating category with null ID");
                return Optional.empty();
            }
            return ratingCategoryRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding rating category by ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<RatingCategory> findByName(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                logger.warn("Attempted to find rating category with null or empty name");
                return Optional.empty();
            }
            return ratingCategoryRepository.findByName(name.trim());
        } catch (Exception e) {
            logger.error("Error finding rating category by name '{}': {}", name, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public RatingCategory save(RatingCategory category) {
        try {
            if (category == null) {
                throw new IllegalArgumentException("Rating category cannot be null");
            }
            return ratingCategoryRepository.save(category);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid rating category data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error saving rating category: {}", e.getMessage());
            throw new RuntimeException("Failed to save rating category", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Rating category ID cannot be null");
            }

            if (!ratingCategoryRepository.existsById(id)) {
                throw new RatingCategoryNotFoundException("Rating category not found with ID: " + id);
            }

            ratingCategoryRepository.deleteById(id);
            logger.info("Deleted rating category with ID: {}", id);

        } catch (RatingCategoryNotFoundException | IllegalArgumentException e) {
            logger.error("Error deleting rating category: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deleting rating category {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete rating category", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<RatingCategory> findCategoriesForPosition(String position) {
        try {
            if (position == null || position.trim().isEmpty()) {
                throw new IllegalArgumentException("Position cannot be null or empty");
            }
            return ratingCategoryRepository.findByPositionSpecificFalseOrApplicablePositionsContaining(position.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid position parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding categories for position '{}': {}", position, e.getMessage());
            throw new RuntimeException("Failed to find categories for position", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<RatingCategory> findUniversalCategories() {
        try {
            return ratingCategoryRepository.findByPositionSpecificFalse();
        } catch (Exception e) {
            logger.error("Error finding universal categories: {}", e.getMessage());
            throw new RuntimeException("Failed to find universal categories", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<RatingCategory> findPositionSpecificCategories() {
        try {
            return ratingCategoryRepository.findByPositionSpecificTrue();
        } catch (Exception e) {
            logger.error("Error finding position-specific categories: {}", e.getMessage());
            throw new RuntimeException("Failed to find position-specific categories", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public RatingCategory createCategory(String name, String description, boolean positionSpecific,
            List<String> applicablePositions) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Category name cannot be null or empty");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Category description cannot be null or empty");
            }

            // Check if category already exists
            if (ratingCategoryRepository.findByName(name.trim()).isPresent()) {
                throw new IllegalArgumentException("Rating category already exists: " + name);
            }

            RatingCategory category = new RatingCategory();
            category.setName(name.trim());
            category.setDescription(description.trim());
            category.setPositionSpecific(positionSpecific);

            if (positionSpecific && applicablePositions != null && !applicablePositions.isEmpty()) {
                // Validate and clean positions
                List<String> cleanedPositions = applicablePositions.stream()
                        .filter(pos -> pos != null && !pos.trim().isEmpty())
                        .map(String::trim)
                        .distinct()
                        .toList();

                if (cleanedPositions.isEmpty()) {
                    throw new IllegalArgumentException("Position-specific categories must have at least one applicable position");
                }

                category.setApplicablePositions(cleanedPositions);
            }

            RatingCategory saved = ratingCategoryRepository.save(category);
            logger.info("Created rating category: {}", saved.getName());

            return saved;

        } catch (IllegalArgumentException e) {
            logger.error("Error creating rating category: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating rating category '{}': {}", name, e.getMessage());
            throw new RuntimeException("Failed to create rating category", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public RatingCategory updateCategory(Long id, String name, String description,
            boolean positionSpecific, List<String> applicablePositions) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Category ID cannot be null");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Category name cannot be null or empty");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Category description cannot be null or empty");
            }

            RatingCategory category = ratingCategoryRepository.findById(id)
                    .orElseThrow(() -> new RatingCategoryNotFoundException("Rating category not found with ID: " + id));

            // Check if name change conflicts with existing category
            if (!category.getName().equals(name.trim())) {
                Optional<RatingCategory> existing = ratingCategoryRepository.findByName(name.trim());
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Rating category name already exists: " + name);
                }
            }

            category.setName(name.trim());
            category.setDescription(description.trim());
            category.setPositionSpecific(positionSpecific);

            if (positionSpecific && applicablePositions != null && !applicablePositions.isEmpty()) {
                // Validate and clean positions
                List<String> cleanedPositions = applicablePositions.stream()
                        .filter(pos -> pos != null && !pos.trim().isEmpty())
                        .map(String::trim)
                        .distinct()
                        .toList();

                if (cleanedPositions.isEmpty()) {
                    throw new IllegalArgumentException("Position-specific categories must have at least one applicable position");
                }

                category.setApplicablePositions(cleanedPositions);
            } else {
                category.setApplicablePositions(List.of());
            }

            RatingCategory saved = ratingCategoryRepository.save(category);
            logger.info("Updated rating category: {}", saved.getName());

            return saved;

        } catch (RatingCategoryNotFoundException | IllegalArgumentException e) {
            logger.error("Error updating rating category: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating rating category {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update rating category", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isCategoryApplicableForPosition(Long categoryId, String position) {
        try {
            if (categoryId == null) {
                throw new IllegalArgumentException("Category ID cannot be null");
            }
            if (position == null || position.trim().isEmpty()) {
                throw new IllegalArgumentException("Position cannot be null or empty");
            }

            RatingCategory category = ratingCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RatingCategoryNotFoundException("Rating category not found with ID: " + categoryId));

            // Universal categories apply to all positions
            if (!category.getPositionSpecific()) {
                return true;
            }

            // Check if position is in applicable positions list
            return category.isApplicableForPosition(position.trim());

        } catch (RatingCategoryNotFoundException | IllegalArgumentException e) {
            logger.error("Error checking category applicability: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error checking if category {} is applicable for position '{}': {}",
                    categoryId, position, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<RatingCategory> getRecommendedCategoriesForPosition(String position) {
        try {
            if (position == null || position.trim().isEmpty()) {
                throw new IllegalArgumentException("Position cannot be null or empty");
            }
            return ratingCategoryRepository.findByPositionSpecificFalseOrApplicablePositionsContaining(position.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid position for recommended categories: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting recommended categories for position '{}': {}", position, e.getMessage());
            throw new RuntimeException("Failed to get recommended categories", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<RatingCategory> searchCategories(String query) {
        try {
            if (query != null && !query.trim().isEmpty()) {
                String cleanQuery = query.trim();
                return ratingCategoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(cleanQuery);
            }
            return ratingCategoryRepository.findAll();
        } catch (Exception e) {
            logger.error("Error searching categories with query '{}': {}", query, e.getMessage());
            throw new RuntimeException("Failed to search categories", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void initializeDefaultCategories() {
        try {
            logger.info("Initializing default rating categories...");

            // Universal categories
            createCategoryIfNotExists("Technical Skills",
                    "Ball control, passing, shooting, dribbling", false, null);

            createCategoryIfNotExists("Physical Attributes",
                    "Speed, strength, stamina, agility", false, null);

            createCategoryIfNotExists("Mental Strength",
                    "Decision making, composure, concentration", false, null);

            createCategoryIfNotExists("Leadership",
                    "Communication, influence, captaincy qualities", false, null);

            createCategoryIfNotExists("Versatility",
                    "Ability to play multiple positions", false, null);

            // Position-specific categories
            createCategoryIfNotExists("Goalkeeping",
                    "Shot stopping, distribution, positioning", true, List.of("GK"));

            createCategoryIfNotExists("Defensive",
                    "Tackling, marking, positioning, aerial ability", true, List.of("DF", "CB", "LB", "RB"));

            createCategoryIfNotExists("Creative",
                    "Vision, key passes, assists, playmaking", true, List.of("MF", "AM", "CM", "FW"));

            createCategoryIfNotExists("Finishing",
                    "Goal scoring ability, composure in box", true, List.of("FW", "ST", "CF"));

            logger.info("Default rating categories initialized successfully");

        } catch (Exception e) {
            logger.error("Error initializing default categories: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize default categories", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private void createCategoryIfNotExists(String name, String description,
            boolean positionSpecific, List<String> positions) {
        try {
            if (!ratingCategoryRepository.findByName(name).isPresent()) {
                createCategory(name, description, positionSpecific, positions);
            }
        } catch (Exception e) {
            logger.warn("Failed to create default category '{}': {}", name, e.getMessage());
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getTotalCategoryCount() {
        try {
            return ratingCategoryRepository.count();
        } catch (Exception e) {
            logger.error("Error getting total category count: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getUniversalCategoryCount() {
        try {
            return ratingCategoryRepository.countByPositionSpecificFalse();
        } catch (Exception e) {
            logger.error("Error getting universal category count: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getPositionSpecificCategoryCount() {
        try {
            return ratingCategoryRepository.countByPositionSpecificTrue();
        } catch (Exception e) {
            logger.error("Error getting position-specific category count: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean canDeleteCategory(Long categoryId) {
        try {
            if (categoryId == null) {
                throw new IllegalArgumentException("Category ID cannot be null");
            }

            // Check if category exists
            if (!ratingCategoryRepository.existsById(categoryId)) {
                return false;
            }

            // Check if category is being used in any player ratings
            return !playerRatingRepository.existsByCategoryId(categoryId);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameter for checking category deletion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error checking if category {} can be deleted: {}", categoryId, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteCategory(Long categoryId) {
        try {
            if (categoryId == null) {
                throw new IllegalArgumentException("Category ID cannot be null");
            }

            RatingCategory category = ratingCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RatingCategoryNotFoundException("Rating category not found with ID: " + categoryId));

            if (!canDeleteCategory(categoryId)) {
                throw new IllegalStateException("Cannot delete category - it is currently in use");
            }

            ratingCategoryRepository.deleteById(categoryId);
            logger.info("Deleted rating category: {}", category.getName());

        } catch (RatingCategoryNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error deleting category: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deleting category {}: {}", categoryId, e.getMessage());
            throw new RuntimeException("Failed to delete category", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public List<RatingCategory> saveAll(List<RatingCategory> categories) {
        try {
            if (categories == null) {
                throw new IllegalArgumentException("Categories list cannot be null");
            }

            // Validate each category
            for (RatingCategory category : categories) {
                if (category == null) {
                    throw new IllegalArgumentException("Category in list cannot be null");
                }
                if (category.getName() == null || category.getName().trim().isEmpty()) {
                    throw new IllegalArgumentException("Category name cannot be null or empty");
                }
            }

            return ratingCategoryRepository.saveAll(categories);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid categories for bulk save: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error saving categories in bulk: {}", e.getMessage());
            throw new RuntimeException("Failed to save categories", e);
        }
    }
}

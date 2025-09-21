package com.talentradar.service.discussion;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.DiscussionCategoryNotFoundException;
import com.talentradar.model.discussion.DiscussionCategory;
import com.talentradar.model.user.User;
import com.talentradar.repository.discussion.DiscussionCategoryRepository;

/**
 * Service layer for managing discussion categories. Provides business logic for
 * category management, permissions, and hierarchical organisation.
 */
@Service
@Transactional
public class DiscussionCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussionCategoryService.class);

    @Autowired
    private DiscussionCategoryRepository categoryRepository;

    /**
     * Retrieves all active discussion categories ordered by display order.
     */
    @Transactional(readOnly = true)
    public List<DiscussionCategory> getAllActiveCategories() {
        try {
            logger.debug("Retrieving all active discussion categories");
            return categoryRepository.findActiveCategoriesOrdered();
        } catch (RuntimeException e) {
            logger.error("Error retrieving active categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active categories", e);
        }
    }

    /**
     * Retrieves all discussion categories regardless of status.
     */
    @Transactional(readOnly = true)
    public List<DiscussionCategory> getAllCategories() {
        try {
            logger.debug("Retrieving all discussion categories");
            return categoryRepository.findAllByOrderByDisplayOrderAscNameAsc();
        } catch (RuntimeException e) {
            logger.error("Error retrieving all categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve categories", e);
        }
    }

    /**
     * Retrieves a specific discussion category by its unique identifier.
     */
    @Transactional(readOnly = true)
    public Optional<DiscussionCategory> getCategoryById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Category ID cannot be null");
            }
            logger.debug("Finding discussion category by ID: {}", id);
            return categoryRepository.findById(id);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid category ID: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error finding category by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to find category by ID", e);
        }
    }

    /**
     * Retrieves a discussion category by its name.
     */
    @Transactional(readOnly = true)
    public Optional<DiscussionCategory> getCategoryByName(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Category name cannot be null or empty");
            }
            logger.debug("Finding discussion category by name: {}", name);
            return categoryRepository.findByNameIgnoreCase(name.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid category name: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error finding category by name: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find category by name", e);
        }
    }

    /**
     * Creates a new discussion category with the specified details.
     */
    public DiscussionCategory createCategory(String name, String description, String color,
            String icon, Integer displayOrder, User creator) {

        try {
            if (creator == null) {
                throw new IllegalArgumentException("Creator cannot be null");
            }
            if (!creator.canModerate()) {
                throw new IllegalStateException("Only moderators can create categories");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Category name is required");
            }

            String trimmedName = name.trim();

            // Check if category name already exists
            if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
                throw new IllegalArgumentException("Category with name '" + trimmedName + "' already exists");
            }

            DiscussionCategory category = new DiscussionCategory();
            category.setName(trimmedName);
            category.setDescription(description);
            category.setColor(color);
            category.setIcon(icon);
            category.setDisplayOrder(displayOrder != null ? displayOrder : getNextDisplayOrder());
            category.setIsActive(true);

            DiscussionCategory savedCategory = categoryRepository.save(category);
            logger.info("Created new discussion category: {} by user: {}", trimmedName, creator.getUsername());
            return savedCategory;

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid category creation parameters: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error creating discussion category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create discussion category", e);
        }
    }

    /**
     * Updates an existing discussion category with new information.
     */
    public DiscussionCategory updateCategory(Long id, String name, String description, String color,
            String icon, Integer displayOrder, Boolean isActive, User requester) {

        try {
            if (requester == null) {
                throw new IllegalArgumentException("Requester cannot be null");
            }
            if (!requester.canModerate()) {
                throw new IllegalStateException("Only moderators can update categories");
            }

            DiscussionCategory category = categoryRepository.findById(id)
                    .orElseThrow(() -> new DiscussionCategoryNotFoundException("Category not found with ID: " + id));

            // Check for name conflicts if name is being changed
            if (name != null && !name.trim().isEmpty()) {
                String trimmedName = name.trim();
                if (!category.getName().equals(trimmedName) && categoryRepository.existsByNameIgnoreCase(trimmedName)) {
                    throw new IllegalArgumentException("Category with name '" + trimmedName + "' already exists");
                }
                category.setName(trimmedName);
            }

            if (description != null) {
                category.setDescription(description);
            }
            if (color != null) {
                category.setColor(color);
            }
            if (icon != null) {
                category.setIcon(icon);
            }
            if (displayOrder != null) {
                category.setDisplayOrder(displayOrder);
            }
            if (isActive != null) {
                category.setIsActive(isActive);
            }

            DiscussionCategory updatedCategory = categoryRepository.save(category);
            logger.info("Updated discussion category with ID: {} by user: {}", id, requester.getUsername());
            return updatedCategory;

        } catch (DiscussionCategoryNotFoundException e) {
            logger.error("Category not found for update: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid category update parameters: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating discussion category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update discussion category", e);
        }
    }

    /**
     * Deletes a discussion category by its unique identifier.
     */
    public void deleteCategory(Long id, User requester) {
        try {
            if (requester == null) {
                throw new IllegalArgumentException("Requester cannot be null");
            }
            if (!requester.canModerate()) {
                throw new IllegalStateException("Only moderators can delete categories");
            }

            DiscussionCategory category = categoryRepository.findById(id)
                    .orElseThrow(() -> new DiscussionCategoryNotFoundException("Category not found with ID: " + id));

            // Check if category has threads
            long threadCount = categoryRepository.countThreadsByCategory(category);
            if (threadCount > 0) {
                throw new IllegalStateException("Cannot delete category: " + threadCount + " threads are associated with it");
            }

            categoryRepository.delete(category);
            logger.info("Deleted discussion category with ID: {} by user: {}", id, requester.getUsername());

        } catch (DiscussionCategoryNotFoundException e) {
            logger.error("Category not found for deletion: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Cannot delete category: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deleting discussion category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete discussion category", e);
        }
    }

    /**
     * Deactivates a discussion category without deleting it.
     */
    public void deactivateCategory(Long id, User requester) {
        try {
            if (requester == null) {
                throw new IllegalArgumentException("Requester cannot be null");
            }
            if (!requester.canModerate()) {
                throw new IllegalStateException("Only moderators can deactivate categories");
            }

            DiscussionCategory category = categoryRepository.findById(id)
                    .orElseThrow(() -> new DiscussionCategoryNotFoundException("Category not found with ID: " + id));

            category.setIsActive(false);
            categoryRepository.save(category);
            logger.info("Deactivated discussion category with ID: {} by user: {}", id, requester.getUsername());

        } catch (DiscussionCategoryNotFoundException e) {
            logger.error("Category not found for deactivation: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Cannot deactivate category: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deactivating discussion category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deactivate discussion category", e);
        }
    }

    /**
     * Reorders discussion categories based on provided list of category IDs.
     */
    public void reorderCategories(List<Long> categoryIds, User requester) {
        try {
            if (requester == null) {
                throw new IllegalArgumentException("Requester cannot be null");
            }
            if (!requester.canModerate()) {
                throw new IllegalStateException("Only moderators can reorder categories");
            }
            if (categoryIds == null || categoryIds.isEmpty()) {
                throw new IllegalArgumentException("Category IDs list cannot be null or empty");
            }

            for (int i = 0; i < categoryIds.size(); i++) {
                Long categoryId = categoryIds.get(i);
                DiscussionCategory category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new DiscussionCategoryNotFoundException("Category not found with ID: " + categoryId));

                category.setDisplayOrder(i + 1);
                categoryRepository.save(category);
            }

            logger.info("Reordered {} discussion categories by user: {}", categoryIds.size(), requester.getUsername());

        } catch (DiscussionCategoryNotFoundException e) {
            logger.error("Category not found during reordering: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid reorder parameters: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error reordering discussion categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reorder discussion categories", e);
        }
    }

    /**
     * Retrieves the number of threads in a specific category.
     */
    @Transactional(readOnly = true)
    public long getThreadCountByCategory(Long categoryId) {
        try {
            if (categoryId == null) {
                throw new IllegalArgumentException("Category ID cannot be null");
            }

            DiscussionCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new DiscussionCategoryNotFoundException("Category not found with ID: " + categoryId));

            return categoryRepository.countThreadsByCategory(category);

        } catch (DiscussionCategoryNotFoundException e) {
            logger.error("Category not found for thread count: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid category ID: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error counting threads by category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to count threads by category", e);
        }
    }

    /**
     * Retrieves discussion categories filtered by color.
     */
    @Transactional(readOnly = true)
    public List<DiscussionCategory> getCategoriesByColor(String color) {
        try {
            if (color == null || color.trim().isEmpty()) {
                throw new IllegalArgumentException("Color cannot be null or empty");
            }
            logger.debug("Retrieving categories by color: {}", color);
            return categoryRepository.findByColorOrderByDisplayOrderAsc(color.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid color parameter: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving categories by color: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve categories by color", e);
        }
    }

    /**
     * Calculates the next available display order for new categories.
     */
    private Integer getNextDisplayOrder() {
        try {
            Integer maxOrder = categoryRepository.findMaxDisplayOrder();
            return maxOrder != null ? maxOrder + 1 : 1;
        } catch (RuntimeException e) {
            logger.error("Error calculating next display order: {}", e.getMessage(), e);
            return 1; // Default fallback
        }
    }
}

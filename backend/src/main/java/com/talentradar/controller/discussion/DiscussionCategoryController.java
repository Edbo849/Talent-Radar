package com.talentradar.controller.discussion;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.discussion.DiscussionCategoryDTO;
import com.talentradar.model.discussion.DiscussionCategory;
import com.talentradar.model.user.User;
import com.talentradar.service.discussion.DiscussionCategoryService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

/**
 * REST controller for managing discussion categories. Provides endpoints for
 * creating, retrieving, and managing forum categories.
 */
@RestController
@RequestMapping("/api/discussions/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class DiscussionCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(DiscussionCategoryController.class);

    @Autowired
    private DiscussionCategoryService categoryService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves all active discussion categories.
     */
    @GetMapping
    public ResponseEntity<List<DiscussionCategoryDTO>> getAllActiveCategories() {
        try {
            List<DiscussionCategory> categories = categoryService.getAllActiveCategories();
            List<DiscussionCategoryDTO> categoryDTOs = categories.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Retrieved {} active categories", categories.size());
            return ResponseEntity.ok(categoryDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving active categories", e);
            throw new RuntimeException("Failed to retrieve categories: " + e.getMessage());
        }
    }

    /**
     * Retrieves all discussion categories including inactive ones.
     */
    @GetMapping("/all")
    public ResponseEntity<List<DiscussionCategoryDTO>> getAllCategories() {
        try {
            List<DiscussionCategory> categories = categoryService.getAllCategories();
            List<DiscussionCategoryDTO> categoryDTOs = categories.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Retrieved {} total categories", categories.size());
            return ResponseEntity.ok(categoryDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving all categories", e);
            throw new RuntimeException("Failed to retrieve all categories: " + e.getMessage());
        }
    }

    /**
     * Retrieves a specific category by its unique identifier.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiscussionCategoryDTO> getCategoryById(@PathVariable @Positive Long id) {
        try {
            DiscussionCategory category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

            logger.debug("Retrieved category: {} (ID: {})", category.getName(), id);
            return ResponseEntity.ok(convertToDTO(category));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Category not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving category with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve category: " + e.getMessage());
        }
    }

    /**
     * Retrieves a specific category by its name.
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<DiscussionCategoryDTO> getCategoryByName(@PathVariable String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Category name cannot be empty");
            }

            DiscussionCategory category = categoryService.getCategoryByName(name.trim())
                    .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));

            logger.debug("Retrieved category by name: {}", name);
            return ResponseEntity.ok(convertToDTO(category));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid category name parameter: {}", e.getMessage());
            throw new RuntimeException("Invalid category name: " + e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Category not found with name: {}", name);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving category with name: {}", name, e);
            throw new RuntimeException("Failed to retrieve category: " + e.getMessage());
        }
    }

    /**
     * Creates a new discussion category.
     */
    @PostMapping
    public ResponseEntity<DiscussionCategoryDTO> createCategory(
            @Valid @RequestBody CategoryCreateRequest request,
            HttpServletRequest httpRequest) {

        try {
            User creator = userService.getCurrentUser(httpRequest);

            DiscussionCategory category = categoryService.createCategory(
                    request.getName(),
                    request.getDescription(),
                    request.getColor(),
                    request.getIcon(),
                    request.getDisplayOrder(),
                    creator
            );

            logger.info("User {} created category: {} (ID: {})",
                    creator.getUsername(), category.getName(), category.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(category));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid category creation request: {}", e.getMessage());
            throw new RuntimeException("Invalid category data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating category", e);
            throw new RuntimeException("Failed to create category: " + e.getMessage());
        }
    }

    /**
     * Updates an existing discussion category.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiscussionCategoryDTO> updateCategory(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CategoryUpdateRequest request,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);

            DiscussionCategory category = categoryService.updateCategory(
                    id,
                    request.getName(),
                    request.getDescription(),
                    request.getColor(),
                    request.getIcon(),
                    request.getDisplayOrder(),
                    request.getIsActive(),
                    requester
            );

            logger.info("User {} updated category: {} (ID: {})",
                    requester.getUsername(), category.getName(), id);
            return ResponseEntity.ok(convertToDTO(category));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Category not found for update with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to update category with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error updating category with ID: {}", id, e);
            throw new RuntimeException("Failed to update category: " + e.getMessage());
        }
    }

    /**
     * Deletes a discussion category.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable @Positive Long id,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);
            categoryService.deleteCategory(id, requester);

            logger.info("User {} deleted category with ID: {}", requester.getUsername(), id);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Category not found for deletion with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to delete category with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (e.getMessage().contains("Cannot delete")) {
                logger.warn("Cannot delete category with ID: {} - has dependencies", id);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting category with ID: {}", id, e);
            throw new RuntimeException("Failed to delete category: " + e.getMessage());
        }
    }

    /**
     * Deactivates a discussion category instead of deleting it.
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCategory(
            @PathVariable @Positive Long id,
            HttpServletRequest httpRequest) {

        try {
            User requester = userService.getCurrentUser(httpRequest);
            categoryService.deactivateCategory(id, requester);

            logger.info("User {} deactivated category with ID: {}", requester.getUsername(), id);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Category not found for deactivation with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to deactivate category with ID: {}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error deactivating category with ID: {}", id, e);
            throw new RuntimeException("Failed to deactivate category: " + e.getMessage());
        }
    }

    /**
     * Reorders multiple categories based on provided category IDs.
     */
    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderCategories(
            @RequestBody List<Long> categoryIds,
            HttpServletRequest httpRequest) {

        try {
            if (categoryIds == null || categoryIds.isEmpty()) {
                throw new IllegalArgumentException("Category IDs list cannot be empty");
            }

            User requester = userService.getCurrentUser(httpRequest);
            categoryService.reorderCategories(categoryIds, requester);

            logger.info("User {} reordered {} categories", requester.getUsername(), categoryIds.size());
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid reorder request: {}", e.getMessage());
            throw new RuntimeException("Invalid reorder data: " + e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authorised") || e.getMessage().contains("permission")) {
                logger.warn("User not authorised to reorder categories");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error reordering categories", e);
            throw new RuntimeException("Failed to reorder categories: " + e.getMessage());
        }
    }

    /**
     * Retrieves the number of threads in a specific category.
     */
    @GetMapping("/{id}/thread-count")
    public ResponseEntity<Long> getThreadCount(@PathVariable @Positive Long id) {
        try {
            long threadCount = categoryService.getThreadCountByCategory(id);
            logger.debug("Category ID {} has {} threads", id, threadCount);
            return ResponseEntity.ok(threadCount);

        } catch (Exception e) {
            logger.error("Error retrieving thread count for category ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve thread count: " + e.getMessage());
        }
    }

    /**
     * Retrieves all categories with a specific color.
     */
    @GetMapping("/color/{color}")
    public ResponseEntity<List<DiscussionCategoryDTO>> getCategoriesByColor(@PathVariable String color) {
        try {
            if (color == null || color.trim().isEmpty()) {
                throw new IllegalArgumentException("Color parameter cannot be empty");
            }

            List<DiscussionCategory> categories = categoryService.getCategoriesByColor(color.trim());
            List<DiscussionCategoryDTO> categoryDTOs = categories.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Retrieved {} categories with color: {}", categories.size(), color);
            return ResponseEntity.ok(categoryDTOs);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid color parameter: {}", e.getMessage());
            throw new RuntimeException("Invalid color: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving categories by color: {}", color, e);
            throw new RuntimeException("Failed to retrieve categories by color: " + e.getMessage());
        }
    }

    /**
     * Converts a DiscussionCategory entity to a DiscussionCategoryDTO for API
     * responses.
     */
    private DiscussionCategoryDTO convertToDTO(DiscussionCategory category) {
        DiscussionCategoryDTO dto = new DiscussionCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setColor(category.getColor());
        dto.setIcon(category.getIcon());
        dto.setDisplayOrder(category.getDisplayOrder());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());

        // Get thread count
        try {
            long threadCount = categoryService.getThreadCountByCategory(category.getId());
            dto.setThreadCount((int) threadCount);
        } catch (Exception e) {
            logger.warn("Failed to get thread count for category {}: {}", category.getId(), e.getMessage());
            dto.setThreadCount(0);
        }

        return dto;
    }

    // Request DTOs
    public static class CategoryCreateRequest {

        private String name;
        private String description;
        private String color;
        private String icon;
        private Integer displayOrder;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public Integer getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }
    }

    public static class CategoryUpdateRequest {

        private String name;
        private String description;
        private String color;
        private String icon;
        private Integer displayOrder;
        private Boolean isActive;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public Integer getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }
}

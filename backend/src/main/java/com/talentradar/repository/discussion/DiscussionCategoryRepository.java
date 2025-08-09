package com.talentradar.repository.discussion;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.discussion.DiscussionCategory;

/**
 * Repository interface for managing DiscussionCategory entities. Provides data
 * access operations for category management, display ordering, search
 * functionality, and statistical operations.
 */
@Repository
public interface DiscussionCategoryRepository extends JpaRepository<DiscussionCategory, Long> {

    /* Basic finder methods */
    // Find category by name
    Optional<DiscussionCategory> findByName(String name);

    // Find category by name (case-insensitive)
    Optional<DiscussionCategory> findByNameIgnoreCase(String name);

    // Find category by display order position
    Optional<DiscussionCategory> findByDisplayOrder(Integer displayOrder);

    /* Active category finder methods */
    // Find only active categories
    List<DiscussionCategory> findByIsActiveTrue();

    // Find active categories ordered by display order
    List<DiscussionCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    // Find active categories ordered by display order and name
    List<DiscussionCategory> findByIsActiveTrueOrderByDisplayOrderAscNameAsc();

    // Find categories by active status ordered by display order and name
    List<DiscussionCategory> findByIsActiveOrderByDisplayOrderAscNameAsc(Boolean isActive);

    /* Ordered finder methods */
    // Find all categories ordered by display order
    List<DiscussionCategory> findAllByOrderByDisplayOrderAsc();

    // Find all categories ordered by display order and name
    List<DiscussionCategory> findAllByOrderByDisplayOrderAscNameAsc();

    // Find active categories ordered (custom query)
    @Query("SELECT dc FROM DiscussionCategory dc WHERE dc.isActive = true ORDER BY dc.displayOrder ASC, dc.name ASC")
    List<DiscussionCategory> findActiveCategoriesOrdered();

    /* Display order management methods */
    // Find categories by display order range
    List<DiscussionCategory> findByDisplayOrderBetween(Integer start, Integer end);

    // Find maximum display order value
    @Query("SELECT MAX(dc.displayOrder) FROM DiscussionCategory dc")
    Integer findMaxDisplayOrder();

    /* Color and icon finder methods */
    // Find categories by color
    List<DiscussionCategory> findByColor(String color);

    // Find categories by color ordered by display order
    List<DiscussionCategory> findByColorOrderByDisplayOrderAsc(String color);

    // Find categories by icon
    List<DiscussionCategory> findByIcon(String icon);

    /* Search methods */
    // Search categories by name or description
    @Query("SELECT dc FROM DiscussionCategory dc WHERE LOWER(dc.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(dc.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<DiscussionCategory> searchCategories(@Param("query") String query);

    /* Validation methods */
    // Check if category exists by name
    boolean existsByName(String name);

    // Check if category exists by name (case-insensitive)
    boolean existsByNameIgnoreCase(String name);

    // Check if category name exists for different ID
    boolean existsByNameAndIdNot(String name, Long id);

    /* Statistical and aggregation methods */
    // Find categories with thread count
    @Query("SELECT dc, COUNT(dt) FROM DiscussionCategory dc LEFT JOIN DiscussionThread dt ON dt.category = dc GROUP BY dc ORDER BY COUNT(dt) DESC")
    List<Object[]> findCategoriesWithThreadCount();

    // Find active categories with thread count
    @Query("SELECT dc, COUNT(dt) FROM DiscussionCategory dc LEFT JOIN DiscussionThread dt ON dt.category = dc WHERE dc.isActive = true GROUP BY dc ORDER BY dc.displayOrder ASC")
    List<Object[]> findActiveCategoriesWithThreadCount();

    // Find most popular categories by thread count
    @Query("SELECT dc FROM DiscussionCategory dc JOIN DiscussionThread dt ON dt.category = dc WHERE dc.isActive = true GROUP BY dc ORDER BY COUNT(dt) DESC")
    List<DiscussionCategory> findMostPopularCategories();

    // Count threads associated with a category
    @Query("SELECT COUNT(dt) FROM DiscussionThread dt WHERE dt.category = :category")
    long countThreadsByCategory(@Param("category") DiscussionCategory category);

    /* Count methods */
    // Count active categories
    long countByIsActiveTrue();

    // Count inactive categories
    long countByIsActiveFalse();

    /* Special category finder methods */
    // Find categories with no threads
    @Query("SELECT dc FROM DiscussionCategory dc WHERE dc.isActive = true AND NOT EXISTS (SELECT dt FROM DiscussionThread dt WHERE dt.category = dc)")
    List<DiscussionCategory> findEmptyCategories();

    // Find categories with recent activity
    @Query("SELECT DISTINCT dc FROM DiscussionCategory dc JOIN DiscussionThread dt ON dt.category = dc WHERE dc.isActive = true AND dt.lastActivityAt >= :since ORDER BY dc.displayOrder ASC")
    List<DiscussionCategory> findCategoriesWithRecentActivity(@Param("since") java.time.LocalDateTime since);
}

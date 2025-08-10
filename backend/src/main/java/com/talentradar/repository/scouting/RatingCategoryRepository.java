package com.talentradar.repository.scouting;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.scouting.RatingCategory;

/**
 * Repository interface for managing RatingCategory entities. Provides data
 * access operations for rating category management, position-specific
 * filtering, search functionality, and validation.
 */
@Repository
public interface RatingCategoryRepository extends JpaRepository<RatingCategory, Long> {

    /* Basic finder methods */
    // Find category by name (unique constraint)
    Optional<RatingCategory> findByName(String name);

    // Find categories ordered by name
    List<RatingCategory> findAllByOrderByNameAsc();

    /* Position-specific finder methods */
    // Find all universal categories (not position-specific)
    List<RatingCategory> findByPositionSpecificFalse();

    // Find all position-specific categories
    List<RatingCategory> findByPositionSpecificTrue();

    // Find categories applicable for a specific position
    @Query("SELECT rc FROM RatingCategory rc WHERE rc.positionSpecific = false OR rc.applicablePositionsJson LIKE %:position%")
    List<RatingCategory> findByPositionSpecificFalseOrApplicablePositionsContaining(@Param("position") String position);

    /* Search methods */
    // Search categories by name or description
    @Query("SELECT rc FROM RatingCategory rc WHERE LOWER(rc.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(rc.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<RatingCategory> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(@Param("query") String query);

    // Find categories by name pattern
    @Query("SELECT rc FROM RatingCategory rc WHERE LOWER(rc.name) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<RatingCategory> findByNamePattern(@Param("pattern") String pattern);

    /* Validation methods */
    // Check if category name exists (for validation)
    boolean existsByName(String name);

    /* Count methods */
    // Count categories by type
    long countByPositionSpecificFalse();

    // Count position-specific categories
    long countByPositionSpecificTrue();
}

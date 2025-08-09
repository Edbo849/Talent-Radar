package com.talentradar.repository.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.user.User;
import com.talentradar.model.user.UserRecommendation;

/**
 * Repository interface for managing UserRecommendation entities. Provides data
 * access operations for user recommendations, skill endorsements, professional
 * networking, and reputation building.
 */
@Repository
public interface UserRecommendationRepository extends JpaRepository<UserRecommendation, Long> {

    /* Basic recommendation queries */
    // Get all recommendations for a user (received recommendations)
    List<UserRecommendation> findByRecommendedUser(User recommendedUser);

    // Get all recommendations made by a user (given recommendations)
    List<UserRecommendation> findByRecommender(User recommender);

    // Get public recommendations for a user
    List<UserRecommendation> findByRecommendedUserAndIsPublicTrue(User recommendedUser);

    // Get public recommendations for a user with pagination
    Page<UserRecommendation> findByRecommendedUserAndIsPublicTrue(User recommendedUser, Pageable pageable);

    /* Existence and validation checks */
    // Check if a recommendation exists between two users
    boolean existsByRecommenderAndRecommendedUser(User recommender, User recommendedUser);

    /* Skill area queries */
    // Get recommendations by skill area (case-insensitive)
    List<UserRecommendation> findBySkillAreaContainingIgnoreCase(String skillArea);

    // Get public recommendations by skill area with pagination
    Page<UserRecommendation> findBySkillAreaContainingIgnoreCaseAndIsPublicTrue(String skillArea, Pageable pageable);

    // Get top skill areas across all recommendations
    @Query("SELECT ur.skillArea FROM UserRecommendation ur WHERE ur.isPublic = true GROUP BY ur.skillArea ORDER BY COUNT(ur.skillArea) DESC")
    List<String> findTopSkillAreas(@Param("limit") int limit);

    /* Public recommendation queries */
    // Get all public recommendations with pagination
    Page<UserRecommendation> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    // Get recent public recommendations (limited)
    @Query("SELECT ur FROM UserRecommendation ur WHERE ur.isPublic = true ORDER BY ur.createdAt DESC")
    List<UserRecommendation> findTopNByIsPublicTrueOrderByCreatedAtDesc(@Param("limit") int limit);

    /* Search functionality */
    // Search public recommendations by text or skill area
    @Query("SELECT ur FROM UserRecommendation ur WHERE ur.isPublic = true "
            + "AND (LOWER(ur.recommendationText) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
            + "OR LOWER(ur.skillArea) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<UserRecommendation> searchPublicRecommendations(@Param("searchTerm") String searchTerm);

    /* Count methods */
    // Count all recommendations for a user
    long countByRecommendedUser(User recommendedUser);

    // Count public recommendations for a user
    long countByRecommendedUserAndIsPublicTrue(User recommendedUser);

    // Count all public recommendations
    long countByIsPublicTrue();
}

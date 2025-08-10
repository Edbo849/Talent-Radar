package com.talentradar.repository.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.enums.BadgeLevel;
import com.talentradar.model.enums.UserRole;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing User entities. Provides data access
 * operations for user management, authentication, role-based queries, search
 * functionality, and user analytics.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /* Basic user lookup methods */
    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by email address
    Optional<User> findByEmail(String email);

    /* User status queries */
    // Find all active users
    List<User> findByIsActiveTrue();

    // Find all verified users
    List<User> findByIsVerifiedTrue();

    // Find active users with pagination
    Page<User> findByIsActiveTrue(Pageable pageable);

    /* Role-based queries */
    // Find users by specific role
    List<User> findByRole(UserRole role);

    // Find users by role with pagination
    Page<User> findByRole(UserRole role, Pageable pageable);

    // Find users with any of the specified roles
    List<User> findByRoleIn(List<UserRole> roles);

    /* Badge and reputation queries */
    // Find users by badge level
    List<User> findByBadgeLevel(BadgeLevel badgeLevel);

    // Find users with minimum reputation score
    List<User> findByReputationScoreGreaterThanEqual(Integer minReputation);

    // Find users ordered by reputation score (descending)
    Page<User> findByOrderByReputationScoreDesc(Pageable pageable);

    // Find top users by reputation score
    @Query("SELECT u FROM User u WHERE u.reputationScore >= :minScore ORDER BY u.reputationScore DESC")
    List<User> findTopUsersByReputation(@Param("minScore") Integer minScore, Pageable pageable);

    /* Search queries */
    // Search users by username, first name, last name, or organisation
    @Query("SELECT u FROM User u WHERE "
            + "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(u.organisation) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            @Param("query") String query);

    // Search users with pagination
    @Query("SELECT u FROM User u WHERE "
            + "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(u.organisation) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            @Param("query") String query, Pageable pageable);

    /* Expert and moderator queries */
    // Find expert users (by role or high reputation and verification)
    @Query("SELECT u FROM User u WHERE u.role IN :roles OR (u.reputationScore >= :minReputation AND u.isVerified = true)")
    List<User> findByRoleInOrReputationScoreGreaterThanEqualAndIsVerifiedTrue(
            @Param("roles") List<UserRole> roles, @Param("minReputation") Integer minReputation);

    // Find moderators (admins, coaches, and high-reputation scouts)
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' OR u.role = 'COACH' OR (u.role = 'SCOUT' AND u.reputationScore >= 25)")
    List<User> findModerators();

    // Find experts (scouts, coaches, and high-reputation verified users)
    @Query("SELECT u FROM User u WHERE u.role = 'SCOUT' OR u.role = 'COACH' OR (u.reputationScore >= 50 AND u.isVerified = true)")
    List<User> findExperts();

    /* Organisation-based queries */
    // Find users by organisation (case-insensitive)
    List<User> findByOrganisationContainingIgnoreCase(String organisation);

    // Get all distinct organisations
    @Query("SELECT DISTINCT u.organisation FROM User u WHERE u.organisation IS NOT NULL AND u.organisation != '' ORDER BY u.organisation")
    List<String> findDistinctOrganisations();

    /* Location-based queries */
    // Find users by location (case-insensitive)
    List<User> findByLocationContainingIgnoreCase(String location);

    /* Activity-based queries */
    // Find users who logged in after specific date
    List<User> findByLastLoginAtAfter(LocalDateTime since);

    // Find users created after specific date
    List<User> findByCreatedAtAfter(LocalDateTime since);

    /* Email verification queries */
    // Find users with unverified emails
    List<User> findByEmailVerifiedAtIsNull();

    // Find users with verified emails
    List<User> findByEmailVerifiedAtIsNotNull();

    /* Expertise queries (for JSON fields) */
    // Find users with expertise in specific league
    @Query("SELECT u FROM User u WHERE u.expertiseLeaguesJson LIKE %:league%")
    List<User> findByExpertiseLeaguesContaining(@Param("league") String league);

    // Find users with expertise in specific position
    @Query("SELECT u FROM User u WHERE u.expertisePositionsJson LIKE %:position%")
    List<User> findByExpertisePositionsContaining(@Param("position") String position);

    /* Count methods */
    // Count active users
    long countByIsActiveTrue();

    // Count verified users
    long countByIsVerifiedTrue();

    // Count users by role
    long countByRole(UserRole role);

    // Count users by badge level
    long countByBadgeLevel(BadgeLevel badgeLevel);

    // Count users with minimum reputation score
    long countByReputationScoreGreaterThanEqual(Integer minScore);

    /* Validation methods */
    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Check if username exists for different user
    boolean existsByUsernameAndIdNot(String username, Long id);

    // Check if email exists for different user
    boolean existsByEmailAndIdNot(String email, Long id);
}

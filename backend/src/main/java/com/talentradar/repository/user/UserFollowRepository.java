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

import com.talentradar.model.user.User;
import com.talentradar.model.user.UserFollow;

/**
 * Repository interface for managing UserFollow entities. Provides data access
 * operations for user follow relationships, social networking features,
 * activity feeds, and user connection analytics.
 */
@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    /* Basic follow relationship queries */
    // Find specific follow relationship between two users
    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);

    // Check if follow relationship exists
    boolean existsByFollowerAndFollowing(User follower, User following);

    // Check if follow relationship exists (alternative method)
    boolean existsByFollowerAndFollowed(User follower, User followed);

    // Check if user is following another user
    @Query("SELECT CASE WHEN COUNT(uf) > 0 THEN true ELSE false END FROM UserFollow uf WHERE uf.follower = :follower AND uf.following = :following")
    boolean isFollowing(@Param("follower") User follower, @Param("following") User following);

    /* Followers and following lists */
    // Find all followers of a user
    List<UserFollow> findByFollowing(User following);

    // Find all users that a user is following
    List<UserFollow> findByFollower(User follower);

    // Find followers with pagination
    Page<UserFollow> findByFollowing(User following, Pageable pageable);

    // Find following with pagination
    Page<UserFollow> findByFollower(User follower, Pageable pageable);

    /* Ordered follow relationships */
    // Find followers ordered by creation date
    List<UserFollow> findByFollowingOrderByCreatedAtDesc(User following);

    // Find following ordered by creation date
    List<UserFollow> findByFollowerOrderByCreatedAtDesc(User follower);

    // Find followers ordered by creation date with pagination
    Page<UserFollow> findByFollowingOrderByCreatedAtDesc(User following, Pageable pageable);

    // Find following ordered by creation date with pagination
    Page<UserFollow> findByFollowerOrderByCreatedAtDesc(User follower, Pageable pageable);

    /* User object queries */
    // Get follower users directly
    @Query("SELECT uf.follower FROM UserFollow uf WHERE uf.following = :user ORDER BY uf.createdAt DESC")
    List<User> findFollowerUsers(@Param("user") User user);

    // Get following users directly
    @Query("SELECT uf.following FROM UserFollow uf WHERE uf.follower = :user ORDER BY uf.createdAt DESC")
    List<User> findFollowingUsers(@Param("user") User user);

    // Get follower users with pagination
    @Query("SELECT uf.follower FROM UserFollow uf WHERE uf.following = :user ORDER BY uf.createdAt DESC")
    Page<User> findFollowerUsers(@Param("user") User user, Pageable pageable);

    // Get following users with pagination
    @Query("SELECT uf.following FROM UserFollow uf WHERE uf.follower = :user ORDER BY uf.createdAt DESC")
    Page<User> findFollowingUsers(@Param("user") User user, Pageable pageable);

    // Get followers by user (alternative method)
    @Query("SELECT uf.follower FROM UserFollow uf WHERE uf.following = :user ORDER BY uf.createdAt DESC")
    List<User> findFollowersByUser(@Param("user") User user);

    // Get followers with pagination (alternative method)
    @Query("SELECT uf.follower FROM UserFollow uf WHERE uf.followed = :user")
    Page<User> findFollowersByUser(@Param("user") User user, Pageable pageable);

    // Get following with pagination (alternative method)
    @Query("SELECT uf.followed FROM UserFollow uf WHERE uf.follower = :user")
    Page<User> findFollowingByUser(@Param("user") User user, Pageable pageable);

    /* Count methods */
    // Count followers of a user
    long countByFollowing(User following);

    // Count users that a user is following
    long countByFollower(User follower);

    // Count followers (alternative method)
    long countByFollowed(User followed);

    /* Recent activity queries */
    // Find recent follows across all users
    List<UserFollow> findByCreatedAtAfter(LocalDateTime since);

    // Find recent followers of a user
    List<UserFollow> findByFollowingAndCreatedAtAfter(User following, LocalDateTime since);

    // Find recent follows by a user
    List<UserFollow> findByFollowerAndCreatedAtAfter(User follower, LocalDateTime since);

    // Find recent activity for user feed
    @Query("SELECT uf FROM UserFollow uf WHERE uf.follower IN "
            + "(SELECT uf2.following FROM UserFollow uf2 WHERE uf2.follower = :user) "
            + "ORDER BY uf.createdAt DESC")
    List<UserFollow> findRecentFollowsByFollowedUsers(@Param("user") User user, Pageable pageable);

    /* Social networking features */
    // Find mutual follows (users who follow each other)
    @Query("SELECT uf1.following FROM UserFollow uf1 WHERE uf1.follower = :user AND EXISTS "
            + "(SELECT uf2 FROM UserFollow uf2 WHERE uf2.follower = uf1.following AND uf2.following = :user)")
    List<User> findMutualFollows(@Param("user") User user);

    // Find follow suggestions (users followed by people you follow)
    @Query("SELECT DISTINCT uf2.following FROM UserFollow uf1 "
            + "JOIN UserFollow uf2 ON uf1.following = uf2.follower "
            + "WHERE uf1.follower = :user AND uf2.following != :user "
            + "AND NOT EXISTS (SELECT uf3 FROM UserFollow uf3 WHERE uf3.follower = :user AND uf3.following = uf2.following)")
    List<User> findFollowSuggestions(@Param("user") User user);

    // Find follow suggestions with connection scores
    @Query("SELECT uf2.following, COUNT(uf2) as commonConnections FROM UserFollow uf1 "
            + "JOIN UserFollow uf2 ON uf1.following = uf2.follower "
            + "WHERE uf1.follower = :user AND uf2.following != :user "
            + "AND NOT EXISTS (SELECT uf3 FROM UserFollow uf3 WHERE uf3.follower = :user AND uf3.following = uf2.following) "
            + "GROUP BY uf2.following ORDER BY COUNT(uf2) DESC")
    List<Object[]> findFollowSuggestionsWithScore(@Param("user") User user);

    /* Popular user queries */
    // Find most followed users
    @Query("SELECT uf.following, COUNT(uf) as followerCount FROM UserFollow uf GROUP BY uf.following ORDER BY COUNT(uf) DESC")
    List<Object[]> findMostFollowedUsers();

    // Find most followed users with pagination
    @Query("SELECT uf.following, COUNT(uf) as followerCount FROM UserFollow uf GROUP BY uf.following ORDER BY COUNT(uf) DESC")
    Page<Object[]> findMostFollowedUsers(Pageable pageable);

    // Find most active followers (users following many people)
    @Query("SELECT uf.follower, COUNT(uf) as followingCount FROM UserFollow uf GROUP BY uf.follower ORDER BY COUNT(uf) DESC")
    List<Object[]> findMostActiveFollowers();

    // Find most active followers with pagination
    @Query("SELECT uf.follower, COUNT(uf) as followingCount FROM UserFollow uf GROUP BY uf.follower ORDER BY COUNT(uf) DESC")
    Page<Object[]> findMostActiveFollowers(Pageable pageable);

    /* Administrative queries */
    // Find all moderators and admins
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' OR u.role = 'MODERATOR'")
    List<User> findAllModerators();

    // Find users with no follow relationships
    @Query("SELECT u FROM User u WHERE u NOT IN (SELECT DISTINCT uf.following FROM UserFollow uf) AND u NOT IN (SELECT DISTINCT uf.follower FROM UserFollow uf)")
    List<User> findUsersWithNoFollowRelationships();

    /* Network analysis */
    // Find follow relationships within a specific group of users
    @Query("SELECT uf.follower, uf.following FROM UserFollow uf WHERE uf.follower IN :users AND uf.following IN :users")
    List<UserFollow> findFollowRelationshipsWithinGroup(@Param("users") List<User> users);

    /* Statistics and analytics */
    // Count distinct followers across all users
    @Query("SELECT COUNT(DISTINCT uf.follower) FROM UserFollow uf")
    long countDistinctFollowers();

    // Count distinct users being followed
    @Query("SELECT COUNT(DISTINCT uf.following) FROM UserFollow uf")
    long countDistinctFollowing();

    // Get daily follow counts for analytics
    @Query("SELECT DATE(uf.createdAt), COUNT(uf) FROM UserFollow uf WHERE uf.createdAt >= :since GROUP BY DATE(uf.createdAt) ORDER BY DATE(uf.createdAt)")
    List<Object[]> getDailyFollowCounts(@Param("since") LocalDateTime since);

    /* Deletion methods */
    // Delete specific follow relationship
    void deleteByFollowerAndFollowing(User follower, User following);

    // Delete specific follow relationship (alternative method)
    void deleteByFollowerAndFollowed(User follower, User followed);

    // Delete all follow relationships for a user
    @Query("DELETE FROM UserFollow uf WHERE uf.follower = :user OR uf.following = :user")
    void deleteAllByUser(@Param("user") User user);
}

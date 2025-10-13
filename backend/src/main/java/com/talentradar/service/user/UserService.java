package com.talentradar.service.user;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.dto.user.UserUpdateDTO;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.enums.BadgeLevel;
import com.talentradar.model.enums.UserRole;
import com.talentradar.model.player.PlayerRating;
import com.talentradar.model.player.PlayerView;
import com.talentradar.model.user.User;
import com.talentradar.model.user.UserFollow;
import com.talentradar.repository.player.PlayerRatingRepository;
import com.talentradar.repository.player.PlayerViewRepository;
import com.talentradar.repository.scouting.ScoutingReportRepository;
import com.talentradar.repository.user.UserFollowRepository;
import com.talentradar.repository.user.UserRepository;
import com.talentradar.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Service responsible for managing user operations. Handles user CRUD
 * operations, authentication, follow relationships, and user management.
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PlayerViewRepository playerViewRepository;

    @Autowired
    private PlayerRatingRepository playerRatingRepository;

    @Autowired
    private ScoutingReportRepository scoutingReportRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        try {
            if (id == null) {
                logger.warn("Attempted to find user with null ID");
                return Optional.empty();
            }
            return userRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding user by ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                logger.warn("Attempted to find user with null or empty username");
                return Optional.empty();
            }
            return userRepository.findByUsername(username.trim());
        } catch (Exception e) {
            logger.error("Error finding user by username '{}': {}", username, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Attempted to find user with null or empty email");
                return Optional.empty();
            }
            return userRepository.findByEmail(email.trim());
        } catch (Exception e) {
            logger.error("Error finding user by email '{}': {}", email, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public User save(User user) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            return userRepository.save(user);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error saving user: {}", e.getMessage());
            throw new RuntimeException("Failed to save user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            if (!userRepository.existsById(id)) {
                throw new UserNotFoundException("User not found with ID: " + id);
            }

            userRepository.deleteById(id);
            logger.info("Deleted user with ID: {}", id);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deleting user {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public User createUser(String username, String email, String password, UserRole role) {
        try {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be null or empty");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be null or empty");
            }

            String cleanUsername = username.trim();
            String cleanEmail = email.trim();

            if (userRepository.findByUsername(cleanUsername).isPresent()) {
                throw new IllegalArgumentException("Username already exists: " + cleanUsername);
            }

            if (userRepository.findByEmail(cleanEmail).isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + cleanEmail);
            }

            User user = new User();
            user.setUsername(cleanUsername);
            user.setEmail(cleanEmail);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role != null ? role : UserRole.USER);
            user.setIsActive(true);
            user.setReputationScore(0);
            user.setBadgeLevel(BadgeLevel.BRONZE);
            user.setIsVerified(false);

            User savedUser = userRepository.save(user);
            logger.info("Created new user: {}", savedUser.getUsername());

            return savedUser;

        } catch (IllegalArgumentException e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating user with username '{}': {}", username, e.getMessage());
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public User updateUser(Long userId, UserUpdateDTO updateDTO) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (updateDTO == null) {
                throw new IllegalArgumentException("Update data cannot be null");
            }

            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            // Update only the fields provided in the DTO (non-null values)
            if (updateDTO.getFirstName() != null) {
                existingUser.setFirstName(updateDTO.getFirstName().trim());
            }
            if (updateDTO.getLastName() != null) {
                existingUser.setLastName(updateDTO.getLastName().trim());
            }
            if (updateDTO.getBio() != null) {
                existingUser.setBio(updateDTO.getBio().trim());
            }
            if (updateDTO.getOrganisation() != null) {
                existingUser.setOrganisation(updateDTO.getOrganisation().trim());
            }
            if (updateDTO.getLocation() != null) {
                existingUser.setLocation(updateDTO.getLocation().trim());
            }
            if (updateDTO.getWebsiteUrl() != null) {
                existingUser.setWebsiteUrl(updateDTO.getWebsiteUrl().trim());
            }
            if (updateDTO.getProfileImageUrl() != null) {
                existingUser.setProfileImageUrl(updateDTO.getProfileImageUrl().trim());
            }

            User savedUser = userRepository.save(existingUser);
            logger.info("Updated user: {}", savedUser.getUsername());
            return savedUser;

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error updating user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void updateLastLogin(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
                logger.debug("Updated last login for user: {}", user.getUsername());
            } else {
                logger.warn("User not found for last login update: {}", userId);
            }

        } catch (IllegalArgumentException e) {
            logger.error("Invalid user ID for last login update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating last login for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update last login", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public User getCurrentUser(HttpServletRequest request) {
        try {
            if (request == null) {
                throw new IllegalArgumentException("Request cannot be null");
            }

            String token = extractTokenFromRequest(request);
            if (token != null) {
                String username = jwtUtil.getUsernameFromToken(token);
                return userRepository.findByUsername(username)
                        .orElseThrow(() -> new UserNotFoundException("User not found"));
            }
            throw new IllegalStateException("No authenticated user found");

        } catch (UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error getting current user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error getting current user from request: {}", e.getMessage());
            throw new RuntimeException("Failed to get current user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public User getCurrentUserOrNull(HttpServletRequest request) {
        try {
            return getCurrentUser(request);
        } catch (Exception e) {
            logger.debug("No authenticated user found in request");
            return null; // Return null for anonymous users
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        try {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            return null;
        } catch (Exception e) {
            logger.debug("Error extracting token from request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get user activity statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserActivityStats(User user) {
        Map<String, Object> activity = new HashMap<>();

        try {
            // Recent activity counts
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

            // Get weekly views
            Long weeklyViews = playerViewRepository.countByUserAndViewedAtAfter(user, weekAgo);
            activity.put("weeklyViews", weeklyViews);

            // Get total reports by user
            Long totalReports = scoutingReportRepository.countByScout(user);
            activity.put("totalReports", totalReports);

            // Get recent views
            List<PlayerView> recentViews = playerViewRepository.findTop20ByUserOrderByCreatedAtDesc(user);
            activity.put("recentViews", recentViews);

            // Get recent ratings
            List<PlayerRating> recentRatings = playerRatingRepository.findByUserAndIsActiveTrueOrderByCreatedAtDesc(user)
                    .stream().limit(10).collect(Collectors.toList());
            activity.put("recentRatings", recentRatings);

            return activity;
        } catch (Exception e) {
            logger.error("Error retrieving user activity stats: {}", e.getMessage());
            return activity;
        }
    }

    /**
     * Get platform statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPlatformStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("totalUsers", userRepository.count());

            LocalDateTime last24h = LocalDateTime.now().minusDays(1);
            stats.put("newUsersLast24h", userRepository.countByCreatedAtAfter(last24h));

            return stats;
        } catch (Exception e) {
            logger.error("Error retrieving platform statistics: {}", e.getMessage());
            return stats;
        }
    }

    /**
     * Get admin statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> adminData = new HashMap<>();

        try {
            long totalUsers = userRepository.count();
            LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
            long newUsersThisWeek = userRepository.countByCreatedAtAfter(lastWeek);

            adminData.put("totalUsers", totalUsers);
            adminData.put("newUsersThisWeek", newUsersThisWeek);

            // Top users by reputation
            List<User> topUsers = userRepository.findTop10ByOrderByReputationScoreDesc();
            adminData.put("topUsers", topUsers);

            return adminData;
        } catch (Exception e) {
            logger.error("Error retrieving admin statistics: {}", e.getMessage());
            return adminData;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void followUser(User follower, User userToFollow) {
        try {
            if (follower == null) {
                throw new UserNotFoundException("Follower cannot be null");
            }
            if (userToFollow == null) {
                throw new UserNotFoundException("User to follow cannot be null");
            }

            // Prevent users from following themselves
            if (follower.getId().equals(userToFollow.getId())) {
                throw new IllegalArgumentException("Users cannot follow themselves");
            }

            // Check if already following
            if (userFollowRepository.existsByFollowerAndFollowing(follower, userToFollow)) {
                throw new IllegalStateException("User is already following this user");
            }

            // Create follow relationship
            UserFollow userFollow = new UserFollow(follower, userToFollow);
            userFollowRepository.save(userFollow);

            logger.info("User {} started following user {}",
                    follower.getUsername(), userToFollow.getUsername());

        } catch (UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error creating follow relationship: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error following user {} by {}: {}",
                    userToFollow != null ? userToFollow.getUsername() : "null",
                    follower != null ? follower.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to follow user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void unfollowUser(User follower, User userToUnfollow) {
        try {
            if (follower == null) {
                throw new UserNotFoundException("Follower cannot be null");
            }
            if (userToUnfollow == null) {
                throw new UserNotFoundException("User to unfollow cannot be null");
            }

            // Check if follow relationship exists
            if (!userFollowRepository.existsByFollowerAndFollowing(follower, userToUnfollow)) {
                throw new IllegalStateException("User is not following this user");
            }

            // Remove follow relationship
            userFollowRepository.deleteByFollowerAndFollowing(follower, userToUnfollow);

            logger.info("User {} unfollowed user {}",
                    follower.getUsername(), userToUnfollow.getUsername());

        } catch (UserNotFoundException | IllegalStateException e) {
            logger.error("Error removing follow relationship: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error unfollowing user {} by {}: {}",
                    userToUnfollow != null ? userToUnfollow.getUsername() : "null",
                    follower != null ? follower.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to unfollow user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<User> getFollowers(User user, Pageable pageable) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return userFollowRepository.findFollowersByUser(user, pageable);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting followers: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error getting followers for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to get followers", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<User> getFollowing(User user, Pageable pageable) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return userFollowRepository.findFollowingByUser(user, pageable);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting following: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error getting following for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to get following", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(User follower, User followed) {
        try {
            if (follower == null || followed == null) {
                return false;
            }
            return userFollowRepository.existsByFollowerAndFollowing(follower, followed);
        } catch (Exception e) {
            logger.error("Error checking follow status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getFollowerCount(User user) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            return userFollowRepository.countByFollowing(user);
        } catch (UserNotFoundException e) {
            logger.error("Error getting follower count: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting follower count for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getFollowingCount(User user) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            return userFollowRepository.countByFollower(user);
        } catch (UserNotFoundException e) {
            logger.error("Error getting following count: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting following count for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isMutualFollow(User user1, User user2) {
        try {
            if (user1 == null || user2 == null) {
                return false;
            }
            return userFollowRepository.existsByFollowerAndFollowing(user1, user2)
                    && userFollowRepository.existsByFollowerAndFollowing(user2, user1);
        } catch (Exception e) {
            logger.error("Error checking mutual follow status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("Current password cannot be null or empty");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("New password cannot be null or empty");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                logger.info("Password changed for user: {}", user.getUsername());
                return true;
            } else {
                logger.warn("Invalid current password for user: {}", user.getUsername());
                return false;
            }

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error changing password: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error changing password for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to change password", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void resetPassword(String email, String newPassword) {
        try {
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("New password cannot be null or empty");
            }

            User user = userRepository.findByEmail(email.trim())
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            logger.info("Password reset for user: {}", user.getUsername());

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error resetting password: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error resetting password for email '{}': {}", email, e.getMessage());
            throw new RuntimeException("Failed to reset password", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void updateReputationScore(Long userId, int points) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            int newScore = Math.max(0, user.getReputationScore() + points);
            user.setReputationScore(newScore);
            user.updateBadgeLevel(); // This will automatically update the badge level
            userRepository.save(user);

            logger.info("Updated reputation for user {}: {} points (total: {})",
                    user.getUsername(), points, newScore);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error updating reputation: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating reputation for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update reputation", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void addReputationPoints(Long userId, int points, String reason) {
        try {
            updateReputationScore(userId, points);
            logger.info("Added {} reputation points to user ID {}: {}", points, userId, reason);
        } catch (Exception e) {
            logger.error("Error adding reputation points: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void subtractReputationPoints(Long userId, int points, String reason) {
        try {
            updateReputationScore(userId, -points);
            logger.info("Subtracted {} reputation points from user ID {}: {}", points, userId, reason);
        } catch (Exception e) {
            logger.error("Error subtracting reputation points: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void verifyUser(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            user.setIsVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
            userRepository.save(user);

            logger.info("Verified user: {}", user.getUsername());

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error verifying user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error verifying user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to verify user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void unverifyUser(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            user.setIsVerified(false);
            user.setEmailVerifiedAt(null);
            userRepository.save(user);

            logger.info("Unverified user: {}", user.getUsername());

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error unverifying user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error unverifying user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to unverify user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("Search query cannot be null or empty");
            }

            String cleanQuery = query.trim();
            return userRepository.findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    cleanQuery);

        } catch (IllegalArgumentException e) {
            logger.error("Error searching users: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching users with query '{}': {}", query, e.getMessage());
            throw new RuntimeException("Failed to search users", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String query, Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            if (query != null && !query.trim().isEmpty()) {
                String cleanQuery = query.trim();
                return userRepository.findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        cleanQuery, pageable);
            }
            return userRepository.findByIsActiveTrue(pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error searching users with pagination: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching users with query '{}': {}", query, e.getMessage());
            throw new RuntimeException("Failed to search users", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(UserRole role) {
        try {
            if (role == null) {
                throw new IllegalArgumentException("Role cannot be null");
            }
            return userRepository.findByRole(role);
        } catch (IllegalArgumentException e) {
            logger.error("Error finding users by role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding users by role {}: {}", role, e.getMessage());
            throw new RuntimeException("Failed to find users by role", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<User> findVerifiedUsers() {
        try {
            return userRepository.findByIsVerifiedTrue();
        } catch (Exception e) {
            logger.error("Error finding verified users: {}", e.getMessage());
            throw new RuntimeException("Failed to find verified users", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<User> findExpertUsers() {
        try {
            return userRepository.findByRoleInOrReputationScoreGreaterThanEqualAndIsVerifiedTrue(
                    List.of(UserRole.SCOUT, UserRole.COACH), 50);
        } catch (Exception e) {
            logger.error("Error finding expert users: {}", e.getMessage());
            throw new RuntimeException("Failed to find expert users", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<User> findUsersByBadgeLevel(BadgeLevel badgeLevel) {
        try {
            if (badgeLevel == null) {
                throw new IllegalArgumentException("Badge level cannot be null");
            }
            return userRepository.findByBadgeLevel(badgeLevel);
        } catch (IllegalArgumentException e) {
            logger.error("Error finding users by badge level: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding users by badge level {}: {}", badgeLevel, e.getMessage());
            throw new RuntimeException("Failed to find users by badge level", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<User> findTopUsersByReputation(int limit) {
        try {
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be positive");
            }
            if (limit > 1000) {
                throw new IllegalArgumentException("Limit cannot exceed 1000");
            }

            return userRepository.findByOrderByReputationScoreDesc(
                    org.springframework.data.domain.PageRequest.of(0, limit)
            ).getContent();

        } catch (IllegalArgumentException e) {
            logger.error("Invalid limit for top users: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding top users by reputation with limit {}: {}", limit, e.getMessage());
            throw new RuntimeException("Failed to find top users by reputation", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void trackUserActivity(Long userId, String activity) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (activity == null || activity.trim().isEmpty()) {
                throw new IllegalArgumentException("Activity cannot be null or empty");
            }

            // This could be expanded to track various user activities
            updateLastLogin(userId);
            logger.debug("User activity tracked - User ID: {}, Activity: {}", userId, activity.trim());

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for activity tracking: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error tracking activity for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to track user activity", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void activateUser(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            user.setIsActive(true);
            userRepository.save(user);

            logger.info("Activated user: {}", user.getUsername());

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error activating user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error activating user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to activate user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deactivateUser(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            user.setIsActive(false);
            userRepository.save(user);

            logger.info("Deactivated user: {}", user.getUsername());

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error deactivating user: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deactivating user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to deactivate user", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void updateUserRole(Long userId, UserRole newRole) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (newRole == null) {
                throw new IllegalArgumentException("New role cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            UserRole oldRole = user.getRole();
            user.setRole(newRole);
            userRepository.save(user);

            logger.info("Updated user role for {}: {} -> {}",
                    user.getUsername(), oldRole, newRole);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error updating user role: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating role for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update user role", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            logger.error("Error getting total user count: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        try {
            return userRepository.countByIsActiveTrue();
        } catch (Exception e) {
            logger.error("Error getting active user count: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getVerifiedUserCount() {
        try {
            return userRepository.countByIsVerifiedTrue();
        } catch (Exception e) {
            logger.error("Error getting verified user count: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getUserCountByRole(UserRole role) {
        try {
            if (role == null) {
                throw new IllegalArgumentException("Role cannot be null");
            }
            return userRepository.countByRole(role);
        } catch (IllegalArgumentException e) {
            logger.error("Error getting user count by role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting count for role {}: {}", role, e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void updateUserExpertise(Long userId, List<String> leagues, List<String> positions) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            // Clean and validate expertise data
            List<String> cleanLeagues = leagues != null
                    ? leagues.stream()
                            .filter(league -> league != null && !league.trim().isEmpty())
                            .map(String::trim)
                            .distinct()
                            .toList() : List.of();

            List<String> cleanPositions = positions != null
                    ? positions.stream()
                            .filter(pos -> pos != null && !pos.trim().isEmpty())
                            .map(String::trim)
                            .distinct()
                            .toList() : List.of();

            user.setExpertiseLeagues(cleanLeagues);
            user.setExpertisePositions(cleanPositions);
            userRepository.save(user);

            logger.info("Updated expertise for user: {}", user.getUsername());

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error updating user expertise: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating expertise for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to update user expertise", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            return !userRepository.findByUsername(username.trim()).isPresent();
        } catch (Exception e) {
            logger.error("Error checking username availability for '{}': {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return false;
            }
            return !userRepository.findByEmail(email.trim()).isPresent();
        } catch (Exception e) {
            logger.error("Error checking email availability for '{}': {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean canUserModerate(Long userId) {
        try {
            if (userId == null) {
                return false;
            }
            return userRepository.findById(userId)
                    .map(User::canModerate)
                    .orElse(false);
        } catch (Exception e) {
            logger.error("Error checking moderation permissions for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isUserExpert(Long userId) {
        try {
            if (userId == null) {
                return false;
            }
            return userRepository.findById(userId)
                    .map(User::isExpert)
                    .orElse(false);
        } catch (Exception e) {
            logger.error("Error checking expert status for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public List<User> saveAll(List<User> users) {
        try {
            if (users == null) {
                throw new IllegalArgumentException("Users list cannot be null");
            }

            // Validate each user
            for (User user : users) {
                if (user == null) {
                    throw new IllegalArgumentException("User in list cannot be null");
                }
            }

            return userRepository.saveAll(users);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid users for bulk save: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error saving users in bulk: {}", e.getMessage());
            throw new RuntimeException("Failed to save users", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteAll(List<User> users) {
        try {
            if (users == null) {
                throw new IllegalArgumentException("Users list cannot be null");
            }

            // Validate each user
            for (User user : users) {
                if (user == null) {
                    throw new IllegalArgumentException("User in list cannot be null");
                }
            }

            userRepository.deleteAll(users);
            logger.info("Deleted {} users in bulk", users.size());

        } catch (IllegalArgumentException e) {
            logger.error("Invalid users for bulk delete: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting users in bulk: {}", e.getMessage());
            throw new RuntimeException("Failed to delete users", e);
        }
    }
}

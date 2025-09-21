package com.talentradar.controller.user;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.user.UserCreateDTO;
import com.talentradar.dto.user.UserDTO;
import com.talentradar.dto.user.UserLoginDTO;
import com.talentradar.dto.user.UserLoginResponseDTO;
import com.talentradar.dto.user.UserUpdateDTO;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.user.User;
import com.talentradar.service.auth.AuthenticationService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for managing user account operations. Provides endpoints for
 * user registration, profile management, and account settings.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Registers a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<UserLoginResponseDTO> register(@Valid @RequestBody UserCreateDTO createDTO) {
        try {
            UserLoginResponseDTO response = authenticationService.registerUser(createDTO);
            logger.info("New user registered: {}", createDTO.getUsername());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid user registration data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register user", e);
        }
    }

    /**
     * Authenticates user login.
     */
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        try {
            UserLoginResponseDTO response = authenticationService.authenticateUser(loginDTO);
            logger.info("User login successful: {}", loginDTO.getUsernameOrEmail());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Login failed for user {}: {}", loginDTO.getUsernameOrEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error during login for user {}: {}", loginDTO.getUsernameOrEmail(), e.getMessage(), e);
            throw new RuntimeException("Login failed", e);
        }
    }

    /**
     * Retrieves the current authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(HttpServletRequest request) {
        try {
            User user = userService.getCurrentUser(request);
            return ResponseEntity.ok(convertToDTO(user));

        } catch (UserNotFoundException e) {
            logger.error("Current user not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving current user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve current user", e);
        }
    }

    /**
     * Updates the current authenticated user's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(
            @Valid @RequestBody UserUpdateDTO updateDTO,
            HttpServletRequest request) {

        try {
            User currentUser = userService.getCurrentUser(request);
            User updatedUser = userService.updateUser(currentUser.getId(), updateDTO);

            logger.info("User profile updated: {}", currentUser.getUsername());
            return ResponseEntity.ok(convertToDTO(updatedUser));

        } catch (UserNotFoundException e) {
            logger.error("User not found for update: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid user update data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user profile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update user profile", e);
        }
    }

    /**
     * Retrieves a specific user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

            logger.info("Retrieved user profile: {}", user.getUsername());
            return ResponseEntity.ok(convertToDTO(user));

        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user", e);
        }
    }

    /**
     * Retrieves a user's public profile information.
     */
    @GetMapping("/{id}/profile")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable Long id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

            // Filter sensitive information for public profile view
            UserDTO publicProfile = convertToPublicDTO(user);

            logger.info("Retrieved public profile for user: {}", user.getUsername());
            return ResponseEntity.ok(publicProfile);

        } catch (UserNotFoundException e) {
            logger.error("User profile not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user profile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user profile", e);
        }
    }

    /**
     * Searches for users based on query parameters.
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String q) {
        try {
            if (q == null || q.trim().isEmpty()) {
                throw new IllegalArgumentException("Search query is required");
            }

            List<User> users = userService.searchUsers(q);
            List<UserDTO> userDTOs = users.stream()
                    .map(this::convertToPublicDTO)
                    .toList();

            logger.info("User search completed: {} results for query: {}", users.size(), q);
            return ResponseEntity.ok(userDTOs);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid search query: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search users", e);
        }
    }

    /**
     * Follows another user.
     */
    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followUser(
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            User currentUser = userService.getCurrentUser(request);
            User targetUser = userService.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

            if (currentUser.getId().equals(targetUser.getId())) {
                throw new IllegalArgumentException("Cannot follow yourself");
            }

            userService.followUser(currentUser, targetUser);

            logger.info("User {} followed user {}", currentUser.getUsername(), targetUser.getUsername());
            return ResponseEntity.ok().build();

        } catch (UserNotFoundException e) {
            logger.error("User not found for follow operation: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid follow operation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error following user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to follow user", e);
        }
    }

    /**
     * Unfollows another user.
     */
    @PostMapping("/{id}/unfollow")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            User currentUser = userService.getCurrentUser(request);
            User targetUser = userService.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

            userService.unfollowUser(currentUser, targetUser);

            logger.info("User {} unfollowed user {}", currentUser.getUsername(), targetUser.getUsername());
            return ResponseEntity.ok().build();

        } catch (UserNotFoundException e) {
            logger.error("User not found for unfollow operation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error unfollowing user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to unfollow user", e);
        }
    }

    /**
     * Retrieves a user's followers with pagination.
     */
    @GetMapping("/{id}/followers")
    public ResponseEntity<Page<UserDTO>> getUserFollowers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

            Pageable pageable = PageRequest.of(page, size);
            Page<User> followers = userService.getFollowers(user, pageable);
            Page<UserDTO> followerDTOs = followers.map(this::convertToPublicDTO);

            logger.info("Retrieved {} followers for user: {}", followers.getNumberOfElements(), user.getUsername());
            return ResponseEntity.ok(followerDTOs);

        } catch (UserNotFoundException e) {
            logger.error("User not found for followers retrieval: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user followers: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user followers", e);
        }
    }

    /**
     * Retrieves users that a specific user is following with pagination.
     */
    @GetMapping("/{id}/following")
    public ResponseEntity<Page<UserDTO>> getUserFollowing(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

            Pageable pageable = PageRequest.of(page, size);
            Page<User> following = userService.getFollowing(user, pageable);
            Page<UserDTO> followingDTOs = following.map(this::convertToPublicDTO);

            logger.info("Retrieved {} following for user: {}", following.getNumberOfElements(), user.getUsername());
            return ResponseEntity.ok(followingDTOs);

        } catch (UserNotFoundException e) {
            logger.error("User not found for following retrieval: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user following: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user following", e);
        }
    }

    /**
     * Converts User entity to UserDTO with full information.
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setDisplayName(user.getDisplayName());
        dto.setRole(user.getRole());
        dto.setIsVerified(user.getIsVerified());
        dto.setIsActive(user.getIsActive());
        dto.setBio(user.getBio());
        dto.setOrganisation(user.getOrganisation());
        dto.setLocation(user.getLocation());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setExpertiseLeagues(user.getExpertiseLeagues());
        dto.setExpertisePositions(user.getExpertisePositions());
        dto.setSocialLinks(user.getSocialLinks());
        dto.setReputationScore(user.getReputationScore());
        dto.setBadgeLevel(user.getBadgeLevel());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setEmailVerifiedAt(user.getEmailVerifiedAt());
        dto.setIsProfilePublic(user.getIsProfilePublic());
        dto.setShowEmail(user.getShowEmail());
        return dto;
    }

    /**
     * Converts User entity to UserDTO with public information only.
     */
    private UserDTO convertToPublicDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setRole(user.getRole());
        dto.setIsVerified(user.getIsVerified());
        dto.setIsActive(user.getIsActive());
        dto.setReputationScore(user.getReputationScore());
        dto.setBadgeLevel(user.getBadgeLevel());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setProfileImageUrl(user.getProfileImageUrl());

        // Only include sensitive information if user allows it
        if (user.getIsProfilePublic() != null && user.getIsProfilePublic()) {
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setBio(user.getBio());
            dto.setOrganisation(user.getOrganisation());
            dto.setLocation(user.getLocation());
            dto.setExpertiseLeagues(user.getExpertiseLeagues());
            dto.setExpertisePositions(user.getExpertisePositions());
            dto.setSocialLinks(user.getSocialLinks());
        }

        if (user.getShowEmail() != null && user.getShowEmail()) {
            dto.setEmail(user.getEmail());
        }

        return dto;
    }

}

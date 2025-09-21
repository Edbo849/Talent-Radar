package com.talentradar.service.auth;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.dto.user.UserCreateDTO;
import com.talentradar.dto.user.UserDTO;
import com.talentradar.dto.user.UserLoginDTO;
import com.talentradar.dto.user.UserLoginResponseDTO;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.user.User;
import com.talentradar.security.JwtUtil;
import com.talentradar.service.user.UserService;

/**
 * Service responsible for user authentication operations. Handles login,
 * registration, and token management.
 */
@Service
@Transactional
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Authenticate user and return login response with JWT token
     */
    public UserLoginResponseDTO authenticateUser(UserLoginDTO loginDTO) {
        try {
            // Find user by username or email
            User user = findUserByUsernameOrEmail(loginDTO.getUsernameOrEmail());

            // Verify password
            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Invalid credentials");
            }

            // Check if user is active
            if (!user.getIsActive()) {
                throw new IllegalArgumentException("Account is deactivated");
            }

            // Generate JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("role", user.getRole().name());

            String token = jwtUtil.generateToken(user.getUsername(), claims);

            // Update last login
            userService.updateLastLogin(user.getId());

            // Convert user to DTO (exclude sensitive information)
            UserDTO userDTO = convertToDTO(user);

            // Create response
            UserLoginResponseDTO response = new UserLoginResponseDTO(
                    token,
                    userDTO,
                    jwtUtil.getExpirationTime()
            );

            logger.info("User {} authenticated successfully", user.getUsername());
            return response;

        } catch (UserNotFoundException e) {
            logger.error("Authentication failed - user not found: {}", loginDTO.getUsernameOrEmail());
            throw new IllegalArgumentException("Invalid credentials");
        } catch (IllegalArgumentException e) {
            logger.error("Authentication failed for user {}: {}", loginDTO.getUsernameOrEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Register new user and return login response
     */
    public UserLoginResponseDTO registerUser(UserCreateDTO createDTO) {
        try {
            // Create the user
            User user = userService.createUser(
                    createDTO.getUsername(),
                    createDTO.getEmail(),
                    createDTO.getPassword(),
                    createDTO.getRole()
            );

            // Generate JWT token for immediate login
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("role", user.getRole().name());

            String token = jwtUtil.generateToken(user.getUsername(), claims);

            // Convert user to DTO
            UserDTO userDTO = convertToDTO(user);

            // Create response
            UserLoginResponseDTO response = new UserLoginResponseDTO(
                    token,
                    userDTO,
                    jwtUtil.getExpirationTime()
            );

            logger.info("User {} registered and authenticated successfully", user.getUsername());
            return response;

        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Find user by username or email
     */
    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        // Try to find by username first
        return userService.findByUsername(usernameOrEmail)
                .or(() -> userService.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UserNotFoundException("User not found with username/email: " + usernameOrEmail));
    }

    /**
     * Convert User entity to UserDTO
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
        dto.setReputationScore(user.getReputationScore());
        dto.setBadgeLevel(user.getBadgeLevel());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setEmailVerifiedAt(user.getEmailVerifiedAt());
        dto.setIsProfilePublic(user.getIsProfilePublic());
        dto.setShowEmail(user.getShowEmail());
        return dto;
    }
}

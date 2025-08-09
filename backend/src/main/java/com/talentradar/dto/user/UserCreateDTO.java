package com.talentradar.dto.user;

import java.util.List;

import com.talentradar.model.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating new User entities. Contains validation
 * rules and required fields for user registration. Includes optional profile
 * and expertise fields for enhanced user setup.
 */
public class UserCreateDTO {

    // Required authentication fields
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    // Role and basic information fields
    private UserRole role = UserRole.USER;

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    // Optional profile fields
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @Size(max = 200, message = "Organization must not exceed 200 characters")
    private String organisation;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @Size(max = 200, message = "Website URL must not exceed 200 characters")
    private String websiteUrl;

    // Expertise fields
    private List<String> expertiseLeagues;
    private List<String> expertisePositions;

    // Privacy settings fields
    private Boolean isProfilePublic = true;
    private Boolean showEmail = false;

    /* Constructors */
    public UserCreateDTO() {
    }

    public UserCreateDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /* Getters and Setters */
    // Required authentication getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Role and basic information getters and setters
    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Optional profile getters and setters
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getOrganization() {
        return organisation;
    }

    public void setOrganization(String organisation) {
        this.organisation = organisation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    // Expertise getters and setters
    public List<String> getExpertiseLeagues() {
        return expertiseLeagues;
    }

    public void setExpertiseLeagues(List<String> expertiseLeagues) {
        this.expertiseLeagues = expertiseLeagues;
    }

    public List<String> getExpertisePositions() {
        return expertisePositions;
    }

    public void setExpertisePositions(List<String> expertisePositions) {
        this.expertisePositions = expertisePositions;
    }

    // Privacy settings getters and setters
    public Boolean getIsProfilePublic() {
        return isProfilePublic;
    }

    public void setIsProfilePublic(Boolean isProfilePublic) {
        this.isProfilePublic = isProfilePublic;
    }

    public Boolean getShowEmail() {
        return showEmail;
    }

    public void setShowEmail(Boolean showEmail) {
        this.showEmail = showEmail;
    }
}

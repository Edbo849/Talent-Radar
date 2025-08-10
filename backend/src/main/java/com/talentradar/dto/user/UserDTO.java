package com.talentradar.dto.user;

import java.time.LocalDateTime;
import java.util.List;

import com.talentradar.model.enums.BadgeLevel;
import com.talentradar.model.enums.UserRole;

/**
 * Data Transfer Object for User entity responses. Contains comprehensive user
 * information including profile data, preferences, social metrics, and privacy
 * settings for client consumption.
 */
public class UserDTO {

    // Core identification fields
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String displayName;

    // Role and verification fields
    private UserRole role;
    private Boolean isVerified;
    private Boolean isActive;

    // Profile information fields
    private String bio;
    private String organisation;
    private String location;
    private String websiteUrl;
    private String profileImageUrl;

    // Expertise and experience fields
    private List<String> expertiseLeagues;
    private List<String> expertisePositions;
    private List<String> socialLinks;

    // Reputation and badge fields
    private Integer reputationScore;
    private BadgeLevel badgeLevel;

    // Timestamp fields
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime emailVerifiedAt;

    // Social statistics fields
    private Integer followersCount;
    private Integer followingCount;
    private Integer ratingsCount;
    private Integer commentsCount;

    // Privacy settings fields
    private Boolean isProfilePublic;
    private Boolean showEmail;

    /* Constructors */
    public UserDTO() {
    }

    public UserDTO(Long id, String username, String displayName, UserRole role) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
    }

    /* Getters and Setters */
    // Core identification getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    // Role and verification getters and setters
    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Profile information getters and setters
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // Expertise and experience getters and setters
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

    public List<String> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<String> socialLinks) {
        this.socialLinks = socialLinks;
    }

    // Reputation and badge getters and setters
    public Integer getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(Integer reputationScore) {
        this.reputationScore = reputationScore;
    }

    public BadgeLevel getBadgeLevel() {
        return badgeLevel;
    }

    public void setBadgeLevel(BadgeLevel badgeLevel) {
        this.badgeLevel = badgeLevel;
    }

    // Timestamp getters and setters
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    // Social statistics getters and setters
    public Integer getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }

    public Integer getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
    }

    public Integer getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(Integer ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
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

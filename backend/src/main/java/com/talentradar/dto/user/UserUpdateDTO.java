package com.talentradar.dto.user;

import java.util.List;

import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for updating existing User entities. Contains optional
 * fields for profile updates with appropriate validation. Excludes
 * system-managed fields like ID, timestamps, and authentication data.
 */
public class UserUpdateDTO {

    // Basic profile information fields
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @Size(max = 200, message = "Organisation must not exceed 200 characters")
    private String organisation;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @Size(max = 200, message = "Website URL must not exceed 200 characters")
    private String websiteUrl;

    @Size(max = 500, message = "Profile image URL must not exceed 500 characters")
    private String profileImageUrl;

    // Expertise and social fields
    private List<String> expertiseLeagues;
    private List<String> expertisePositions;
    private List<String> socialLinks;

    // Privacy settings fields
    private Boolean isProfilePublic;
    private Boolean showEmail;

    /* Constructors */
    public UserUpdateDTO() {
    }

    /* Getters and Setters */
    // Basic profile information getters and setters
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

    // Expertise and social getters and setters
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

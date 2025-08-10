package com.talentradar.model.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import java.io.IOException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentradar.model.enums.BadgeLevel;
import com.talentradar.model.enums.UserRole;

/**
 * Entity representing users in the Talent Radar platform. Stores user
 * authentication details, profile information, preferences, and tracks user
 * engagement through badges, roles, and activity metrics.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "organization", length = 200)
    private String organisation;

    @Column(name = "expertise_leagues", columnDefinition = "JSON")
    private String expertiseLeaguesJson;

    @Column(name = "expertise_positions", columnDefinition = "JSON")
    private String expertisePositionsJson;

    @Column(name = "reputation_score")
    private Integer reputationScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_level")
    private BadgeLevel badgeLevel = BadgeLevel.BRONZE;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "website_url", length = 200)
    private String websiteUrl;

    @Column(name = "social_links", columnDefinition = "JSON")
    private String socialLinksJson;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_profile_public")
    private Boolean isProfilePublic = true;

    @Column(name = "show_email")
    private Boolean showEmail = false;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods for JSON fields
    public List<String> getExpertiseLeagues() {
        if (this.expertiseLeaguesJson == null) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.expertiseLeaguesJson, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void setExpertiseLeagues(List<String> leagues) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.expertiseLeaguesJson = mapper.writeValueAsString(leagues);
        } catch (IOException e) {
            this.expertiseLeaguesJson = "[]";
        }
    }

    public List<String> getExpertisePositions() {
        if (this.expertisePositionsJson == null) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.expertisePositionsJson, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void setExpertisePositions(List<String> positions) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.expertisePositionsJson = mapper.writeValueAsString(positions);
        } catch (IOException e) {
            this.expertisePositionsJson = "[]";
        }
    }

    public void updateBadgeLevel() {
        if (this.reputationScore >= 100) {
            this.badgeLevel = BadgeLevel.PLATINUM;
        } else if (this.reputationScore >= 50) {
            this.badgeLevel = BadgeLevel.GOLD;
        } else if (this.reputationScore >= 10) {
            this.badgeLevel = BadgeLevel.SILVER;
        } else {
            this.badgeLevel = BadgeLevel.BRONZE;
        }
    }

    // Helper methods
    public String getFullName() {
        if (this.firstName != null && this.lastName != null) {
            return this.firstName + " " + this.lastName;
        } else if (this.firstName != null) {
            return this.firstName;
        } else if (this.lastName != null) {
            return this.lastName;
        } else {
            return this.username;
        }
    }

    public String getDisplayName() {
        return getFullName();
    }

    public boolean canModerate() {
        return this.role == UserRole.ADMIN || this.role == UserRole.COACH
                || (this.role == UserRole.SCOUT && this.reputationScore >= 25);
    }

    public boolean isExpert() {
        return this.role == UserRole.SCOUT || this.role == UserRole.COACH
                || (this.reputationScore >= 50 && this.isVerified);
    }

    public List<String> getSocialLinks() {
        if (this.socialLinksJson == null) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.socialLinksJson, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void setSocialLinks(List<String> socialLinks) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.socialLinksJson = mapper.writeValueAsString(socialLinks);
        } catch (IOException e) {
            this.socialLinksJson = "[]";
        }
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
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

    public Integer getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(Integer reputationScore) {
        this.reputationScore = reputationScore;
        updateBadgeLevel();
    }

    public BadgeLevel getBadgeLevel() {
        return badgeLevel;
    }

    public void setBadgeLevel(BadgeLevel badgeLevel) {
        this.badgeLevel = badgeLevel;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

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

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{"
                + "id=" + id
                + ", username='" + username + '\''
                + ", email='" + email + '\''
                + ", role=" + role
                + ", reputationScore=" + reputationScore
                + ", badgeLevel=" + badgeLevel
                + '}';
    }
}

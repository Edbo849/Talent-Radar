package com.talentradar.dto.group;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for GroupMember entities. Used to transfer group
 * membership information between layers including user details, group roles,
 * permissions, and membership metadata.
 */
public class GroupMemberDTO {

    // Primary identifier
    private Long id;

    // Relationship identifiers
    private Long userId;
    private Long groupId;

    // User information (from User relationship)
    private String username;
    private String fullName;
    private String profileImageUrl;

    // Group role (enum: OWNER, ADMIN, MODERATOR, MEMBER)
    private String role;

    // User role in the platform (enum: USER, SCOUT, COACH, ADMIN)
    private String userRole;

    // User profile information (derived from User model)
    private String badgeLevel;
    private Integer reputationScore;
    private Boolean isVerified;
    private String organisation;

    // Membership metadata
    private LocalDateTime joinedAt;
    private Long invitedByUserId;
    private String invitedByUsername;

    // Timestamp fields
    private LocalDateTime createdAt;

    // Constructors
    public GroupMemberDTO() {
    }

    public GroupMemberDTO(Long id, Long userId, String username, String role) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    // Helper methods
    public boolean isOwner() {
        return "OWNER".equals(role);
    }

    public boolean isAdmin() {
        return "OWNER".equals(role) || "ADMIN".equals(role);
    }

    public boolean canModerate() {
        return "OWNER".equals(role) || "ADMIN".equals(role) || "MODERATOR".equals(role);
    }

    public String getDisplayName() {
        return fullName != null && !fullName.trim().isEmpty() ? fullName : username;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getBadgeLevel() {
        return badgeLevel;
    }

    public void setBadgeLevel(String badgeLevel) {
        this.badgeLevel = badgeLevel;
    }

    public Integer getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(Integer reputationScore) {
        this.reputationScore = reputationScore;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getOrganization() {
        return organisation;
    }

    public void setOrganization(String organisation) {
        this.organisation = organisation;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Long getInvitedByUserId() {
        return invitedByUserId;
    }

    public void setInvitedByUserId(Long invitedByUserId) {
        this.invitedByUserId = invitedByUserId;
    }

    public String getInvitedByUsername() {
        return invitedByUsername;
    }

    public void setInvitedByUsername(String invitedByUsername) {
        this.invitedByUsername = invitedByUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "GroupMemberDTO{"
                + "id=" + id
                + ", userId=" + userId
                + ", username='" + username + '\''
                + ", role='" + role + '\''
                + ", joinedAt=" + joinedAt
                + '}';
    }
}

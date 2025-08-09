package com.talentradar.dto.group;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for UserGroup entities. Used to transfer user group
 * information between layers including group details, membership information,
 * user permissions, and group statistics.
 */
public class UserGroupDTO {

    // Primary identifier
    private Long id;

    // Group name (required, max 100 characters)
    private String name;

    // Group description (max 1000 characters)
    private String description;

    // Type of group (enum: PUBLIC, PRIVATE, INVITE_ONLY)
    private String groupType;

    // Creator information (from User relationship)
    private Long createdByUserId;
    private String createdByUsername;
    private String createdByFullName;

    // Member statistics
    private Integer memberCount;
    private Integer maxMembers;

    // Group appearance
    private String groupImageUrl;

    // Group status
    private Boolean isActive;

    // Activity tracking
    private LocalDateTime lastActivityAt;

    // Timestamp fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for member context (derived fields)
    // Role of the requesting user in this group
    private String currentUserRole;
    private Boolean currentUserIsMember;
    private Boolean currentUserCanInvite;
    private Boolean currentUserCanModerate;

    // Recent members (for display)
    private List<GroupMemberDTO> recentMembers;

    // Group stats (derived fields)
    private Integer activeMembers;

    // Constructors
    public UserGroupDTO() {
    }

    public UserGroupDTO(Long id, String name, String groupType, String createdByUsername) {
        this.id = id;
        this.name = name;
        this.groupType = groupType;
        this.createdByUsername = createdByUsername;
    }

    // Helper methods
    public boolean isPublic() {
        return "PUBLIC".equals(groupType);
    }

    public boolean isPrivate() {
        return "PRIVATE".equals(groupType);
    }

    public boolean isInviteOnly() {
        return "INVITE_ONLY".equals(groupType);
    }

    public boolean isFull() {
        return maxMembers != null && memberCount != null && memberCount >= maxMembers;
    }

    public boolean canJoin() {
        return Boolean.TRUE.equals(isActive) && !isFull() && (isPublic() || isInviteOnly());
    }

    public String getCreatedByDisplayName() {
        return createdByFullName != null && !createdByFullName.trim().isEmpty()
                ? createdByFullName : createdByUsername;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public String getCreatedByFullName() {
        return createdByFullName;
    }

    public void setCreatedByFullName(String createdByFullName) {
        this.createdByFullName = createdByFullName;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public String getGroupImageUrl() {
        return groupImageUrl;
    }

    public void setGroupImageUrl(String groupImageUrl) {
        this.groupImageUrl = groupImageUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
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

    // Derived field getters and setters
    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public void setCurrentUserRole(String currentUserRole) {
        this.currentUserRole = currentUserRole;
    }

    public Boolean getCurrentUserIsMember() {
        return currentUserIsMember;
    }

    public void setCurrentUserIsMember(Boolean currentUserIsMember) {
        this.currentUserIsMember = currentUserIsMember;
    }

    public Boolean getCurrentUserCanInvite() {
        return currentUserCanInvite;
    }

    public void setCurrentUserCanInvite(Boolean currentUserCanInvite) {
        this.currentUserCanInvite = currentUserCanInvite;
    }

    public Boolean getCurrentUserCanModerate() {
        return currentUserCanModerate;
    }

    public void setCurrentUserCanModerate(Boolean currentUserCanModerate) {
        this.currentUserCanModerate = currentUserCanModerate;
    }

    public List<GroupMemberDTO> getRecentMembers() {
        return recentMembers;
    }

    public void setRecentMembers(List<GroupMemberDTO> recentMembers) {
        this.recentMembers = recentMembers;
    }

    public Integer getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(Integer activeMembers) {
        this.activeMembers = activeMembers;
    }

    @Override
    public String toString() {
        return "UserGroupDTO{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", groupType='" + groupType + '\''
                + ", memberCount=" + memberCount
                + ", isActive=" + isActive
                + '}';
    }
}

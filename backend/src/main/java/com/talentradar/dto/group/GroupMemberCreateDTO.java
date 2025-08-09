package com.talentradar.dto.group;

import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating new GroupMember entities. Used to transfer
 * group membership creation data from client to server with validation
 * constraints matching the model requirements.
 */
public class GroupMemberCreateDTO {

    // Required fields matching model constraints
    // Group reference (required)
    @NotNull(message = "Group ID is required")
    private Long groupId;

    // User reference (required)
    @NotNull(message = "User ID is required")
    private Long userId;

    // Group role (defaults to MEMBER, will be converted to GroupRole enum)
    private String role = "MEMBER";

    // Constructors
    public GroupMemberCreateDTO() {
    }

    public GroupMemberCreateDTO(Long groupId, Long userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public GroupMemberCreateDTO(Long groupId, Long userId, String role) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
    }

    // Getters and setters
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "GroupMemberCreateDTO{"
                + "groupId=" + groupId
                + ", userId=" + userId
                + ", role='" + role + '\''
                + '}';
    }
}

package com.talentradar.dto.group;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating new UserGroup entities. Used to transfer
 * group creation data from client to server with validation constraints
 * matching the model requirements.
 */
public class UserGroupCreateDTO {

    // Required fields matching model constraints
    // Group name (required, max 100 characters to match model)
    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name cannot exceed 100 characters")
    private String name;

    // Group description (max 1000 characters to match model)
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    // Group type (will be converted to GroupType enum)
    @NotBlank(message = "Group type is required")
    private String groupType;

    // Maximum members constraint
    @Min(value = 2, message = "Group must allow at least 2 members")
    @Max(value = 1000, message = "Group cannot exceed 1000 members")
    private Integer maxMembers = 50;

    // Constructors
    public UserGroupCreateDTO() {
    }

    public UserGroupCreateDTO(String name, String groupType) {
        this.name = name;
        this.groupType = groupType;
    }

    // Getters and setters
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

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    @Override
    public String toString() {
        return "UserGroupCreateDTO{"
                + "name='" + name + '\''
                + ", groupType='" + groupType + '\''
                + ", maxMembers=" + maxMembers
                + '}';
    }
}

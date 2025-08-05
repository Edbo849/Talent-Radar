package com.talentradar.model.enums;

/**
 * Enumeration representing different roles users can have within groups.
 * Defines hierarchical permissions for group management including content
 * moderation, member management, and administrative capabilities with varying
 * levels of access.
 */
public enum GroupRole {
    OWNER("Owner"),
    ADMIN("Administrator"),
    MODERATOR("Moderator"),
    MEMBER("Member");

    private final String displayName;

    GroupRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasAdminAccess() {
        return this == OWNER || this == ADMIN;
    }

    public boolean canModerate() {
        return this == OWNER || this == ADMIN || this == MODERATOR;
    }

    public boolean canInviteMembers() {
        return this == OWNER || this == ADMIN;
    }

    public boolean canRemoveMembers() {
        return this == OWNER || this == ADMIN;
    }

    public boolean canEditGroup() {
        return this == OWNER || this == ADMIN;
    }

    public boolean canDeleteGroup() {
        return this == OWNER;
    }

    public boolean canManageGroup() {
        return this == OWNER || this == ADMIN;
    }
}

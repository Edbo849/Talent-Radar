package com.talentradar.model.enums;

/**
 * Enumeration representing different user roles and permission levels within
 * the platform. Defines access control hierarchy from regular users to
 * administrators, enabling role-based functionality and specialized features
 * for different user types.
 */
public enum UserRole {
    USER("User"),
    SCOUT("Scout"),
    COACH("Coach"),
    ADMIN("Admin");

    private final String description;

    UserRole(String description
    ) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasAdminAccess() {
        return this == ADMIN;
    }

}

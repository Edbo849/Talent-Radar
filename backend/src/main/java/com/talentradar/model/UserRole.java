package com.talentradar.model;

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

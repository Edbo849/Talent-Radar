package com.talentradar.model.enums;

/**
 * Enumeration representing different types of user groups based on
 * accessibility and visibility. Determines group joining mechanisms, invitation
 * requirements, and public visibility for organising communities with
 * appropriate privacy and access controls.
 */
public enum GroupType {
    PUBLIC("Public"),
    PRIVATE("Private"),
    INVITE_ONLY("Invite Only");

    private final String displayName;

    GroupType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isJoinable() {
        return this == PUBLIC || this == INVITE_ONLY;
    }

    public boolean requiresInvitation() {
        return this == PRIVATE || this == INVITE_ONLY;
    }

    public boolean isVisible() {
        return this == PUBLIC || this == INVITE_ONLY;
    }
}

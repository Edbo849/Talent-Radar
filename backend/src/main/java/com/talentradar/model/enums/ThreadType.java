package com.talentradar.model.enums;

/**
 * Enumeration representing different categories of discussion threads.
 * Organizes forum content by topic type enabling better content discovery,
 * filtering, and specialized discussions around specific football-related
 * subjects.
 */
public enum ThreadType {
    GENERAL("General Discussion"),
    PLAYER_COMPARISON("Player Comparison"),
    MATCH_PERFORMANCE("Match Performance"),
    TRANSFER_SPECULATION("Transfer Speculation"),
    SCOUT_REPORT("Scout Report"),
    RISING_STARS("Rising Stars"),
    POLL("Poll");

    private final String displayName;

    ThreadType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ThreadType fromString(String type) {
        for (ThreadType threadType : ThreadType.values()) {
            if (threadType.name().equalsIgnoreCase(type)) {
                return threadType;
            }
        }
        throw new IllegalArgumentException("Unknown thread type: " + type);
    }
}

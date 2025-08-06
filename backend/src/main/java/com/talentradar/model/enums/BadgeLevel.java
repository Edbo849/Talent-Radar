package com.talentradar.model.enums;

/**
 * Enumeration representing different badge levels achievable by users in the
 * platform. Each level has an associated display name, color code, and minimum
 * reputation requirement. Used for gamification and user recognition based on
 * platform participation and contribution quality.
 */
public enum BadgeLevel {
    BRONZE("Bronze", "#CD7F32", 0),
    SILVER("Silver", "#C0C0C0", 10),
    GOLD("Gold", "#FFD700", 50),
    PLATINUM("Platinum", "#E5E4E2", 100);

    private final String displayName;
    private final String color;
    private final int requiredReputation;

    BadgeLevel(String displayName, String color, int requiredReputation) {
        this.displayName = displayName;
        this.color = color;
        this.requiredReputation = requiredReputation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public int getRequiredReputation() {
        return requiredReputation;
    }
}

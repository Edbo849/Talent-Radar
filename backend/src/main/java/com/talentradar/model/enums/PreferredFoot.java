package com.talentradar.model.enums;

/**
 * Enumeration representing a football player's preferred foot for playing. Used
 * to track player characteristics for scouting analysis, tactical planning, and
 * player profiling with support for left-footed, right-footed, and ambidextrous
 * players.
 */
public enum PreferredFoot {
    LEFT("Left"),
    RIGHT("Right"),
    BOTH("Both");

    private final String displayName;

    PreferredFoot(String displayName
    ) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static PreferredFoot fromString(String foot) {
        if (foot == null) {
            return null;
        }

        return switch (foot.toLowerCase().trim()) {
            case "left", "l" ->
                LEFT;
            case "right", "r" ->
                RIGHT;
            case "both", "ambidextrous" ->
                BOTH;
            default ->
                throw new IllegalArgumentException("Unknown preferred foot: " + foot);
        };
    }

}

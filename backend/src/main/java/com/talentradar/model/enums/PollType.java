package com.talentradar.model.enums;

/**
 * Enumeration representing different types of polls that can be created.
 * Defines poll interaction mechanisms including single selection, multiple
 * choices, rating scales, and binary yes/no questions for various community
 * engagement scenarios.
 */
public enum PollType {
    SINGLE_CHOICE("Single Choice"),
    MULTIPLE_CHOICE("Multiple Choice"),
    RATING("Rating"),
    YES_NO("Yes/No");

    private final String displayName;

    PollType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

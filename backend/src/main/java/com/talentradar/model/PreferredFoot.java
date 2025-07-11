package com.talentradar.model;

public enum PreferredFoot {
    LEFT("Left"),
    RIGHT("Right"),
    BOTH("Both");

    private final String displayName;

    PreferredFoot(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

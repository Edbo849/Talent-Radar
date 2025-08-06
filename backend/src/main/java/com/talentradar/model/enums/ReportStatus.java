package com.talentradar.model.enums;

/**
 * Enumeration representing the lifecycle status of scouting reports. Tracks
 * report progression from initial draft creation through publication to
 * archival, enabling workflow management and content organisation.
 */
public enum ReportStatus {
    DRAFT("Draft"),
    PUBLISHED("Published"),
    ARCHIVED("Archived");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

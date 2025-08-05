package com.talentradar.model.enums;

/**
 * Enumeration representing different types of messages that can be sent within
 * the platform. Categorizes message content including text, media files, and
 * system-generated messages for proper handling, display formatting, and
 * content processing.
 */
public enum MessageType {
    TEXT("Text"),
    IMAGE("Image"),
    FILE("File"),
    SYSTEM("System");

    private final String displayName;

    MessageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

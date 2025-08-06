package com.talentradar.model.enums;

/**
 * Enumeration representing different types of notifications sent to users.
 * Covers social interactions, system alerts, content updates, and platform
 * activities to keep users informed about relevant events and engagement
 * opportunities.
 */
public enum NotificationType {
    REPLY("Reply to your comment"),
    MENTION("You were mentioned"),
    VOTE("Your content was voted on"),
    FOLLOW("Someone followed you"),
    MESSAGE("New private message"),
    GROUP_INVITE("Group invitation"),
    RECOMMENDATION("New recommendation"),
    SYSTEM("System notification"),
    PLAYER_COMMENT("New comment on watched player"),
    THREAD_UPDATE("Thread you participated in was updated"),
    RATING("New player rating"),
    THREAD("New discussion thread"),
    POLL("New poll created"),
    GROUP_ROLE("Group role changed"),
    GROUP_REMOVAL("Removed from group"),
    COMMENT_REPLY("New reply on your comment"),
    REPORT("New report filed"),
    SCOUTING_REPORT("New scouting report");

    private final String description;

    NotificationType(String description
    ) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

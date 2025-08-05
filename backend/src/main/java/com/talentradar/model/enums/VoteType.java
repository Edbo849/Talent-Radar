package com.talentradar.model.enums;

/**
 * Enumeration representing voting actions on content within the platform.
 * Enables community-driven content quality assessment through upvoting and
 * downvoting mechanisms for comments, replies, and other user-generated
 * content.
 */
public enum VoteType {
    UPVOTE("Upvote"),
    DOWNVOTE("Downvote");

    private final String displayName;

    VoteType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPositive() {
        return this == UPVOTE;
    }

    public boolean isNegative() {
        return this == DOWNVOTE;
    }

    public VoteType opposite() {
        return this == UPVOTE ? DOWNVOTE : UPVOTE;
    }
}

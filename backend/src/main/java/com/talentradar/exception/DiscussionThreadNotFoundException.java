package com.talentradar.exception;

/**
 * Exception thrown when a discussion thread is not found in the system.
 */
public class DiscussionThreadNotFoundException extends RuntimeException {

    public DiscussionThreadNotFoundException(String message) {
        super(message);
    }

    public DiscussionThreadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

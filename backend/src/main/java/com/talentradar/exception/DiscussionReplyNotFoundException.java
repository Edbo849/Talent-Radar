package com.talentradar.exception;

/**
 * Exception thrown when a discussion reply is not found.
 */
public class DiscussionReplyNotFoundException extends RuntimeException {

    public DiscussionReplyNotFoundException(String message) {
        super(message);
    }

    public DiscussionReplyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

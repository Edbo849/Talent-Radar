package com.talentradar.exception;

/**
 * Exception thrown when a discussion category is not found.
 */
public class DiscussionCategoryNotFoundException extends RuntimeException {

    public DiscussionCategoryNotFoundException(String message) {
        super(message);
    }

    public DiscussionCategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

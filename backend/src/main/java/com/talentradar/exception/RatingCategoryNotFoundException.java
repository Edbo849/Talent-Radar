package com.talentradar.exception;

/**
 * Exception thrown when a rating category is not found in the system.
 */
public class RatingCategoryNotFoundException extends RuntimeException {

    public RatingCategoryNotFoundException(String message) {
        super(message);
    }

    public RatingCategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

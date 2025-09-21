package com.talentradar.exception;

/**
 * Exception thrown when a user recommendation is not found in the system.
 */
public class UserRecommendationNotFoundException extends RuntimeException {

    public UserRecommendationNotFoundException(String message) {
        super(message);
    }

    public UserRecommendationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

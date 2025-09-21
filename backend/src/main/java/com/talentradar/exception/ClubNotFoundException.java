package com.talentradar.exception;

/**
 * Exception thrown when a club is not found in the system.
 */
public class ClubNotFoundException extends RuntimeException {

    public ClubNotFoundException(String message) {
        super(message);
    }

    public ClubNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

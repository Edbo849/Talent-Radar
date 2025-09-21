package com.talentradar.exception;

/**
 * Exception thrown when a poll is not found in the system.
 */
public class PollNotFoundException extends RuntimeException {

    public PollNotFoundException(String message) {
        super(message);
    }

    public PollNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

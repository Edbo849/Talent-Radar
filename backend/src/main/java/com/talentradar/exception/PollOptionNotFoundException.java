package com.talentradar.exception;

/**
 * Exception thrown when a poll option is not found in the system.
 */
public class PollOptionNotFoundException extends RuntimeException {

    public PollOptionNotFoundException(String message) {
        super(message);
    }

    public PollOptionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

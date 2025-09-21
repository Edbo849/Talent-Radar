package com.talentradar.exception;

/**
 * Exception thrown when a requested group cannot be found in the system.
 */
public class GroupNotFoundException extends RuntimeException {

    public GroupNotFoundException(String message) {
        super(message);
    }

    public GroupNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

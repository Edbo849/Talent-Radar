package com.talentradar.exception;

/**
 * Exception thrown when a requested player cannot be found in the system.
 */
public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(String message) {
        super(message);
    }

    public PlayerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

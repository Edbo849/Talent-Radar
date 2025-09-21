package com.talentradar.exception;

/**
 * Exception thrown when a requested message cannot be found in the system.
 */
public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException(String message) {
        super(message);
    }

    public MessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

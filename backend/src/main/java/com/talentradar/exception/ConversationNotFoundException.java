package com.talentradar.exception;

/**
 * Exception thrown when a requested conversation cannot be found in the system.
 */
public class ConversationNotFoundException extends RuntimeException {

    public ConversationNotFoundException(String message) {
        super(message);
    }

    public ConversationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

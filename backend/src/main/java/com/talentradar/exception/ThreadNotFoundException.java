package com.talentradar.exception;

/**
 * Exception thrown when a discussion thread is not found in the system.
 */
public class ThreadNotFoundException extends RuntimeException {

    public ThreadNotFoundException(String message) {
        super(message);
    }

    public ThreadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

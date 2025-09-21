package com.talentradar.exception;

/**
 * Exception thrown when a league is not found in the system.
 */
public class LeagueNotFoundException extends RuntimeException {

    public LeagueNotFoundException(String message) {
        super(message);
    }

    public LeagueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.talentradar.exception;

/**
 * Exception thrown when a scouting report is not found in the system.
 */
public class ScoutingReportNotFoundException extends RuntimeException {

    public ScoutingReportNotFoundException(String message) {
        super(message);
    }

    public ScoutingReportNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

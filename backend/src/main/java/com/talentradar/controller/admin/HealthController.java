package com.talentradar.controller.admin;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Health check endpoints for the Talent Radar Backend
 */
@RestController
public class HealthController {

    /*
     * Root endpoint
     */
    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
                "status", "UP",
                "application", "Talent Radar Backend",
                "version", "1.0.0"
        );
    }

    /*
     * Health endpoint
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    /*
     * Status endpoint
     */
    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of(
                "status", "UP",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
    }

    /*
     * Error endpoint
     */
    @GetMapping("/error")
    public Map<String, String> error() {
        return Map.of(
                "status", "ERROR",
                "message", "This is a test error endpoint",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
    }
}

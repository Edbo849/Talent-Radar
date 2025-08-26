package com.talentradar.controller.admin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.service.external.DataPopulationService;
import com.talentradar.service.system.ScheduledTaskService;

/**
 * REST controller for managing scheduled tasks and data population operations.
 * Provides endpoints for monitoring and manually triggering data population
 * processes.
 */
@RestController
@RequestMapping("/admin/scheduled-tasks")
@CrossOrigin(origins = "http://localhost:3000")
public class ScheduledTaskController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskController.class);

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @Autowired
    private DataPopulationService dataPopulationService;

    /**
     * Retrieves the current status of scheduled tasks including population
     * status, last run time, and current player count.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getScheduledTaskStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("isPopulationRunning", scheduledTaskService.isPopulationRunning());
            response.put("lastRunTime", scheduledTaskService.getLastRunTime());
            response.put("lastRunStatus", scheduledTaskService.getLastRunStatus());
            response.put("currentPlayerCount", dataPopulationService.getU21PlayerCount());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get scheduled task status", e);
            response.put("error", "Failed to get status: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Manually triggers the data population process if not already running.
     */
    @PostMapping("/trigger-population")
    public ResponseEntity<Map<String, String>> triggerManualPopulation() {
        Map<String, String> response = new HashMap<>();

        try {
            if (scheduledTaskService.isPopulationRunning()) {
                response.put("status", "warning");
                response.put("message", "Data population is already running");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            scheduledTaskService.triggerManualPopulation();
            response.put("status", "success");
            response.put("message", "Manual data population triggered successfully");
            logger.info("Manual data population triggered successfully");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (Exception e) {
            logger.error("Failed to trigger manual population", e);
            response.put("status", "error");
            response.put("message", "Failed to trigger population: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Calculates and returns the next scheduled run time for data population.
     */
    @GetMapping("/next-scheduled-run")
    public ResponseEntity<Map<String, Object>> getNextScheduledRun() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Calculate next run time (2:00 AM tomorrow)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextRun = now.toLocalDate().plusDays(1).atTime(2, 0);

            response.put("nextScheduledRun", nextRun);
            response.put("hoursUntilNextRun", java.time.Duration.between(now, nextRun).toHours());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to calculate next run time", e);
            response.put("error", "Failed to calculate next run time: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

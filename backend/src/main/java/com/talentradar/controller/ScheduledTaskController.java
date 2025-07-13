package com.talentradar.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.service.DataPopulationService;
import com.talentradar.service.ScheduledTaskService;

@RestController
@RequestMapping("/admin/scheduled-tasks")
@CrossOrigin(origins = "http://localhost:3000")
public class ScheduledTaskController {

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @Autowired
    private DataPopulationService dataPopulationService;

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
            response.put("error", "Failed to get status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/trigger-population")
    public ResponseEntity<Map<String, String>> triggerManualPopulation() {
        Map<String, String> response = new HashMap<>();

        try {
            if (scheduledTaskService.isPopulationRunning()) {
                response.put("status", "warning");
                response.put("message", "Data population is already running");
                return ResponseEntity.ok(response);
            }

            scheduledTaskService.triggerManualPopulation();
            response.put("status", "success");
            response.put("message", "Manual data population triggered successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to trigger population: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/next-scheduled-run")
    public ResponseEntity<Map<String, Object>> getNextScheduledRun() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Calculate next run time (2:00 AM tomorrow)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextRun = now.toLocalDate().plusDays(1).atTime(2, 0);

            response.put("nextScheduledRun", nextRun);
            response.put("hoursUntilNextRun", java.time.Duration.between(now, nextRun).toHours());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to calculate next run time: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

package com.talentradar.service.system;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.talentradar.service.external.DataPopulationService;

/**
 * Service responsible for managing scheduled tasks. Handles automated data
 * population scheduling, monitoring, and task coordination.
 */
@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    @Qualifier("externalDataPopulationService")
    private DataPopulationService dataPopulationService;

    @Autowired
    private TaskScheduler taskScheduler;

    private volatile boolean isPopulationRunning = false;
    private volatile LocalDateTime lastRunTime;
    private volatile String lastRunStatus = "Never run";

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledDataPopulation() {
        if (isPopulationRunning) {
            logger.warn("Data population is already running, skipping this scheduled execution");
            return;
        }

        logger.info("Starting scheduled U21 player data population at {}",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            isPopulationRunning = true;
            lastRunTime = LocalDateTime.now();

            dataPopulationService.populateU21PlayersAsync();

            lastRunStatus = "Started successfully";
            logger.info("Scheduled data population started successfully");

        } catch (IllegalStateException e) {
            lastRunStatus = "Failed: Population service unavailable - " + e.getMessage();
            logger.error("Population service unavailable during scheduled execution: {}", e.getMessage());
        } catch (SecurityException e) {
            lastRunStatus = "Failed: Security error - " + e.getMessage();
            logger.error("Security error during scheduled data population: {}", e.getMessage());
        } catch (RuntimeException e) {
            lastRunStatus = "Failed: " + e.getMessage();
            logger.error("Runtime error during scheduled data population: {}", e.getMessage(), e);
        } finally {
            // We don't set isPopulationRunning to false here because the actual 
            // population runs asynchronously. We'll handle that in the completion callback.
        }
    }

    /**
     * Schedules the data population to resume tomorrow when API limits reset.
     */
    public void schedulePopulationForTomorrow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.toLocalDate().plusDays(1).atTime(LocalTime.of(0, 5)); // 5 minutes past midnight

        long delayInSeconds = ChronoUnit.SECONDS.between(now, tomorrow);

        logger.info("Scheduling data population to resume in {} seconds at {}", delayInSeconds, tomorrow);

        taskScheduler.schedule(() -> {
            logger.info("Daily API limit has reset. Resuming data population...");
            dataPopulationService.populateU21PlayersAsync();
        }, Instant.now().plusSeconds(delayInSeconds));
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public boolean isPopulationRunning() {
        return isPopulationRunning;
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public LocalDateTime getLastRunTime() {
        return lastRunTime;
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public String getLastRunStatus() {
        return lastRunStatus;
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void triggerManualPopulation() {
        try {
            if (isPopulationRunning) {
                logger.warn("Cannot trigger manual population - population is already running");
                throw new IllegalStateException("Data population is already running");
            }

            logger.info("Manual data population triggered");
            scheduledDataPopulation();

        } catch (IllegalStateException e) {
            logger.error("Failed to trigger manual population: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.error("Security error during manual population trigger: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error during manual population trigger: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to trigger manual population", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void onPopulationComplete(boolean success, String message) {
        try {
            isPopulationRunning = false;

            if (success) {
                lastRunStatus = "Completed successfully";
                logger.info("Data population completed successfully");
            } else {
                lastRunStatus = message != null && !message.trim().isEmpty()
                        ? "Failed: " + message.trim()
                        : "Failed: Unknown error";
                logger.error("Data population failed: {}", lastRunStatus);
            }

        } catch (SecurityException e) {
            logger.error("Security error during population completion callback: {}", e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Runtime error during population completion callback: {}", e.getMessage(), e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public long getCurrentPlayerCount() {
        try {
            return dataPopulationService.getU21PlayerCount();
        } catch (IllegalStateException e) {
            logger.error("Data service unavailable for player count: {}", e.getMessage());
            return -1L;
        } catch (SecurityException e) {
            logger.error("Security error getting player count: {}", e.getMessage());
            return -1L;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting player count: {}", e.getMessage(), e);
            return -1L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public boolean isDataPopulationServiceAvailable() {
        try {
            if (dataPopulationService == null) {
                return false;
            }

            // Test if service is responsive by getting player count
            dataPopulationService.getU21PlayerCount();
            return true;

        } catch (IllegalStateException e) {
            logger.debug("Data population service unavailable: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            logger.debug("Security restriction on data population service: {}", e.getMessage());
            return false;
        } catch (RuntimeException e) {
            logger.debug("Data population service not responsive: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public long getTimeSinceLastRun() {
        try {
            if (lastRunTime == null) {
                return -1L;
            }
            return java.time.Duration.between(lastRunTime, LocalDateTime.now()).toMinutes();
        } catch (RuntimeException e) {
            logger.error("Error calculating time since last run: {}", e.getMessage());
            return -1L;
        }
    }
}

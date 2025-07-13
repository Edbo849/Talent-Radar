package com.talentradar.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private DataPopulationService dataPopulationService;

    private boolean isPopulationRunning = false;
    private LocalDateTime lastRunTime;
    private String lastRunStatus = "Never run";

    /**
     * Run data population every day at 2:00 AM This timing ensures minimal API
     * usage conflicts and gives fresh data daily
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

        } catch (Exception e) {
            lastRunStatus = "Failed: " + e.getMessage();
            logger.error("Error during scheduled data population: {}", e.getMessage(), e);
        } finally {
            // We don't set isPopulationRunning to false here because the actual 
            // population runs asynchronously. We'll handle that in the completion callback.
        }

    }

    /**
     * Run a lighter data check every 6 hours to monitor for new players This
     * uses fewer API calls but keeps our data more current
     */
    @Scheduled(fixedRate = 21600000) // 6 hours in milliseconds
    public void scheduledDataCheck() {
        if (isPopulationRunning) {
            logger.debug("Skipping data check - full population is running");
            return;
        }

        logger.info("Running scheduled data check at {}",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            long currentPlayerCount = dataPopulationService.getU21PlayerCount();
            logger.info("Current U21 player count: {}", currentPlayerCount);

            // If we have very few players, trigger a full population
            if (currentPlayerCount < 500) {
                logger.info("Low player count detected, triggering full data population");
                scheduledDataPopulation();
            }

        } catch (Exception e) {
            logger.error("Error during scheduled data check: {}", e.getMessage(), e);
        }
    }

    public boolean isPopulationRunning() {
        return isPopulationRunning;
    }

    public LocalDateTime getLastRunTime() {
        return lastRunTime;
    }

    public String getLastRunStatus() {
        return lastRunStatus;
    }

    // Method to manually trigger population (useful for testing)
    public void triggerManualPopulation() {
        logger.info("Manual data population triggered");
        scheduledDataPopulation();
    }

    // Callback method for when async population completes
    public void onPopulationComplete(boolean success, String message) {
        isPopulationRunning = false;
        lastRunStatus = success ? "Completed successfully" : "Failed: " + message;
        logger.info("Data population completed. Status: {}", lastRunStatus);
    }
}

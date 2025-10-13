package com.talentradar.service.scouting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.PlayerNotFoundException;
import com.talentradar.exception.ScoutingReportNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.enums.ReportStatus;
import com.talentradar.model.player.Player;
import com.talentradar.model.scouting.ScoutingReport;
import com.talentradar.model.user.User;
import com.talentradar.repository.player.PlayerRepository;
import com.talentradar.repository.scouting.ScoutingReportRepository;
import com.talentradar.service.notification.NotificationService;

/**
 * Service responsible for managing scouting reports. Handles scouting report
 * CRUD operations, publication, search, and analytics.
 */
@Service
@Transactional
public class ScoutingReportService {

    private static final Logger logger = LoggerFactory.getLogger(ScoutingReportService.class);

    @Autowired
    private ScoutingReportRepository reportRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public ScoutingReport createReport(Long playerId, User scout, String title, String content,
            LocalDate matchDate, String opponentClub, Integer overallRating, Integer technicalRating,
            Integer physicalRating, Integer mentalRating, Boolean isPublic, String strengths,
            String weaknesses, String recommendations) {
        try {
            if (playerId == null) {
                throw new PlayerNotFoundException("Player ID cannot be null");
            }
            if (scout == null) {
                throw new UserNotFoundException("Scout cannot be null");
            }
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Report title cannot be null or empty");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("Report content cannot be null or empty");
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            ScoutingReport report = new ScoutingReport(player, scout, title.trim(), content.trim());
            report.setMatchDate(matchDate);
            report.setOpponentClub(opponentClub != null ? opponentClub.trim() : null);
            report.setOverallRating(overallRating);
            report.setTechnicalRating(technicalRating);
            report.setPhysicalRating(physicalRating);
            report.setMentalRating(mentalRating);
            report.setIsPublic(isPublic != null ? isPublic : false);
            report.setStrengths(strengths != null ? strengths.trim() : null);
            report.setWeaknesses(weaknesses != null ? weaknesses.trim() : null);
            report.setRecommendations(recommendations != null ? recommendations.trim() : null);
            report.setStatus(ReportStatus.DRAFT);

            // Validate ratings consistency before saving
            try {
                report.validateRatings();
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid rating values: " + e.getMessage());
            }

            ScoutingReport savedReport = reportRepository.save(report);

            // Notify if report is public
            if (Boolean.TRUE.equals(isPublic)) {
                try {
                    notificationService.notifyOfNewScoutingReport(savedReport);
                } catch (Exception e) {
                    logger.warn("Failed to send scouting report notification: {}", e.getMessage());
                }
            }

            logger.info("Created scouting report '{}' for player {} by scout {}",
                    title, player.getName(), scout.getUsername());
            return savedReport;

        } catch (PlayerNotFoundException | UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error creating scouting report: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error creating scouting report for player {} by scout {}: {}",
                    playerId, scout != null ? scout.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to create scouting report", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<ScoutingReport> getPlayerReports(Long playerId, Boolean publicOnly, Pageable pageable) {
        try {
            if (playerId == null) {
                throw new PlayerNotFoundException("Player ID cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            if (Boolean.TRUE.equals(publicOnly)) {
                return reportRepository.findByPlayerAndIsPublicTrueOrderByCreatedAtDesc(player, pageable);
            } else {
                return reportRepository.findByPlayerOrderByCreatedAtDesc(player, pageable);
            }

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting player reports: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving reports for player {}: {}", playerId, e.getMessage());
            throw new RuntimeException("Failed to get player reports", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<ScoutingReport> getScoutReports(User scout, Pageable pageable) {
        try {
            if (scout == null) {
                throw new UserNotFoundException("Scout cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return reportRepository.findByScoutOrderByCreatedAtDesc(scout, pageable);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting scout reports: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving reports for scout {}: {}",
                    scout != null ? scout.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to get scout reports", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<ScoutingReport> getPublicReports(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return reportRepository.findByIsPublicTrueAndStatusOrderByCreatedAtDesc(ReportStatus.PUBLISHED, pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting public reports: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving public reports: {}", e.getMessage());
            throw new RuntimeException("Failed to get public reports", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<ScoutingReport> searchReports(String searchTerm, Pageable pageable) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                throw new IllegalArgumentException("Search term cannot be null or empty");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return reportRepository.findByFullTextSearchAndIsPublicTrue(searchTerm.trim(), pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error searching reports: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching reports with term '{}': {}", searchTerm, e.getMessage());
            throw new RuntimeException("Failed to search reports", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<ScoutingReport> getReportById(Long reportId) {
        try {
            if (reportId == null) {
                logger.warn("Attempted to find scouting report with null ID");
                return Optional.empty();
            }
            return reportRepository.findById(reportId);
        } catch (Exception e) {
            logger.error("Error finding scouting report by ID {}: {}", reportId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public ScoutingReport updateReport(Long reportId, User requester, String title, String content,
            LocalDate matchDate, String opponentClub, Integer overallRating, Integer technicalRating,
            Integer physicalRating, Integer mentalRating, Boolean isPublic, String strengths,
            String weaknesses, String recommendations) {
        try {
            if (reportId == null) {
                throw new ScoutingReportNotFoundException("Report ID cannot be null");
            }
            if (requester == null) {
                throw new UserNotFoundException("Requester cannot be null");
            }

            ScoutingReport report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ScoutingReportNotFoundException("Report not found with ID: " + reportId));

            // Check permissions
            if (!report.getScout().getId().equals(requester.getId()) && !requester.canModerate()) {
                throw new IllegalStateException("Cannot edit this report");
            }

            // Update fields with validation
            if (title != null) {
                if (title.trim().isEmpty()) {
                    throw new IllegalArgumentException("Report title cannot be empty");
                }
                report.setTitle(title.trim());
            }
            if (content != null) {
                if (content.trim().isEmpty()) {
                    throw new IllegalArgumentException("Report content cannot be empty");
                }
                report.setContent(content.trim());
            }
            if (matchDate != null) {
                report.setMatchDate(matchDate);
            }
            if (opponentClub != null) {
                report.setOpponentClub(opponentClub.trim());
            }
            if (overallRating != null) {
                report.setOverallRating(overallRating);
            }
            if (technicalRating != null) {
                report.setTechnicalRating(technicalRating);
            }
            if (physicalRating != null) {
                report.setPhysicalRating(physicalRating);
            }
            if (mentalRating != null) {
                report.setMentalRating(mentalRating);
            }
            if (isPublic != null) {
                report.setIsPublic(isPublic);
            }
            if (strengths != null) {
                report.setStrengths(strengths.trim());
            }
            if (weaknesses != null) {
                report.setWeaknesses(weaknesses.trim());
            }
            if (recommendations != null) {
                report.setRecommendations(recommendations.trim());
            }

            // Validate ratings consistency before saving
            try {
                report.validateRatings();
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid rating values: " + e.getMessage());
            }

            ScoutingReport savedReport = reportRepository.save(report);
            logger.info("Updated scouting report {} by user {}", reportId, requester.getUsername());

            return savedReport;

        } catch (ScoutingReportNotFoundException | UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error updating scouting report: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating scouting report {} by user {}: {}",
                    reportId, requester != null ? requester.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to update scouting report", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void publishReport(Long reportId, User requester) {
        try {
            if (reportId == null) {
                throw new ScoutingReportNotFoundException("Report ID cannot be null");
            }
            if (requester == null) {
                throw new UserNotFoundException("Requester cannot be null");
            }

            ScoutingReport report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ScoutingReportNotFoundException("Report not found with ID: " + reportId));

            // Check permissions
            if (!report.getScout().getId().equals(requester.getId()) && !requester.canModerate()) {
                throw new IllegalStateException("Cannot publish this report");
            }

            report.setStatus(ReportStatus.PUBLISHED);
            report.setIsPublic(true);
            ScoutingReport savedReport = reportRepository.save(report);

            try {
                notificationService.notifyOfNewScoutingReport(savedReport);
            } catch (Exception e) {
                logger.warn("Failed to send publication notification: {}", e.getMessage());
            }

            logger.info("Published scouting report {} by user {}", reportId, requester.getUsername());

        } catch (ScoutingReportNotFoundException | UserNotFoundException | IllegalStateException e) {
            logger.error("Error publishing scouting report: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error publishing scouting report {} by user {}: {}",
                    reportId, requester != null ? requester.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to publish scouting report", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void unpublishReport(Long reportId, User requester) {
        try {
            if (reportId == null) {
                throw new ScoutingReportNotFoundException("Report ID cannot be null");
            }
            if (requester == null) {
                throw new UserNotFoundException("Requester cannot be null");
            }

            ScoutingReport report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ScoutingReportNotFoundException("Report not found with ID: " + reportId));

            // Check permissions
            if (!report.getScout().getId().equals(requester.getId()) && !requester.canModerate()) {
                throw new IllegalStateException("Cannot unpublish this report");
            }

            report.setStatus(ReportStatus.DRAFT);
            report.setIsPublic(false);
            reportRepository.save(report);

            logger.info("Unpublished scouting report {} by user {}", reportId, requester.getUsername());

        } catch (ScoutingReportNotFoundException | UserNotFoundException | IllegalStateException e) {
            logger.error("Error unpublishing scouting report: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error unpublishing scouting report {} by user {}: {}",
                    reportId, requester != null ? requester.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to unpublish scouting report", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteReport(Long reportId, User requester) {
        try {
            if (reportId == null) {
                throw new ScoutingReportNotFoundException("Report ID cannot be null");
            }
            if (requester == null) {
                throw new UserNotFoundException("Requester cannot be null");
            }

            ScoutingReport report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ScoutingReportNotFoundException("Report not found with ID: " + reportId));

            // Check permissions
            if (!report.getScout().getId().equals(requester.getId()) && !requester.canModerate()) {
                throw new IllegalStateException("Cannot delete this report");
            }

            reportRepository.delete(report);
            logger.info("Deleted scouting report {} by user {}", reportId, requester.getUsername());

        } catch (ScoutingReportNotFoundException | UserNotFoundException | IllegalStateException e) {
            logger.error("Error deleting scouting report: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deleting scouting report {} by user {}: {}",
                    reportId, requester != null ? requester.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to delete scouting report", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<ScoutingReport> getRecentReports(int limit) {
        try {
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be positive");
            }
            if (limit > 100) {
                throw new IllegalArgumentException("Limit cannot exceed 100");
            }

            return reportRepository.findTopNByIsPublicTrueOrderByCreatedAtDesc(limit);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid limit for recent reports: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting recent reports with limit {}: {}", limit, e.getMessage());
            throw new RuntimeException("Failed to get recent reports", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<ScoutingReport> getTopRatedReports(int limit) {
        try {
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be positive");
            }
            if (limit > 100) {
                throw new IllegalArgumentException("Limit cannot exceed 100");
            }

            return reportRepository.findTopNByIsPublicTrueOrderByOverallRatingDesc(limit);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid limit for top rated reports: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting top rated reports with limit {}: {}", limit, e.getMessage());
            throw new RuntimeException("Failed to get top rated reports", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getReportCountByScout(User scout) {
        try {
            if (scout == null) {
                throw new UserNotFoundException("Scout cannot be null");
            }

            return reportRepository.countByScout(scout);

        } catch (UserNotFoundException e) {
            logger.error("Error getting report count by scout: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting report count for scout {}: {}",
                    scout != null ? scout.getUsername() : "null", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getPublicReportCountByScout(User scout) {
        try {
            if (scout == null) {
                throw new UserNotFoundException("Scout cannot be null");
            }

            return reportRepository.countByScoutAndIsPublicTrue(scout);

        } catch (UserNotFoundException e) {
            logger.error("Error getting public report count by scout: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting public report count for scout {}: {}",
                    scout != null ? scout.getUsername() : "null", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public double getAverageRatingForPlayer(Long playerId) {
        try {
            if (playerId == null) {
                throw new PlayerNotFoundException("Player ID cannot be null");
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            Double averageRating = reportRepository.getAverageOverallRatingByPlayerAndIsPublicTrue(player);
            return averageRating != null ? averageRating : 0.0;

        } catch (PlayerNotFoundException e) {
            logger.error("Error getting average rating for player: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error calculating average rating for player {}: {}", playerId, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Gets recent public reports with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ScoutingReport> getRecentPublicReports(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return reportRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting recent public reports: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving recent public reports: {}", e.getMessage());
            throw new RuntimeException("Failed to get recent public reports", e);
        }
    }

    /**
     * Counts total reports by scout.
     */
    @Transactional(readOnly = true)
    public Long countByScout(User scout) {
        try {
            if (scout == null) {
                throw new UserNotFoundException("Scout cannot be null");
            }

            return reportRepository.countByScout(scout);

        } catch (UserNotFoundException e) {
            logger.error("Error counting reports by scout: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error counting reports for scout {}: {}",
                    scout != null ? scout.getUsername() : "null", e.getMessage());
            return 0L;
        }
    }

    /**
     * Counts reports by scout and status.
     */
    @Transactional(readOnly = true)
    public Long countByScoutAndStatus(User scout, ReportStatus status) {
        try {
            if (scout == null) {
                throw new UserNotFoundException("Scout cannot be null");
            }
            if (status == null) {
                throw new IllegalArgumentException("Status cannot be null");
            }

            return reportRepository.countByScoutAndStatus(scout, status);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error counting reports by scout and status: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error counting reports for scout {} with status {}: {}",
                    scout != null ? scout.getUsername() : "null", status, e.getMessage());
            return 0L;
        }
    }

    /**
     * Gets reports by scout with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ScoutingReport> getReportsByScout(User scout, Pageable pageable) {
        try {
            if (scout == null) {
                throw new UserNotFoundException("Scout cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return reportRepository.findByScoutOrderByCreatedAtDesc(scout, pageable);

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting reports by scout: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving reports for scout {}: {}",
                    scout != null ? scout.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to get reports by scout", e);
        }
    }
}

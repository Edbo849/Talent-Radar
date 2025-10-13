package com.talentradar.controller.scouting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.scouting.ScoutingReportCreateDTO;
import com.talentradar.dto.scouting.ScoutingReportDTO;
import com.talentradar.exception.ScoutingReportNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.enums.ReportStatus;
import com.talentradar.model.scouting.ScoutingReport;
import com.talentradar.model.user.User;
import com.talentradar.service.scouting.ScoutingReportService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for managing scouting reports. Provides endpoints for
 * creating, retrieving, and managing detailed player scouting reports.
 */
@RestController
@RequestMapping("/api/scouting-reports")
@CrossOrigin(origins = "http://localhost:3000")
public class ScoutingReportController {

    private static final Logger logger = LoggerFactory.getLogger(ScoutingReportController.class);

    @Autowired
    private ScoutingReportService scoutingReportService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves paginated public scouting reports.
     */
    @GetMapping
    public ResponseEntity<Page<ScoutingReportDTO>> getPublicReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ScoutingReport> reports = scoutingReportService.getPublicReports(pageable);
            Page<ScoutingReportDTO> reportDTOs = reports.map(this::convertToDTO);

            logger.info("Retrieved {} public scouting reports", reports.getNumberOfElements());
            return ResponseEntity.ok(reportDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving public reports: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve public reports", e);
        }
    }

    /**
     * Retrieves scouting reports for a specific player.
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<Page<ScoutingReportDTO>> getPlayerReports(
            @PathVariable Long playerId,
            @RequestParam(defaultValue = "true") Boolean publicOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ScoutingReport> reports = scoutingReportService.getPlayerReports(playerId, publicOnly, pageable);
            Page<ScoutingReportDTO> reportDTOs = reports.map(this::convertToDTO);

            logger.info("Retrieved {} reports for player ID: {}", reports.getNumberOfElements(), playerId);
            return ResponseEntity.ok(reportDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving player reports: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve player reports", e);
        }
    }

    /**
     * Retrieves scouting reports created by a specific scout.
     */
    @GetMapping("/scout/{scoutId}")
    public ResponseEntity<Page<ScoutingReportDTO>> getScoutReports(
            @PathVariable Long scoutId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            User scout = userService.findById(scoutId)
                    .orElseThrow(() -> new UserNotFoundException("Scout not found with ID: " + scoutId));

            Pageable pageable = PageRequest.of(page, size);
            Page<ScoutingReport> reports = scoutingReportService.getScoutReports(scout, pageable);
            Page<ScoutingReportDTO> reportDTOs = reports.map(this::convertToDTO);

            logger.info("Retrieved {} reports for scout ID: {}", reports.getNumberOfElements(), scoutId);
            return ResponseEntity.ok(reportDTOs);

        } catch (UserNotFoundException e) {
            logger.error("Scout not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving scout reports: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve scout reports", e);
        }
    }

    /**
     * Retrieves a specific scouting report by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScoutingReportDTO> getReportById(@PathVariable Long id) {
        try {
            ScoutingReport report = scoutingReportService.getReportById(id)
                    .orElseThrow(() -> new ScoutingReportNotFoundException("Report not found with ID: " + id));

            logger.info("Retrieved scouting report with ID: {}", id);
            return ResponseEntity.ok(convertToDTO(report));

        } catch (ScoutingReportNotFoundException e) {
            logger.error("Scouting report not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving scouting report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve scouting report", e);
        }
    }

    /**
     * Searches scouting reports based on query criteria.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ScoutingReportDTO>> searchReports(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("Search query is required");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<ScoutingReport> reports = scoutingReportService.searchReports(query, pageable);
            Page<ScoutingReportDTO> reportDTOs = reports.map(this::convertToDTO);

            logger.info("Found {} reports for query: {}", reports.getNumberOfElements(), query);
            return ResponseEntity.ok(reportDTOs);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid search query: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching reports: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search reports", e);
        }
    }

    /**
     * Creates a new scouting report.
     */
    @PostMapping
    public ResponseEntity<ScoutingReportDTO> createReport(
            @Valid @RequestBody ScoutingReportCreateDTO createDTO,
            HttpServletRequest request) {

        try {
            // TODO: Get scout from authentication context instead of using fallback
            User scout = userService.getCurrentUserOrNull(request);
            if (scout == null) {
                throw new UserNotFoundException("User authentication required");
            }

            ScoutingReport report = scoutingReportService.createReport(
                    createDTO.getPlayerId(),
                    scout,
                    createDTO.getTitle(),
                    createDTO.getContent(),
                    createDTO.getMatchDate(),
                    createDTO.getOpponentClub(),
                    createDTO.getOverallRating(),
                    createDTO.getTechnicalRating(),
                    createDTO.getPhysicalRating(),
                    createDTO.getMentalRating(),
                    createDTO.getIsPublic(),
                    createDTO.getStrengths(),
                    createDTO.getWeaknesses(),
                    createDTO.getRecommendations()
            );

            logger.info("Created new scouting report with ID: {} by scout: {}", report.getId(), scout.getUsername());
            return ResponseEntity.ok(convertToDTO(report));

        } catch (UserNotFoundException e) {
            logger.error("User not found while creating report: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid report data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating scouting report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create scouting report", e);
        }
    }

    /**
     * Updates an existing scouting report.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ScoutingReportDTO> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ScoutingReportCreateDTO updateDTO,
            HttpServletRequest request) {

        try {
            User requester = userService.getCurrentUserOrNull(request);
            if (requester == null) {
                throw new UserNotFoundException("User authentication required");
            }

            ScoutingReport report = scoutingReportService.updateReport(
                    id,
                    requester,
                    updateDTO.getTitle(),
                    updateDTO.getContent(),
                    updateDTO.getMatchDate(),
                    updateDTO.getOpponentClub(),
                    updateDTO.getOverallRating(),
                    updateDTO.getTechnicalRating(),
                    updateDTO.getPhysicalRating(),
                    updateDTO.getMentalRating(),
                    updateDTO.getIsPublic(),
                    updateDTO.getStrengths(),
                    updateDTO.getWeaknesses(),
                    updateDTO.getRecommendations()
            );

            logger.info("Updated scouting report with ID: {} by user: {}", id, requester.getUsername());
            return ResponseEntity.ok(convertToDTO(report));

        } catch (ScoutingReportNotFoundException | UserNotFoundException e) {
            logger.error("Entity not found while updating report: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Invalid report update: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error updating scouting report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update scouting report", e);
        }
    }

    /**
     * Publishes a scouting report.
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<Void> publishReport(
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            User requester = userService.getCurrentUserOrNull(request);
            if (requester == null) {
                throw new UserNotFoundException("User authentication required");
            }

            scoutingReportService.publishReport(id, requester);
            logger.info("Published scouting report with ID: {} by user: {}", id, requester.getUsername());
            return ResponseEntity.ok().build();

        } catch (ScoutingReportNotFoundException | UserNotFoundException e) {
            logger.error("Entity not found while publishing report: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Cannot publish report: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error publishing scouting report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish scouting report", e);
        }
    }

    /**
     * Unpublishes a scouting report.
     */
    @PutMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublishReport(
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            User requester = userService.getCurrentUserOrNull(request);
            if (requester == null) {
                throw new UserNotFoundException("User authentication required");
            }

            scoutingReportService.unpublishReport(id, requester);
            logger.info("Unpublished scouting report with ID: {} by user: {}", id, requester.getUsername());
            return ResponseEntity.ok().build();

        } catch (ScoutingReportNotFoundException | UserNotFoundException e) {
            logger.error("Entity not found while unpublishing report: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Cannot unpublish report: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error unpublishing scouting report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to unpublish scouting report", e);
        }
    }

    /**
     * Deletes a scouting report.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long id,
            HttpServletRequest request) {

        try {
            User requester = userService.getCurrentUserOrNull(request);
            if (requester == null) {
                throw new UserNotFoundException("User authentication required");
            }

            scoutingReportService.deleteReport(id, requester);
            logger.info("Deleted scouting report with ID: {} by user: {}", id, requester.getUsername());
            return ResponseEntity.ok().build();

        } catch (ScoutingReportNotFoundException | UserNotFoundException e) {
            logger.error("Entity not found while deleting report: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Cannot delete report: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deleting scouting report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete scouting report", e);
        }
    }

    /**
     * Retrieves recent scouting reports.
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ScoutingReportDTO>> getRecentReports(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            if (limit <= 0 || limit > 100) {
                throw new IllegalArgumentException("Limit must be between 1 and 100");
            }

            List<ScoutingReport> reports = scoutingReportService.getRecentReports(limit);
            List<ScoutingReportDTO> reportDTOs = reports.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} recent scouting reports", reports.size());
            return ResponseEntity.ok(reportDTOs);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid limit parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving recent reports: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve recent reports", e);
        }
    }

    /**
     * Retrieves top-rated scouting reports.
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<ScoutingReportDTO>> getTopRatedReports(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            if (limit <= 0 || limit > 100) {
                throw new IllegalArgumentException("Limit must be between 1 and 100");
            }

            List<ScoutingReport> reports = scoutingReportService.getTopRatedReports(limit);
            List<ScoutingReportDTO> reportDTOs = reports.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} top-rated scouting reports", reports.size());
            return ResponseEntity.ok(reportDTOs);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid limit parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving top-rated reports: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve top-rated reports", e);
        }
    }

    /**
     * Get latest public reports
     */
    @GetMapping("/public/latest")
    public ResponseEntity<List<ScoutingReportDTO>> getLatestPublicReports(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            Page<ScoutingReport> reports = scoutingReportService.getRecentPublicReports(pageable);
            List<ScoutingReportDTO> reportDTOs = reports.getContent().stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} latest public reports", reportDTOs.size());
            return ResponseEntity.ok(reportDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving latest public reports", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user's report statistics
     */
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getMyReportStats(
            HttpServletRequest request) {
        try {
            User user = userService.getCurrentUser(request);

            Map<String, Object> stats = new HashMap<>();

            // Get report counts
            Long totalReports = scoutingReportService.countByScout(user);
            Long draftReports = scoutingReportService.countByScoutAndStatus(user, ReportStatus.DRAFT);
            Long publishedReports = scoutingReportService.countByScoutAndStatus(user, ReportStatus.PUBLISHED);

            stats.put("totalReports", totalReports);
            stats.put("draftReports", draftReports);
            stats.put("publishedReports", publishedReports);

            // Get recent reports
            Pageable pageable = PageRequest.of(0, 5);
            Page<ScoutingReport> recentReports = scoutingReportService.getReportsByScout(user, pageable);
            stats.put("recentReports", recentReports.getContent().stream()
                    .map(this::convertToDTO)
                    .toList());

            logger.info("Retrieved report stats for scout: {}", user.getUsername());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error retrieving scout report stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieves average rating for a specific player.
     */
    @GetMapping("/player/{playerId}/average-rating")
    public ResponseEntity<Double> getPlayerAverageRating(@PathVariable Long playerId) {
        try {
            double averageRating = scoutingReportService.getAverageRatingForPlayer(playerId);
            logger.info("Retrieved average rating {} for player ID: {}", averageRating, playerId);
            return ResponseEntity.ok(averageRating);

        } catch (Exception e) {
            logger.error("Error retrieving player average rating: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve player average rating", e);
        }
    }

    /**
     * Converts ScoutingReport entity to ScoutingReportDTO.
     */
    private ScoutingReportDTO convertToDTO(ScoutingReport report) {
        ScoutingReportDTO dto = new ScoutingReportDTO();
        dto.setId(report.getId());
        dto.setPlayerId(report.getPlayer().getId());
        dto.setPlayerName(report.getPlayer().getName());
        dto.setScoutId(report.getScout().getId());
        dto.setScoutName(report.getScout().getUsername());
        dto.setTitle(report.getTitle());
        dto.setContent(report.getContent());
        dto.setMatchDate(report.getMatchDate());
        dto.setOpponentClub(report.getOpponentClub());
        dto.setOverallRating(report.getOverallRating());
        dto.setTechnicalRating(report.getTechnicalRating());
        dto.setPhysicalRating(report.getPhysicalRating());
        dto.setMentalRating(report.getMentalRating());
        dto.setStatus(report.getStatus() != null ? report.getStatus().toString() : null);
        dto.setIsPublic(report.getIsPublic());
        dto.setStrengths(report.getStrengths());
        dto.setWeaknesses(report.getWeaknesses());
        dto.setRecommendations(report.getRecommendations());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());
        return dto;
    }
}

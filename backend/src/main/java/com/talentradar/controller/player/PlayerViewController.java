package com.talentradar.controller.player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.player.PlayerViewDTO;
import com.talentradar.exception.PlayerNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerView;
import com.talentradar.model.user.User;
import com.talentradar.service.player.PlayerService;
import com.talentradar.service.player.PlayerViewService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller for managing player view tracking and analytics. Provides
 * endpoints for recording player views and retrieving view statistics with
 * proper error handling and validation.
 */
@RestController
@RequestMapping("/api/players/{playerId}/views")
@CrossOrigin(origins = "http://localhost:3000")
public class PlayerViewController {

    private static final Logger logger = LoggerFactory.getLogger(PlayerViewController.class);

    @Autowired
    private PlayerViewService viewService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private UserService userService;

    /**
     * Records a view for a specific player.
     */
    @PostMapping
    public ResponseEntity<Void> recordPlayerView(
            @PathVariable Long playerId,
            HttpServletRequest request) {

        try {
            // Try to get authenticated user, but allow anonymous views
            User user = null;
            try {
                user = userService.getCurrentUser(request);
            } catch (UserNotFoundException e) {
                // Anonymous view - this is allowed
                logger.debug("Recording anonymous view for player: {}", playerId);
            }

            Player player = playerService.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(request);
            String referrerUrl = request.getHeader("Referer");

            logger.debug("Recording view for player {} from IP: {}", player.getName(), ipAddress);

            viewService.recordView(player, user, ipAddress, userAgent, referrerUrl);

            logger.info("Successfully recorded view for player: {}", playerId);
            return ResponseEntity.ok().build();
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when recording view: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error recording view for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves view statistics for a specific player.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPlayerViewStats(
            @PathVariable Long playerId,
            HttpServletRequest request) {

        try {
            Player player = playerService.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            logger.debug("Retrieving view statistics for player: {}", player.getName());

            Map<String, Object> stats = viewService.getViewStatistics(player);

            // Add recent views as DTOs
            List<PlayerView> recentViews = viewService.getRecentViewsForPlayer(player, 10);
            List<PlayerViewDTO> recentViewDTOs = recentViews.stream()
                    .map(this::convertToDTO)
                    .toList();
            stats.put("recentViews", recentViewDTOs);

            logger.info("Retrieved view statistics for player: {}", playerId);
            return ResponseEntity.ok(stats);
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when retrieving view stats: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving view stats for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves trending players based on recent views.
     */
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingPlayers(HttpServletRequest request) {
        try {
            logger.debug("Retrieving trending players based on views");

            Pageable pageable = PageRequest.of(0, 10);
            Page<Player> trendingPage = viewService.getTrendingPlayers(pageable);

            Map<String, Object> trendingData = new HashMap<>();
            trendingData.put("players", trendingPage.getContent());
            trendingData.put("totalElements", trendingPage.getTotalElements());
            trendingData.put("totalPages", trendingPage.getTotalPages());
            trendingData.put("pageNumber", trendingPage.getNumber());
            trendingData.put("pageSize", trendingPage.getSize());

            logger.info("Retrieved trending players data");
            return ResponseEntity.ok(trendingData);
        } catch (Exception e) {
            logger.error("Error retrieving trending players", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves view history for the current user.
     */
    @GetMapping("/history")
    public ResponseEntity<Page<PlayerViewDTO>> getUserViewHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        try {
            User user = userService.getCurrentUser(request);

            logger.debug("Retrieving view history for user: {}", user.getUsername());

            Pageable pageable = PageRequest.of(page, size);
            Page<PlayerView> viewHistory = viewService.getUserViewHistory(user, pageable);
            Page<PlayerViewDTO> viewHistoryDTOs = viewHistory.map(this::convertToDTO);

            logger.info("Retrieved view history for user: {}", user.getUsername());
            return ResponseEntity.ok(viewHistoryDTOs);
        } catch (UserNotFoundException e) {
            logger.warn("User not found when retrieving view history");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.error("Error retrieving user view history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves detailed view logs for a specific player (admin/analytics
     * endpoint).
     */
    @GetMapping("/logs")
    public ResponseEntity<Page<PlayerViewDTO>> getPlayerViewLogs(
            @PathVariable Long playerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        try {
            Player player = playerService.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            logger.debug("Retrieving view logs for player: {}", player.getName());

            Pageable pageable = PageRequest.of(page, size);
            Page<PlayerView> viewLogs = viewService.getPlayerViewLogs(player, pageable);
            Page<PlayerViewDTO> viewLogDTOs = viewLogs.map(this::convertToDTO);

            logger.info("Retrieved view logs for player: {}", playerId);
            return ResponseEntity.ok(viewLogDTOs);
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when retrieving view logs: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving view logs for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Extracts the client IP address from the HTTP request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Converts a PlayerView entity to a PlayerViewDTO for API responses.
     */
    private PlayerViewDTO convertToDTO(PlayerView view) {
        if (view == null) {
            return null;
        }

        PlayerViewDTO dto = new PlayerViewDTO();
        dto.setId(view.getId());
        dto.setPlayerId(view.getPlayer().getId());
        dto.setPlayerName(view.getPlayer().getName());
        dto.setUserId(view.getUser() != null ? view.getUser().getId() : null);
        dto.setUserName(view.getUser() != null ? view.getUser().getUsername() : null);
        dto.setIpAddress(view.getIpAddress());
        dto.setUserAgent(view.getUserAgent());
        dto.setReferrerUrl(view.getReferrerUrl());
        dto.setViewDurationSeconds(view.getViewDurationSeconds());
        dto.setCreatedAt(view.getCreatedAt());

        return dto;
    }
}

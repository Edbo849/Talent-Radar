package com.talentradar.controller.player;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.player.PlayerDTO;
import com.talentradar.exception.PlayerNotFoundException;
import com.talentradar.model.player.Player;
import com.talentradar.service.player.PlayerService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller for managing player information and retrieval. Provides
 * endpoints for searching, filtering, and retrieving player data with proper
 * error handling and validation.
 */
@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "http://localhost:3000")
public class PlayerController {

    private static final Logger logger = LoggerFactory.getLogger(PlayerController.class);

    @Autowired
    private PlayerService playerService;

    /**
     * Retrieves all players with pagination and optional filtering.
     */
    @GetMapping
    public ResponseEntity<Page<PlayerDTO>> getAllPlayers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String nationality,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String position,
            HttpServletRequest request) {

        try {
            Pageable pageable = PageRequest.of(page, size);

            logger.debug("Retrieving players with filters - nationality: {}, age: {}-{}, position: {}",
                    nationality, minAge, maxAge, position);

            Page<Player> players = playerService.findAllPlayers(pageable);
            Page<PlayerDTO> playerDTOs = players.map(this::convertToDTO);

            logger.info("Retrieved {} players for page {} with size {}",
                    playerDTOs.getTotalElements(), page, size);

            return ResponseEntity.ok(playerDTOs);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid filter parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving players", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a specific player by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlayerDTO> getPlayerById(@PathVariable Long id, HttpServletRequest request) {
        try {
            Player player = playerService.findById(id)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + id));

            logger.debug("Retrieved player: {}", player.getName());
            return ResponseEntity.ok(convertToDTO(player));
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving player with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Searches for players by name or other criteria.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PlayerDTO>> searchPlayers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        try {
            if (query == null || query.trim().isEmpty()) {
                logger.warn("Empty search query provided");
                return ResponseEntity.badRequest().build();
            }

            Pageable pageable = PageRequest.of(page, size);

            logger.debug("Searching players with query: {}", query);

            Page<Player> players = playerService.searchPlayers(query, "", "", "", pageable);
            Page<PlayerDTO> playerDTOs = players.map(this::convertToDTO);

            logger.info("Found {} players matching query: {}", playerDTOs.getTotalElements(), query);
            return ResponseEntity.ok(playerDTOs);
        } catch (Exception e) {
            logger.error("Error searching players with query: {}", query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves trending or popular players.
     */
    @GetMapping("/trending")
    public ResponseEntity<List<PlayerDTO>> getTrendingPlayers(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        try {
            Pageable pageable = PageRequest.of(0, limit);
            Page<Player> players = playerService.findTrendingPlayers(pageable);
            List<PlayerDTO> playerDTOs = players.getContent().stream()
                    .map(this::convertToDTO)
                    .toList();

            return ResponseEntity.ok(playerDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving trending players", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get top rated players
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<PlayerDTO>> getTopRatedPlayers(
            @RequestParam(defaultValue = "5") int limit,
            HttpServletRequest request) {
        try {
            List<Player> players = playerService.getTopRatedPlayers(limit);
            List<PlayerDTO> playerDTOs = players.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} top rated players", playerDTOs.size());
            return ResponseEntity.ok(playerDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving top rated players", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get recently added players
     */
    @GetMapping("/recent")
    public ResponseEntity<List<PlayerDTO>> getRecentPlayers(
            @RequestParam(defaultValue = "5") int limit,
            HttpServletRequest request) {
        try {
            List<Player> players = playerService.getRecentlyAddedPlayers(limit);
            List<PlayerDTO> playerDTOs = players.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} recently added players", playerDTOs.size());
            return ResponseEntity.ok(playerDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving recent players", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get top players by statistics
     */
    @GetMapping("/top-by-stats")
    public ResponseEntity<List<PlayerDTO>> getTopPlayersByStats(
            @RequestParam String statistic,
            @RequestParam(defaultValue = "2024") Integer season,
            @RequestParam(defaultValue = "5") int limit,
            HttpServletRequest request) {
        try {
            List<Player> players = playerService.getTopPlayersByStatistic(statistic, season, limit);
            List<PlayerDTO> playerDTOs = players.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} top players by {}", playerDTOs.size(), statistic);
            return ResponseEntity.ok(playerDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving top players by stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get top rated players for 2025 season
     */
    @GetMapping("/top-rated-season")
    public ResponseEntity<List<PlayerDTO>> getTopRatedPlayersSeason(
            @RequestParam(defaultValue = "25") int limit,
            HttpServletRequest request) {
        try {
            List<Player> players = playerService.getTopRatedPlayersSeason(limit);
            List<PlayerDTO> playerDTOs = players.stream()
                    .map(player -> playerService.convertToDTO(player))
                    .toList();

            logger.info("Retrieved {} top rated players for 2025", playerDTOs.size());

            if (!playerDTOs.isEmpty()) {
                PlayerDTO firstPlayer = playerDTOs.get(0);
                return ResponseEntity.ok(playerDTOs);
            }
        } catch (Exception e) {
            logger.error("Error retrieving top rated players for 2025", e);
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build(); // Default return in case no players are found
    }

    /**
     * Retrieves U21 eligible players.
     */
    @GetMapping("/u21")
    public ResponseEntity<Page<PlayerDTO>> getU21Players(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {

        try {
            Pageable pageable = PageRequest.of(page, size);

            logger.debug("Retrieving U21 players (page: {}, size: {})", page, size);

            Page<Player> players = playerService.findU21Players(pageable);
            Page<PlayerDTO> playerDTOs = players.map(this::convertToDTO);

            logger.info("Retrieved {} U21 players", playerDTOs.getTotalElements());
            return ResponseEntity.ok(playerDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving U21 players", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Converts a Player entity to a PlayerDTO for API responses.
     */
    private PlayerDTO convertToDTO(Player player) {
        if (player == null) {
            return null;
        }

        PlayerDTO dto = new PlayerDTO();
        dto.setId(player.getId());
        dto.setExternalId(player.getExternalId());
        dto.setName(player.getName());
        dto.setFirstName(player.getFirstName());
        dto.setLastName(player.getLastName());
        dto.setDateOfBirth(player.getDateOfBirth());
        dto.setBirthPlace(player.getBirthPlace());
        dto.setBirthCountry(player.getBirthCountry());
        dto.setNationality(player.getNationality());
        dto.setPosition(player.getPosition());
        dto.setHeightCm(player.getHeightCm());
        dto.setWeightKg(player.getWeightKg());
        dto.setPhotoUrl(player.getPhotoUrl());
        dto.setIsInjured(player.getIsInjured());
        dto.setIsActive(player.getIsActive());
        dto.setJerseyNumber(player.getJerseyNumber());
        dto.setAge(player.getAge());
        dto.setIsEligibleForU21(player.isEligibleForU21());
        dto.setTrendingScore(player.getTrendingScore());
        dto.setTotalViews(player.getTotalViews());
        dto.setWeeklyViews(player.getWeeklyViews());
        dto.setMonthlyViews(player.getMonthlyViews());
        dto.setCreatedAt(player.getCreatedAt());
        dto.setUpdatedAt(player.getUpdatedAt());

        // Current club information
        if (player.getCurrentClub() != null) {
            dto.setCurrentClubId(player.getCurrentClub().getId());
            dto.setCurrentClubName(player.getCurrentClub().getName());
            dto.setCurrentClubLogoUrl(player.getCurrentClub().getLogoUrl());
            dto.setCurrentClubCountry(player.getCurrentClub().getCountry().toString());
        }

        return dto;
    }
}

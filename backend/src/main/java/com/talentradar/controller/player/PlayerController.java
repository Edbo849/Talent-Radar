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
import com.talentradar.dto.player.PlayerInjuryDTO;
import com.talentradar.dto.player.PlayerSidelinedDTO;
import com.talentradar.dto.player.PlayerStatisticDTO;
import com.talentradar.dto.player.PlayerTransferDTO;
import com.talentradar.dto.player.PlayerTrophyDTO;
import com.talentradar.exception.PlayerNotFoundException;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerInjury;
import com.talentradar.model.player.PlayerSidelined;
import com.talentradar.model.player.PlayerStatistic;
import com.talentradar.model.player.PlayerTransfer;
import com.talentradar.model.player.PlayerTrophy;
import com.talentradar.service.player.PlayerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;

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
     * Retrieves all statistics for a specific player.
     */
    @GetMapping("/{playerId}/statistics")
    public ResponseEntity<List<PlayerStatisticDTO>> getPlayerStatistics(
            @PathVariable Long playerId,
            HttpServletRequest request) {

        try {
            logger.debug("Retrieving statistics for player: {}", playerId);

            // Verify player exists
            playerService.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            List<PlayerStatistic> statistics = playerService.getPlayerStatistics(playerId);
            List<PlayerStatisticDTO> statisticDTOs = statistics.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} statistics for player {}", statisticDTOs.size(), playerId);
            return ResponseEntity.ok(statisticDTOs);

        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when retrieving statistics: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving statistics for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all transfers for a specific player.
     */
    @GetMapping("/{playerId}/transfers")
    public ResponseEntity<List<PlayerTransferDTO>> getPlayerTransfers(
            @PathVariable Long playerId,
            HttpServletRequest request) {

        try {
            logger.debug("Retrieving transfers for player: {}", playerId);

            // Verify player exists
            Player player = playerService.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            List<PlayerTransfer> transfers = playerService.getPlayerTransfers(player);
            List<PlayerTransferDTO> transferDTOs = transfers.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} transfers for player {}", transferDTOs.size(), playerId);
            return ResponseEntity.ok(transferDTOs);

        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when retrieving transfers: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving transfers for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all injuries for a specific player.
     */
    @GetMapping("/{playerId}/injuries")
    public ResponseEntity<List<PlayerInjuryDTO>> getPlayerInjuries(
            @PathVariable Long playerId,
            HttpServletRequest request) {

        try {
            logger.debug("Retrieving injuries for player: {}", playerId);

            // Verify player exists
            Player player = playerService.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            List<PlayerInjury> injuries = playerService.getPlayerInjuries(player);
            List<PlayerInjuryDTO> injuryDTOs = injuries.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} injuries for player {}", injuryDTOs.size(), playerId);
            return ResponseEntity.ok(injuryDTOs);

        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when retrieving injuries: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving injuries for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all sidelined periods for a specific player.
     */
    @GetMapping("/{playerId}/sidelined")
    public ResponseEntity<List<PlayerSidelinedDTO>> getPlayerSidelined(
            @PathVariable Long playerId,
            HttpServletRequest request) {

        try {
            logger.debug("Retrieving sidelined periods for player: {}", playerId);

            // Verify player exists
            Player player = playerService.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            List<PlayerSidelined> sidelinedPeriods = player.getSidelinedPeriods().stream()
                    .sorted((s1, s2) -> {
                        if (s1.getStartDate() == null && s2.getStartDate() == null) {
                            return 0;
                        }
                        if (s1.getStartDate() == null) {
                            return 1;
                        }
                        if (s2.getStartDate() == null) {
                            return -1;
                        }
                        return s2.getStartDate().compareTo(s1.getStartDate());
                    })
                    .toList();

            List<PlayerSidelinedDTO> sidelinedDTOs = sidelinedPeriods.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} sidelined periods for player {}", sidelinedDTOs.size(), playerId);
            return ResponseEntity.ok(sidelinedDTOs);

        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when retrieving sidelined periods: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving sidelined periods for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all trophies for a specific player.
     */
    @GetMapping("/{playerId}/trophies")
    public ResponseEntity<List<PlayerTrophyDTO>> getPlayerTrophies(
            @PathVariable Long playerId,
            HttpServletRequest request) {

        try {
            logger.debug("Retrieving trophies for player: {}", playerId);

            // Verify player exists
            Player player = playerService.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            List<PlayerTrophy> trophies = player.getTrophies().stream()
                    .sorted((t1, t2) -> {
                        // Sort by season (most recent first)
                        if (t1.getSeason() == null && t2.getSeason() == null) {
                            return 0;
                        }
                        if (t1.getSeason() == null) {
                            return 1;
                        }
                        if (t2.getSeason() == null) {
                            return -1;
                        }
                        return t2.getSeason().compareTo(t1.getSeason());
                    })
                    .toList();

            List<PlayerTrophyDTO> trophyDTOs = trophies.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} trophies for player {}", trophyDTOs.size(), playerId);
            return ResponseEntity.ok(trophyDTOs);

        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when retrieving trophies: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving trophies for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get top rated players by league
     */
    @GetMapping("/top-rated-by-league/{leagueId}")
    public ResponseEntity<List<PlayerDTO>> getTopRatedPlayersByLeague(
            @PathVariable @Positive Long leagueId,
            @RequestParam(defaultValue = "25") int limit,
            HttpServletRequest request) {
        try {
            List<Player> players = playerService.getTopRatedPlayersByLeague(leagueId, limit);
            List<PlayerDTO> playerDTOs = players.stream()
                    .map(player -> playerService.convertToDTO(player))
                    .toList();

            logger.info("Retrieved {} top rated players for league ID: {}", playerDTOs.size(), leagueId);
            return ResponseEntity.ok(playerDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving top rated players for league ID: {}", leagueId, e);
            return ResponseEntity.internalServerError().build();
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

    private PlayerStatisticDTO convertToDTO(PlayerStatistic statistic) {
        PlayerStatisticDTO dto = new PlayerStatisticDTO();
        dto.setId(statistic.getId());
        dto.setPlayerId(statistic.getPlayer().getId());
        dto.setPlayerName(statistic.getPlayer().getName());
        dto.setSeason(statistic.getSeason());

        // Basic stats
        dto.setAppearances(statistic.getAppearances());
        dto.setLineups(statistic.getLineups());
        dto.setMinutesPlayed(statistic.getMinutesPlayed());
        dto.setPosition(statistic.getPosition());
        dto.setRating(statistic.getRating());
        dto.setIsCaptain(statistic.getIsCaptain());

        // Goals and assists
        dto.setGoals(statistic.getGoals());
        dto.setAssists(statistic.getAssists());
        dto.setGoalsConceded(statistic.getGoalsConceded());
        dto.setSaves(statistic.getSaves());

        // Shooting stats
        dto.setShotsTotal(statistic.getShotsTotal());
        dto.setShotsOnTarget(statistic.getShotsOnTarget());

        // Passing stats
        dto.setPassesTotal(statistic.getPassesTotal());
        dto.setPassesKey(statistic.getPassesKey());

        // Defensive stats
        dto.setTacklesTotal(statistic.getTacklesTotal());
        dto.setTacklesBlocks(statistic.getTacklesBlocks());
        dto.setInterceptions(statistic.getInterceptions());

        // Dribbling stats
        dto.setDribblesAttempts(statistic.getDribblesAttempts());
        dto.setDribblesSuccess(statistic.getDribblesSuccess());

        // Disciplinary stats
        dto.setFoulsDrawn(statistic.getFoulsDrawn());
        dto.setFoulsCommitted(statistic.getFoulsCommitted());
        dto.setYellowCards(statistic.getYellowCards());
        dto.setRedCards(statistic.getRedCards());

        // Penalty stats
        dto.setPenaltiesWon(statistic.getPenaltiesWon());
        dto.setPenaltiesScored(statistic.getPenaltiesScored());
        dto.setPenaltiesMissed(statistic.getPenaltiesMissed());

        // Substitution stats
        dto.setSubstitutesIn(statistic.getSubstitutesIn());
        dto.setSubstitutesOut(statistic.getSubstitutesOut());
        dto.setSubstitutesBench(statistic.getSubstitutesBench());

        // Timestamps
        dto.setCreatedAt(statistic.getCreatedAt());
        dto.setUpdatedAt(statistic.getUpdatedAt());

        // Club context
        if (statistic.getClub() != null) {
            dto.setClubId(statistic.getClub().getId());
            dto.setClubName(statistic.getClub().getName());
            dto.setClubLogoUrl(statistic.getClub().getLogoUrl());
        }

        // League context
        if (statistic.getLeague() != null) {
            dto.setLeagueId(statistic.getLeague().getId());
            dto.setLeagueName(statistic.getLeague().getName());
            dto.setLeagueLogoUrl(statistic.getLeague().getLogoUrl());
        }

        // Calculate derived statistics
        dto.calculateDerivedStats();

        return dto;
    }

    /**
     * Converts a PlayerTransfer entity to a PlayerTransferDTO.
     */
    private PlayerTransferDTO convertToDTO(PlayerTransfer transfer) {
        PlayerTransferDTO dto = new PlayerTransferDTO();
        dto.setId(transfer.getId());
        dto.setPlayerId(transfer.getPlayer().getId());
        dto.setFromClub(transfer.getClubFrom() != null ? transfer.getClubFrom().getName() : null);
        dto.setToClub(transfer.getClubTo() != null ? transfer.getClubTo().getName() : null);
        dto.setFromClubLogoUrl(transfer.getClubFrom() != null ? transfer.getClubFrom().getLogoUrl() : null);
        dto.setToClubLogoUrl(transfer.getClubTo() != null ? transfer.getClubTo().getLogoUrl() : null);
        dto.setTransferDate(transfer.getTransferDate());
        dto.setTransferType(transfer.getTransferType());
        dto.setCreatedAt(transfer.getCreatedAt());
        return dto;
    }

    /**
     * Converts a PlayerInjury entity to a PlayerInjuryDTO.
     */
    private PlayerInjuryDTO convertToDTO(PlayerInjury injury) {
        PlayerInjuryDTO dto = new PlayerInjuryDTO();
        dto.setId(injury.getId());
        dto.setPlayerId(injury.getPlayer().getId());
        dto.setInjuryType(injury.getInjuryType());
        dto.setReason(injury.getReason());
        dto.setStartDate(injury.getStartDate());
        dto.setFixtureId(injury.getFixtureId());
        dto.setCreatedAt(injury.getCreatedAt());

        // Map club details if available
        if (injury.getClub() != null) {
            dto.setClubId(injury.getClub().getId());
            dto.setClubName(injury.getClub().getName());
        }

        // Map league details if available
        if (injury.getLeague() != null) {
            dto.setLeagueId(injury.getLeague().getId());
            dto.setLeagueName(injury.getLeague().getName());
            dto.setLeagueLogoUrl(injury.getLeague().getLogoUrl());

        }
        return dto;
    }

    /**
     * Converts a PlayerSidelined entity to a PlayerSidelinedDTO.
     */
    private PlayerSidelinedDTO convertToDTO(PlayerSidelined sidelined) {
        PlayerSidelinedDTO dto = new PlayerSidelinedDTO();
        dto.setId(sidelined.getId());
        dto.setPlayerId(sidelined.getPlayer().getId());
        dto.setType(sidelined.getType());
        dto.setStartDate(sidelined.getStartDate());
        dto.setEndDate(sidelined.getEndDate());
        dto.setCreatedAt(sidelined.getCreatedAt());
        return dto;
    }

    /**
     * Converts a PlayerTrophy entity to a PlayerTrophyDTO.
     */
    private PlayerTrophyDTO convertToDTO(PlayerTrophy trophy) {
        PlayerTrophyDTO dto = new PlayerTrophyDTO();
        dto.setId(trophy.getId());
        dto.setPlayerId(trophy.getPlayer().getId());
        dto.setLeagueName(trophy.getLeagueName());
        dto.setCountry(trophy.getCountry());
        dto.setSeason(trophy.getSeason());
        dto.setPlace(trophy.getPlace());
        dto.setCreatedAt(trophy.getCreatedAt());
        return dto;
    }

}

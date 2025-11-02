package com.talentradar.service.player;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.dto.player.PlayerDTO;
import com.talentradar.exception.PlayerNotFoundException;
import com.talentradar.model.club.League;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerInjury;
import com.talentradar.model.player.PlayerStatistic;
import com.talentradar.model.player.PlayerTransfer;
import com.talentradar.model.player.PlayerView;
import com.talentradar.model.user.User;
import com.talentradar.repository.club.LeagueRepository;
import com.talentradar.repository.player.PlayerRatingRepository;
import com.talentradar.repository.player.PlayerRepository;
import com.talentradar.repository.player.PlayerStatisticRepository;
import com.talentradar.repository.player.PlayerViewRepository;

/**
 * Service responsible for managing player operations. Handles player CRUD
 * operations, search, filtering, and view tracking.
 */
@Service
@Transactional
public class PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerStatisticRepository playerStatisticRepository;

    @Autowired
    private PlayerViewRepository playerViewRepository;

    @Autowired
    private PlayerRatingRepository playerRatingRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<Player> findById(Long id) {
        try {
            if (id == null) {
                logger.warn("Attempted to find player with null ID");
                return Optional.empty();
            }
            return playerRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding player by ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<Player> findByExternalId(Integer externalId) {
        try {
            if (externalId == null) {
                logger.warn("Attempted to find player with null external ID");
                return Optional.empty();
            }
            return playerRepository.findByExternalId(externalId);
        } catch (Exception e) {
            logger.error("Error finding player by external ID {}: {}", externalId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public Player save(Player player) {
        try {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null");
            }
            return playerRepository.save(player);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid player data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error saving player: {}", e.getMessage());
            throw new RuntimeException("Failed to save player", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }
            if (!playerRepository.existsById(id)) {
                throw new PlayerNotFoundException("Player not found with ID: " + id);
            }
            playerRepository.deleteById(id);
            logger.info("Deleted player with ID: {}", id);
        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Error deleting player: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error deleting player with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete player", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<Player> findByNameContaining(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Search name cannot be null or empty");
            }
            return playerRepository.findByFirstNameOrLastNameContainingIgnoreCase(name.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid search parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching players by name '{}': {}", name, e.getMessage());
            throw new RuntimeException("Failed to search players by name", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<Player> findByNationality(String nationality) {
        try {
            if (nationality == null || nationality.trim().isEmpty()) {
                throw new IllegalArgumentException("Nationality cannot be null or empty");
            }
            return playerRepository.findByNationality(nationality.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid nationality parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding players by nationality '{}': {}", nationality, e.getMessage());
            throw new RuntimeException("Failed to find players by nationality", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<Player> findByBirthCountry(String birthCountry) {
        try {
            if (birthCountry == null || birthCountry.trim().isEmpty()) {
                throw new IllegalArgumentException("Birth country cannot be null or empty");
            }
            return playerRepository.findByBirthCountry(birthCountry.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid birth country parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding players by birth country '{}': {}", birthCountry, e.getMessage());
            throw new RuntimeException("Failed to find players by birth country", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<Player> findActivePlayersOnly() {
        try {
            return playerRepository.findByIsActiveTrue();
        } catch (Exception e) {
            logger.error("Error finding active players: {}", e.getMessage());
            throw new RuntimeException("Failed to find active players", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Player> findAllPlayers(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }
            return playerRepository.findAll(pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid pageable parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding all players: {}", e.getMessage());
            throw new RuntimeException("Failed to find all players", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Player> findU21PlayersPage(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }
            return playerRepository.findU21PlayersPage(pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid pageable parameter for U21 players: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding U21 players page: {}", e.getMessage());
            throw new RuntimeException("Failed to find U21 players page", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<Player> findU21Players() {
        try {
            return playerRepository.findU21Players();
        } catch (Exception e) {
            logger.error("Error finding U21 players: {}", e.getMessage());
            throw new RuntimeException("Failed to find U21 players", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long countU21Players() {
        try {
            return playerRepository.countByU21Eligible();
        } catch (Exception e) {
            logger.error("Error counting U21 players: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Player> findU21Players(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            List<Player> u21Players = playerRepository.findU21Players();
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), u21Players.size());
            List<Player> paged = u21Players.subList(start, end);
            return new PageImpl<>(paged, pageable, u21Players.size());

        } catch (IllegalArgumentException e) {
            logger.error("Invalid pageable parameter for U21 players: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding U21 players with pagination: {}", e.getMessage());
            throw new RuntimeException("Failed to find U21 players with pagination", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Player> findTrendingPlayers(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }
            return playerRepository.findByTrendingScoreGreaterThanOrderByTrendingScoreDesc(-1.0, pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid pageable parameter for trending players: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding trending players: {}", e.getMessage());
            throw new RuntimeException("Failed to find trending players", e);
        }
    }

    /**
     * Get top rated players
     */
    @Transactional(readOnly = true)
    public List<Player> getTopRatedPlayers(int limit) {
        try {
            // Get players with highest average ratings from PlayerRating
            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> topRatedResults = playerRatingRepository.findTopRatedPlayersOverall(pageable);

            return topRatedResults.stream()
                    .map(result -> (Player) result[0])
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting top rated players: {}", e.getMessage());
            throw new RuntimeException("Failed to get top rated players", e);
        }
    }

    /**
     * Get recently added players
     */
    @Transactional(readOnly = true)
    public List<Player> getRecentlyAddedPlayers(int limit) {
        try {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            Pageable pageable = PageRequest.of(0, limit);
            List<Player> players = playerRepository.findByCreatedAtAfterOrderByCreatedAtDesc(weekAgo, pageable);
            return players;
        } catch (Exception e) {
            logger.error("Error getting recently added players: {}", e.getMessage());
            throw new RuntimeException("Failed to get recently added players", e);
        }
    }

    /**
     * Get top players by statistics
     */
    @Transactional(readOnly = true)
    public List<Player> getTopPlayersByStatistic(String statistic, Integer season, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);

            return switch (statistic.toLowerCase()) {
                case "goals" ->
                    playerStatisticRepository.findTopGoalScorersBySeason(season, pageable)
                    .getContent().stream()
                    .map(PlayerStatistic::getPlayer)
                    .collect(Collectors.toList());
                case "assists" ->
                    playerStatisticRepository.findTopAssistProvidersBySeason(season, pageable)
                    .getContent().stream()
                    .map(PlayerStatistic::getPlayer)
                    .collect(Collectors.toList());
                case "appearances" ->
                    playerStatisticRepository.findMostActivePlayersBySeason(season, pageable)
                    .getContent().stream()
                    .map(PlayerStatistic::getPlayer)
                    .collect(Collectors.toList());
                default ->
                    throw new IllegalArgumentException("Invalid statistic: " + statistic);
            };
        } catch (RuntimeException e) {
            logger.error("Error getting top players by statistic {}: {}", statistic, e.getMessage());
            throw new RuntimeException("Failed to get top players by statistic", e);
        }
    }

    /**
     * Get top rated players for current season based on statistics
     */
    @Transactional(readOnly = true)
    public List<Player> getTopRatedPlayersSeason(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            Page<PlayerStatistic> topRatedStats = playerStatisticRepository.findTopRatedPlayersSeason(1000, pageable);
            logger.info("Top rated stats: {}", topRatedStats);

            logger.debug("Found {} top rated player statistics for current season", topRatedStats.getContent().size());

            List<Player> players = topRatedStats.getContent().stream()
                    .map(PlayerStatistic::getPlayer)
                    .collect(Collectors.toList());

            logger.debug("Converted to {} players", players.size());
            return players;
        } catch (Exception e) {
            logger.error("Error getting top rated players for current season: {}", e.getMessage());
            // Fallback to any players with statistics
            try {
                Pageable pageable = PageRequest.of(0, limit);
                List<PlayerStatistic> anyStats = playerStatisticRepository.findAll(pageable).getContent();
                return anyStats.stream()
                        .map(PlayerStatistic::getPlayer)
                        .distinct()
                        .limit(limit)
                        .collect(Collectors.toList());
            } catch (Exception ex) {
                logger.error("Fallback also failed: {}", ex.getMessage());
                return List.of();
            }
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void trackPlayerView(Long playerId, User user, String ipAddress, String userAgent) {
        try {
            if (playerId == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }

            Optional<Player> playerOpt = playerRepository.findById(playerId);
            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();

                // Create view record
                PlayerView view = new PlayerView();
                view.setPlayer(player);
                view.setUser(user);
                view.setIpAddress(ipAddress);
                view.setUserAgent(userAgent);
                view.setViewedAt(LocalDateTime.now());

                playerViewRepository.save(view);

                // Update player view counts
                player.setTotalViews(player.getTotalViews() + 1);
                player.setWeeklyViews(player.getWeeklyViews() + 1);
                player.setMonthlyViews(player.getMonthlyViews() + 1);

                // Calculate trending score based on recent activity
                updateTrendingScore(player);

                playerRepository.save(player);

                logger.debug("Tracked view for player: {} by user: {}", player.getName(),
                        user != null ? user.getUsername() : "anonymous");
            } else {
                logger.warn("Cannot track view for non-existent player ID: {}", playerId);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for tracking player view: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error tracking player view for player ID: {}", playerId, e);
            throw new RuntimeException("Failed to track player view", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerStatistic> getPlayerStatistics(Long playerId) {
        try {
            if (playerId == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }

            Optional<Player> player = playerRepository.findById(playerId);
            if (player.isPresent()) {
                return playerStatisticRepository.findByPlayer(player.get());
            }
            return List.of();

        } catch (IllegalArgumentException e) {
            logger.error("Invalid player ID for getting statistics: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting player statistics for player ID {}: {}", playerId, e.getMessage());
            throw new RuntimeException("Failed to get player statistics", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerStatistic> getPlayerStatisticsBySeason(Long playerId, Integer season) {
        try {
            if (playerId == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }

            Optional<Player> player = playerRepository.findById(playerId);
            if (player.isPresent()) {
                return playerStatisticRepository.findByPlayerAndSeason(player.get(), season);
            }
            return List.of();

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting player statistics by season: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting player statistics for player ID {} and season {}: {}", playerId, season, e.getMessage());
            throw new RuntimeException("Failed to get player statistics by season", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerTransfer> getPlayerTransfers(Player player) {
        try {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null");
            }

            return player.getTransfers().stream()
                    .sorted((t1, t2) -> t2.getTransferDate().compareTo(t1.getTransferDate())) // Most recent first
                    .toList();

        } catch (IllegalArgumentException e) {
            logger.error("Invalid player for getting transfers: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error getting player transfers for player {}: {}", player != null ? player.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to get player transfers", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerInjury> getPlayerInjuries(Player player) {
        try {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null");
            }

            return player.getInjuries().stream()
                    .sorted((i1, i2) -> {
                        // Sort by start date (most recent first)
                        if (i1.getStartDate() == null && i2.getStartDate() == null) {
                            return 0;
                        }
                        if (i1.getStartDate() == null) {
                            return 1;
                        }
                        if (i2.getStartDate() == null) {
                            return -1;
                        }
                        return i2.getStartDate().compareTo(i1.getStartDate());
                    })
                    .toList();

        } catch (IllegalArgumentException e) {
            logger.error("Invalid player for getting injuries: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting player injuries for player {}: {}", player != null ? player.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to get player injuries", e);
        }
    }

    /*
    * Calculates view growth from the last week
     */
    public Double calculateWeeklyGrowthPercentage(Player player) {
        try {
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);

            Long thisWeekViews = playerViewRepository.countByPlayerAndCreatedAtBetween(
                    player, oneWeekAgo, LocalDateTime.now());
            Long lastWeekViews = playerViewRepository.countByPlayerAndCreatedAtBetween(
                    player, twoWeeksAgo, oneWeekAgo);

            if (lastWeekViews == 0) {
                return thisWeekViews > 0 ? 100.0 : 0.0; // 100% if new views, 0% if no change
            }

            return ((double) (thisWeekViews - lastWeekViews) / lastWeekViews) * 100;
        } catch (Exception e) {
            logger.error("Error calculating weekly growth for player {}: {}", player.getName(), e.getMessage());
            return 0.0;
        }
    }

    /**
     * Converts a Player entity to a PlayerDTO.
     */
    public PlayerDTO convertToDTO(Player player) {
        try {
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null");
            }

            PlayerDTO dto = new PlayerDTO();

            // Basic information
            dto.setId(player.getId());
            dto.setExternalId(player.getExternalId());
            dto.setName(player.getName());
            dto.setFirstName(player.getFirstName());
            dto.setLastName(player.getLastName());
            dto.setDateOfBirth(player.getDateOfBirth());
            dto.setAge(player.getAge());
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

            // Trending information
            dto.setTrendingScore(player.getTrendingScore());
            dto.setTotalViews(player.getTotalViews());
            dto.setWeeklyViews(player.getWeeklyViews());
            dto.setMonthlyViews(player.getMonthlyViews());
            dto.setWeeklyGrowthPercentage(calculateWeeklyGrowthPercentage(player));

            // Current club information
            if (player.getCurrentClub() != null) {
                dto.setCurrentClubId(player.getCurrentClub().getId());
                dto.setCurrentClubName(player.getCurrentClub().getName());
                dto.setCurrentClubLogoUrl(player.getCurrentClub().getLogoUrl());
                if (player.getCurrentClub().getCountry() != null) {
                    dto.setCurrentClubCountry(player.getCurrentClub().getCountry().getName());
                }
            }

            // Get ALL 2025 statistics and combine them
            List<PlayerStatistic> season2025Stats = null;
            try {
                // Get all 2025 statistics for this player
                season2025Stats = playerStatisticRepository.findByPlayerAndSeason(player, 2025);

                if (season2025Stats == null || season2025Stats.isEmpty()) {
                    // Fallback to most recent season if no 2025 data
                    List<PlayerStatistic> allStats = playerStatisticRepository.findByPlayerOrderBySeasonDescCreatedAtDesc(player);
                    if (!allStats.isEmpty()) {
                        // Get all stats from the most recent season
                        Integer mostRecentSeason = allStats.get(0).getSeason();
                        season2025Stats = playerStatisticRepository.findByPlayerAndSeason(player, mostRecentSeason);
                    }
                }
            } catch (Exception e) {
                logger.debug("Error fetching player statistics: {}", e.getMessage());
            }

            // Combine statistics and collect club information
            if (season2025Stats != null && !season2025Stats.isEmpty()) {
                // Combine all statistics from 2025
                CombinedSeasonStats combinedStats = combineSeasonStatistics(season2025Stats);

                // Set combined statistics
                dto.setAppearances(combinedStats.getTotalAppearances());
                dto.setGoals(combinedStats.getTotalGoals());
                dto.setAssists(combinedStats.getTotalAssists());
                dto.setMinutesPlayed(combinedStats.getTotalMinutesPlayed());
                dto.setRating(combinedStats.getAverageRating());

                // Set clubs played for this season
                dto.setSeasonClubs(combinedStats.getClubNames());
            } else {
                logger.debug("No statistics found for player: {}", player.getName());
                // Set default values
                dto.setAppearances(0);
                dto.setGoals(0);
                dto.setAssists(0);
                dto.setMinutesPlayed(0);
                dto.setRating(0.0);
                dto.setSeasonClubs(List.of());
            }

            dto.setCreatedAt(player.getCreatedAt());
            dto.setUpdatedAt(player.getUpdatedAt());

            return dto;

        } catch (IllegalArgumentException e) {
            logger.error("Invalid player for DTO conversion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error converting player to DTO: {}", e.getMessage());
            throw new RuntimeException("Failed to convert player to DTO", e);
        }
    }

    private CombinedSeasonStats combineSeasonStatistics(List<PlayerStatistic> seasonStats) {
        int totalAppearances = 0;
        int totalGoals = 0;
        int totalAssists = 0;
        int totalMinutesPlayed = 0;
        double totalRatingPoints = 0.0;
        int ratingsCount = 0;
        List<String> clubNames = new ArrayList<>();

        for (PlayerStatistic stat : seasonStats) {
            // Sum up the statistics
            totalAppearances += (stat.getAppearances() != null ? stat.getAppearances() : 0);
            totalGoals += (stat.getGoals() != null ? stat.getGoals() : 0);
            totalAssists += (stat.getAssists() != null ? stat.getAssists() : 0);
            totalMinutesPlayed += (stat.getMinutesPlayed() != null ? stat.getMinutesPlayed() : 0);

            // Calculate weighted average rating (weighted by appearances)
            if (stat.getRating() != null && stat.getAppearances() != null && stat.getAppearances() > 0) {
                totalRatingPoints += stat.getRating().doubleValue() * stat.getAppearances();
                ratingsCount += stat.getAppearances();
            }

            // Collect club names
            if (stat.getClub() != null && stat.getClub().getName() != null) {
                String clubName = stat.getClub().getName();
                if (!clubNames.contains(clubName)) {
                    clubNames.add(clubName);
                }
            }
        }

        // Calculate average rating
        double averageRating = ratingsCount > 0 ? totalRatingPoints / ratingsCount : 0.0;

        return new CombinedSeasonStats(totalAppearances, totalGoals, totalAssists,
                totalMinutesPlayed, averageRating, clubNames);
    }

    private static class CombinedSeasonStats {

        private final int totalAppearances;
        private final int totalGoals;
        private final int totalAssists;
        private final int totalMinutesPlayed;
        private final double averageRating;
        private final List<String> clubNames;

        public CombinedSeasonStats(int totalAppearances, int totalGoals, int totalAssists,
                int totalMinutesPlayed, double averageRating, List<String> clubNames) {
            this.totalAppearances = totalAppearances;
            this.totalGoals = totalGoals;
            this.totalAssists = totalAssists;
            this.totalMinutesPlayed = totalMinutesPlayed;
            this.averageRating = averageRating;
            this.clubNames = clubNames;
        }

        public int getTotalAppearances() {
            return totalAppearances;
        }

        public int getTotalGoals() {
            return totalGoals;
        }

        public int getTotalAssists() {
            return totalAssists;
        }

        public int getTotalMinutesPlayed() {
            return totalMinutesPlayed;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public List<String> getClubNames() {
            return clubNames;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Player> searchPlayers(String query, String position, String nationality, String league,
            Integer minAge, Integer maxAge, String sortBy, Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            Sort sort = switch (sortBy != null ? sortBy : "") {
                case "name" ->
                    Sort.by(Sort.Direction.ASC, "firstName", "lastName");
                case "age" ->
                    Sort.by(Sort.Direction.ASC, "dateOfBirth");
                case "views" ->
                    Sort.by(Sort.Direction.DESC, "totalViews");
                default ->
                    Sort.by(Sort.Direction.DESC, "trendingScore");
            };

            Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(),
                    pageable.getPageSize(), sort);

            // Handle search with query
            if (query != null && !query.trim().isEmpty()) {
                // Build a filtered query using specifications or repository methods
                boolean hasPositionFilter = position != null && !position.trim().isEmpty();
                boolean hasNationalityFilter = nationality != null && !nationality.trim().isEmpty();
                boolean hasLeagueFilter = league != null && !league.trim().isEmpty();
                boolean hasAgeFilter = minAge != null || maxAge != null;

                if (hasPositionFilter || hasNationalityFilter || hasLeagueFilter || hasAgeFilter) {
                    // Get all matching players first
                    List<Player> allMatchingPlayers = playerRepository
                            .findByFirstNameOrLastNameContainingIgnoreCase(query.trim());

                    // Apply filters
                    List<Player> filteredPlayers = allMatchingPlayers.stream()
                            .filter(p -> {
                                boolean matches = true;

                                // Position filter - map display names to position codes
                                if (hasPositionFilter) {
                                    matches = position.equalsIgnoreCase(p.getPosition());
                                }

                                // Nationality filter
                                if (matches && hasNationalityFilter) {
                                    matches = nationality.trim().equalsIgnoreCase(p.getNationality());
                                }

                                // League filter
                                if (matches && hasLeagueFilter) {
                                    matches = playerHasLeague(p, league.trim());
                                }

                                // Age filter
                                if (matches && hasAgeFilter) {
                                    int playerAge = p.getAge();
                                    if (minAge != null && playerAge < minAge) {
                                        matches = false;
                                    }
                                    if (maxAge != null && playerAge > maxAge) {
                                        matches = false;
                                    }
                                }

                                return matches;
                            })
                            .toList();

                    // Apply pagination manually
                    int start = (int) sortedPageable.getOffset();
                    int end = Math.min((start + sortedPageable.getPageSize()), filteredPlayers.size());

                    List<Player> pageContent = start < filteredPlayers.size()
                            ? filteredPlayers.subList(start, end)
                            : List.of();

                    return new PageImpl<>(pageContent, sortedPageable, filteredPlayers.size());
                }

                // No additional filters - use repository pagination directly
                return playerRepository.findByFirstNameOrLastNameContainingIgnoreCase(
                        query.trim(), sortedPageable);
            }

            // No query - return all active players
            return playerRepository.findByIsActiveTrue(sortedPageable);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for searching players: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching players: {}", e.getMessage());
            throw new RuntimeException("Failed to search players", e);
        }
    }

    /**
     * Check if player belongs to a specific league
     */
    private boolean playerHasLeague(Player player, String leagueName) {
        if (player.getStatistics() == null || player.getStatistics().isEmpty()) {
            return false;
        }

        // Check if any of the player's statistics are for the specified league
        return player.getStatistics().stream()
                .anyMatch(stat -> stat.getLeague() != null
                && leagueName.equalsIgnoreCase(stat.getLeague().getName()));
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Player> findPlayersByFilters(String position, String nationality, Integer minAge, Integer maxAge, Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            // If no filters are provided, return all active players
            if ((position == null || position.trim().isEmpty())
                    && (nationality == null || nationality.trim().isEmpty())
                    && minAge == null && maxAge == null) {
                return playerRepository.findByIsActiveTrue(pageable);
            }

            // Handle age filtering
            if (minAge != null && maxAge != null) {
                List<Player> ageFilteredPlayers = playerRepository.findByAgeRange(minAge, maxAge);

                // Further filter by position and/or nationality if provided
                if (position != null && !position.trim().isEmpty()) {
                    ageFilteredPlayers = ageFilteredPlayers.stream()
                            .filter(p -> position.trim().equalsIgnoreCase(p.getPosition()))
                            .toList();
                }

                if (nationality != null && !nationality.trim().isEmpty()) {
                    ageFilteredPlayers = ageFilteredPlayers.stream()
                            .filter(p -> nationality.trim().equalsIgnoreCase(p.getNationality()))
                            .toList();
                }

                // Convert to Page (manual pagination)
                return createPageFromList(ageFilteredPlayers, pageable);
            }

            if (position != null && !position.trim().isEmpty()) {
                if (nationality != null && !nationality.trim().isEmpty()) {
                    // Filter by both position and nationality
                    return playerRepository.findByPositionAndNationalityAndIsActiveTrue(position.trim(), nationality.trim(), pageable);
                }
                return playerRepository.findByPositionAndIsActiveTrue(position.trim(), pageable);
            }

            if (nationality != null && !nationality.trim().isEmpty()) {
                return playerRepository.findByNationalityAndIsActiveTrue(nationality.trim(), pageable);
            }

            return playerRepository.findByIsActiveTrue(pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for filtering players: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error filtering players: {}", e.getMessage());
            throw new RuntimeException("Failed to filter players", e);
        }
    }

    /**
     * Get top rated players by league for current season
     */
    @Transactional(readOnly = true)
    public List<Player> getTopRatedPlayersByLeague(Long leagueId, int limit) {
        try {
            if (leagueId == null) {
                throw new IllegalArgumentException("League ID cannot be null");
            }

            Optional<League> league = leagueRepository.findById(leagueId);
            if (league.isEmpty()) {
                throw new IllegalArgumentException("League not found with ID: " + leagueId);
            }

            logger.info("Searching for players in league: {} (ID: {})", league.get().getName(), leagueId);

            // Try current season first (2025)
            List<PlayerStatistic> topRatedStats = playerStatisticRepository
                    .findByLeagueAndSeasonOrderByRatingDesc(league.get(), 2025);

            logger.info("Found {} player statistics for season 2025", topRatedStats.size());

            // If no results for 2025, try 2024
            if (topRatedStats.isEmpty()) {
                topRatedStats = playerStatisticRepository
                        .findByLeagueAndSeasonOrderByRatingDesc(league.get(), 2024);
                logger.info("Found {} player statistics for season 2024", topRatedStats.size());
            }

            // Filter by rating and limit results
            List<Player> players = topRatedStats.stream()
                    .filter(stat -> stat.getRating() != null && stat.getRating().compareTo(BigDecimal.ZERO) > 0)
                    .limit(limit)
                    .map(PlayerStatistic::getPlayer)
                    .distinct()
                    .collect(Collectors.toList());

            logger.info("Returning {} players for league {}", players.size(), league.get().getName());
            return players;

        } catch (IllegalArgumentException e) {
            logger.error("Error getting top rated players for league {}: {}", leagueId, e.getMessage(), e);
            throw new RuntimeException("Failed to get top rated players for league", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public List<Player> saveAll(List<Player> players) {
        try {
            if (players == null || players.isEmpty()) {
                throw new IllegalArgumentException("Players list cannot be null or empty");
            }
            return playerRepository.saveAll(players);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid players list: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error saving players batch: {}", e.getMessage());
            throw new RuntimeException("Failed to save players batch", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getTotalPlayerCount() {
        try {
            return playerRepository.count();
        } catch (Exception e) {
            logger.error("Error counting total players: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getActivePlayerCount() {
        try {
            return playerRepository.countByIsActiveTrue();
        } catch (Exception e) {
            logger.error("Error counting active players: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<Player> findPlayersByPosition(String position) {
        try {
            if (position == null || position.trim().isEmpty()) {
                throw new IllegalArgumentException("Position cannot be null or empty");
            }
            return playerRepository.findByPositionAndIsActiveTrue(position.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid position parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding players by position '{}': {}", position, e.getMessage());
            throw new RuntimeException("Failed to find players by position", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<Player> findPlayersByAgeRange(int minAge, int maxAge) {
        try {
            if (minAge < 0 || maxAge < 0 || minAge > maxAge) {
                throw new IllegalArgumentException("Invalid age range: minAge=" + minAge + ", maxAge=" + maxAge);
            }
            return playerRepository.findByAgeRange(minAge, maxAge);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid age range parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding players by age range {}-{}: {}", minAge, maxAge, e.getMessage());
            throw new RuntimeException("Failed to find players by age range", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private Page<Player> createPageFromList(List<Player> players, Pageable pageable) {
        try {
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), players.size());

            List<Player> pageContent = players.subList(start, end);

            return new PageImpl<>(pageContent, pageable, players.size());

        } catch (Exception e) {
            logger.error("Error creating page from player list: {}", e.getMessage());
            throw new RuntimeException("Failed to create page from player list", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private void updateTrendingScore(Player player) {
        try {
            // Simple trending score calculation based on views and recency
            int totalViews = Objects.requireNonNullElse(player.getTotalViews(), 0);
            int weeklyViews = Objects.requireNonNullElse(player.getWeeklyViews(), 0);
            int monthlyViews = Objects.requireNonNullElse(player.getMonthlyViews(), 0);

            // Weight recent views more heavily
            double trendingScore = (weeklyViews * 3.0) + (monthlyViews * 1.5) + (totalViews * 0.1);

            // Cap the score at 100
            trendingScore = Math.min(trendingScore, 100.0);

            player.setTrendingScore(trendingScore);

        } catch (Exception e) {
            logger.error("Error updating trending score for player: {}", player.getName(), e);
        }
    }
}

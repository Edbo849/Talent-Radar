package com.talentradar.service.player;

import java.math.BigDecimal;
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
import com.talentradar.model.club.Club;
import com.talentradar.model.club.League;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerStatistic;
import com.talentradar.repository.club.ClubRepository;
import com.talentradar.repository.club.LeagueRepository;
import com.talentradar.repository.player.PlayerRepository;
import com.talentradar.repository.player.PlayerStatisticRepository;

/**
 * Service responsible for managing player statistics. Handles statistic
 * creation, updates, retrieval, and analytics operations.
 */
@Service
@Transactional
public class PlayerStatisticService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerStatisticService.class);

    @Autowired
    private PlayerStatisticRepository statisticRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerStatistic> getPlayerStatistics(Long playerId) {
        try {
            if (playerId == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            return statisticRepository.findByPlayerOrderBySeasonDesc(player);

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting player statistics: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving statistics for player ID {}: {}", playerId, e.getMessage());
            throw new RuntimeException("Failed to retrieve player statistics", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<PlayerStatistic> getPlayerStatisticsBySeason(Long playerId, Integer season) {
        try {
            if (playerId == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            List<PlayerStatistic> statistics = statisticRepository.findByPlayerAndSeason(player, season);
            return statistics.isEmpty() ? Optional.empty() : Optional.of(statistics.get(0));

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting player statistics by season: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving statistics for player ID {} and season {}: {}", playerId, season, e.getMessage());
            throw new RuntimeException("Failed to retrieve player statistics by season", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerStatistic> getPlayerStatisticsByClub(Long playerId, Long clubId) {
        try {
            if (playerId == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }
            if (clubId == null) {
                throw new IllegalArgumentException("Club ID cannot be null");
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("Club not found with ID: " + clubId));

            return statisticRepository.findByPlayerAndClubOrderBySeasonDesc(player, club);

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting player statistics by club: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving statistics for player ID {} and club ID {}: {}", playerId, clubId, e.getMessage());
            throw new RuntimeException("Failed to retrieve player statistics by club", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<PlayerStatistic> getTopGoalScorers(Integer season, Pageable pageable) {
        try {
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return statisticRepository.findTopGoalScorersBySeason(season, pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting top goal scorers: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving top goal scorers for season {}: {}", season, e.getMessage());
            throw new RuntimeException("Failed to retrieve top goal scorers", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<PlayerStatistic> getTopAssistProviders(Integer season, Pageable pageable) {
        try {
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return statisticRepository.findTopAssistProvidersBySeason(season, pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting top assist providers: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving top assist providers for season {}: {}", season, e.getMessage());
            throw new RuntimeException("Failed to retrieve top assist providers", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<PlayerStatistic> getMostActivePlayer(Integer season, Pageable pageable) {
        try {
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return statisticRepository.findMostActivePlayersBySeason(season, pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting most active players: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving most active players for season {}: {}", season, e.getMessage());
            throw new RuntimeException("Failed to retrieve most active players", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerStatistic> getClubStatistics(Long clubId, Integer season) {
        try {
            if (clubId == null) {
                throw new IllegalArgumentException("Club ID cannot be null");
            }
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }

            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("Club not found with ID: " + clubId));

            return statisticRepository.findByClubAndSeasonOrderByGoalsDesc(club, season);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting club statistics: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving statistics for club ID {} and season {}: {}", clubId, season, e.getMessage());
            throw new RuntimeException("Failed to retrieve club statistics", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerStatistic> getLeagueStatistics(Long leagueId, Integer season) {
        try {
            if (leagueId == null) {
                throw new IllegalArgumentException("League ID cannot be null");
            }
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }

            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new IllegalArgumentException("League not found with ID: " + leagueId));

            return statisticRepository.findByLeagueAndSeasonOrderByGoalsDesc(league, season);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting league statistics: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving statistics for league ID {} and season {}: {}", leagueId, season, e.getMessage());
            throw new RuntimeException("Failed to retrieve league statistics", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PlayerStatistic createOrUpdateStatistic(Long playerId, Long clubId, Long leagueId, Integer season) {
        try {
            if (playerId == null) {
                throw new IllegalArgumentException("Player ID cannot be null");
            }
            if (clubId == null) {
                throw new IllegalArgumentException("Club ID cannot be null");
            }
            if (leagueId == null) {
                throw new IllegalArgumentException("League ID cannot be null");
            }
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));

            Club club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new IllegalArgumentException("Club not found with ID: " + clubId));

            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new IllegalArgumentException("League not found with ID: " + leagueId));

            // Check if statistic already exists
            Optional<PlayerStatistic> existingStatOpt = statisticRepository
                    .findByPlayerAndClubAndLeagueAndSeason(player, club, league, season);

            PlayerStatistic statistic;
            if (existingStatOpt.isPresent()) {
                statistic = existingStatOpt.get();
                logger.info("Updating existing statistic for player {} in season {}", player.getName(), season);
            } else {
                statistic = new PlayerStatistic(player, club, league, season);
            }

            return statisticRepository.save(statistic);

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Error creating or updating statistic: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error creating or updating statistic for player ID {}: {}", playerId, e.getMessage());
            throw new RuntimeException("Failed to create or update statistic", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public PlayerStatistic updateStatisticField(Long statisticId, String fieldName, Object value) {
        try {
            if (statisticId == null) {
                throw new IllegalArgumentException("Statistic ID cannot be null");
            }
            if (fieldName == null || fieldName.trim().isEmpty()) {
                throw new IllegalArgumentException("Field name cannot be null or empty");
            }
            if (value == null) {
                throw new IllegalArgumentException("Value cannot be null");
            }

            PlayerStatistic statistic = statisticRepository.findById(statisticId)
                    .orElseThrow(() -> new IllegalArgumentException("Statistic not found with ID: " + statisticId));

            switch (fieldName.toLowerCase().trim()) {
                case "goals" ->
                    statistic.setGoals((Integer) value);
                case "assists" ->
                    statistic.setAssists((Integer) value);
                case "appearances" ->
                    statistic.setAppearances((Integer) value);
                case "minutesplayed" ->
                    statistic.setMinutesPlayed((Integer) value);
                case "rating" ->
                    statistic.setRating((BigDecimal) value);
                case "yellowcards" ->
                    statistic.setYellowCards((Integer) value);
                case "redcards" ->
                    statistic.setRedCards((Integer) value);
                default ->
                    throw new IllegalArgumentException("Unknown field: " + fieldName);
            }

            PlayerStatistic savedStatistic = statisticRepository.save(statistic);
            logger.info("Updated statistic field '{}' for statistic ID {}", fieldName, statisticId);
            return savedStatistic;

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for updating statistic field: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating statistic field '{}' for statistic ID {}: {}", fieldName, statisticId, e.getMessage());
            throw new RuntimeException("Failed to update statistic field", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<PlayerStatistic> getStatisticById(Long statisticId) {
        try {
            if (statisticId == null) {
                logger.warn("Attempted to find statistic with null ID");
                return Optional.empty();
            }
            return statisticRepository.findById(statisticId);
        } catch (Exception e) {
            logger.error("Error finding statistic by ID {}: {}", statisticId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteStatistic(Long statisticId) {
        try {
            if (statisticId == null) {
                throw new IllegalArgumentException("Statistic ID cannot be null");
            }

            if (!statisticRepository.existsById(statisticId)) {
                throw new IllegalArgumentException("Statistic not found with ID: " + statisticId);
            }

            statisticRepository.deleteById(statisticId);
            logger.info("Deleted statistic with ID: {}", statisticId);

        } catch (IllegalArgumentException e) {
            logger.error("Error deleting statistic: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting statistic with ID {}: {}", statisticId, e.getMessage());
            throw new RuntimeException("Failed to delete statistic", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<PlayerStatistic> getStatisticsByPosition(String position, Integer season, Pageable pageable) {
        try {
            if (position == null || position.trim().isEmpty()) {
                throw new IllegalArgumentException("Position cannot be null or empty");
            }
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return statisticRepository.findByPositionAndSeasonOrderByGoalsDesc(position.trim(), season, pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting statistics by position: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving statistics for position '{}' and season {}: {}", position, season, e.getMessage());
            throw new RuntimeException("Failed to retrieve statistics by position", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerStatistic> comparePlayerStatistics(List<Long> playerIds, Integer season) {
        try {
            if (playerIds == null || playerIds.isEmpty()) {
                throw new IllegalArgumentException("Player IDs list cannot be null or empty");
            }
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }

            return playerIds.stream()
                    .map(playerId -> {
                        try {
                            Player player = playerRepository.findById(playerId)
                                    .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));
                            List<PlayerStatistic> stats = statisticRepository.findByPlayerAndSeason(player, season);
                            return stats.isEmpty() ? null : stats.get(0);
                        } catch (Exception e) {
                            logger.warn("Could not retrieve statistics for player ID {}: {}", playerId, e.getMessage());
                            return null;
                        }
                    })
                    .filter(stat -> stat != null)
                    .toList();

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for comparing player statistics: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error comparing player statistics for season {}: {}", season, e.getMessage());
            throw new RuntimeException("Failed to compare player statistics", e);
        }
    }
}

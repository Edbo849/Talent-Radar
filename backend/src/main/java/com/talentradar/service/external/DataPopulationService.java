package com.talentradar.service.external;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.talentradar.exception.DailyLimitExceededException;
import com.talentradar.model.club.Club;
import com.talentradar.model.club.Country;
import com.talentradar.model.club.League;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerInjury;
import com.talentradar.model.player.PlayerSidelined;
import com.talentradar.model.player.PlayerStatistic;
import com.talentradar.model.player.PlayerTransfer;
import com.talentradar.model.player.PlayerTrophy;
import com.talentradar.repository.club.ClubRepository;
import com.talentradar.repository.club.CountryRepository;
import com.talentradar.repository.club.LeagueRepository;
import com.talentradar.repository.player.PlayerInjuryRepository;
import com.talentradar.repository.player.PlayerRepository;
import com.talentradar.repository.player.PlayerSidelinedRepository;
import com.talentradar.repository.player.PlayerStatisticRepository;
import com.talentradar.repository.player.PlayerTransferRepository;
import com.talentradar.repository.player.PlayerTrophyRepository;
import com.talentradar.service.system.ScheduledTaskService;

import jakarta.transaction.Transactional;

/**
 * Service responsible for populating the database with football data from
 * external APIs. Manages the retrieval and storage of leagues, clubs, players,
 * and statistics.
 */
@Primary
@Service("externalDataPopulationService")
public class DataPopulationService {

    private static final Logger logger = LoggerFactory.getLogger(DataPopulationService.class);
    private static final int CURRENT_SEASON = 2025;
    private static final int MAX_API_CALLS = 75000;

    @Autowired
    private ApiFootballService apiFootballService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private PlayerStatisticRepository playerStatisticRepository;

    @Autowired
    private PlayerInjuryRepository playerInjuryRepository;

    @Autowired
    private PlayerTransferRepository playerTransferRepository;

    @Autowired
    private PlayerSidelinedRepository playerSidelinedRepository;

    @Autowired
    private PlayerTrophyRepository playerTrophyRepository;

    @Autowired
    private ApplicationContext applicationContext;

    private int apiCallCounter = 0;

    /**
     * Automatically trigger data population when the application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application ready. Triggering data population...");
        populateU21PlayersAsync();
    }

    /**
     * Asynchronously populates the database with U21 player data.
     */
    @Async
    public void populateU21PlayersAsync() {
        logger.info("Starting U21 player population process...");

        boolean success = false;
        String message = "";

        try {
            populateU21Players();
            success = true;
            logger.info("U21 player population completed successfully!");
        } catch (Exception e) {
            message = e.getMessage();
            logger.error("Error during U21 player population: {}", message, e);
        } finally {
            // Notify scheduled service that population is complete
            notifyScheduledService(success, message);
        }
    }

    /**
     * Populates the database with U21 players from top European leagues.
     */
    @Transactional
    public void populateU21Players() {
        logger.info("Starting comprehensive U21 player population...");
        apiCallCounter = 0;

        try {
            List<League> topLeagues = apiFootballService.getTopLeagues();
            if (topLeagues.isEmpty()) {
                throw new RuntimeException("No leagues retrieved from API");
            }

            apiCallCounter += topLeagues.size(); // Count league API calls
            logger.info("Retrieved {} leagues", topLeagues.size());

            for (League league : topLeagues) {
                if (apiCallCounter >= MAX_API_CALLS * 0.95) { // Stop at 95% to leave buffer
                    logger.info("Approaching API limit ({}). Stopping data population.", apiCallCounter);
                    break;
                }

                try {
                    processLeague(league);
                    logger.info("League {} processing completed",
                            league.getName());

                } catch (DailyLimitExceededException e) {
                    logger.warn("Daily API limit reached: {}", e.getMessage());
                    handleDailyLimitReached();
                    return;
                } catch (Exception e) {
                    logger.error("Error processing league {}: {}", league.getName(), e.getMessage());
                }
            }

            logger.info("Data population completed successfully. Total API calls made: {}/{}",
                    apiCallCounter, MAX_API_CALLS);

        } catch (DailyLimitExceededException e) {
            logger.warn("Daily API limit reached during league retrieval: {}", e.getMessage());
            handleDailyLimitReached();
        }
    }

    /**
     * Handles the daily API limit being reached by scheduling continuation for
     * tomorrow.
     */
    /**
     * Handles the daily API limit being reached by stopping data population.
     */
    private void handleDailyLimitReached() {
        LocalDateTime now = LocalDateTime.now();

        logger.warn("=== DAILY API LIMIT REACHED ===");
        logger.warn("Data population has been STOPPED due to API limit.");
        logger.warn("Total API calls made: {}/{}", apiCallCounter, MAX_API_CALLS);
        logger.warn("Time stopped: {}", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logger.warn("To resume data population, use the manual trigger endpoint:");
        logger.warn("POST /api/admin/scheduled-tasks/trigger-population");
        logger.warn("===============================");

        // Mark as completed with failure message for the scheduled service
        try {
            ScheduledTaskService scheduledService = applicationContext.getBean(ScheduledTaskService.class);
            scheduledService.onPopulationComplete(false, "Stopped due to daily API limit reached");
        } catch (BeansException e) {
            logger.error("Error notifying scheduled service: {}", e.getMessage());
        }

        // Throw exception to stop the current population process
        throw new RuntimeException("Data population stopped - daily API limit reached");
    }

    /**
     * Method to process a league
     */
    private void processLeague(League league) {
        logger.info("Processing league: {}", league.getName());

        League savedLeague = saveLeague(league);

        try {
            // Get ALL U21 player IDs from this league using pagination
            List<Integer> playerIds = apiFootballService.getPlayerIdsFromLeague(
                    savedLeague.getExternalId(), CURRENT_SEASON);
            int seasonUsed = CURRENT_SEASON;

            // Count this as multiple API calls due to pagination
            apiCallCounter += Math.max(1, playerIds.size() / 20); // Estimate API calls based on typical page size

            // If no players found in 2025, try 2024
            if (playerIds.isEmpty()) {
                logger.info("No U21 players found for league: {} in season {}",
                        league.getName(), CURRENT_SEASON);

                seasonUsed = 2024;
                playerIds = apiFootballService.getPlayerIdsFromLeague(
                        savedLeague.getExternalId(), seasonUsed);

                // Count additional API calls
                apiCallCounter += Math.max(1, playerIds.size() / 20);
            }

            if (playerIds.isEmpty()) {
                logger.warn("No U21 players found for league: {} in season {}",
                        league.getName(), 2024);
                return;
            }

            logger.info("Found {} U21 eligible players in league {}",
                    playerIds.size(), league.getName());

            // Get all teams from this league
            List<Club> leagueClubs = getLeagueClubs(savedLeague, seasonUsed);
            logger.info("Retrieved {} clubs from league {}\n", leagueClubs.size(), league.getName());

            // Process each player with comprehensive data across all seasons
            int playersProcessed = 0;
            int playersSkipped = 0;

            for (Integer playerId : playerIds) {
                if (apiCallCounter >= MAX_API_CALLS) {
                    logger.warn("API limit reached, stopping player processing at {}/{}",
                            playersProcessed, playerIds.size());
                    break;
                }

                try {
                    // Check if player already exists to avoid duplicate processing
                    Optional<Player> existingPlayer = playerRepository.findByExternalId(playerId);
                    if (existingPlayer.isPresent()) {
                        playersSkipped++;
                        if (playersSkipped % 100 == 0) {
                            logger.info("Skipped {} existing players", playersSkipped);
                        }
                        continue;
                    }

                    // Pass the league and its clubs to help determine correct current club
                    processPlayerComprehensively(playerId, savedLeague, leagueClubs);
                    playersProcessed++;

                } catch (DailyLimitExceededException e) {
                    logger.warn("Daily API limit reached while processing player {}: {}", playerId, e.getMessage());
                    throw e;
                } catch (Exception e) {
                    logger.error("Error processing player {}: {}", playerId, e.getMessage());
                    // Continue with next player rather than failing entire league
                }
            }

            logger.info("Completed league {} processing: {} new players processed, {} existing players skipped",
                    league.getName(), playersProcessed, playersSkipped);

        } catch (DailyLimitExceededException e) {
            throw e; // Re-throw daily limit exceptions
        }
    }

    /**
     * Get all clubs from a league for club determination
     */
    private List<Club> getLeagueClubs(League league, int season) {
        try {
            // First try to get clubs from existing statistics
            List<Club> existingClubs = clubRepository.findByCurrentLeague(league);
            if (!existingClubs.isEmpty()) {
                return existingClubs;
            }

            // If no existing clubs, fetch from API using the specified season
            List<Club> apiClubs = apiFootballService.getTeamsFromLeague(league.getExternalId(), season);
            apiCallCounter++;

            // Save the clubs
            List<Club> savedClubs = new ArrayList<>();
            for (Club club : apiClubs) {
                try {
                    Club savedClub = processClubForStatistics(club, null);
                    if (savedClub != null) {
                        savedClubs.add(savedClub);
                    }
                } catch (Exception e) {
                    logger.warn("Error processing club {} from league {}: {}", club.getName(), league.getName(), e.getMessage());
                }
            }

            return savedClubs;

        } catch (Exception e) {
            logger.error("Error getting clubs for league {} for season {}: {}", league.getName(), season, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Processes a single player with comprehensive data.
     */
    private void processPlayerComprehensively(Integer playerId, League league, List<Club> leagueClubs) {
        try {

            // Get comprehensive player information across all seasons
            Player player = apiFootballService.getPlayerDetailsAllSeasons(playerId);
            apiCallCounter += 2; // getPlayerSeasons + getPlayerDetailsAllSeasons

            if (player == null || !player.isEligibleForU21()) {
                logger.debug("Player {} is not U21 eligible or data unavailable", playerId);
                return;
            }

            // Determine current club with priority to the league being processed
            Club currentClub = determinePlayerCurrentClubForLeague(player, league, leagueClubs);
            player.setCurrentClub(currentClub);

            // Extract and clear statistics to handle separately
            List<PlayerStatistic> originalStats = new ArrayList<>(player.getStatistics());
            player.getStatistics().clear();

            // Save player first
            Player savedPlayer = playerRepository.save(player);

            // Get and save comprehensive data across all seasons
            try {
                // 1. Get and save player transfers (covers all seasons)
                List<PlayerTransfer> transfers = apiFootballService.getPlayerTransfers(playerId);
                apiCallCounter++;
                savePlayerTransfers(savedPlayer, transfers);
                logger.debug("Saved {} transfers for player: {}", transfers.size(), savedPlayer.getName());

                // 2. Get and save player injuries across all seasons
                List<PlayerInjury> injuries = apiFootballService.getAllPlayerInjuries(playerId);
                apiCallCounter += Math.max(1, injuries.size() / 10); // Estimate based on seasons queried
                savePlayerInjuries(savedPlayer, injuries, currentClub, league);
                logger.debug("Saved {} injuries for player: {}", injuries.size(), savedPlayer.getName());

                // 3. Get and save sidelined periods across all seasons
                List<PlayerSidelined> sidelinedPeriods = apiFootballService.getAllPlayerSidelined(playerId);
                apiCallCounter++;
                savePlayerSidelined(savedPlayer, sidelinedPeriods);
                logger.debug("Saved {} sidelined periods for player: {}", sidelinedPeriods.size(), savedPlayer.getName());

                // 4. Get and save trophies (covers career)
                List<PlayerTrophy> trophies = apiFootballService.getPlayerTrophies(playerId);
                apiCallCounter++;
                savePlayerTrophies(savedPlayer, trophies);
                logger.debug("Saved {} trophies for player: {}", trophies.size(), savedPlayer.getName());

            } catch (Exception e) {
                logger.warn("Error retrieving additional data for player {}: {}", savedPlayer.getName(), e.getMessage());
                // Continue with statistics even if other data fails
            }

            // 5. Process and save all statistics
            if (!originalStats.isEmpty()) {
                savedPlayer.getStatistics().addAll(originalStats);
                for (PlayerStatistic stat : savedPlayer.getStatistics()) {
                    stat.setPlayer(savedPlayer);
                }
                saveAllPlayerStatistics(savedPlayer);
            }

            logger.info("Successfully completed processing for player: {}\n",
                    savedPlayer.getName());

        } catch (Exception e) {
            logger.error("Error in comprehensive player processing for {}: {}", playerId, e.getMessage());
            throw new RuntimeException("Failed to process player comprehensively: " + playerId, e);
        }
    }

    /**
     * Enhanced method to determine player's current club with league priority
     */
    private Club determinePlayerCurrentClubForLeague(Player player, League league, List<Club> leagueClubs) {
        try {

            // Check if this is a national team competition
            boolean isNationalCompetition = isNationalCompetition(league);

            if (isNationalCompetition) {
                // For national competitions, find the player's actual club team
                return determineActualClubForNationalCompetition(player);
            }

            // PRIORITY 1: Find the player's club within THIS specific league for the current season
            if (player.getStatistics() != null && !player.getStatistics().isEmpty()) {

                // First, look for statistics in the current league and current season
                Optional<PlayerStatistic> currentLeagueStat = player.getStatistics().stream()
                        .filter(stat -> stat.getLeague() != null
                        && stat.getClub() != null
                        && Objects.equals(stat.getLeague().getExternalId(), league.getExternalId())
                        && Objects.equals(stat.getSeason(), CURRENT_SEASON))
                        .findFirst();

                if (currentLeagueStat.isPresent()) {
                    Club leagueClub = currentLeagueStat.get().getClub();
                    Club processedClub = processClubForStatistics(leagueClub, null);
                    if (processedClub != null && processedClub.getId() != null) {
                        return processedClub;
                    }
                }

                // Second, look for any statistics in this league (any season)
                Optional<PlayerStatistic> anyLeagueStat = player.getStatistics().stream()
                        .filter(stat -> stat.getLeague() != null
                        && stat.getClub() != null
                        && Objects.equals(stat.getLeague().getExternalId(), league.getExternalId()))
                        .max((s1, s2) -> {
                            Integer season1 = s1.getSeason();
                            Integer season2 = s2.getSeason();
                            int value1 = season1 != null ? season1 : 0;
                            int value2 = season2 != null ? season2 : 0;
                            return Integer.compare(value1, value2);
                        });

                if (anyLeagueStat.isPresent()) {
                    Club leagueClub = anyLeagueStat.get().getClub();
                    Club processedClub = processClubForStatistics(leagueClub, null);
                    if (processedClub != null && processedClub.getId() != null) {
                        return processedClub;
                    }
                }

                // Third, check if any of the player's recent clubs are in this league's club list
                PlayerStatistic mostRecentNonNationalStat = player.getStatistics().stream()
                        .filter(stat -> stat.getClub() != null && !isNationalTeam(stat.getClub()))
                        .max((s1, s2) -> {
                            Integer season1 = s1.getSeason();
                            Integer season2 = s2.getSeason();
                            int value1 = season1 != null ? season1 : 0;
                            int value2 = season2 != null ? season2 : 0;
                            return Integer.compare(value1, value2);
                        })
                        .orElse(null);
                if (mostRecentNonNationalStat != null) {
                    Club recentClub = mostRecentNonNationalStat.getClub();

                    // Check if this club is in our league's club list
                    boolean isInLeague = leagueClubs.stream()
                            .anyMatch(leagueClub
                                    -> Objects.equals(leagueClub.getExternalId(), recentClub.getExternalId())
                            || (leagueClub.getName() != null && recentClub.getName() != null
                            && leagueClub.getName().equalsIgnoreCase(recentClub.getName())));

                    if (isInLeague) {
                        Club processedClub = processClubForStatistics(recentClub, null);
                        if (processedClub != null && processedClub.getId() != null) {
                            return processedClub;
                        }
                    }
                }
            }

            // PRIORITY 2: If no specific league match, try to find any club from the league clubs list
            if (!leagueClubs.isEmpty()) {
                // Just pick the first club from the league as a fallback
                Club fallbackClub = leagueClubs.get(0);
                Club processedClub = processClubForStatistics(fallbackClub, null);
                if (processedClub != null && processedClub.getId() != null) {
                    return processedClub;
                }
            }

            // PRIORITY 3: Create a Free Agent club as last resort
            Club freeAgentClub = getOrCreateFreeAgentClub();
            logger.debug("No suitable club found, using Free Agent club for player in league {}", league.getName());
            return freeAgentClub;

        } catch (Exception e) {
            logger.error("Error determining current club for player in league {}: {}", league.getName(), e.getMessage());
            return getOrCreateFreeAgentClub();
        }
    }

    /**
     * Determines if a league/competition is national team based
     */
    @Transactional
    private boolean isNationalCompetition(League league) {
        if (league == null || league.getName() == null) {
            return false;
        }

        String leagueName = league.getName().toLowerCase();

        // Look for clear international competition patterns in league name
        boolean hasInternationalKeywords = leagueName.contains("uefa")
                || leagueName.contains("fifa")
                || leagueName.contains("conmebol")
                || leagueName.contains("ofc")
                || leagueName.contains("afc")
                || leagueName.contains("caf")
                || leagueName.contains("world")
                || leagueName.contains("nations")
                || leagueName.contains("euro")
                || leagueName.contains("copa")
                || leagueName.contains("asia")
                || leagueName.contains("africa")
                || leagueName.contains("international")
                || leagueName.contains("olympics")
                || leagueName.contains("olympic")
                || leagueName.contains("agcff")
                || leagueName.contains("aff")
                || leagueName.contains("baltic")
                || leagueName.contains("concacaf")
                || leagueName.contains("cecafa")
                || leagueName.contains("asean")
                || leagueName.contains("caribbean")
                || leagueName.contains("cafa")
                || leagueName.contains("confederations")
                || leagueName.contains("cosafa")
                || leagueName.contains("eaff")
                || leagueName.contains("cotif")
                || leagueName.contains("friendlies")
                || leagueName.contains("gulf")
                || leagueName.contains("waff")
                || leagueName.contains("u20 elite league")
                || leagueName.contains("atlantic")
                || leagueName.contains("sudamericano")
                || leagueName.contains("saff")
                || leagueName.contains("south american")
                || leagueName.contains("european")
                || leagueName.contains("american")
                || leagueName.contains("pacific")
                || leagueName.contains("mediterranean")
                || leagueName.contains("asian")
                || leagueName.contains("african")
                || leagueName.contains("arabic")
                || leagueName.contains("conmenbol")
                || leagueName.contains("viareggio")
                || leagueName.contains("arab");

        return hasInternationalKeywords;
    }

    /**
     * Finds the player's actual club team when discovered through national
     * competition
     */
    private Club determineActualClubForNationalCompetition(Player player) {
        if (player.getStatistics() == null || player.getStatistics().isEmpty()) {
            return getOrCreateFreeAgentClub();
        }

        // Find most recent NON-NATIONAL team statistics
        Optional<PlayerStatistic> mostRecentClubStat = player.getStatistics().stream()
                .filter(stat -> stat.getClub() != null && !isNationalTeam(stat.getClub()))
                .filter(stat -> stat.getLeague() != null && !isNationalCompetition(stat.getLeague()))
                .max((s1, s2) -> {
                    Integer season1 = s1.getSeason();
                    Integer season2 = s2.getSeason();
                    int value1 = season1 != null ? season1 : 0;
                    int value2 = season2 != null ? season2 : 0;
                    return Integer.compare(value1, value2);
                });

        if (mostRecentClubStat.isPresent()) {
            Club clubTeam = mostRecentClubStat.get().getClub();
            Club processedClub = processClubForStatistics(clubTeam, null);
            if (processedClub != null && processedClub.getId() != null) {
                return processedClub;
            }
        }

        // If no club team found, try to get additional seasons data
        return findClubFromAdditionalSeasons(player);
    }

    /**
     * Attempts to find club team by fetching additional seasons if needed
     */
    private Club findClubFromAdditionalSeasons(Player player) {
        try {
            // Get more comprehensive player data if we don't have enough seasons
            List<Integer> playerSeasons = apiFootballService.getPlayerSeasons(player.getExternalId());
            apiCallCounter++;

            // Look for club teams in recent seasons
            for (Integer season : playerSeasons.subList(0, Math.min(3, playerSeasons.size()))) {
                List<PlayerStatistic> seasonStats = apiFootballService.getPlayerStatisticsBySeason(
                        player.getExternalId(), season);
                apiCallCounter++;

                for (PlayerStatistic stat : seasonStats) {
                    if (stat.getClub() != null && !isNationalTeam(stat.getClub())
                            && stat.getLeague() != null && !isNationalCompetition(stat.getLeague())) {

                        Club processedClub = processClubForStatistics(stat.getClub(), null);
                        if (processedClub != null && processedClub.getId() != null) {
                            return processedClub;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error fetching additional seasons for club determination: {}", e.getMessage());
        }

        return getOrCreateFreeAgentClub();
    }

    /**
     * Helper method to check if a club is a national team
     */
    private boolean isNationalTeam(Club club) {
        if (club == null || club.getName() == null) {
            return false;
        }

        String clubName = club.getName().toLowerCase();

        // Check if it's marked as national team
        if (club.getIsNational() != null && club.getIsNational()) {
            return true;
        }

        // Check common national team name patterns
        return clubName.contains("national team");
    }

    /**
     * Gets or creates a "Free Agent" club for players without a clear current
     * club
     */
    private Club getOrCreateFreeAgentClub() {
        try {
            // Try to find existing "Free Agent" club
            Optional<Club> existingFreeAgent = clubRepository.findByNameIgnoreCase("Free Agent");
            if (existingFreeAgent.isPresent()) {
                return existingFreeAgent.get();
            }

            // Create new "Free Agent" club
            Club freeAgentClub = new Club();
            freeAgentClub.setName("Free Agent");
            freeAgentClub.setIsActive(true);
            freeAgentClub.setIsNational(false);

            return clubRepository.saveAndFlush(freeAgentClub);

        } catch (Exception e) {
            logger.error("Error creating Free Agent club: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Saves all player statistics across multiple seasons and clubs.
     */
    @Transactional
    private void saveAllPlayerStatistics(Player savedPlayer) {
        if (savedPlayer.getStatistics().isEmpty()) {
            logger.debug("No statistics to save for player: {}", savedPlayer.getName());
            return;
        }

        // Convert to list to avoid concurrent modification and work with detached entities
        List<PlayerStatistic> statsList = new ArrayList<>(savedPlayer.getStatistics());

        // Clear the original collection to avoid any entity state issues
        savedPlayer.getStatistics().clear();
        playerRepository.saveAndFlush(savedPlayer);
        int savedCount = 0;
        for (PlayerStatistic stats : statsList) {
            try {
                // Extract data from the statistic BEFORE any entity manipulation
                Integer season = stats.getSeason();

                // Process club first - make sure it's fully persisted
                Club statsClub = processClubForStatistics(stats.getClub(), savedPlayer.getCurrentClub());
                if (statsClub == null) {
                    logger.error("Could not resolve club for statistics, skipping");
                    continue;
                }
                if (statsClub.getId() == null) {
                    logger.error("Club for PlayerStatistic is not persisted! Club: {}", statsClub);
                    continue;
                }

                // Process league - make sure it's fully persisted
                League statsLeague = processLeagueForStatistics(stats.getLeague(), season);
                if (statsLeague == null) {
                    continue;
                }

                // Check if statistics already exist for this specific combination
                Optional<PlayerStatistic> existingStats = playerStatisticRepository
                        .findByPlayerAndClubAndLeagueAndSeason(
                                savedPlayer, statsClub, statsLeague, season);

                PlayerStatistic savedStats;
                if (existingStats.isEmpty()) {
                    // Create completely new PlayerStatistic entity for each season/club combination
                    PlayerStatistic newStats = new PlayerStatistic();
                    newStats.setPlayer(savedPlayer);
                    newStats.setClub(statsClub);
                    newStats.setLeague(statsLeague);
                    newStats.setSeason(season);

                    // Copy all statistical data
                    copyStatisticsData(stats, newStats);

                    // Save this specific season/club record
                    savedStats = playerStatisticRepository.saveAndFlush(newStats);
                    savedCount++;
                } else {
                    // Update existing statistics
                    PlayerStatistic existingStat = existingStats.get();
                    copyStatisticsData(stats, existingStat);
                    savedStats = playerStatisticRepository.saveAndFlush(existingStat);
                    savedCount++;
                }

                // Add to player's statistics collection
                savedPlayer.getStatistics().add(savedStats);

            } catch (Exception e) {
                logger.error("Error saving statistics for player {} (season {}): {}",
                        savedPlayer.getName(), stats.getSeason(), e.getMessage(), e);
            }
        }

    }

    /**
     * Process club entity for statistics, ensuring it's properly persisted.
     */
    private Club processClubForStatistics(Club sourceClub, Club fallbackClub) {
        try {

            // Case 1: Source club has external ID
            if (sourceClub != null && sourceClub.getExternalId() != null) {
                Optional<Club> existingClub = clubRepository.findByExternalId(sourceClub.getExternalId());
                if (existingClub.isPresent()) {
                    return existingClub.get();
                }

                // Fetch complete club details from API using external ID
                Club completeClub = apiFootballService.getClubById(sourceClub.getExternalId());
                apiCallCounter++;

                if (completeClub != null) {
                    // Save country if it exists
                    if (completeClub.getCountry() != null) {
                        completeClub.setCountry(saveCountry(completeClub.getCountry()));
                    }
                    return clubRepository.saveAndFlush(completeClub);
                } else {
                    // Save source club as fallback if API lookup fails
                    if (sourceClub.getCountry() != null) {
                        sourceClub.setCountry(saveCountry(sourceClub.getCountry()));
                    }
                    return clubRepository.saveAndFlush(sourceClub);
                }
            }

            // Case 2: Try to find by name if external ID is missing
            if (sourceClub != null && sourceClub.getName() != null) {
                Optional<Club> existingClub = clubRepository.findByNameIgnoreCase(sourceClub.getName().trim());
                if (existingClub.isPresent()) {
                    return existingClub.get();
                }

                // Since we don't have the external ID, we'll save the source club directly
                if (sourceClub.getCountry() != null) {
                    sourceClub.setCountry(saveCountry(sourceClub.getCountry()));
                }
                return clubRepository.saveAndFlush(sourceClub);
            }

            // Case 3: Try fallback club
            if (fallbackClub != null) {
                // Check if fallback club is already persisted (has ID)
                if (fallbackClub.getId() != null) {
                    return fallbackClub;
                }

                // Check if club with same external ID exists
                if (fallbackClub.getExternalId() != null) {
                    Optional<Club> existingClub = clubRepository.findByExternalId(fallbackClub.getExternalId());
                    if (existingClub.isPresent()) {
                        return existingClub.get();
                    }
                }

                // Save fallback club if it doesn't have an ID
                return clubRepository.saveAndFlush(fallbackClub);
            }

            // Case 4: Create a default club if no valid source or fallback
            Club defaultClub = new Club();
            defaultClub.setName("Free agent");
            defaultClub.setIsActive(true);
            defaultClub.setIsNational(false);
            return clubRepository.saveAndFlush(defaultClub);

        } catch (Exception e) {
            logger.error("Error processing club for statistics: {}", e.getMessage());

            // Last resort - create and save a minimal club
            try {
                Club emergencyClub = new Club();
                emergencyClub.setName("Error Fallback Club");
                emergencyClub.setIsActive(true);
                return clubRepository.saveAndFlush(emergencyClub);
            } catch (Exception inner) {
                logger.error("Failed to create emergency club: {}", inner.getMessage());
                return null;
            }
        }
    }

    /**
     * Process league entity for statistics, ensuring it's properly persisted.
     */
    private League processLeagueForStatistics(League sourceLeague, Integer season) {
        try {
            // Case 1: Source league has external ID
            if (sourceLeague != null && sourceLeague.getExternalId() != null) {
                Optional<League> existingLeague = leagueRepository.findByExternalId(sourceLeague.getExternalId());
                if (existingLeague.isPresent()) {
                    return existingLeague.get();
                }

                // Fetch complete league details from API using external ID
                League completeLeague = apiFootballService.getLeagueById(sourceLeague.getExternalId());
                apiCallCounter++;

                if (completeLeague != null) {
                    // Save country if it exists
                    if (completeLeague.getCountry() != null) {
                        completeLeague.setCountry(saveCountry(completeLeague.getCountry()));
                    }

                    // Ensure season is set
                    if (completeLeague.getSeason() == null) {
                        completeLeague.setSeason(season != null ? season : 2024);
                    }

                    return leagueRepository.saveAndFlush(completeLeague);
                }
            }

            // Case 2: Try to find by name if external ID is missing
            if (sourceLeague != null && sourceLeague.getName() != null) {
                // First check if league exists in database by name and season
                List<League> existingLeagues = leagueRepository.findByNameContainingIgnoreCase(sourceLeague.getName().trim());

                // Try to find exact match for the season
                Optional<League> seasonMatch = existingLeagues.stream()
                        .filter(l -> Objects.equals(l.getSeason(), season))
                        .findFirst();

                if (seasonMatch.isPresent()) {
                    return seasonMatch.get();
                }

                // If no season match, take the first one
                if (!existingLeagues.isEmpty()) {
                    return existingLeagues.get(0);
                }

                // Create new league with the name
                League newLeague = new League();
                newLeague.setName(sourceLeague.getName().trim());
                newLeague.setType("League");
                newLeague.setSeason(season != null ? season : 2024);

                if (sourceLeague.getCountry() != null) {
                    newLeague.setCountry(saveCountry(sourceLeague.getCountry()));
                }

                return leagueRepository.saveAndFlush(newLeague);
            }

            // Case 3: No valid league found, return null
            return null;

        } catch (Exception e) {
            logger.error("Error processing league for statistics: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Copies statistics data from source to target PlayerStatistic entity.
     */
    private void copyStatisticsData(PlayerStatistic source, PlayerStatistic target) {
        target.setAppearances(source.getAppearances());
        target.setLineups(source.getLineups());
        target.setMinutesPlayed(source.getMinutesPlayed());
        target.setPosition(source.getPosition());
        target.setRating(source.getRating());
        target.setIsCaptain(source.getIsCaptain());

        target.setGoals(source.getGoals());
        target.setAssists(source.getAssists());
        target.setGoalsConceded(source.getGoalsConceded());
        target.setSaves(source.getSaves());

        target.setShotsTotal(source.getShotsTotal());
        target.setShotsOnTarget(source.getShotsOnTarget());

        target.setPassesTotal(source.getPassesTotal());
        target.setPassesKey(source.getPassesKey());
        target.setPassAccuracy(source.getPassAccuracy());

        target.setTacklesTotal(source.getTacklesTotal());
        target.setTacklesBlocks(source.getTacklesBlocks());
        target.setInterceptions(source.getInterceptions());

        target.setDribblesAttempts(source.getDribblesAttempts());
        target.setDribblesSuccess(source.getDribblesSuccess());

        target.setFoulsDrawn(source.getFoulsDrawn());
        target.setFoulsCommitted(source.getFoulsCommitted());

        target.setYellowCards(source.getYellowCards());
        target.setRedCards(source.getRedCards());

        target.setPenaltiesWon(source.getPenaltiesWon());
        target.setPenaltiesScored(source.getPenaltiesScored());
        target.setPenaltiesMissed(source.getPenaltiesMissed());

        target.setSubstitutesIn(source.getSubstitutesIn());
        target.setSubstitutesOut(source.getSubstitutesOut());
        target.setSubstitutesBench(source.getSubstitutesBench());
    }

    /**
     * Saves player transfers to the database.
     */
    private void savePlayerTransfers(Player player, List<PlayerTransfer> transfers) {
        for (PlayerTransfer transfer : transfers) {
            try {
                transfer.setPlayer(player);

                if (transfer.getClubFrom() != null) {
                    Club processedClubFrom = processClubForStatistics(transfer.getClubFrom(), null);
                    if (processedClubFrom != null && processedClubFrom.getId() != null) {
                        transfer.setClubFrom(processedClubFrom);
                    } else {
                        logger.warn("Could not process clubFrom for transfer, setting to null");
                        transfer.setClubFrom(null);
                    }
                }

                if (transfer.getClubTo() != null) {
                    Club processedClubTo = processClubForStatistics(transfer.getClubTo(), player.getCurrentClub());
                    if (processedClubTo != null && processedClubTo.getId() != null) {
                        transfer.setClubTo(processedClubTo);
                    } else {
                        logger.warn("Could not process clubTo for transfer, using player's current club");
                        transfer.setClubTo(player.getCurrentClub());
                    }
                }

                // Validate that we have valid clubs before saving
                if (transfer.getClubFrom() != null && transfer.getClubFrom().getId() == null) {
                    logger.warn("ClubFrom is not persisted, skipping transfer for player: {}", player.getName());
                    continue;
                }

                if (transfer.getClubTo() != null && transfer.getClubTo().getId() == null) {
                    logger.warn("ClubTo is not persisted, skipping transfer for player: {}", player.getName());
                    continue;
                }

                // Check for duplicate transfers
                if (transfer.getTransferDate() != null) {
                    Optional<PlayerTransfer> existingTransfer = playerTransferRepository
                            .findByPlayerAndTransferDateAndClubFromAndClubTo(
                                    player,
                                    transfer.getTransferDate(),
                                    transfer.getClubFrom(),
                                    transfer.getClubTo()
                            );

                    if (existingTransfer.isPresent()) {
                        continue;
                    }
                }

                playerTransferRepository.save(transfer);

            } catch (Exception e) {
                logger.error("Error saving transfer for player {}: {}", player.getName(), e.getMessage(), e);
            }
        }

    }

    /**
     * Saves player injuries to the database with proper club/league linking.
     */
    private void savePlayerInjuries(Player player, List<PlayerInjury> injuries, Club fallbackClub, League fallbackLeague) {
        for (PlayerInjury injury : injuries) {
            try {
                injury.setPlayer(player);

                // Try to link club from injury data, fallback to current club
                if (injury.getClub() != null) {
                    Optional<Club> existingClub = clubRepository.findByExternalId(injury.getClub().getExternalId());
                    if (existingClub.isPresent()) {
                        injury.setClub(existingClub.get());
                    } else {
                        // Save the new club from injury data
                        Club savedClub = clubRepository.save(injury.getClub());
                        injury.setClub(savedClub);
                    }
                } else {
                    injury.setClub(fallbackClub);
                }

                // Try to link league from injury data, fallback to current league
                if (injury.getLeague() != null) {
                    Optional<League> existingLeague = leagueRepository.findByExternalId(injury.getLeague().getExternalId());
                    if (existingLeague.isPresent()) {
                        injury.setLeague(existingLeague.get());
                    } else {
                        // Save the new league from injury data
                        League savedLeague = leagueRepository.save(injury.getLeague());
                        injury.setLeague(savedLeague);
                    }
                } else {
                    injury.setLeague(fallbackLeague);
                }

                playerInjuryRepository.save(injury);
            } catch (Exception e) {
                logger.error("Error saving injury for player {}: {}", player.getName(), e.getMessage());
            }
        }
    }

    /**
     * Saves player sidelined periods to the database.
     */
    private void savePlayerSidelined(Player player, List<PlayerSidelined> sidelinedPeriods) {
        for (PlayerSidelined sidelined : sidelinedPeriods) {
            try {
                sidelined.setPlayer(player);

                playerSidelinedRepository.save(sidelined);
            } catch (Exception e) {
                logger.error("Error saving sidelined period for player {}: {}", player.getName(), e.getMessage());
            }
        }
    }

    /**
     * Saves player trophies to the database.
     */
    private void savePlayerTrophies(Player player, List<PlayerTrophy> trophies) {
        for (PlayerTrophy trophy : trophies) {
            try {
                trophy.setPlayer(player);

                playerTrophyRepository.save(trophy);
            } catch (Exception e) {
                logger.error("Error saving trophy for player {}: {}", player.getName(), e.getMessage());
            }
        }
    }

    /**
     * Saves or updates a league entity in the database.
     */
    private League saveLeague(League league) {
        try {
            Optional<League> existingLeague = leagueRepository.findByExternalId(league.getExternalId());
            if (existingLeague.isPresent()) {
                return existingLeague.get();
            }

            // Save country first if it exists
            if (league.getCountry() != null) {
                Country savedCountry = saveCountry(league.getCountry());
                league.setCountry(savedCountry);
            }

            league.setSeason(CURRENT_SEASON);
            return leagueRepository.save(league);
        } catch (Exception e) {
            logger.error("Error saving league {}: {}", league.getName(), e.getMessage());
            throw new RuntimeException("Failed to save league: " + league.getName(), e);
        }
    }

    /**
     * Enhanced country saving with API lookup fallback.
     */
    private Country saveCountry(Country country) {
        try {
            if (country == null || country.getName() == null || country.getName().trim().isEmpty()) {
                logger.warn("Invalid country data provided");
                return null;
            }

            String countryName = country.getName().trim();

            // First, try to find existing country
            Optional<Country> existingCountry = countryRepository.findByName(countryName);
            if (existingCountry.isPresent()) {
                Country found = existingCountry.get();
                if (found.getId() != null) {
                    return found;
                }
            }

            // Try to get complete country information from API
            Country completeCountry = null;
            try {
                completeCountry = apiFootballService.getCountryByName(countryName);
                apiCallCounter++;
            } catch (Exception apiEx) {
                logger.warn("Could not fetch complete country details from API for: {}, using provided data", countryName);
            }

            // Create new country with either API data or provided data
            Country newCountry = new Country();
            newCountry.setName(countryName);

            // Use API data if available, otherwise use provided data
            if (completeCountry != null) {
                if (completeCountry.getCode() != null && !completeCountry.getCode().trim().isEmpty()) {
                    String code = completeCountry.getCode().trim();
                    if (code.length() > 10) {
                        code = code.substring(0, 10);
                        logger.warn("Truncated country code for {}: {} -> {}", countryName, completeCountry.getCode(), code);
                    }
                    newCountry.setCode(code);
                }

                if (completeCountry.getFlagUrl() != null && !completeCountry.getFlagUrl().trim().isEmpty()) {
                    newCountry.setFlagUrl(completeCountry.getFlagUrl().trim());
                }
            } else {
                // Fallback to provided data
                if (country.getCode() != null && !country.getCode().trim().isEmpty()) {
                    String code = country.getCode().trim();
                    if (code.length() > 10) {
                        code = code.substring(0, 10);
                        logger.warn("Truncated country code for {}: {} -> {}", countryName, country.getCode(), code);
                    }
                    newCountry.setCode(code);
                }

                if (country.getFlagUrl() != null && !country.getFlagUrl().trim().isEmpty()) {
                    newCountry.setFlagUrl(country.getFlagUrl().trim());
                }
            }

            Country savedCountry = countryRepository.save(newCountry);
            return savedCountry;

        } catch (Exception e) {
            logger.error("Error saving country {}: {}",
                    country != null ? country.getName() : "null", e.getMessage(), e);
            return null;

        }
    }

    /**
     * Notifies the scheduled service about the completion status.
     */
    private void notifyScheduledService(boolean success, String message) {
        try {
            ScheduledTaskService scheduledService = applicationContext.getBean(ScheduledTaskService.class
            );
            scheduledService.onPopulationComplete(success, message);
        } catch (BeansException | IllegalStateException e) {
            logger.warn("Could not notify scheduled service: {}", e.getMessage());
        }
    }

    /**
     * Triggers the data population process manually.
     */
    public void scheduleDataPopulation() {
        logger.info("Scheduling U21 player data population...");
        populateU21PlayersAsync();
    }

    /**
     * Returns the current count of U21 eligible players in the database.
     */
    public long getU21PlayerCount() {
        try {
            return playerRepository.countByU21Eligible();
        } catch (Exception e) {
            logger.error("Error getting U21 player count: {}", e.getMessage());
            return 0;
        }
    }
}

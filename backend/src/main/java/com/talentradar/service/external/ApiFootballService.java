package com.talentradar.service.external;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Service for interacting with the API-Football external API. Handles player,
 * team, league, and statistics data retrieval with rate limiting.
 */
@Service("externalApiFootballService")
public class ApiFootballService {

    private static final Logger logger = LoggerFactory.getLogger(ApiFootballService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3; // Maximum retry attempts
    private static final int BASE_RETRY_DELAY = 3000; // Base delay for retries (3 seconds)

    @Value("${api-football.api.key}")
    private String apiKey;

    @Value("${api-football.api.base-url}")
    private String baseUrl;

    @Value("${api-football.api.host}")
    private String apiHost;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private long lastRequestTime = 0;

    public ApiFootballService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Implements rate limiting to comply with API usage restrictions.
     */
    private void rateLimitDelay(int attemptNumber) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        // Calculate delay based on attempt number (exponential backoff for retries)
        long requiredDelay = 150; // Normal rate limit

        if (attemptNumber > 1) {
            // Exponential backoff for retries: 2s, 4s, 8s, etc.
            requiredDelay = BASE_RETRY_DELAY * (long) Math.pow(2, attemptNumber - 2);
            logger.info("Retry attempt {}: using exponential backoff delay of {} ms",
                    attemptNumber, requiredDelay);
        }

        if (timeSinceLastRequest < requiredDelay) {
            long waitTime = requiredDelay - timeSinceLastRequest;
            performRateLimitDelay(waitTime);
        }
        lastRequestTime = System.currentTimeMillis();
    }

    /**
     * Performs rate limiting delay with proper interruption handling.
     */
    private void performRateLimitDelay(long delayMs) {
        try {
            java.util.concurrent.TimeUnit.MILLISECONDS.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Rate limiting interrupted (likely application shutdown), continuing without delay");
        }
    }

    /**
     * Creates HTTP headers required for API-Football requests.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", apiKey);
        headers.set("x-rapidapi-host", apiHost);
        return headers;
    }

    /**
     * Checks if the error indicates daily limit has been reached.
     */
    private boolean isDailyLimitError(JsonNode errors) {
        for (JsonNode error : errors) {
            String errorMessage = error.asText().toLowerCase();
            if (errorMessage.contains("request limit for the day")
                    || errorMessage.contains("daily limit")
                    || errorMessage.contains("reached the request limit")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes a REST API call to the specified endpoint with error handling and
     * retry logic.
     */
    private JsonNode makeApiCall(String endpoint) {
        return makeApiCallWithRetry(endpoint, 1);
    }

    /**
     * Makes a REST API call with retry logic and exponential backoff.
     */
    private JsonNode makeApiCallWithRetry(String endpoint, int attemptNumber) {
        rateLimitDelay(attemptNumber);

        try {
            String url = baseUrl + endpoint;
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // Check if response is successful
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new HttpClientErrorException(response.getStatusCode(),
                        "API returned non-success status: " + response.getStatusCode());
            }

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            // Check for API-specific errors in response
            JsonNode errors = jsonResponse.get("errors");
            if (errors != null && errors.size() > 0) {
                String errorMessage = "API returned errors: " + errors;
                logger.error(errorMessage);

                // Check if this is a daily limit error
                if (isDailyLimitError(errors)) {
                    throw new DailyLimitExceededException("Daily API limit reached: " + errors);
                }

                // Check if this is a rate limit error that we should retry
                if (isRetryableError(errors) && attemptNumber < MAX_RETRY_ATTEMPTS) {
                    logger.warn("Retryable error detected, attempting retry {} of {}",
                            attemptNumber + 1, MAX_RETRY_ATTEMPTS);
                    return makeApiCallWithRetry(endpoint, attemptNumber + 1);
                }

                return null;
            }

            // Success - reset any retry state
            if (attemptNumber > 1) {
                logger.info("API call succeeded on attempt {}", attemptNumber);
            }

            return jsonResponse;

        } catch (DailyLimitExceededException e) {
            // Re-throw daily limit exceptions to be handled by the caller
            throw e;

        } catch (IOException | HttpServerErrorException e) {
            String errorType = e instanceof IOException ? "IOException" : "HTTP server error";
            logger.error("{} during API call to {} (attempt {}): {}",
                    errorType, endpoint, attemptNumber, e.getMessage());

            if (attemptNumber < MAX_RETRY_ATTEMPTS) {
                logger.warn("{} detected, attempting retry {} of {}",
                        errorType, attemptNumber + 1, MAX_RETRY_ATTEMPTS);
                return makeApiCallWithRetry(endpoint, attemptNumber + 1);
            }

            logger.error("Max retry attempts ({}) reached for {} on endpoint: {}",
                    MAX_RETRY_ATTEMPTS, errorType.toLowerCase(), endpoint);
            return null;

        } catch (HttpClientErrorException e) {
            logger.error("HTTP client error during API call to {} (attempt {}): {} - {}",
                    endpoint, attemptNumber, e.getStatusCode(), e.getMessage());

            // Retry on specific HTTP errors (rate limit, temporary server issues)
            if (isRetryableHttpError(e) && attemptNumber < MAX_RETRY_ATTEMPTS) {
                logger.warn("Retryable HTTP error detected, attempting retry {} of {}",
                        attemptNumber + 1, MAX_RETRY_ATTEMPTS);
                return makeApiCallWithRetry(endpoint, attemptNumber + 1);
            }

            logger.error("Non-retryable HTTP error or max attempts reached for endpoint: {}", endpoint);
            return null;

        } catch (RuntimeException e) {
            logger.error("Unexpected error during API call to {} (attempt {}): {}",
                    endpoint, attemptNumber, e.getMessage());

            // Retry on unexpected errors
            if (attemptNumber < MAX_RETRY_ATTEMPTS) {
                logger.warn("Unexpected error, attempting retry {} of {}",
                        attemptNumber + 1, MAX_RETRY_ATTEMPTS);
                return makeApiCallWithRetry(endpoint, attemptNumber + 1);
            }

            logger.error("Max retry attempts ({}) reached for unexpected error on endpoint: {}",
                    MAX_RETRY_ATTEMPTS, endpoint);
            return null;
        }

    }

    /**
     * Determines if an API error response indicates a retryable condition.
     */
    private boolean isRetryableError(JsonNode errors) {
        for (JsonNode error : errors) {
            String errorMessage = error.asText().toLowerCase();

            // Check for daily limit reached - this should NOT be retried immediately
            if (errorMessage.contains("request limit for the day")
                    || errorMessage.contains("daily limit")
                    || errorMessage.contains("reached the request limit")) {
                return false; // Don't retry daily limits
            }

            // Check for rate limiting or temporary API issues
            if (errorMessage.contains("rate limit")
                    || errorMessage.contains("too many requests")
                    || errorMessage.contains("quota exceeded")
                    || errorMessage.contains("temporarily unavailable")
                    || errorMessage.contains("service unavailable")
                    || errorMessage.contains("timeout")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if an HTTP error is retryable.
     */
    private boolean isRetryableHttpError(HttpClientErrorException e) {
        int statusCode = e.getStatusCode().value();

        // Retry on:
        // 429 - Too Many Requests (rate limiting)
        // 408 - Request Timeout
        // 502 - Bad Gateway
        // 503 - Service Unavailable  
        // 504 - Gateway Timeout
        return statusCode == 429 || statusCode == 408
                || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    /**
     * Retrieves top leagues that likely contain U21 players.
     */
    public List<League> getTopLeagues() {
        List<League> leagues = new ArrayList<>();

        // Get major leagues that likely have U21 players
        // Only leagues here that need to be populated, otherwise leave blank!
        int[] leagueIds = {1128};

        for (int leagueId : leagueIds) {
            try {
                JsonNode response = makeApiCall("/leagues?id=" + leagueId);
                if (response != null) {
                    JsonNode responseArray = response.get("response");
                    if (responseArray != null && responseArray.size() > 0) {
                        JsonNode leagueData = responseArray.get(0);
                        League league = parseLeague(leagueData);
                        if (league != null) {
                            leagues.add(league);
                        }
                    }
                }
            } catch (DailyLimitExceededException e) {
                // Re-throw daily limit exceptions to be handled by DataPopulationService
                logger.warn("Daily API limit reached while retrieving league {}: {}", leagueId, e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("Error retrieving league with ID {}: {}", leagueId, e.getMessage());
            }
        }

        return leagues;
    }

    /**
     * Retrieves all U21 eligible players from a specific league for a given
     * season using pagination. This ensures ALL players are retrieved.
     */
    public List<Integer> getPlayerIdsFromLeague(int leagueId, int season) {
        List<Integer> playerIds = new ArrayList<>();
        int currentPage = 1;
        int totalPages = 1;

        try {
            do {
                String endpoint = "/players?league=" + leagueId + "&season=" + season + "&page=" + currentPage;
                JsonNode response = makeApiCall(endpoint);

                if (response != null) {
                    // Update total pages from first response
                    if (currentPage == 1) {
                        JsonNode paging = response.get("paging");
                        if (paging != null && paging.has("total")) {
                            totalPages = paging.get("total").asInt();
                        }
                    }

                    JsonNode responseArray = response.get("response");
                    if (responseArray != null) {
                        for (JsonNode playerData : responseArray) {
                            JsonNode player = playerData.get("player");
                            if (player != null) {
                                // Check if player is U21 eligible based on birth date
                                JsonNode birth = player.get("birth");
                                if (birth != null && birth.has("date") && !birth.get("date").isNull()) {
                                    try {
                                        LocalDate birthDate = LocalDate.parse(birth.get("date").asText(), DateTimeFormatter.ISO_LOCAL_DATE);
                                        int age = java.time.Period.between(birthDate, LocalDate.now()).getYears();
                                        if (age <= 21) {
                                            playerIds.add(player.get("id").asInt());
                                        }
                                    } catch (DateTimeParseException e) {
                                        logger.warn("Could not parse birth date for player {}", player.get("id").asInt());
                                    }
                                }
                            }
                        }
                    }
                }

                currentPage++;

                // Add delay between pagination requests
                if (currentPage <= totalPages) {
                    performRateLimitDelay(500); // 500ms delay between pages
                }

            } while (currentPage <= totalPages);

        } catch (DailyLimitExceededException e) {
            logger.warn("Daily API limit reached while retrieving player IDs from league {} for season {}: {}", leagueId, season, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving player IDs from league {} for season {}: {}", leagueId, season, e.getMessage());
        }

        return playerIds;
    }

    /**
     * Retrieves all teams from a specific league for a given season.
     */
    public List<Club> getTeamsFromLeague(int leagueId, int season) {
        List<Club> clubs = new ArrayList<>();

        try {
            JsonNode response = makeApiCall("/teams?league=" + leagueId + "&season=" + season);
            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null) {
                    for (JsonNode teamData : responseArray) {
                        Club club = parseTeam(teamData);
                        if (club != null) {
                            clubs.add(club);
                        }
                    }
                }
            }
        } catch (DailyLimitExceededException e) {
            // Re-throw daily limit exceptions to be handled by DataPopulationService
            logger.warn("Daily API limit reached while retrieving teams from league {} for season {}: {}", leagueId, season, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving teams from league {} for season {}: {}", leagueId, season, e.getMessage());
        }

        return clubs;
    }

    /**
     * Retrieves club information by ID from the API.
     */
    public Club getClubById(int clubId) {
        try {
            JsonNode response = makeApiCall("/teams?id=" + clubId);

            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null && responseArray.size() > 0) {
                    return parseTeam(responseArray.get(0));
                }
            }

            logger.debug("No club found with ID: {}", clubId);
            return null;

        } catch (DailyLimitExceededException e) {
            // Re-throw daily limit exceptions to be handled by DataPopulationService
            logger.warn("Daily API limit reached while retrieving club {}: {}", clubId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving club by ID '{}': {}", clubId, e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves league information by ID from the API.
     */
    public League getLeagueById(int leagueId) {
        try {
            JsonNode response = makeApiCall("/leagues?id=" + leagueId);

            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null && responseArray.size() > 0) {
                    return parseLeague(responseArray.get(0));
                }
            }

            logger.debug("No league found with ID: {}", leagueId);
            return null;

        } catch (Exception e) {
            logger.error("Error retrieving league by ID '{}': {}", leagueId, e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves country information by name from the API.
     */
    public Country getCountryByName(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return null;
            }

            String encodedName = java.net.URLEncoder.encode(name.trim(), "UTF-8");
            JsonNode response = makeApiCall("/countries?name=" + encodedName);

            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null && responseArray.size() > 0) {
                    return parseCountry(responseArray.get(0));
                }
            }

            logger.debug("No country found with name: {}", name);
            return null;

        } catch (UnsupportedEncodingException e) {
            logger.error("Error encoding country name '{}': {}", name, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Error retrieving country by name '{}': {}", name, e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves all seasons that a player has statistics for using the
     * dedicated seasons endpoint.
     */
    public List<Integer> getPlayerSeasons(int playerId) {
        List<Integer> seasons = new ArrayList<>();

        try {
            JsonNode response = makeApiCall("/players/seasons?player=" + playerId);
            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null) {
                    for (JsonNode seasonNode : responseArray) {
                        if (!seasonNode.isNull()) {
                            seasons.add(seasonNode.asInt());
                        }
                    }
                }
            }

            // Sort seasons in descending order (most recent first)
            seasons.sort((a, b) -> b.compareTo(a));

        } catch (DailyLimitExceededException e) {
            // Re-throw daily limit exceptions to be handled by DataPopulationService
            logger.warn("Daily API limit reached while retrieving seasons for player {}: {}", playerId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving seasons for player {}: {}", playerId, e.getMessage());

            // Fallback to common recent seasons if API call fails
            logger.warn("Falling back to default seasons for player {}", playerId);
            seasons = List.of(2024, 2023, 2022, 2021, 2020);
        }

        return seasons;
    }

    /**
     * Enhanced method to get comprehensive player data for ALL seasons they
     * played
     */
    public Player getPlayerDetailsAllSeasons(int playerId) {
        try {
            // Get all seasons this player has data for
            List<Integer> playerSeasons = getPlayerSeasons(playerId);

            if (playerSeasons.isEmpty()) {
                logger.warn("No seasons found for player ID: {}, using fallback seasons", playerId);
                playerSeasons = List.of(2024, 2023, 2022, 2021, 2020);
            }

            // Get player's basic info from the most recent season
            Player player = null;
            for (Integer season : playerSeasons) {
                player = getPlayerDetails(playerId, season);
                if (player != null) {
                    break;
                }
            }

            if (player == null) {
                logger.warn("No player data found for player ID: {} in any season", playerId);
                return null;
            }

            // Clear existing statistics to rebuild with all seasons
            player.getStatistics().clear();

            // Get comprehensive data for each season
            for (Integer season : playerSeasons) {
                try {
                    // Get statistics for this season
                    List<PlayerStatistic> seasonStats = getPlayerStatisticsBySeason(playerId, season);
                    for (PlayerStatistic stat : seasonStats) {
                        stat.setPlayer(player);
                        player.getStatistics().add(stat);
                    }

                } catch (Exception e) {
                    logger.warn("Could not get statistics for player {} in season {}: {}",
                            playerId, season, e.getMessage());
                }
            }

            logger.info("Retrieved comprehensive data for player {} across {} seasons",
                    player.getName(), playerSeasons.size());

            return player;

        } catch (DailyLimitExceededException e) {
            logger.warn("Daily API limit reached while retrieving comprehensive player details for {}: {}", playerId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving comprehensive player details for {}: {}", playerId, e.getMessage());
            return null;
        }
    }

    /**
     * Gets player statistics for a specific season with all club records.
     */
    public List<PlayerStatistic> getPlayerStatisticsBySeason(int playerId, int season) {
        List<PlayerStatistic> seasonStats = new ArrayList<>();

        try {
            JsonNode response = makeApiCall("/players?id=" + playerId + "&season=" + season);
            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null && responseArray.size() > 0) {
                    JsonNode playerData = responseArray.get(0);
                    JsonNode statisticsArray = playerData.get("statistics");

                    if (statisticsArray != null) {
                        // Process each statistics record (different clubs/leagues in same season)
                        for (JsonNode statsRecord : statisticsArray) {
                            PlayerStatistic stats = parsePlayerStatistics(statsRecord, null, season);
                            if (stats != null) {
                                seasonStats.add(stats);
                            }
                        }
                    }
                }
            }
        } catch (DailyLimitExceededException e) {
            // Re-throw daily limit exceptions to be handled by DataPopulationService
            logger.warn("Daily API limit reached while retrieving statistics for player {} in season {}: {}", playerId, season, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving statistics for player {} in season {}: {}",
                    playerId, season, e.getMessage());
        }

        return seasonStats;
    }

    /**
     * Retrieves comprehensive injury history for a player across all their
     * active seasons.
     */
    public List<PlayerInjury> getAllPlayerInjuries(int playerId) {
        List<PlayerInjury> allInjuries = new ArrayList<>();

        // Get player's actual seasons from the API
        List<Integer> playerSeasons = getPlayerSeasons(playerId);

        // If no seasons found via API, fallback to recent seasons
        if (playerSeasons.isEmpty()) {
            logger.warn("No seasons found for player {}, using fallback seasons", playerId);
            playerSeasons = List.of(2024, 2023, 2022, 2021, 2020);
        }

        for (int season : playerSeasons) {
            try {
                List<PlayerInjury> seasonInjuries = getPlayerInjuries(playerId, season);
                allInjuries.addAll(seasonInjuries);

            } catch (DailyLimitExceededException e) {
                // Re-throw daily limit exceptions to be handled by DataPopulationService
                logger.warn("Daily API limit reached while retrieving injuries for player {} in season {}: {}", playerId, season, e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.warn("Could not retrieve injuries for player {} in season {}: {}",
                        playerId, season, e.getMessage());
            }
        }

        return allInjuries;
    }

    /**
     * Retrieves sidelined periods for a player (API only supports player
     * parameter alone).
     */
    public List<PlayerSidelined> getAllPlayerSidelined(int playerId) {
        List<PlayerSidelined> allSidelined = new ArrayList<>();

        try {
            // The sidelined endpoint only accepts player parameter alone
            JsonNode response = makeApiCall("/sidelined?player=" + playerId);

            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null && responseArray.isArray()) {
                    for (JsonNode sidelinedData : responseArray) {
                        PlayerSidelined sidelined = parseSidelined(sidelinedData);
                        if (sidelined != null) {
                            allSidelined.add(sidelined);
                        }
                    }
                }
            }

        } catch (DailyLimitExceededException e) {
            // Re-throw daily limit exceptions to be handled by DataPopulationService
            logger.warn("Daily API limit reached while retrieving sidelined periods for player {}: {}", playerId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving sidelined periods for player {}: {}", playerId, e.getMessage());
        }

        return allSidelined;
    }

    /**
     * Retrieves detailed player information for a specific player and season.
     */
    public Player getPlayerDetails(int playerId, int season) {
        try {
            JsonNode response = makeApiCall("/players?id=" + playerId + "&season=" + season);
            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null && responseArray.size() > 0) {
                    return parseDetailedPlayer(responseArray.get(0));
                }
            }
        } catch (DailyLimitExceededException e) {
            // Re-throw daily limit exceptions to be handled by DataPopulationService
            logger.warn("Daily API limit reached while retrieving player details for player {}: {}", playerId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving player details for player {}: {}", playerId, e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves transfer history for a specific player.
     */
    public List<PlayerTransfer> getPlayerTransfers(int playerId) {
        List<PlayerTransfer> transfers = new ArrayList<>();

        try {
            JsonNode response = makeApiCall("/transfers?player=" + playerId);
            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null) {
                    for (JsonNode transferData : responseArray) {
                        JsonNode transfersArray = transferData.get("transfers");
                        if (transfersArray != null) {
                            for (JsonNode transfer : transfersArray) {
                                PlayerTransfer playerTransfer = parseTransfer(transfer);
                                if (playerTransfer != null) {
                                    transfers.add(playerTransfer);
                                }
                            }
                        }
                    }
                }
            }
        } catch (DailyLimitExceededException e) {
            // Re-throw daily limit exceptions to be handled by DataPopulationService
            logger.warn("Daily API limit reached while retrieving transfers for player {}: {}", playerId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving transfers for player {}: {}", playerId, e.getMessage());
        }

        return transfers;
    }

    /**
     * Retrieves injury history for a specific player.
     */
    public List<PlayerInjury> getPlayerInjuries(int playerId, int season) {
        List<PlayerInjury> injuries = new ArrayList<>();

        try {
            JsonNode response = makeApiCall("/injuries?player=" + playerId + "&season=" + season);
            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null && responseArray.size() > 0) {
                    for (JsonNode injuryData : responseArray) {
                        PlayerInjury injury = parseInjury(injuryData);
                        if (injury != null) {
                            injuries.add(injury);
                        }
                    }
                }
            }
        } catch (DailyLimitExceededException e) {
            // Re-throw daily limit exceptions to be handled by DataPopulationService
            logger.warn("Daily API limit reached while retrieving injuries for player {}: {}", playerId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving injuries for player {}: {}", playerId, e.getMessage());
        }

        return injuries;
    }

    /**
     * Retrieves trophies for a specific player.
     */
    public List<PlayerTrophy> getPlayerTrophies(int playerId) {
        List<PlayerTrophy> trophies = new ArrayList<>();

        try {
            JsonNode response = makeApiCall("/trophies?player=" + playerId);
            if (response != null) {
                JsonNode responseArray = response.get("response");
                if (responseArray != null) {
                    for (JsonNode trophyData : responseArray) {
                        PlayerTrophy trophy = parseTrophy(trophyData);
                        if (trophy != null) {
                            trophies.add(trophy);
                        }
                    }
                }
            }
        } catch (DailyLimitExceededException e) {
            // Re-throw daily limit exceptions to be handled by DataPopulationService
            logger.warn("Daily API limit reached while retrieving trophies for player {}: {}", playerId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving trophies for player {}: {}", playerId, e.getMessage());
        }

        return trophies;
    }

    /**
     * Parses detailed team data including stadium information.
     */
    private Club parseTeam(JsonNode teamData) {
        try {
            JsonNode team = teamData.get("team");
            JsonNode venue = teamData.get("venue");

            if (team == null) {
                logger.warn("Team information is null in API response");
                return null;
            }

            Club club = new Club();
            club.setExternalId(team.get("id").asInt());
            club.setName(team.get("name").asText());

            if (team.has("code") && !team.get("code").isNull()) {
                club.setShortName(team.get("code").asText());
            }

            if (team.has("logo") && !team.get("logo").isNull()) {
                club.setLogoUrl(team.get("logo").asText());
            }

            if (team.has("national") && !team.get("national").isNull()) {
                club.setIsNational(team.get("national").asBoolean());
            }

            if (team.has("country") && !team.get("country").isNull()) {
                Country country = new Country();
                country.setName(team.get("country").asText());
                club.setCountry(country);
            }

            if (team.has("founded") && !team.get("founded").isNull()) {
                club.setFounded(team.get("founded").asInt());
            }

            // Parse venue information
            if (venue != null) {
                if (venue.has("name") && !venue.get("name").isNull()) {
                    club.setStadium(venue.get("name").asText());
                }

                if (venue.has("capacity") && !venue.get("capacity").isNull()) {
                    club.setStadiumCapacity(venue.get("capacity").asInt());
                }

                if (venue.has("city") && !venue.get("city").isNull()) {
                    club.setCity(venue.get("city").asText());
                }
            }

            return club;
        } catch (Exception e) {
            logger.error("Error parsing detailed team data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses detailed player data with all available information.
     */
    private Player parseDetailedPlayer(JsonNode playerData) {
        try {
            JsonNode player = playerData.get("player");

            if (player == null) {
                logger.warn("Player information is null in API response");
                return null;
            }

            Player playerEntity = new Player();
            playerEntity.setExternalId(player.get("id").asInt());
            playerEntity.setName(player.get("name").asText());

            if (player.has("firstname") && !player.get("firstname").isNull()) {
                playerEntity.setFirstName(player.get("firstname").asText());
            }

            if (player.has("lastname") && !player.get("lastname").isNull()) {
                playerEntity.setLastName(player.get("lastname").asText());
            }

            if (player.has("nationality") && !player.get("nationality").isNull()) {
                playerEntity.setNationality(player.get("nationality").asText());
            }

            if (player.has("photo") && !player.get("photo").isNull()) {
                playerEntity.setPhotoUrl(player.get("photo").asText());
            }

            if (player.has("injured") && !player.get("injured").isNull()) {
                playerEntity.setIsInjured(player.get("injured").asBoolean());
            }

            // Parse birth information
            JsonNode birth = player.get("birth");
            if (birth != null) {
                if (birth.has("date") && !birth.get("date").isNull()) {
                    String birthDate = birth.get("date").asText();
                    try {
                        playerEntity.setDateOfBirth(LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE));
                    } catch (DateTimeParseException e) {
                        logger.warn("Could not parse birth date '{}' for player: {}", birthDate, e.getMessage());
                    }
                }

                if (birth.has("place") && !birth.get("place").isNull()) {
                    playerEntity.setBirthPlace(birth.get("place").asText());
                }

                if (birth.has("country") && !birth.get("country").isNull()) {
                    playerEntity.setBirthCountry(birth.get("country").asText());
                }
            }

            // Parse physical attributes
            if (player.has("height") && !player.get("height").isNull()) {
                String height = player.get("height").asText();
                if (height.contains("cm")) {
                    try {
                        int heightCm = Integer.parseInt(height.replace(" cm", "").trim());
                        playerEntity.setHeightCm(heightCm);
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse height '{}': {}", height, e.getMessage());
                    }
                }
            }

            if (player.has("weight") && !player.get("weight").isNull()) {
                String weight = player.get("weight").asText();
                if (weight.contains("kg")) {
                    try {
                        int weightKg = Integer.parseInt(weight.replace(" kg", "").trim());
                        playerEntity.setWeightKg(weightKg);
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse weight '{}': {}", weight, e.getMessage());
                    }
                }
            }

            // Parse statistics for position and other details
            JsonNode statisticsArray = playerData.get("statistics");
            if (statisticsArray != null && statisticsArray.size() > 0) {
                for (JsonNode statsRecord : statisticsArray) {
                    PlayerStatistic stats = parsePlayerStatistics(statsRecord, playerEntity, 2024);
                    if (stats != null) {
                        playerEntity.getStatistics().add(stats);
                    }
                }

                // Set position from first available statistics record
                JsonNode firstStats = statisticsArray.get(0);
                JsonNode games = firstStats.get("games");
                if (games != null && games.has("position") && !games.get("position").isNull()) {
                    playerEntity.setPosition(games.get("position").asText());
                }
                if (games != null && games.has("number") && !games.get("number").isNull()) {
                    playerEntity.setJerseyNumber(games.get("number").asInt());
                }
            }

            return playerEntity;
        } catch (Exception e) {
            logger.error("Error parsing detailed player data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses transfer data from API response.
     */
    private PlayerTransfer parseTransfer(JsonNode transferData) {
        try {
            PlayerTransfer transfer = new PlayerTransfer();

            // Parse transfer date
            if (transferData.has("date") && !transferData.get("date").isNull()) {
                String transferDate = transferData.get("date").asText();
                try {
                    transfer.setTransferDate(LocalDate.parse(transferDate, DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (DateTimeParseException e) {
                    logger.warn("Could not parse transfer date: {}", transferDate);
                }
            }

            // Parse transfer type
            if (transferData.has("type") && !transferData.get("type").isNull()) {
                transfer.setTransferType(transferData.get("type").asText());
            }

            // Parse teams (from/to clubs)
            JsonNode teams = transferData.get("teams");
            if (teams != null) {
                // Parse destination club (team the player transferred TO)
                JsonNode teamIn = teams.get("in");
                if (teamIn != null && teamIn.has("id") && !teamIn.get("id").isNull()) {
                    Club clubTo = new Club();
                    clubTo.setExternalId(teamIn.get("id").asInt());
                    clubTo.setName(teamIn.get("name").asText());
                    if (teamIn.has("logo") && !teamIn.get("logo").isNull()) {
                        clubTo.setLogoUrl(teamIn.get("logo").asText());
                    }
                    transfer.setClubTo(clubTo);
                }

                // Parse source club (team the player transferred FROM)
                JsonNode teamOut = teams.get("out");
                if (teamOut != null && teamOut.has("id") && !teamOut.get("id").isNull()) {
                    Club clubFrom = new Club();
                    clubFrom.setExternalId(teamOut.get("id").asInt());
                    clubFrom.setName(teamOut.get("name").asText());
                    if (teamOut.has("logo") && !teamOut.get("logo").isNull()) {
                        clubFrom.setLogoUrl(teamOut.get("logo").asText());
                    }
                    transfer.setClubFrom(clubFrom);
                }
            }

            return transfer;
        } catch (Exception e) {
            logger.error("Error parsing transfer data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses injury data from API response.
     */
    private PlayerInjury parseInjury(JsonNode injuryData) {
        try {
            PlayerInjury injury = new PlayerInjury();

            // Parse injury type from player object
            JsonNode player = injuryData.get("player");
            if (player != null) {
                if (player.has("type") && !player.get("type").isNull()) {
                    injury.setInjuryType(player.get("type").asText());
                }

                if (player.has("reason") && !player.get("reason").isNull()) {
                    injury.setReason(player.get("reason").asText());
                }
            }

            // Parse team information for club linking
            JsonNode team = injuryData.get("team");
            if (team != null && team.has("id") && !team.get("id").isNull()) {
                Club club = new Club();
                club.setExternalId(team.get("id").asInt());
                club.setName(team.get("name").asText());
                if (team.has("logo") && !team.get("logo").isNull()) {
                    club.setLogoUrl(team.get("logo").asText());
                }
                injury.setClub(club);
            }

            // Parse league information
            JsonNode league = injuryData.get("league");
            if (league != null && league.has("id") && !league.get("id").isNull()) {
                League leagueEntity = new League();
                leagueEntity.setExternalId(league.get("id").asInt());
                leagueEntity.setName(league.get("name").asText());
                if (league.has("logo") && !league.get("logo").isNull()) {
                    leagueEntity.setLogoUrl(league.get("logo").asText());
                }
                injury.setLeague(leagueEntity);
            }

            // Parse fixture information
            JsonNode fixture = injuryData.get("fixture");
            if (fixture != null && fixture.has("id") && !fixture.get("id").isNull()) {
                injury.setFixtureId(fixture.get("id").asInt());
            }

            // Parse fixture date as start date
            if (fixture != null && fixture.has("date") && !fixture.get("date").isNull()) {
                String fixtureDate = fixture.get("date").asText();
                try {
                    // Parse ISO date format: "2021-04-07T19:00:00+00:00"
                    injury.setStartDate(LocalDate.parse(fixtureDate.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (DateTimeParseException e) {
                    logger.warn("Could not parse fixture date: {}", fixtureDate);
                }
            }

            return injury;
        } catch (Exception e) {
            logger.error("Error parsing injury data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses sidelined data from API response.
     */
    private PlayerSidelined parseSidelined(JsonNode sidelinedData) {
        try {
            PlayerSidelined sidelined = new PlayerSidelined();

            // Parse sidelined type
            if (sidelinedData.has("type") && !sidelinedData.get("type").isNull()) {
                sidelined.setType(sidelinedData.get("type").asText());
            }

            // Parse start date
            if (sidelinedData.has("start") && !sidelinedData.get("start").isNull()) {
                String startDate = sidelinedData.get("start").asText();
                try {
                    sidelined.setStartDate(LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (DateTimeParseException e) {
                    logger.warn("Could not parse sidelined start date: {}", startDate);
                }
            }

            // Parse end date
            if (sidelinedData.has("end") && !sidelinedData.get("end").isNull()) {
                String endDate = sidelinedData.get("end").asText();
                try {
                    sidelined.setEndDate(LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE));
                } catch (DateTimeParseException e) {
                    logger.warn("Could not parse sidelined end date: {}", endDate);
                }
            }

            return sidelined;
        } catch (Exception e) {
            logger.error("Error parsing sidelined data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses trophy data from API response.
     */
    private PlayerTrophy parseTrophy(JsonNode trophyData) {
        try {
            PlayerTrophy trophy = new PlayerTrophy();

            if (trophyData.has("league") && !trophyData.get("league").isNull()) {
                trophy.setLeagueName(trophyData.get("league").asText());
            }

            if (trophyData.has("country") && !trophyData.get("country").isNull()) {
                trophy.setCountry(trophyData.get("country").asText());
            }

            if (trophyData.has("season") && !trophyData.get("season").isNull()) {
                trophy.setSeason(trophyData.get("season").asText());
            }
            if (trophyData.has("place") && !trophyData.get("place").isNull()) {
                trophy.setPlace(trophyData.get("place").asText());
            }

            return trophy;
        } catch (Exception e) {
            logger.error("Error parsing trophy data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses league data from API response into League entity.
     */
    private League parseLeague(JsonNode leagueData) {
        try {
            JsonNode leagueInfo = leagueData.get("league");
            JsonNode countryInfo = leagueData.get("country");

            if (leagueInfo == null) {
                logger.warn("League information is null in API response");
                return null;
            }

            League league = new League();
            league.setExternalId(leagueInfo.get("id").asInt());
            league.setName(leagueInfo.get("name").asText());

            if (leagueInfo.has("logo") && !leagueInfo.get("logo").isNull()) {
                league.setLogoUrl(leagueInfo.get("logo").asText());
            }

            if (leagueInfo.has("type") && !leagueInfo.get("type").isNull()) {
                league.setType(leagueInfo.get("type").asText());
            }

            // Create country if provided
            if (countryInfo != null) {
                Country country = new Country();
                country.setName(countryInfo.get("name").asText());

                if (countryInfo.has("code") && !countryInfo.get("code").isNull()) {
                    String countryCode = countryInfo.get("code").asText();
                    if (countryCode.length() > 10) {
                        countryCode = countryCode.substring(0, 10);
                        logger.warn("Country code truncated for {}: {}", country.getName(), countryCode);
                    }
                    country.setCode(countryCode);
                }

                if (countryInfo.has("flag") && !countryInfo.get("flag").isNull()) {
                    country.setFlagUrl(countryInfo.get("flag").asText());
                }

                league.setCountry(country);
            }

            return league;
        } catch (Exception e) {
            logger.error("Error parsing league data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses country data from API response into Country entity.
     */
    private Country parseCountry(JsonNode countryData) {
        try {
            if (countryData == null) {
                logger.warn("Country data is null in API response");
                return null;
            }

            Country country = new Country();

            if (countryData.has("name") && !countryData.get("name").isNull()) {
                country.setName(countryData.get("name").asText());
            }

            if (countryData.has("code") && !countryData.get("code").isNull()) {
                String countryCode = countryData.get("code").asText();
                // Handle long country codes by truncating if necessary
                if (countryCode.length() > 10) {
                    countryCode = countryCode.substring(0, 10);
                    logger.warn("Country code truncated for {}: {}", country.getName(), countryCode);
                }
                country.setCode(countryCode);
            }

            if (countryData.has("flag") && !countryData.get("flag").isNull()) {
                country.setFlagUrl(countryData.get("flag").asText());
            }

            return country;

        } catch (Exception e) {
            logger.error("Error parsing country data: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses player statistics from API response into PlayerStatistic entity.
     */
    private PlayerStatistic parsePlayerStatistics(JsonNode statsData, Player player, int season) {
        try {
            if (statsData == null) {
                logger.warn("Statistics data is null for player");
                return null;
            }

            PlayerStatistic stats = new PlayerStatistic();
            stats.setSeason(season);

            // Parse team information for club linking
            JsonNode team = statsData.get("team");
            if (team != null && team.has("id") && !team.get("id").isNull()) {
                Club club = new Club();
                club.setExternalId(team.get("id").asInt());
                club.setName(team.get("name").asText());

                if (team.has("logo") && !team.get("logo").isNull()) {
                    club.setLogoUrl(team.get("logo").asText());
                }

                club.setIsActive(true);
                stats.setClub(club);
            }

            // Parse league information
            JsonNode league = statsData.get("league");
            if (league != null && league.has("id") && !league.get("id").isNull()) {
                League leagueEntity = new League();
                leagueEntity.setExternalId(league.get("id").asInt());
                leagueEntity.setName(league.get("name").asText());
                if (league.has("logo") && !league.get("logo").isNull()) {
                    leagueEntity.setLogoUrl(league.get("logo").asText());
                }
                if (league.has("country") && !league.get("country").isNull()) {
                    Country country = new Country();
                    country.setName(league.get("country").asText());
                    leagueEntity.setCountry(country);
                }
                if (league.has("season") && !league.get("season").isNull()) {
                    leagueEntity.setSeason(league.get("season").asInt());
                }
                stats.setLeague(leagueEntity);
            }

            // Parse games statistics
            JsonNode games = statsData.get("games");
            if (games != null) {
                if (games.has("appearences") && !games.get("appearences").isNull()) {
                    stats.setAppearances(games.get("appearences").asInt());
                }
                if (games.has("lineups") && !games.get("lineups").isNull()) {
                    stats.setLineups(games.get("lineups").asInt());
                }
                if (games.has("minutes") && !games.get("minutes").isNull()) {
                    stats.setMinutesPlayed(games.get("minutes").asInt());
                }
                if (games.has("position") && !games.get("position").isNull()) {
                    stats.setPosition(games.get("position").asText());
                }
                if (games.has("rating") && !games.get("rating").isNull()) {
                    try {
                        String ratingStr = games.get("rating").asText();
                        if (!ratingStr.isEmpty() && !"null".equals(ratingStr)) {
                            double ratingValue = Double.parseDouble(ratingStr);
                            stats.setRating(BigDecimal.valueOf(ratingValue));
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse rating '{}': {}", games.get("rating").asText(), e.getMessage());
                    }
                }
                if (games.has("captain") && !games.get("captain").isNull()) {
                    stats.setIsCaptain(games.get("captain").asBoolean());
                }
                if (player != null && games.has("number") && !games.get("number").isNull()) {
                    player.setJerseyNumber(games.get("number").asInt());
                }
            }

            // Parse goals and assists
            JsonNode goals = statsData.get("goals");
            if (goals != null) {
                if (goals.has("total") && !goals.get("total").isNull()) {
                    stats.setGoals(goals.get("total").asInt());
                }
                if (goals.has("assists") && !goals.get("assists").isNull()) {
                    stats.setAssists(goals.get("assists").asInt());
                }
                if (goals.has("conceded") && !goals.get("conceded").isNull()) {
                    stats.setGoalsConceded(goals.get("conceded").asInt());
                }
                if (goals.has("saves") && !goals.get("saves").isNull()) {
                    stats.setSaves(goals.get("saves").asInt());
                }
            }

            // Parse shots
            JsonNode shots = statsData.get("shots");
            if (shots != null) {
                if (shots.has("total") && !shots.get("total").isNull()) {
                    stats.setShotsTotal(shots.get("total").asInt());
                }
                if (shots.has("on") && !shots.get("on").isNull()) {
                    stats.setShotsOnTarget(shots.get("on").asInt());
                }
            }

            JsonNode passes = statsData.get("passes");
            if (passes != null) {
                if (passes.has("total") && !passes.get("total").isNull()) {
                    stats.setPassesTotal(passes.get("total").asInt());
                }
                if (passes.has("key") && !passes.get("key").isNull()) {
                    stats.setPassesKey(passes.get("key").asInt());
                }
                if (passes.has("accuracy") && !passes.get("accuracy").isNull()) {
                    try {
                        int accuracyInt = passes.get("accuracy").asInt();
                        stats.setPassAccuracy(BigDecimal.valueOf(accuracyInt));
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse pass accuracy: {}", e.getMessage());
                    }
                }
            }

            // Parse tackles
            JsonNode tackles = statsData.get("tackles");
            if (tackles != null) {
                if (tackles.has("total") && !tackles.get("total").isNull()) {
                    stats.setTacklesTotal(tackles.get("total").asInt());
                }
                if (tackles.has("blocks") && !tackles.get("blocks").isNull()) {
                    stats.setTacklesBlocks(tackles.get("blocks").asInt());
                }
                if (tackles.has("interceptions") && !tackles.get("interceptions").isNull()) {
                    stats.setInterceptions(tackles.get("interceptions").asInt());
                }
            }

            // Parse dribbles
            JsonNode dribbles = statsData.get("dribbles");
            if (dribbles != null) {
                if (dribbles.has("attempts") && !dribbles.get("attempts").isNull()) {
                    stats.setDribblesAttempts(dribbles.get("attempts").asInt());
                }
                if (dribbles.has("success") && !dribbles.get("success").isNull()) {
                    stats.setDribblesSuccess(dribbles.get("success").asInt());
                }
            }

            // Parse fouls
            JsonNode fouls = statsData.get("fouls");
            if (fouls != null) {
                if (fouls.has("drawn") && !fouls.get("drawn").isNull()) {
                    stats.setFoulsDrawn(fouls.get("drawn").asInt());
                }
                if (fouls.has("committed") && !fouls.get("committed").isNull()) {
                    stats.setFoulsCommitted(fouls.get("committed").asInt());
                }
            }

            // Parse cards
            JsonNode cards = statsData.get("cards");
            if (cards != null) {
                if (cards.has("yellow") && !cards.get("yellow").isNull()) {
                    stats.setYellowCards(cards.get("yellow").asInt());
                }
                if (cards.has("red") && !cards.get("red").isNull()) {
                    stats.setRedCards(cards.get("red").asInt());
                }
                if (cards.has("yellowred") && !cards.get("yellowred").isNull()) {
                    int yellowRed = cards.get("yellowred").asInt();
                    Integer currentRedCards = stats.getRedCards();
                    int totalRedCards = (currentRedCards != null ? currentRedCards : 0) + yellowRed;
                    stats.setRedCards(totalRedCards);
                }
            }

            JsonNode penalty = statsData.get("penalty");
            if (penalty != null) {
                if (penalty.has("won") && !penalty.get("won").isNull()) {
                    stats.setPenaltiesWon(penalty.get("won").asInt());
                }
                if (penalty.has("scored") && !penalty.get("scored").isNull()) {
                    stats.setPenaltiesScored(penalty.get("scored").asInt());
                }
                if (penalty.has("missed") && !penalty.get("missed").isNull()) {
                    stats.setPenaltiesMissed(penalty.get("missed").asInt());
                }
            }

            // Parse substitutions
            JsonNode substitutes = statsData.get("substitutes");
            if (substitutes != null) {
                if (substitutes.has("in") && !substitutes.get("in").isNull()) {
                    stats.setSubstitutesIn(substitutes.get("in").asInt());
                }
                if (substitutes.has("out") && !substitutes.get("out").isNull()) {
                    stats.setSubstitutesOut(substitutes.get("out").asInt());
                }
                if (substitutes.has("bench") && !substitutes.get("bench").isNull()) {
                    stats.setSubstitutesBench(substitutes.get("bench").asInt());
                }
            }

            return stats;
        } catch (Exception e) {
            logger.error("Error parsing player statistics: {}", e.getMessage());
            return null;
        }
    }
}

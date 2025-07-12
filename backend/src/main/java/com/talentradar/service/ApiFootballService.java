package com.talentradar.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentradar.model.Club;
import com.talentradar.model.Country;
import com.talentradar.model.League;
import com.talentradar.model.Player;
import com.talentradar.model.PlayerStatistic;

import io.jsonwebtoken.io.IOException;

@Service
public class ApiFootballService {

    private static final Logger logger = LoggerFactory.getLogger(ApiFootballService.class);
    private static final int RATE_LIMIT_DELAY = 900; // 15 minutes between requests (to stay under 100/day)

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

    private void rateLimitDelay() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (timeSinceLastRequest < RATE_LIMIT_DELAY * 1000) {
            long waitTime = (RATE_LIMIT_DELAY * 1000) - timeSinceLastRequest;
            logger.info("Rate limiting: waiting {} seconds before next API call", waitTime / 1000);

            try {
                Thread.sleep(waitTime);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Rate limiting interrupted", e);

            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", apiKey);
        headers.set("x-rapidapi-host", apiHost);
        return headers;
    }

    private JsonNode makeApiCall(String endpoint) {
        rateLimitDelay();

        try {
            String url = baseUrl + endpoint;
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());

            logger.info("Making API call to {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            JsonNode errors = jsonResponse.get("errors");
            if (errors != null && errors.size() > 0) {
                logger.error("API returned errors: {}", errors);
                return null;
            }
            return jsonResponse;

        } catch (IOException | HttpClientErrorException | HttpServerErrorException | JsonProcessingException e) {
            logger.error("Error making API call to {}: {}", endpoint, e.getMessage());
            return null;
        }
    }

    public List<League> getTopLeagues() {
        List<League> leagues = new ArrayList<>();

        // Get major European leagues that likely have U21 players
        int[] leagueIds = {39, 61, 78, 135, 140};

        for (int leagueId : leagueIds) {
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
        }

        return leagues;
    }

    public List<Club> getTeamsFromLeague(int leagueId, int season) {
        List<Club> clubs = new ArrayList<>();

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
        return clubs;

    }

    public List<Player> getPlayersFromTeam(int teamId, int season) {
        List<Player> players = new ArrayList<>();

        JsonNode response = makeApiCall("/players?team=" + teamId + "&season=" + season);
        if (response != null) {
            JsonNode responseArray = response.get("response");
            if (responseArray != null) {
                for (JsonNode playerData : responseArray) {
                    Player player = parsePlayer(playerData);
                    if (player != null && player.isEligibleForU21()) {
                        players.add(player);

                        // Parse statistics for this player
                        JsonNode statisticsArray = playerData.get("statistics");
                        if (statisticsArray != null && statisticsArray.size() > 0) {
                            JsonNode stats = statisticsArray.get(0);
                            PlayerStatistic playerStats = parsePlayerStatistics(stats, player, season);
                            if (playerStats != null) {
                                player.getStatistics().add(playerStats);
                            }
                        }
                    }
                }
            }
        }

        return players;
    }

    private League parseLeague(JsonNode leagueData) {
        try {
            JsonNode leagueInfo = leagueData.get("league");
            JsonNode countryInfo = leagueData.get("country");

            League league = new League();
            league.setExternalId(leagueInfo.get("id").asInt());
            league.setName(leagueInfo.get("name").asText());
            league.setLogoUrl(leagueInfo.get("logo").asText());
            league.setType(leagueInfo.get("type").asText());

            // Create country if provided
            if (countryInfo != null) {
                Country country = new Country();
                country.setName(countryInfo.get("name").asText());
                country.setCode(countryInfo.get("code").asText());
                if (countryInfo.has("flag")) {
                    country.setFlagUrl(countryInfo.get("flag").asText());
                }
                league.setCountry(country);
            }

            return league;
        } catch (Exception e) {
            System.err.println("Error parsing league data: " + e.getMessage());
            return null;
        }
    }

    private Club parseTeam(JsonNode teamData) {
        try {
            JsonNode team = teamData.get("team");

            Club club = new Club();
            club.setExternalId(team.get("id").asInt());
            club.setName(team.get("name").asText());
            club.setLogoUrl(team.get("logo").asText());

            if (team.has("country")) {
                club.setCountry(team.get("country").asText());
            }

            return club;
        } catch (Exception e) {
            System.err.println("Error parsing team data: " + e.getMessage());
            return null;
        }
    }

    private Player parsePlayer(JsonNode playerData) {
        try {
            JsonNode player = playerData.get("player");
            JsonNode birth = player.get("birth");

            Player playerEntity = new Player();
            playerEntity.setExternalId(player.get("id").asInt());
            playerEntity.setName(player.get("name").asText());
            playerEntity.setFirstName(player.get("firstname").asText());
            playerEntity.setLastName(player.get("lastname").asText());
            playerEntity.setNationality(player.get("nationality").asText());
            playerEntity.setPhotoUrl(player.get("photo").asText());

            if (player.has("injured")) {
                playerEntity.setIsInjured(player.get("injured").asBoolean());
            }

            // Parse birth information
            if (birth != null) {
                if (birth.has("date")) {
                    String birthDate = birth.get("date").asText();
                    playerEntity.setDateOfBirth(LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE));
                }
                if (birth.has("place")) {
                    playerEntity.setBirthPlace(birth.get("place").asText());
                }
                if (birth.has("country")) {
                    playerEntity.setBirthCountry(birth.get("country").asText());
                }
            }

            // Parse height and weight
            if (player.has("height") && !player.get("height").isNull()) {
                String height = player.get("height").asText();
                if (height.contains("cm")) {
                    try {
                        int heightCm = Integer.parseInt(height.replace(" cm", "").trim());
                        playerEntity.setHeightCm(heightCm);
                    } catch (NumberFormatException e) {
                        System.err.println("Could not parse height: " + height);
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
                        System.err.println("Could not parse weight: " + weight);
                    }
                }
            }

            return playerEntity;
        } catch (Exception e) {
            System.err.println("Error parsing player data: " + e.getMessage());
            return null;
        }
    }

    private PlayerStatistic parsePlayerStatistics(JsonNode statsData, Player player, int season) {
        try {
            PlayerStatistic stats = new PlayerStatistic();
            stats.setPlayer(player);
            stats.setSeason(season);

            // Parse games statistics
            JsonNode games = statsData.get("games");
            if (games != null) {
                if (games.has("appearences") && !games.get("appearences").isNull()) {
                    stats.setAppearances(games.get("appearences").asInt());
                }
                if (games.has("minutes") && !games.get("minutes").isNull()) {
                    stats.setMinutesPlayed(games.get("minutes").asInt());
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

            // Parse cards
            JsonNode cards = statsData.get("cards");
            if (cards != null) {
                if (cards.has("yellow") && !cards.get("yellow").isNull()) {
                    stats.setYellowCards(cards.get("yellow").asInt());
                }
                if (cards.has("red") && !cards.get("red").isNull()) {
                    stats.setRedCards(cards.get("red").asInt());
                }
            }

            // Parse passes
            JsonNode passes = statsData.get("passes");
            if (passes != null && passes.has("accuracy") && !passes.get("accuracy").isNull()) {
                stats.setPassAccuracy(BigDecimal.valueOf(passes.get("accuracy").asDouble()));
            }

            return stats;
        } catch (Exception e) {
            System.err.println("Error parsing player statistics: " + e.getMessage());
            return null;
        }
    }

}

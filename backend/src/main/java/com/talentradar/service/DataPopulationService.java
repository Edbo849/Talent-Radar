package com.talentradar.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.talentradar.model.Club;
import com.talentradar.model.Country;
import com.talentradar.model.League;
import com.talentradar.model.Player;
import com.talentradar.model.PlayerStatistic;
import com.talentradar.repository.ClubRepository;
import com.talentradar.repository.CountryRepository;
import com.talentradar.repository.LeagueRepository;
import com.talentradar.repository.PlayerRepository;
import com.talentradar.repository.PlayerStatisticRepository;

import jakarta.transaction.Transactional;

@Service
public class DataPopulationService {

    private static final Logger logger = LoggerFactory.getLogger(ApiFootballService.class);

    private static final int CURRENT_SEASON = 2024;

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

    @Async
    public void populateU21PlayersAsync() {
        logger.info("Starting U21 player population process...");

        try {
            populateU21Players();
            logger.info("U21 player population completed successfully!");
        } catch (Exception e) {
            logger.error("Error during U21 player population: " + e.getMessage(), e);
        }

    }

    @Transactional
    public void populateU21Players() {
        logger.info("Retreiving top leagues...");
        List<League> topLeagues = apiFootballService.getTopLeagues();

        int apiCalls = 0;
        int maxCalls = 95;

        for (League league : topLeagues) {
            if (apiCalls >= maxCalls) {
                logger.info("Approaching API limit. Stopping data population.");
                break;
            }
            logger.info("Processing league: {}", league);

            // Save league
            League savedLeague = saveLeague(league);
            apiCalls++;

            // Get teams from this league
            List<Club> clubs = apiFootballService.getTeamsFromLeague(savedLeague.getExternalId(), CURRENT_SEASON);
            apiCalls++;
            if (apiCalls >= maxCalls) {
                logger.info("Approaching API limit. Stopping data population.");
                break;
            }

            // Process only a subset of teams to respect API limits
            int teamsToProcess = Math.min(clubs.size(), (apiCalls - maxCalls));
            logger.info("Processing " + teamsToProcess + " teams from " + league.getName() + " (total: "
                    + clubs.size() + ")");

            for (int i = 0; i < teamsToProcess; i++) {
                Club club = clubs.get(i);
                if (apiCalls >= maxCalls) {
                    break;
                }

                logger.info("Processing team: " + club.getName());
                Club savedClub = saveClub(club);

                // Get players from this team
                List<Player> players = apiFootballService.getPlayersFromTeam(savedClub.getExternalId(), CURRENT_SEASON);
                apiCalls++; // We made 1 API call to get players

                logger.info("Found " + players.size() + " U21 players in " + club.getName());

                // Save U21 players
                for (Player player : players) {
                    if (player.isEligibleForU21()) {
                        savePlayerWithStatistics(player, savedClub, savedLeague);
                    }
                }
            }
        }

    }

    private League saveLeague(League league) {
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
    }

    private Country saveCountry(Country country) {
        Optional<Country> existingCountry = countryRepository.findByName(country.getName());
        if (existingCountry.isPresent()) {
            return existingCountry.get();
        } else {
            return countryRepository.save(country);
        }
    }

    private Club saveClub(Club club) {
        Optional<Club> existingClub = clubRepository.findByExternalId(club.getExternalId());
        if (existingClub.isPresent()) {
            return existingClub.get();
        } else {
            return clubRepository.save(club);
        }
    }

    private void savePlayerWithStatistics(Player player, Club club, League league) {
        // Check if player already exists
        Optional<Player> existingPlayer = playerRepository.findByExternalId(player.getExternalId());
        Player savedPlayer;

        if (existingPlayer.isPresent()) {
            savedPlayer = existingPlayer.get();
            // Update player information
            savedPlayer.setName(player.getName());
            savedPlayer.setFirstName(player.getFirstName());
            savedPlayer.setLastName(player.getLastName());
            savedPlayer.setNationality(player.getNationality());
            savedPlayer.setPhotoUrl(player.getPhotoUrl());
            savedPlayer.setIsInjured(player.getIsInjured());
            savedPlayer.setHeightCm(player.getHeightCm());
            savedPlayer.setWeightKg(player.getWeightKg());
            savedPlayer.setBirthPlace(player.getBirthPlace());
            savedPlayer.setBirthCountry(player.getBirthCountry());

            savedPlayer = playerRepository.save(savedPlayer);
        } else {
            savedPlayer = playerRepository.save(player);
        }

        // Save player statistics if available
        if (!player.getStatistics().isEmpty()) {
            for (PlayerStatistic stats : player.getStatistics()) {
                // Check if statistics already exist for this player/season
                List<PlayerStatistic> existingStats = playerStatisticRepository
                        .findByPlayerAndSeason(savedPlayer, stats.getSeason());

                if (existingStats.isEmpty()) {
                    stats.setPlayer(savedPlayer);
                    stats.setClub(club);
                    stats.setLeague(league);
                    playerStatisticRepository.save(stats);
                    logger.info("Saved statistics for player: " + savedPlayer.getName());
                } else {
                    logger.info("Statistics already exist for player: " + savedPlayer.getName());
                }
            }
        }

        logger.info("Saved U21 player: " + savedPlayer.getName() + " (Age: " + savedPlayer.getAge() + ")");
    }

    public void scheduleDataPopulation() {
        logger.info("Scheduling U21 player data population...");
        populateU21PlayersAsync();
    }

    public long getU21PlayerCount() {
        return playerRepository.countByU21Eligible();
    }
}

package com.talentradar.service.club;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.CountryNotFoundException;
import com.talentradar.exception.LeagueNotFoundException;
import com.talentradar.model.club.Club;
import com.talentradar.model.club.Country;
import com.talentradar.model.club.League;
import com.talentradar.repository.club.ClubRepository;
import com.talentradar.repository.club.CountryRepository;
import com.talentradar.repository.club.LeagueRepository;

/**
 * Service layer for managing league-related operations. Provides business logic
 * for league management, club associations, and seasonal data operations.
 */
@Service
@Transactional
public class LeagueService {

    private static final Logger logger = LoggerFactory.getLogger(LeagueService.class);

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ClubRepository clubRepository;

    /**
     * Retrieves all leagues ordered by name.
     */
    @Transactional(readOnly = true)
    public List<League> getAllLeagues() {
        try {
            logger.debug("Retrieving all leagues");
            return leagueRepository.findAllByOrderByNameAsc();
        } catch (Exception e) {
            logger.error("Error retrieving all leagues: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve leagues", e);
        }
    }

    /**
     * Retrieves leagues with pagination support.
     */
    @Transactional(readOnly = true)
    public Page<League> getLeaguesPaginated(Pageable pageable) {
        try {
            logger.debug("Retrieving leagues with pagination");
            return leagueRepository.findAllByOrderByNameAsc(pageable);
        } catch (Exception e) {
            logger.error("Error retrieving paginated leagues: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve paginated leagues", e);
        }
    }

    /**
     * Retrieves a league by its unique identifier.
     */
    @Transactional(readOnly = true)
    public Optional<League> getLeagueById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("League ID cannot be null");
            }
            logger.debug("Finding league by ID: {}", id);
            return leagueRepository.findById(id);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid league ID: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding league by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to find league by ID", e);
        }
    }

    /**
     * Retrieves a league by its external API identifier.
     */
    @Transactional(readOnly = true)
    public Optional<League> getLeagueByExternalId(Integer externalId) {
        try {
            if (externalId == null) {
                throw new IllegalArgumentException("External ID cannot be null");
            }
            logger.debug("Finding league by external ID: {}", externalId);
            return leagueRepository.findByExternalId(externalId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid external ID: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding league by external ID {}: {}", externalId, e.getMessage(), e);
            throw new RuntimeException("Failed to find league by external ID", e);
        }
    }

    /**
     * Retrieves leagues by season.
     */
    @Transactional(readOnly = true)
    public List<League> getLeaguesBySeason(Integer season) {
        try {
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }
            logger.debug("Retrieving leagues by season: {}", season);
            return leagueRepository.findBySeason(season);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid season parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving leagues by season: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve leagues by season", e);
        }
    }

    /**
     * Retrieves clubs associated with a specific league.
     */
    @Transactional(readOnly = true)
    public List<Club> getLeagueClubs(League league) {
        try {
            if (league == null) {
                throw new IllegalArgumentException("League cannot be null");
            }
            logger.debug("Retrieving clubs for league: {}", league.getName());
            return clubRepository.findByCurrentLeague(league);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid league parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving league clubs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve league clubs", e);
        }
    }

    /**
     * Retrieves leagues by country.
     */
    @Transactional(readOnly = true)
    public List<League> getLeaguesByCountry(Long countryId) {
        try {
            if (countryId == null) {
                throw new IllegalArgumentException("Country ID cannot be null");
            }

            Country country = countryRepository.findById(countryId)
                    .orElseThrow(() -> new CountryNotFoundException("Country not found with ID: " + countryId));

            logger.debug("Retrieving leagues by country: {}", country.getName());
            return leagueRepository.findByCountryOrderByNameAsc(country);
        } catch (IllegalArgumentException | CountryNotFoundException e) {
            logger.error("Invalid country parameter: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving leagues by country: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve leagues by country", e);
        }
    }

    /**
     * Retrieves leagues by type.
     */
    @Transactional(readOnly = true)
    public List<League> getLeaguesByType(String type) {
        try {
            if (type == null || type.trim().isEmpty()) {
                throw new IllegalArgumentException("League type cannot be null or empty");
            }
            logger.debug("Retrieving leagues by type: {}", type);
            return leagueRepository.findByTypeOrderByNameAsc(type.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid league type: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving leagues by type: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve leagues by type", e);
        }
    }

    /**
     * Searches leagues by name containing the specified term.
     */
    @Transactional(readOnly = true)
    public List<League> searchLeagues(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                throw new IllegalArgumentException("Search term cannot be null or empty");
            }
            logger.debug("Searching leagues with term: {}", searchTerm);
            return leagueRepository.findByNameContainingIgnoreCase(searchTerm.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid search term: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching leagues: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search leagues", e);
        }
    }

    /**
     * Retrieves a league by external ID and season.
     */
    @Transactional(readOnly = true)
    public Optional<League> getLeagueByExternalIdAndSeason(Integer externalId, Integer season) {
        try {
            if (externalId == null) {
                throw new IllegalArgumentException("External ID cannot be null");
            }
            if (season == null) {
                throw new IllegalArgumentException("Season cannot be null");
            }
            logger.debug("Finding league by external ID {} and season {}", externalId, season);
            return leagueRepository.findByExternalIdAndSeason(externalId, season);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding league by external ID and season: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find league by external ID and season", e);
        }
    }

    /**
     * Creates a new league with the specified details.
     */
    public League createLeague(String name, Long countryId, String logoUrl, Integer season, String type, Integer externalId) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("League name is required");
            }

            Country country = null;
            if (countryId != null) {
                country = countryRepository.findById(countryId)
                        .orElseThrow(() -> new CountryNotFoundException("Country not found with ID: " + countryId));
            }

            // Check if league with external ID and season already exists
            if (externalId != null && season != null) {
                Optional<League> existingLeague = leagueRepository.findByExternalIdAndSeason(externalId, season);
                if (existingLeague.isPresent()) {
                    throw new IllegalArgumentException("League with external ID " + externalId + " and season " + season + " already exists");
                }
            }

            League league = new League();
            league.setName(name.trim());
            league.setCountry(country);
            league.setLogoUrl(logoUrl);
            league.setSeason(season);
            league.setType(type != null ? type.trim() : null);
            league.setExternalId(externalId);

            League savedLeague = leagueRepository.save(league);
            logger.info("Created new league: {} for season: {}", name, season);
            return savedLeague;
        } catch (IllegalArgumentException | CountryNotFoundException e) {
            logger.error("Invalid league creation parameters: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error creating league: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create league", e);
        }
    }

    /**
     * Updates an existing league with new information.
     */
    public League updateLeague(Long id, String name, Long countryId, String logoUrl, Integer season, String type) {
        try {
            League league = leagueRepository.findById(id)
                    .orElseThrow(() -> new LeagueNotFoundException("League not found with ID: " + id));

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("League name is required");
            }

            Country country = null;
            if (countryId != null) {
                country = countryRepository.findById(countryId)
                        .orElseThrow(() -> new CountryNotFoundException("Country not found with ID: " + countryId));
            }

            league.setName(name.trim());
            league.setCountry(country);
            league.setLogoUrl(logoUrl);
            league.setSeason(season);
            league.setType(type != null ? type.trim() : null);

            League updatedLeague = leagueRepository.save(league);
            logger.info("Updated league with ID: {}", id);
            return updatedLeague;
        } catch (LeagueNotFoundException | CountryNotFoundException e) {
            logger.error("Entity not found for league update: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid league update parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating league: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update league", e);
        }
    }

    /**
     * Deletes a league by its unique identifier.
     */
    public void deleteLeague(Long id) {
        try {
            League league = leagueRepository.findById(id)
                    .orElseThrow(() -> new LeagueNotFoundException("League not found with ID: " + id));

            // Check if league has associated clubs or statistics
            long clubCount = leagueRepository.countClubsByLeague(league);
            if (clubCount > 0) {
                throw new IllegalStateException("Cannot delete league: " + clubCount + " clubs are associated with it");
            }

            leagueRepository.delete(league);
            logger.info("Deleted league with ID: {}", id);
        } catch (LeagueNotFoundException e) {
            logger.error("League not found for deletion: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Cannot delete league: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting league: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete league", e);
        }
    }

    /**
     * Retrieves the total number of leagues in the system.
     */
    @Transactional(readOnly = true)
    public long getTotalLeagueCount() {
        try {
            logger.debug("Counting total leagues");
            return leagueRepository.count();
        } catch (Exception e) {
            logger.error("Error counting total leagues: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to count leagues", e);
        }
    }

    /**
     * Retrieves the top major European leagues.
     */
    @Transactional(readOnly = true)
    public List<League> getTopLeagues() {
        try {
            logger.debug("Retrieving top leagues");
            // Return major European leagues - Premier League, Ligue 1, Bundesliga, Serie A, La Liga
            List<Integer> topLeagueIds = List.of(39, 61, 78, 135, 140);
            return leagueRepository.findByExternalIdInOrderByNameAsc(topLeagueIds);
        } catch (Exception e) {
            logger.error("Error retrieving top leagues: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve top leagues", e);
        }
    }

    /**
     * Retrieves leagues for the current season.
     */
    @Transactional(readOnly = true)
    public List<League> getCurrentSeasonLeagues() {
        try {
            Integer currentSeason = java.time.Year.now().getValue();
            logger.debug("Retrieving current season leagues for: {}", currentSeason);
            return leagueRepository.findBySeason(currentSeason);
        } catch (Exception e) {
            logger.error("Error retrieving current season leagues: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve current season leagues", e);
        }
    }

    /**
     * Retrieves all distinct league types.
     */
    @Transactional(readOnly = true)
    public List<String> getAllLeagueTypes() {
        try {
            logger.debug("Retrieving all league types");
            return leagueRepository.findDistinctTypes();
        } catch (Exception e) {
            logger.error("Error retrieving league types: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve league types", e);
        }
    }

    /**
     * Retrieves all distinct seasons in descending order.
     */
    @Transactional(readOnly = true)
    public List<Integer> getAllSeasons() {
        try {
            logger.debug("Retrieving all seasons");
            return leagueRepository.findDistinctSeasonsOrderByDesc();
        } catch (Exception e) {
            logger.error("Error retrieving seasons: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve seasons", e);
        }
    }
}

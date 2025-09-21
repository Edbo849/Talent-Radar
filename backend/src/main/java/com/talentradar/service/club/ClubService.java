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

import com.talentradar.exception.ClubNotFoundException;
import com.talentradar.model.club.Club;
import com.talentradar.model.club.Country;
import com.talentradar.model.player.Player;
import com.talentradar.repository.club.ClubRepository;
import com.talentradar.repository.club.CountryRepository;
import com.talentradar.repository.player.PlayerRepository;

/**
 * Service layer for managing club-related operations. Provides business logic
 * for club management, player associations, and country-based operations.
 */
@Service
@Transactional
public class ClubService {

    private static final Logger logger = LoggerFactory.getLogger(ClubService.class);

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private CountryRepository countryRepository;

    /**
     * Retrieves a club by its unique identifier.
     */
    @Transactional(readOnly = true)
    public Optional<Club> findById(Long id) {
        try {
            logger.debug("Finding club by ID: {}", id);
            return clubRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding club by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to find club by ID", e);
        }
    }

    /**
     * Retrieves a club by its external API identifier.
     */
    @Transactional(readOnly = true)
    public Optional<Club> findByExternalId(Integer externalId) {
        try {
            logger.debug("Finding club by external ID: {}", externalId);
            return clubRepository.findByExternalId(externalId);
        } catch (Exception e) {
            logger.error("Error finding club by external ID {}: {}", externalId, e.getMessage(), e);
            throw new RuntimeException("Failed to find club by external ID", e);
        }
    }

    /**
     * Saves a club entity to the database.
     */
    public Club save(Club club) {
        try {
            if (club == null) {
                throw new IllegalArgumentException("Club cannot be null");
            }
            logger.debug("Saving club: {}", club.getName());
            return clubRepository.save(club);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid club data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error saving club: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save club", e);
        }
    }

    /**
     * Deletes a club by its unique identifier.
     */
    public void deleteById(Long id) {
        try {
            if (!clubRepository.existsById(id)) {
                throw new ClubNotFoundException("Club not found with ID: " + id);
            }
            logger.info("Deleting club with ID: {}", id);
            clubRepository.deleteById(id);
        } catch (ClubNotFoundException e) {
            logger.error("Club not found for deletion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting club with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete club", e);
        }
    }

    /**
     * Retrieves all clubs with pagination support.
     */
    @Transactional(readOnly = true)
    public Page<Club> getAllClubs(Pageable pageable) {
        try {
            logger.debug("Retrieving all clubs with pagination");
            return clubRepository.findAll(pageable);
        } catch (Exception e) {
            logger.error("Error retrieving all clubs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve clubs", e);
        }
    }

    /**
     * Retrieves all active clubs with pagination support.
     */
    @Transactional(readOnly = true)
    public Page<Club> getActiveClubs(Pageable pageable) {
        try {
            logger.debug("Retrieving active clubs with pagination");
            return clubRepository.findByIsActiveTrue(pageable);
        } catch (Exception e) {
            logger.error("Error retrieving active clubs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active clubs", e);
        }
    }

    /**
     * Searches for clubs by name containing the specified term.
     */
    @Transactional(readOnly = true)
    public List<Club> searchClubsByName(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Search name cannot be null or empty");
            }
            logger.debug("Searching clubs by name: {}", name);
            return clubRepository.findByNameContainingIgnoreCase(name);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid search parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching clubs by name: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search clubs by name", e);
        }
    }

    /**
     * Searches for clubs with pagination support.
     */
    @Transactional(readOnly = true)
    public Page<Club> searchClubs(String searchTerm, Pageable pageable) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                throw new IllegalArgumentException("Search term cannot be null or empty");
            }
            logger.debug("Searching clubs with term: {}", searchTerm);
            return clubRepository.findByNameContainingIgnoreCase(searchTerm, pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid search parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching clubs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search clubs", e);
        }
    }

    /**
     * Retrieves clubs by country name.
     */
    @Transactional(readOnly = true)
    public List<Club> getClubsByCountry(String country) {
        try {
            if (country == null || country.trim().isEmpty()) {
                throw new IllegalArgumentException("Country cannot be null or empty");
            }
            logger.debug("Retrieving clubs by country: {}", country);
            return clubRepository.findByCountry(country);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid country parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving clubs by country: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve clubs by country", e);
        }
    }

    /**
     * Retrieves clubs by country with pagination support.
     */
    @Transactional(readOnly = true)
    public Page<Club> getClubsByCountry(String country, Pageable pageable) {
        try {
            if (country == null || country.trim().isEmpty()) {
                throw new IllegalArgumentException("Country cannot be null or empty");
            }
            logger.debug("Retrieving clubs by country with pagination: {}", country);
            return clubRepository.findByCountryAndIsActiveTrue(country, pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid country parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving clubs by country with pagination: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve clubs by country", e);
        }
    }

    /**
     * Retrieves clubs by league.
     */
    @Transactional(readOnly = true)
    public List<Club> getClubsByLeague(String league) {
        try {
            if (league == null || league.trim().isEmpty()) {
                throw new IllegalArgumentException("Country cannot be null or empty");
            }
            logger.debug("Retrieving clubs by league: {}", league);
            return clubRepository.findByLeagueName(league);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid league parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving clubs by league: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve clubs by league", e);
        }
    }

    /**
     * Retrieves clubs by league with pagination support.
     */
    @Transactional(readOnly = true)
    public Page<Club> getClubsByLeague(String league, Pageable pageable) {
        try {
            if (league == null || league.trim().isEmpty()) {
                throw new IllegalArgumentException("Country cannot be null or empty");
            }
            logger.debug("Retrieving clubs by league: {}", league);
            return clubRepository.findByLeagueName(league, pageable);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid league parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving clubs by league: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve clubs by league", e);
        }
    }

    /**
     * Retrieves active clubs by country name.
     */
    @Transactional(readOnly = true)
    public List<Club> getActiveClubsByCountry(String country) {
        try {
            if (country == null || country.trim().isEmpty()) {
                throw new IllegalArgumentException("Country cannot be null or empty");
            }
            logger.debug("Retrieving active clubs by country: {}", country);
            return clubRepository.findByCountryAndIsActiveTrue(country);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid country parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving active clubs by country: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active clubs by country", e);
        }
    }

    /**
     * Retrieves players associated with a specific club.
     */
    @Transactional(readOnly = true)
    public List<Player> getClubPlayers(Club club) {
        try {
            if (club == null) {
                throw new IllegalArgumentException("Club cannot be null");
            }
            logger.debug("Retrieving players for club: {}", club.getName());
            return playerRepository.findCurrentPlayersByClub(club);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid club parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving club players: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve club players", e);
        }
    }

    /**
     * Retrieves players by club and position.
     */
    @Transactional(readOnly = true)
    public List<Player> getClubPlayersByPosition(Club club, String position) {
        try {
            if (club == null) {
                throw new IllegalArgumentException("Club cannot be null");
            }
            if (position == null || position.trim().isEmpty()) {
                throw new IllegalArgumentException("Position cannot be null or empty");
            }
            logger.debug("Retrieving players for club {} by position: {}", club.getName(), position);
            return playerRepository.findCurrentPlayersByClubAndPosition(club, position);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving club players by position: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve club players by position", e);
        }
    }

    /**
     * Counts the number of players in a specific club.
     */
    @Transactional(readOnly = true)
    public long getClubPlayerCount(Club club) {
        try {
            if (club == null) {
                throw new IllegalArgumentException("Club cannot be null");
            }
            logger.debug("Counting players for club: {}", club.getName());
            return playerRepository.countCurrentPlayersByClub(club);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid club parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error counting club players: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to count club players", e);
        }
    }

    /**
     * Retrieves the total number of clubs in the system.
     */
    @Transactional(readOnly = true)
    public long getTotalClubCount() {
        try {
            logger.debug("Counting total clubs");
            return clubRepository.count();
        } catch (Exception e) {
            logger.error("Error counting total clubs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to count total clubs", e);
        }
    }

    /**
     * Retrieves the number of active clubs.
     */
    @Transactional(readOnly = true)
    public long getActiveClubCount() {
        try {
            logger.debug("Counting active clubs");
            return clubRepository.countByIsActiveTrue();
        } catch (Exception e) {
            logger.error("Error counting active clubs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to count active clubs", e);
        }
    }

    /**
     * Retrieves all distinct countries that have clubs.
     */
    @Transactional(readOnly = true)
    public List<String> getAllCountries() {
        try {
            logger.debug("Retrieving all countries with clubs");
            return clubRepository.findDistinctCountries();
        } catch (Exception e) {
            logger.error("Error retrieving all countries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve countries", e);
        }
    }

    /**
     * Creates a new club with the specified details.
     */
    public Club createClub(String name, String country, String logoUrl) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Club name is required");
            }
            if (country == null || country.trim().isEmpty()) {
                throw new IllegalArgumentException("Country is required");
            }

            Club club = new Club();
            club.setName(name.trim());
            club.setCountry(findOrCreateCountry(country.trim()));
            club.setLogoUrl(logoUrl);
            club.setIsActive(true);

            Club savedClub = clubRepository.save(club);
            logger.info("Created new club: {} in country: {}", name, country);
            return savedClub;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid club creation parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating club: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create club", e);
        }
    }

    /**
     * Updates an existing club with new information.
     */
    public Club updateClub(Long clubId, String name, String countryName, String logoUrl) {
        try {
            Club club = findById(clubId)
                    .orElseThrow(() -> new ClubNotFoundException("Club not found with ID: " + clubId));

            if (name != null && !name.trim().isEmpty()) {
                club.setName(name.trim());
            }
            if (countryName != null && !countryName.trim().isEmpty()) {
                Country country = findOrCreateCountry(countryName.trim());
                club.setCountry(country);
            }
            if (logoUrl != null) {
                club.setLogoUrl(logoUrl);
            }

            Club updatedClub = clubRepository.save(club);
            logger.info("Updated club with ID: {}", clubId);
            return updatedClub;
        } catch (ClubNotFoundException e) {
            logger.error("Club not found for update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating club: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update club", e);
        }
    }

    /**
     * Activates a club by setting its status to active.
     */
    public void activateClub(Long clubId) {
        try {
            Club club = findById(clubId)
                    .orElseThrow(() -> new ClubNotFoundException("Club not found with ID: " + clubId));

            club.setIsActive(true);
            clubRepository.save(club);
            logger.info("Activated club with ID: {}", clubId);
        } catch (ClubNotFoundException e) {
            logger.error("Club not found for activation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error activating club: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to activate club", e);
        }
    }

    /**
     * Deactivates a club by setting its status to inactive.
     */
    public void deactivateClub(Long clubId) {
        try {
            Club club = findById(clubId)
                    .orElseThrow(() -> new ClubNotFoundException("Club not found with ID: " + clubId));

            club.setIsActive(false);
            clubRepository.save(club);
            logger.info("Deactivated club with ID: {}", clubId);
        } catch (ClubNotFoundException e) {
            logger.error("Club not found for deactivation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deactivating club: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deactivate club", e);
        }
    }

    /**
     * Checks if a club exists with the specified name.
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return false;
            }
            logger.debug("Checking if club exists with name: {}", name);
            return !clubRepository.findByNameContainingIgnoreCase(name).isEmpty();
        } catch (Exception e) {
            logger.error("Error checking club existence by name: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check club existence", e);
        }
    }

    /**
     * Checks if a club exists with the specified external ID.
     */
    @Transactional(readOnly = true)
    public boolean existsByExternalId(Integer externalId) {
        try {
            if (externalId == null) {
                return false;
            }
            logger.debug("Checking if club exists with external ID: {}", externalId);
            return clubRepository.findByExternalId(externalId).isPresent();
        } catch (Exception e) {
            logger.error("Error checking club existence by external ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to check club existence", e);
        }
    }

    /**
     * Helper method to find or create a country by name.
     */
    private Country findOrCreateCountry(String countryName) {
        Optional<Country> existingCountry = countryRepository.findByName(countryName);
        if (existingCountry.isPresent()) {
            return existingCountry.get();
        } else {
            Country newCountry = new Country();
            newCountry.setName(countryName);
            return countryRepository.save(newCountry);
        }
    }
}

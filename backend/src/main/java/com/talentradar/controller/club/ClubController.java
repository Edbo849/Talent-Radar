package com.talentradar.controller.club;

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

import com.talentradar.dto.club.ClubDTO;
import com.talentradar.dto.club.CountryDTO;
import com.talentradar.dto.player.PlayerDTO;
import com.talentradar.model.club.Club;
import com.talentradar.model.club.Country;
import com.talentradar.model.player.Player;
import com.talentradar.service.club.ClubService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * REST controller for managing football club operations. Provides endpoints for
 * retrieving club information, statistics, and player teams.
 */
@RestController
@RequestMapping("/api/clubs")
@CrossOrigin(origins = "http://localhost:3000")
public class ClubController {

    private static final Logger logger = LoggerFactory.getLogger(ClubController.class);
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private ClubService clubService;

    /**
     * Retrieves a paginated list of clubs with optional filters.
     */
    @GetMapping
    public ResponseEntity<Page<ClubDTO>> getClubs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String league,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        try {
            // Validate page size
            if (size > MAX_PAGE_SIZE) {
                size = MAX_PAGE_SIZE;
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Club> clubs;

            if (search != null && !search.trim().isEmpty()) {
                clubs = clubService.searchClubs(search.trim(), pageable);
                logger.debug("Searching clubs with term: {}", search);
            } else if (country != null && !country.trim().isEmpty()) {
                clubs = clubService.getClubsByCountry(country.trim(), pageable);
                logger.debug("Filtering clubs by country: {}", country);
            } else if (league != null && !league.trim().isEmpty()) {
                clubs = clubService.getClubsByLeague(league.trim(), pageable);
                logger.debug("Filtering clubs by league: {}", league);
            } else {
                clubs = clubService.getAllClubs(pageable);
                logger.debug("Retrieving all clubs");
            }

            Page<ClubDTO> clubDTOs = clubs.map(this::convertToDTO);

            logger.debug("Retrieved {} clubs (page {}, size {})",
                    clubs.getTotalElements(), page, size);
            return ResponseEntity.ok(clubDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving clubs with search: {}, league: {}, country: {}",
                    search, league, country, e);
            throw new RuntimeException("Failed to retrieve clubs: " + e.getMessage());
        }
    }

    /**
     * Retrieves a specific club by its unique identifier.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClubDTO> getClub(@PathVariable @Positive Long id) {
        try {
            Club club = clubService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Club not found with ID: " + id));

            logger.debug("Retrieved club: {} (ID: {})", club.getName(), id);
            return ResponseEntity.ok(convertToDTO(club));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Club not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving club with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve club: " + e.getMessage());
        }
    }

    /**
     * Retrieves clubs based on search input with name matching.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ClubDTO>> searchClubs(@RequestParam String q) {
        try {
            if (q == null || q.trim().isEmpty()) {
                throw new IllegalArgumentException("Search query cannot be empty");
            }

            List<Club> clubs = clubService.searchClubsByName(q.trim());
            List<ClubDTO> clubDTOs = clubs.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Search for '{}' returned {} clubs", q, clubs.size());
            return ResponseEntity.ok(clubDTOs);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid search query: {}", e.getMessage());
            throw new RuntimeException("Invalid search query: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error searching clubs with query: {}", q, e);
            throw new RuntimeException("Failed to search clubs: " + e.getMessage());
        }
    }

    /**
     * Retrieves all players belonging to a specific club.
     */
    @GetMapping("/{id}/players")
    public ResponseEntity<List<PlayerDTO>> getClubPlayers(@PathVariable @Positive Long id) {
        try {
            Club club = clubService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Club not found with ID: " + id));

            List<Player> players = clubService.getClubPlayers(club);
            List<PlayerDTO> playerDTOs = players.stream()
                    .map(this::convertPlayerToDTO)
                    .toList();

            logger.debug("Retrieved {} players for club: {} (ID: {})",
                    players.size(), club.getName(), id);
            return ResponseEntity.ok(playerDTOs);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Club not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving players for club with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve club players: " + e.getMessage());
        }
    }

    /**
     * Retrieves the total count of clubs in the system.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalClubCount() {
        try {
            long count = clubService.getTotalClubCount();
            logger.debug("Total club count: {}", count);
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            logger.error("Error retrieving club count", e);
            throw new RuntimeException("Failed to retrieve club count: " + e.getMessage());
        }
    }

    /**
     * Retrieves all distinct countries that have clubs.
     */
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getAllCountries() {
        try {
            List<String> countries = clubService.getAllCountries();
            logger.debug("Retrieved {} distinct countries", countries.size());
            return ResponseEntity.ok(countries);

        } catch (Exception e) {
            logger.error("Error retrieving countries", e);
            throw new RuntimeException("Failed to retrieve countries: " + e.getMessage());
        }
    }

    /**
     * Converts a Club entity to a ClubDTO for API responses.
     */
    private ClubDTO convertToDTO(Club club) {
        ClubDTO dto = new ClubDTO();
        dto.setId(club.getId());
        dto.setExternalId(club.getExternalId());
        dto.setName(club.getName());
        dto.setLogoUrl(club.getLogoUrl());
        dto.setFounded(club.getFounded());
        dto.setIsActive(club.getIsActive());
        dto.setShortName(club.getShortName());
        dto.setCity(club.getCity());
        dto.setStadium(club.getStadium());
        dto.setStadiumCapacity(club.getStadiumCapacity());
        dto.setIsNational(club.getIsNational());
        dto.setCreatedAt(club.getCreatedAt());
        dto.setUpdatedAt(club.getUpdatedAt());

        // Set country as CountryDTO instead of string
        if (club.getCountry() != null) {
            dto.setCountry(convertCountryToDTO(club.getCountry()));
        }

        dto.setPlayerCount(club.getPlayerStatistics() != null ? club.getPlayerStatistics().size() : 0);

        return dto;
    }

    /**
     * Converts a Country entity to a CountryDTO for API responses.
     */
    private CountryDTO convertCountryToDTO(Country country) {
        CountryDTO dto = new CountryDTO();
        dto.setId(country.getId());
        dto.setName(country.getName());
        dto.setCode(country.getCode());
        dto.setFlagUrl(country.getFlagUrl());
        dto.setCreatedAt(country.getCreatedAt());
        dto.setUpdatedAt(country.getUpdatedAt());
        return dto;
    }

    /**
     * Converts a Player entity to a PlayerDTO for API responses.
     */
    private PlayerDTO convertPlayerToDTO(Player player) {
        PlayerDTO dto = new PlayerDTO();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setPosition(player.getPosition());
        dto.setAge(player.getAge());
        dto.setNationality(player.getNationality());
        dto.setHeightCm(player.getHeightCm());
        dto.setWeightKg(player.getWeightKg());
        dto.setPhotoUrl(player.getPhotoUrl());
        dto.setCurrentClubId(player.getCurrentClub() != null ? player.getCurrentClub().getId() : null);
        dto.setCurrentClubName(player.getCurrentClub() != null ? player.getCurrentClub().getName() : null);
        return dto;
    }
}

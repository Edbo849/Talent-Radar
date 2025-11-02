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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.club.ClubDTO;
import com.talentradar.dto.club.CountryDTO;
import com.talentradar.dto.club.LeagueDTO;
import com.talentradar.model.club.Club;
import com.talentradar.model.club.Country;
import com.talentradar.model.club.League;
import com.talentradar.service.club.LeagueService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * REST controller for managing football league operations. Provides endpoints
 * for retrieving league information, standings, and associated clubs.
 */
@RestController
@RequestMapping("/api/leagues")
@CrossOrigin(origins = "http://localhost:3000")
public class LeagueController {

    private static final Logger logger = LoggerFactory.getLogger(LeagueController.class);
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private LeagueService leagueService;

    /**
     * Retrieves a paginated list of all leagues.
     */
    @GetMapping
    public ResponseEntity<Page<LeagueDTO>> getAllLeagues(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        try {
            // Validate page size
            if (size > MAX_PAGE_SIZE) {
                size = MAX_PAGE_SIZE;
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<League> leagues = leagueService.getLeaguesPaginated(pageable);
            Page<LeagueDTO> leagueDTOs = leagues.map(this::convertToDTO);

            return ResponseEntity.ok(leagueDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving leagues with pagination", e);
            throw new RuntimeException("Failed to retrieve leagues: " + e.getMessage());
        }
    }

    /**
     * Retrieves a specific league by its unique identifier.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeagueDTO> getLeagueById(@PathVariable @Positive Long id) {
        try {
            League league = leagueService.getLeagueById(id)
                    .orElseThrow(() -> new RuntimeException("League not found with ID: " + id));

            logger.debug("Retrieved league: {} (ID: {})", league.getName(), id);
            return ResponseEntity.ok(convertToDTO(league));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("League not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving league with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve league: " + e.getMessage());
        }
    }

    /**
     * Retrieves a league by its external API identifier.
     */
    @GetMapping("/external/{externalId}")
    public ResponseEntity<LeagueDTO> getLeagueByExternalId(@PathVariable @Positive Integer externalId) {
        try {
            League league = leagueService.getLeagueByExternalId(externalId)
                    .orElseThrow(() -> new RuntimeException("League not found with external ID: " + externalId));

            logger.debug("Retrieved league: {} (External ID: {})", league.getName(), externalId);
            return ResponseEntity.ok(convertToDTO(league));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("League not found with external ID: {}", externalId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving league with external ID: {}", externalId, e);
            throw new RuntimeException("Failed to retrieve league: " + e.getMessage());
        }
    }

    /**
     * Retrieves all leagues for a specific season.
     */
    @GetMapping("/season/{season}")
    public ResponseEntity<List<LeagueDTO>> getLeaguesBySeason(@PathVariable @Positive Integer season) {
        try {
            List<League> leagues = leagueService.getLeaguesBySeason(season);
            List<LeagueDTO> leagueDTOs = leagues.stream()
                    .map(this::convertToDTO)
                    .toList();
            return ResponseEntity.ok(leagueDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving leagues for season: {}", season, e);
            throw new RuntimeException("Failed to retrieve leagues by season: " + e.getMessage());
        }
    }

    /**
     * Retrieves all leagues for a specific country.
     */
    @GetMapping("/country/{countryId}")
    public ResponseEntity<List<LeagueDTO>> getLeaguesByCountry(@PathVariable @Positive Long countryId) {
        try {
            List<League> leagues = leagueService.getLeaguesByCountry(countryId);
            List<LeagueDTO> leagueDTOs = leagues.stream()
                    .map(this::convertToDTO)
                    .toList();

            return ResponseEntity.ok(leagueDTOs);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("Country not found with ID: {}", countryId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving leagues for country ID: {}", countryId, e);
            throw new RuntimeException("Failed to retrieve leagues by country: " + e.getMessage());
        }
    }

    /**
     * Retrieves all leagues of a specific type.
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<LeagueDTO>> getLeaguesByType(@PathVariable String type) {
        try {
            if (type == null || type.trim().isEmpty()) {
                throw new IllegalArgumentException("League type cannot be empty");
            }

            List<League> leagues = leagueService.getLeaguesByType(type.trim());
            List<LeagueDTO> leagueDTOs = leagues.stream()
                    .map(this::convertToDTO)
                    .toList();

            return ResponseEntity.ok(leagueDTOs);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid league type parameter: {}", e.getMessage());
            throw new RuntimeException("Invalid league type: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving leagues for type: {}", type, e);
            throw new RuntimeException("Failed to retrieve leagues by type: " + e.getMessage());
        }
    }

    /**
     * Searches for leagues by name with case-insensitive matching.
     */
    @GetMapping("/search")
    public ResponseEntity<List<LeagueDTO>> searchLeagues(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                throw new IllegalArgumentException("Search query cannot be empty");
            }

            List<League> leagues = leagueService.searchLeagues(query.trim());
            List<LeagueDTO> leagueDTOs = leagues.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Search for '{}' returned {} leagues", query, leagues.size());
            return ResponseEntity.ok(leagueDTOs);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid search query: {}", e.getMessage());
            throw new RuntimeException("Invalid search query: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error searching leagues with query: {}", query, e);
            throw new RuntimeException("Failed to search leagues: " + e.getMessage());
        }
    }

    /**
     * Retrieves the top major football leagues.
     */
    @GetMapping("/top")
    public ResponseEntity<List<LeagueDTO>> getTopLeagues() {
        try {
            List<League> leagues = leagueService.getTopLeagues();
            List<LeagueDTO> leagueDTOs = leagues.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Retrieved {} top leagues", leagues.size());
            return ResponseEntity.ok(leagueDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving top leagues", e);
            throw new RuntimeException("Failed to retrieve top leagues: " + e.getMessage());
        }
    }

    /**
     * Retrieves all leagues for the current season.
     */
    @GetMapping("/current-season")
    public ResponseEntity<List<LeagueDTO>> getCurrentSeasonLeagues() {
        try {
            List<League> leagues = leagueService.getCurrentSeasonLeagues();
            List<LeagueDTO> leagueDTOs = leagues.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Retrieved {} current season leagues", leagues.size());
            return ResponseEntity.ok(leagueDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving current season leagues", e);
            throw new RuntimeException("Failed to retrieve current season leagues: " + e.getMessage());
        }
    }

    /**
     * Retrieves all clubs belonging to a specific league.
     */
    @GetMapping("/{id}/clubs")
    public ResponseEntity<List<ClubDTO>> getLeagueClubs(@PathVariable @Positive Long id) {
        try {
            League league = leagueService.getLeagueById(id)
                    .orElseThrow(() -> new RuntimeException("League not found with ID: " + id));

            List<Club> clubs = leagueService.getLeagueClubs(league);
            List<ClubDTO> clubDTOs = clubs.stream()
                    .map(this::convertClubToDTO)
                    .toList();

            logger.debug("Retrieved {} clubs for league: {} (ID: {})",
                    clubs.size(), league.getName(), id);
            return ResponseEntity.ok(clubDTOs);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("League not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving clubs for league with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve league clubs: " + e.getMessage());
        }
    }

    /**
     * Retrieves all distinct league types in the system.
     */
    @GetMapping("/types")
    public ResponseEntity<List<String>> getAllLeagueTypes() {
        try {
            List<String> types = leagueService.getAllLeagueTypes();
            logger.debug("Retrieved {} distinct league types", types.size());
            return ResponseEntity.ok(types);

        } catch (Exception e) {
            logger.error("Error retrieving league types", e);
            throw new RuntimeException("Failed to retrieve league types: " + e.getMessage());
        }
    }

    /**
     * Retrieves all distinct seasons available in the system.
     */
    @GetMapping("/seasons")
    public ResponseEntity<List<Integer>> getAllSeasons() {
        try {
            List<Integer> seasons = leagueService.getAllSeasons();
            logger.debug("Retrieved {} distinct seasons", seasons.size());
            return ResponseEntity.ok(seasons);

        } catch (Exception e) {
            logger.error("Error retrieving seasons", e);
            throw new RuntimeException("Failed to retrieve seasons: " + e.getMessage());
        }
    }

    /**
     * Creates a new league in the system.
     */
    @PostMapping
    public ResponseEntity<LeagueDTO> createLeague(@Valid @RequestBody LeagueCreateRequest request) {
        try {
            League league = leagueService.createLeague(
                    request.getName(),
                    request.getCountryId(),
                    request.getLogoUrl(),
                    request.getSeason(),
                    request.getType(),
                    request.getExternalId()
            );

            logger.info("Created new league: {} (ID: {})", league.getName(), league.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(league));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid league creation request: {}", e.getMessage());
            throw new RuntimeException("Invalid league data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating league", e);
            throw new RuntimeException("Failed to create league: " + e.getMessage());
        }
    }

    /**
     * Updates an existing league in the system.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeagueDTO> updateLeague(
            @PathVariable @Positive Long id,
            @Valid @RequestBody LeagueUpdateRequest request) {

        try {
            League league = leagueService.updateLeague(
                    id,
                    request.getName(),
                    request.getCountryId(),
                    request.getLogoUrl(),
                    request.getSeason(),
                    request.getType()
            );

            logger.info("Updated league: {} (ID: {})", league.getName(), id);
            return ResponseEntity.ok(convertToDTO(league));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("League not found for update with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error updating league with ID: {}", id, e);
            throw new RuntimeException("Failed to update league: " + e.getMessage());
        }
    }

    /**
     * Deletes a league from the system.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeague(@PathVariable @Positive Long id) {
        try {
            leagueService.deleteLeague(id);
            logger.info("Deleted league with ID: {}", id);
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                logger.warn("League not found for deletion with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if (e.getMessage().contains("Cannot delete")) {
                logger.warn("Cannot delete league with ID: {} - has dependencies", id);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting league with ID: {}", id, e);
            throw new RuntimeException("Failed to delete league: " + e.getMessage());
        }
    }

    /**
     * Retrieves the total count of leagues in the system.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalLeagueCount() {
        try {
            long count = leagueService.getTotalLeagueCount();
            logger.debug("Total league count: {}", count);
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            logger.error("Error retrieving league count", e);
            throw new RuntimeException("Failed to retrieve league count: " + e.getMessage());
        }
    }

    /**
     * Converts a League entity to a LeagueDTO for API responses.
     */
    private LeagueDTO convertToDTO(League league) {
        LeagueDTO dto = new LeagueDTO();
        dto.setId(league.getId());
        dto.setExternalId(league.getExternalId());
        dto.setName(league.getName());
        dto.setLogoUrl(league.getLogoUrl());
        dto.setSeason(league.getSeason());
        dto.setType(league.getType());
        dto.setCreatedAt(league.getCreatedAt());
        dto.setUpdatedAt(league.getUpdatedAt());

        if (league.getCountry() != null) {
            dto.setCountry(convertCountryToDTO(league.getCountry()));
        }

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
     * Converts a Club entity to a ClubDTO for API responses.
     */
    private ClubDTO convertClubToDTO(Club club) {
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
        if (club.getCountry() != null) {
            dto.setCountry(convertCountryToDTO(club.getCountry()));
        }
        dto.setPlayerCount(club.getPlayerStatistics() != null ? club.getPlayerStatistics().size() : 0);

        return dto;
    }

    // Request DTOs
    public static class LeagueCreateRequest {

        private String name;
        private Long countryId;
        private String logoUrl;
        private Integer season;
        private String type;
        private Integer externalId;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getCountryId() {
            return countryId;
        }

        public void setCountryId(Long countryId) {
            this.countryId = countryId;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public void setLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
        }

        public Integer getSeason() {
            return season;
        }

        public void setSeason(Integer season) {
            this.season = season;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getExternalId() {
            return externalId;
        }

        public void setExternalId(Integer externalId) {
            this.externalId = externalId;
        }
    }

    public static class LeagueUpdateRequest {

        private String name;
        private Long countryId;
        private String logoUrl;
        private Integer season;
        private String type;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getCountryId() {
            return countryId;
        }

        public void setCountryId(Long countryId) {
            this.countryId = countryId;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public void setLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
        }

        public Integer getSeason() {
            return season;
        }

        public void setSeason(Integer season) {
            this.season = season;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}

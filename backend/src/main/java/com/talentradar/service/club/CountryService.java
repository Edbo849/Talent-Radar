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
import com.talentradar.model.club.Country;
import com.talentradar.repository.club.CountryRepository;

/**
 * Service layer for managing country-related operations. Provides business
 * logic for country management, validation, and geographical data operations.
 */
@Service
@Transactional
public class CountryService {

    private static final Logger logger = LoggerFactory.getLogger(CountryService.class);

    @Autowired
    private CountryRepository countryRepository;

    /**
     * Retrieves all countries ordered by name.
     */
    @Transactional(readOnly = true)
    public List<Country> getAllCountries() {
        try {
            logger.debug("Retrieving all countries");
            return countryRepository.findAllByOrderByNameAsc();
        } catch (Exception e) {
            logger.error("Error retrieving all countries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve countries", e);
        }
    }

    /**
     * Retrieves countries with pagination support.
     */
    @Transactional(readOnly = true)
    public Page<Country> getCountriesPaginated(Pageable pageable) {
        try {
            logger.debug("Retrieving countries with pagination");
            return countryRepository.findAllByOrderByNameAsc(pageable);
        } catch (Exception e) {
            logger.error("Error retrieving paginated countries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve paginated countries", e);
        }
    }

    /**
     * Retrieves a country by its unique identifier.
     */
    @Transactional(readOnly = true)
    public Optional<Country> getCountryById(Long id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("Country ID cannot be null");
            }
            logger.debug("Finding country by ID: {}", id);
            return countryRepository.findById(id);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid country ID: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding country by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to find country by ID", e);
        }
    }

    /**
     * Retrieves a country by its name.
     */
    @Transactional(readOnly = true)
    public Optional<Country> getCountryByName(String name) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Country name cannot be null or empty");
            }
            logger.debug("Finding country by name: {}", name);
            return countryRepository.findByName(name.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid country name: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding country by name: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find country by name", e);
        }
    }

    /**
     * Retrieves a country by its ISO code.
     */
    @Transactional(readOnly = true)
    public Optional<Country> getCountryByCode(String code) {
        try {
            if (code == null || code.trim().isEmpty()) {
                throw new IllegalArgumentException("Country code cannot be null or empty");
            }
            logger.debug("Finding country by code: {}", code);
            return countryRepository.findByCode(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid country code: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error finding country by code: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find country by code", e);
        }
    }

    /**
     * Searches countries by name containing the specified term.
     */
    @Transactional(readOnly = true)
    public List<Country> searchCountries(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                throw new IllegalArgumentException("Search term cannot be null or empty");
            }
            logger.debug("Searching countries with term: {}", searchTerm);
            return countryRepository.findByNameContainingIgnoreCase(searchTerm.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid search term: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching countries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search countries", e);
        }
    }

    /**
     * Creates a new country with the specified details.
     */
    public Country createCountry(String name, String code, String flagUrl) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Country name is required");
            }
            if (code == null || code.trim().isEmpty()) {
                throw new IllegalArgumentException("Country code is required");
            }

            String trimmedName = name.trim();
            String trimmedCode = code.trim().toUpperCase();

            // Check if country already exists
            if (countryRepository.findByName(trimmedName).isPresent()) {
                throw new IllegalArgumentException("Country with name '" + trimmedName + "' already exists");
            }

            if (countryRepository.findByCode(trimmedCode).isPresent()) {
                throw new IllegalArgumentException("Country with code '" + trimmedCode + "' already exists");
            }

            Country country = new Country();
            country.setName(trimmedName);
            country.setCode(trimmedCode);
            country.setFlagUrl(flagUrl);

            Country savedCountry = countryRepository.save(country);
            logger.info("Created new country: {} with code: {}", trimmedName, trimmedCode);
            return savedCountry;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid country creation parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating country: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create country", e);
        }
    }

    /**
     * Updates an existing country with new information.
     */
    public Country updateCountry(Long id, String name, String code, String flagUrl) {
        try {
            Country country = countryRepository.findById(id)
                    .orElseThrow(() -> new CountryNotFoundException("Country not found with ID: " + id));

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Country name is required");
            }
            if (code == null || code.trim().isEmpty()) {
                throw new IllegalArgumentException("Country code is required");
            }

            String trimmedName = name.trim();
            String trimmedCode = code.trim().toUpperCase();

            // Check for duplicates if name or code is being changed
            if (!country.getName().equals(trimmedName)) {
                Optional<Country> existingByName = countryRepository.findByName(trimmedName);
                if (existingByName.isPresent() && !existingByName.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Country with name '" + trimmedName + "' already exists");
                }
            }

            if (!country.getCode().equals(trimmedCode)) {
                Optional<Country> existingByCode = countryRepository.findByCode(trimmedCode);
                if (existingByCode.isPresent() && !existingByCode.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Country with code '" + trimmedCode + "' already exists");
                }
            }

            country.setName(trimmedName);
            country.setCode(trimmedCode);
            country.setFlagUrl(flagUrl);

            Country updatedCountry = countryRepository.save(country);
            logger.info("Updated country with ID: {}", id);
            return updatedCountry;
        } catch (CountryNotFoundException e) {
            logger.error("Country not found for update: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid country update parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating country: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update country", e);
        }
    }

    /**
     * Deletes a country by its unique identifier.
     */
    public void deleteCountry(Long id) {
        try {
            Country country = countryRepository.findById(id)
                    .orElseThrow(() -> new CountryNotFoundException("Country not found with ID: " + id));

            // Check if country is being used by leagues
            long leagueCount = countryRepository.countLeaguesByCountry(country);
            if (leagueCount > 0) {
                throw new IllegalStateException("Cannot delete country: " + leagueCount + " leagues are associated with it");
            }

            countryRepository.delete(country);
            logger.info("Deleted country with ID: {}", id);
        } catch (CountryNotFoundException e) {
            logger.error("Country not found for deletion: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Cannot delete country: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting country: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete country", e);
        }
    }

    /**
     * Retrieves the total number of countries in the system.
     */
    @Transactional(readOnly = true)
    public long getTotalCountryCount() {
        try {
            logger.debug("Counting total countries");
            return countryRepository.count();
        } catch (Exception e) {
            logger.error("Error counting total countries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to count countries", e);
        }
    }

    /**
     * Retrieves countries that have associated leagues.
     */
    @Transactional(readOnly = true)
    public List<Country> getCountriesWithLeagues() {
        try {
            logger.debug("Retrieving countries with leagues");
            return countryRepository.findCountriesWithLeagues();
        } catch (Exception e) {
            logger.error("Error retrieving countries with leagues: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve countries with leagues", e);
        }
    }

    /**
     * Retrieves all country codes in the system.
     */
    @Transactional(readOnly = true)
    public List<String> getAllCountryCodes() {
        try {
            logger.debug("Retrieving all country codes");
            return countryRepository.findAllCountryCodes();
        } catch (Exception e) {
            logger.error("Error retrieving country codes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve country codes", e);
        }
    }
}

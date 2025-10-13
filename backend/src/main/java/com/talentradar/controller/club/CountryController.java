package com.talentradar.controller.club;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.club.CountryDTO;
import com.talentradar.model.club.Country;
import com.talentradar.service.club.CountryService;

@RestController
@RequestMapping("/api/countries")
@CrossOrigin(origins = "http://localhost:3000")
public class CountryController {

    private static final Logger logger = LoggerFactory.getLogger(CountryController.class);

    @Autowired
    private CountryService countryService;

    /**
     * Get all countries with their flags
     */
    @GetMapping
    public ResponseEntity<List<CountryDTO>> getAllCountries() {
        try {
            List<Country> countries = countryService.getAllCountries();
            List<CountryDTO> countryDTOs = countries.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.debug("Retrieved {} countries with flags", countryDTOs.size());
            return ResponseEntity.ok(countryDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving countries", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get country flag by country name
     */
    @GetMapping("/flag/{countryName}")
    public ResponseEntity<String> getCountryFlag(@PathVariable String countryName) {
        try {
            Optional<Country> country = countryService.getCountryByName(countryName);
            if (country.isPresent() && country.get().getFlagUrl() != null) {
                return ResponseEntity.ok(country.get().getFlagUrl());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving flag for country: {}", countryName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Convert Country entity to CountryDTO
     */
    private CountryDTO convertToDTO(Country country) {
        CountryDTO dto = new CountryDTO();
        dto.setId(country.getId());
        dto.setName(country.getName());
        dto.setCode(country.getCode());
        dto.setFlagUrl(country.getFlagUrl());
        dto.setCreatedAt(country.getCreatedAt());
        dto.setUpdatedAt(country.getUpdatedAt());
        return dto;
    }
}

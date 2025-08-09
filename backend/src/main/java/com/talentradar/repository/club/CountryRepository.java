package com.talentradar.repository.club;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.club.Country;

/**
 * Repository interface for managing Country entities. Provides data access
 * operations for country management, code-based lookups, geographical data
 * operations, and league associations.
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    /* Basic finder methods */
    // Find country by name (used in your service)
    Optional<Country> findByName(String name);

    // Find country by ISO code
    Optional<Country> findByCode(String code);

    /* Search methods */
    // Find countries by name containing text (case-insensitive)
    List<Country> findByNameContainingIgnoreCase(String name);

    // For searching by name (non-paginated)
    List<Country> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    // For searching by name (paginated, if needed)
    Page<Country> findByNameContainingIgnoreCaseOrderByNameAsc(String name, Pageable pageable);

    /* Ordered finder methods */
    // Get all countries ordered by name
    List<Country> findAllByOrderByNameAsc();

    // For paginated results
    Page<Country> findAllByOrderByNameAsc(Pageable pageable);

    /* Existence check methods */
    // For checking duplicates
    boolean existsByNameIgnoreCase(String name);

    // For checking duplicates
    boolean existsByCodeIgnoreCase(String code);

    /* Statistical and aggregation queries */
    // For counting leagues by country
    @Query("SELECT COUNT(l) FROM League l WHERE l.country = :country")
    long countLeaguesByCountry(@Param("country") Country country);

    // For countries with leagues
    @Query("SELECT DISTINCT l.country FROM League l WHERE l.country IS NOT NULL")
    List<Country> findCountriesWithLeagues();

    // For all country codes
    @Query("SELECT c.code FROM Country c")
    List<String> findAllCountryCodes();
}

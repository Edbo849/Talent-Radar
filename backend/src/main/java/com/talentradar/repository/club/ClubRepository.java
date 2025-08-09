package com.talentradar.repository.club;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.club.Club;
import com.talentradar.model.club.League;

/**
 * Repository interface for managing Club entities. Provides data access
 * operations for club management, search functionality, country-based
 * filtering, and statistical queries.
 */
@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    /* Basic finder methods */
    // Find club by external API ID (from API-Football)
    Optional<Club> findByExternalId(Integer externalId);

    /* Search methods */
    // Search clubs by name (case-insensitive)
    List<Club> findByNameContainingIgnoreCase(String name);

    // Pagination support for search
    Page<Club> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /* Status-based finder methods */
    // Find only active clubs
    List<Club> findByIsActiveTrue();

    // Active clubs with pagination
    Page<Club> findByIsActiveTrue(Pageable pageable);

    /* Country-based finder methods */
    // Find clubs by country
    @Query("SELECT c FROM Club c WHERE c.country.name = :countryName")
    List<Club> findByCountry(@Param("countryName") String countryName);

    // Find clubs by country and active status
    @Query("SELECT c FROM Club c WHERE c.country.name = :countryName AND c.isActive = true")
    List<Club> findByCountryAndIsActiveTrue(@Param("countryName") String countryName);

    // Country-based queries with pagination
    @Query("SELECT c FROM Club c WHERE c.country.name = :countryName AND c.isActive = true")
    Page<Club> findByCountryAndIsActiveTrue(@Param("countryName") String countryName, Pageable pageable);

    /* League-based finder methods */
    // Find clubs by current league (through statistics)
    @Query("SELECT DISTINCT ps.club FROM PlayerStatistic ps WHERE ps.league = :league AND ps.season = (SELECT MAX(ps2.season) FROM PlayerStatistic ps2 WHERE ps2.club = ps.club)")
    List<Club> findByCurrentLeague(@Param("league") League league);

    /* Statistical and aggregation methods */
    // Statistical methods
    long countByIsActiveTrue();

    // Get distinct countries
    @Query("SELECT DISTINCT c.country.name FROM Club c WHERE c.country IS NOT NULL ORDER BY c.country.name")
    List<String> findDistinctCountries();
}

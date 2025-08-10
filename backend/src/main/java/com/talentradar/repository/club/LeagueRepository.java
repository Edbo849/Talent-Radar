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
import com.talentradar.model.club.League;

/**
 * Repository interface for managing League entities. Provides data access
 * operations for league management, season-based queries, competition-related
 * functionality, and statistical operations.
 */
@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    /* Basic finder methods */
    // Find league by external API ID (from API-Football)
    Optional<League> findByExternalId(Integer externalId);

    // Find specific league by external ID and season (used in your service)
    Optional<League> findByExternalIdAndSeason(Integer externalId, Integer season);

    /* Search methods */
    // Search leagues by name (case-insensitive)
    List<League> findByNameContainingIgnoreCase(String name);

    // Search leagues by name with ordering
    List<League> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    /* Season-based finder methods */
    // Find leagues by season
    List<League> findBySeason(Integer season);

    // Find leagues by season ordered by name
    List<League> findBySeasonOrderByNameAsc(Integer season);

    /* Type-based finder methods */
    // Find leagues by type (e.g., "League", "Cup")
    List<League> findByType(String type);

    // Find leagues by type ordered by name
    List<League> findByTypeOrderByNameAsc(String type);

    /* Country-based finder methods */
    // Find leagues by country name
    @Query("SELECT l FROM League l WHERE l.country.name = :countryName")
    List<League> findByCountryName(@Param("countryName") String countryName);

    // Find leagues by country ordered by name
    List<League> findByCountryOrderByNameAsc(Country country);

    /* Ordered finder methods */
    // Find all leagues ordered by name
    List<League> findAllByOrderByNameAsc();

    // Find all leagues ordered by name (paginated)
    Page<League> findAllByOrderByNameAsc(Pageable pageable);

    // Find leagues by external IDs in list
    List<League> findByExternalIdInOrderByNameAsc(List<Integer> externalIds);

    /* Statistical and aggregation queries */
    // Count clubs by league
    @Query("SELECT COUNT(DISTINCT ps.club) FROM PlayerStatistic ps WHERE ps.league = :league")
    long countClubsByLeague(@Param("league") League league);

    // Get distinct types
    @Query("SELECT DISTINCT l.type FROM League l WHERE l.type IS NOT NULL ORDER BY l.type")
    List<String> findDistinctTypes();

    // Get distinct seasons ordered by desc
    @Query("SELECT DISTINCT l.season FROM League l WHERE l.season IS NOT NULL ORDER BY l.season DESC")
    List<Integer> findDistinctSeasonsOrderByDesc();
}

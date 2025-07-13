package com.talentradar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.talentradar.model.League;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    // Find league by external API ID (from API-Football)
    Optional<League> findByExternalId(Integer externalId);

    // Search leagues by name (case-insensitive)
    List<League> findByNameContainingIgnoreCase(String name);

    // Find leagues by season
    List<League> findBySeason(Integer season);

    // Find specific league by external ID and season (used in your service)
    Optional<League> findByExternalIdAndSeason(Integer externalId, Integer season);

    // Find leagues by type (e.g., "League", "Cup")
    List<League> findByType(String type);

    // Find leagues by country name
    List<League> findByCountryName(String countryName);
}

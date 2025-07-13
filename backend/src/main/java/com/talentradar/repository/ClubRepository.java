package com.talentradar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.talentradar.model.Club;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    // Find club by external API ID (from API-Football)
    Optional<Club> findByExternalId(Integer externalId);

    // Search clubs by name (case-insensitive)
    List<Club> findByNameContainingIgnoreCase(String name);

    // Find clubs by country
    List<Club> findByCountry(String country);

    // Find only active clubs
    List<Club> findByIsActiveTrue();

    // Find clubs by country and active status
    List<Club> findByCountryAndIsActiveTrue(String country);
}

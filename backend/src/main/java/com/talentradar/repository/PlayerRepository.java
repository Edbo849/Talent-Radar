package com.talentradar.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.talentradar.model.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    // Find player by external API ID (from API-Football)
    Optional<Player> findByExternalId(Integer externalId);

    // Search players by name (case-insensitive)
    List<Player> findByNameContainingIgnoreCase(String name);

    // Find players by nationality
    List<Player> findByNationality(String nationality);

    // Find all U21 players (custom query)
    @Query("SELECT p FROM Player p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) <= 21")
    List<Player> findU21Players();

    // Count U21 players (used in your service)
    @Query("SELECT COUNT(p) FROM Player p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) <= 21")
    long countByU21Eligible();

    // Find active players born after a certain date
    @Query("SELECT p FROM Player p WHERE p.dateOfBirth >= :minDate AND p.isActive = true")
    List<Player> findByDateOfBirthAfterAndIsActiveTrue(LocalDate minDate);

    // Find players by birth country
    List<Player> findByBirthCountry(String birthCountry);

    // Find active players only
    List<Player> findByIsActiveTrue();

}

package com.talentradar.repository;

import com.talentradar.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    // Find country by name (used in your service)
    Optional<Country> findByName(String name);

    // Find country by ISO code
    Optional<Country> findByCode(String code);

    // Find countries by name containing text (case-insensitive)
    List<Country> findByNameContainingIgnoreCase(String name);

    // Get all countries ordered by name
    List<Country> findAllByOrderByNameAsc();
}

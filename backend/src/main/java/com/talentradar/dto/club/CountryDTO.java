package com.talentradar.dto.club;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Country entities. Used to transfer country
 * information between layers including name, ISO code, flag URL, and
 * timestamps.
 */
public class CountryDTO {

    // Primary identifier
    private Long id;

    // Country name (required, max 100 characters)
    private String name;

    // ISO country code (max 3 characters)
    private String code;

    // URL to country flag image
    private String flagUrl;

    // Timestamp when country was created
    private LocalDateTime createdAt;

    // Timestamp when country was last updated
    private LocalDateTime updatedAt;

    // Constructors
    public CountryDTO() {
    }

    public CountryDTO(Long id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public void setFlagUrl(String flagUrl) {
        this.flagUrl = flagUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

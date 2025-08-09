package com.talentradar.dto.player;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating new Player entities. Used to transfer
 * player creation data from client to server with validation constraints
 * matching the model requirements.
 */
public class PlayerCreateDTO {

    // Required fields matching model constraints
    // Player name (required, max 100 characters)
    @NotBlank(message = "Player name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    // Optional name components
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    // Date of birth (required)
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    // Birth information
    @Size(max = 100, message = "Birth place must not exceed 100 characters")
    private String birthPlace;

    @Size(max = 50, message = "Birth country must not exceed 50 characters")
    private String birthCountry;

    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;

    // Physical attributes with validation matching model
    @Min(value = 150, message = "Height must be at least 150cm")
    @Max(value = 220, message = "Height must not exceed 220cm")
    private Integer heightCm;

    @Min(value = 40, message = "Weight must be at least 40kg")
    @Max(value = 120, message = "Weight must not exceed 120kg")
    private Integer weightKg;

    // Position and playing style
    @Size(max = 50, message = "Position must not exceed 50 characters")
    private String position;

    private String preferredFoot;
    private String photoUrl;

    // Additional profile information
    private String bio;
    private Integer jerseyNumber;
    private Long marketValue;
    private LocalDate contractExpires;
    private Long currentClubId;

    // Constructors
    public PlayerCreateDTO() {
    }

    public PlayerCreateDTO(String name, LocalDate dateOfBirth) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getBirthCountry() {
        return birthCountry;
    }

    public void setBirthCountry(String birthCountry) {
        this.birthCountry = birthCountry;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Integer heightCm) {
        this.heightCm = heightCm;
    }

    public Integer getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Integer weightKg) {
        this.weightKg = weightKg;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPreferredFoot() {
        return preferredFoot;
    }

    public void setPreferredFoot(String preferredFoot) {
        this.preferredFoot = preferredFoot;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public Long getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Long marketValue) {
        this.marketValue = marketValue;
    }

    public LocalDate getContractExpires() {
        return contractExpires;
    }

    public void setContractExpires(LocalDate contractExpires) {
        this.contractExpires = contractExpires;
    }

    public Long getCurrentClubId() {
        return currentClubId;
    }

    public void setCurrentClubId(Long currentClubId) {
        this.currentClubId = currentClubId;
    }

    @Override
    public String toString() {
        return "PlayerCreateDTO{"
                + "name='" + name + '\''
                + ", dateOfBirth=" + dateOfBirth
                + ", position='" + position + '\''
                + ", currentClubId=" + currentClubId
                + '}';
    }
}

package com.talentradar.dto.player;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for updating existing Player entities. Used to transfer
 * player update data from client to server with validation constraints matching
 * the model requirements.
 */
public class PlayerUpdateDTO {

    // Optional fields for updating - all validation matches model constraints
    // Name fields
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    // Birth information
    @Size(max = 100, message = "Birth place must not exceed 100 characters")
    private String birthPlace;

    @Size(max = 50, message = "Birth country must not exceed 50 characters")
    private String birthCountry;

    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;

    // Physical attributes
    @Min(value = 150, message = "Height must be at least 150cm")
    @Max(value = 220, message = "Height must not exceed 220cm")
    private Integer heightCm;

    @Min(value = 40, message = "Weight must be at least 40kg")
    @Max(value = 120, message = "Weight must not exceed 120kg")
    private Integer weightKg;

    // Playing information
    @Size(max = 50, message = "Position must not exceed 50 characters")
    private String position;

    private String photoUrl;

    // Profile information
    private Integer jerseyNumber;
    private Long currentClubId;
    private Boolean isActive;

    // Constructors
    public PlayerUpdateDTO() {
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public Long getCurrentClubId() {
        return currentClubId;
    }

    public void setCurrentClubId(Long currentClubId) {
        this.currentClubId = currentClubId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "PlayerUpdateDTO{"
                + "name='" + name + '\''
                + ", position='" + position + '\''
                + ", currentClubId=" + currentClubId
                + '}';
    }
}

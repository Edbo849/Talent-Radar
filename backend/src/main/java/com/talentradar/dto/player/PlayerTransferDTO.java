package com.talentradar.dto.player;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PlayerTransferDTO {

    private Long id;
    private Long playerId;
    private String fromClub;
    private String toClub;
    private String fromClubLogoUrl;
    private String toClubLogoUrl;
    private LocalDate transferDate;
    private String transferType;
    private LocalDateTime createdAt;

    // Constructors
    public PlayerTransferDTO() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getFromClub() {
        return fromClub;
    }

    public void setFromClub(String fromClub) {
        this.fromClub = fromClub;
    }

    public String getToClub() {
        return toClub;
    }

    public void setToClub(String toClub) {
        this.toClub = toClub;
    }

    public String getFromClubLogoUrl() {
        return fromClubLogoUrl;
    }

    public void setFromClubLogoUrl(String fromClubLogoUrl) {
        this.fromClubLogoUrl = fromClubLogoUrl;
    }

    public String getToClubLogoUrl() {
        return toClubLogoUrl;
    }

    public void setToClubLogoUrl(String toClubLogoUrl) {
        this.toClubLogoUrl = toClubLogoUrl;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDate transferDate) {
        this.transferDate = transferDate;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

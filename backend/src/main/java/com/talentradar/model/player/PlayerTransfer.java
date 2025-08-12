package com.talentradar.model.player;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.talentradar.model.club.Club;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing player transfer records between clubs. Tracks transfer
 * details including source and destination clubs, transfer dates, fees, and
 * transfer types (loan, permanent, free transfer).
 */
@Entity
@Table(name = "player_transfers")
public class PlayerTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    @NotNull(message = "Player is required")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_from_id")
    private Club clubFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_to_id")
    private Club clubTo;

    @Column(name = "transfer_date", nullable = false)
    @NotNull(message = "Transfer date is required")
    private LocalDate transferDate;

    @Column(name = "transfer_type", length = 20)
    private String transferType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PlayerTransfer() {
    }

    public PlayerTransfer(Player player, Club clubFrom, Club clubTo, LocalDate transferDate, String transferType) {
        this.player = player;
        this.clubFrom = clubFrom;
        this.clubTo = clubTo;
        this.transferDate = transferDate;
        this.transferType = transferType;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Club getClubFrom() {
        return clubFrom;
    }

    public void setClubFrom(Club clubFrom) {
        this.clubFrom = clubFrom;
    }

    public Club getClubTo() {
        return clubTo;
    }

    public void setClubTo(Club clubTo) {
        this.clubTo = clubTo;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayerTransfer that = (PlayerTransfer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PlayerTransfer{"
                + "id=" + id
                + ", transferDate=" + transferDate
                + ", transferType='" + transferType + '\''
                + '}';
    }
}

package com.talentradar.repository.player;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.club.Club;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerTransfer;

/**
 * Repository interface for managing PlayerTransfer entities. Provides data
 * access operations for transfer tracking, market analysis, club activity
 * monitoring, and financial analytics.
 */
@Repository
public interface PlayerTransferRepository extends JpaRepository<PlayerTransfer, Long> {

    /* Basic transfer finder methods */
    // Find all transfers for a specific player
    List<PlayerTransfer> findByPlayer(Player player);

    // Find all transfers for a player ordered by transfer date
    List<PlayerTransfer> findByPlayerOrderByTransferDateDesc(Player player);

    // Find transfer by player, date, and clubs to avoid duplicates.
    @Query("SELECT pt FROM PlayerTransfer pt WHERE pt.player = :player AND pt.transferDate = :transferDate AND pt.clubFrom = :clubFrom AND pt.clubTo = :clubTo")
    Optional<PlayerTransfer> findByPlayerAndTransferDateAndClubFromAndClubTo(
            @Param("player") Player player,
            @Param("transferDate") LocalDate transferDate,
            @Param("clubFrom") Club clubFrom,
            @Param("clubTo") Club clubTo
    );

    /* Club-based finder methods */
    // Find transfers from a specific club
    List<PlayerTransfer> findByClubFrom(Club clubFrom);

    // Find transfers to a specific club
    List<PlayerTransfer> findByClubTo(Club clubTo);

    // Find transfers involving a specific club (either from or to)
    @Query("SELECT pt FROM PlayerTransfer pt WHERE pt.clubFrom = :club OR pt.clubTo = :club")
    List<PlayerTransfer> findByClubFromOrClubTo(@Param("club") Club club);

    // Find transfers between two specific clubs
    @Query("SELECT pt FROM PlayerTransfer pt WHERE pt.clubFrom = :clubFrom AND pt.clubTo = :clubTo")
    List<PlayerTransfer> findByClubFromAndClubTo(@Param("clubFrom") Club clubFrom, @Param("clubTo") Club clubTo);

    /* Transfer type finder methods */
    // Find transfers by type
    List<PlayerTransfer> findByTransferType(String transferType);

    // Find loan transfers
    @Query("SELECT pt FROM PlayerTransfer pt WHERE pt.transferType = 'Loan'")
    List<PlayerTransfer> findLoanTransfers();

    // Find permanent transfers
    @Query("SELECT pt FROM PlayerTransfer pt WHERE pt.transferType = 'Permanent' OR pt.transferType IS NULL")
    List<PlayerTransfer> findPermanentTransfers();

    /* Time-based finder methods */
    // Find transfers between specific dates
    @Query("SELECT pt FROM PlayerTransfer pt WHERE pt.transferDate >= :startDate AND pt.transferDate <= :endDate")
    List<PlayerTransfer> findByTransferDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find transfers in a specific year
    @Query("SELECT pt FROM PlayerTransfer pt WHERE YEAR(pt.transferDate) = :year")
    List<PlayerTransfer> findByTransferYear(@Param("year") int year);

    // Find recent transfers (within specified days)
    @Query("SELECT pt FROM PlayerTransfer pt WHERE pt.transferDate >= :date ORDER BY pt.transferDate DESC")
    List<PlayerTransfer> findRecentTransfers(@Param("date") LocalDate date);

    // Find transfer window activity (summer/winter)
    @Query("SELECT pt FROM PlayerTransfer pt WHERE MONTH(pt.transferDate) IN (6, 7, 8) OR MONTH(pt.transferDate) IN (1, 2)")
    List<PlayerTransfer> findTransferWindowActivity();

    /* Count methods */
    // Count transfers for a player
    long countByPlayer(Player player);

    // Count transfers from a club
    long countByClubFrom(Club clubFrom);

    // Count transfers to a club
    long countByClubTo(Club clubTo);

    /* Market activity analysis */
    // Find most active transfer periods
    @Query("SELECT YEAR(pt.transferDate), MONTH(pt.transferDate), COUNT(pt) FROM PlayerTransfer pt GROUP BY YEAR(pt.transferDate), MONTH(pt.transferDate) ORDER BY COUNT(pt) DESC")
    List<Object[]> findMostActiveTransferPeriods();
}

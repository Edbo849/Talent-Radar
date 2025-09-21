package com.talentradar.service.player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.PlayerNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerView;
import com.talentradar.model.user.User;
import com.talentradar.repository.player.PlayerRepository;
import com.talentradar.repository.player.PlayerViewRepository;

/**
 * Service responsible for managing player view tracking and analytics. Handles
 * view recording, statistics calculation, and trending analysis.
 */
@Service
@Transactional
public class PlayerViewService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerViewService.class);

    @Autowired
    private PlayerViewRepository playerViewRepository;

    @Autowired
    private PlayerRepository playerRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void recordView(Player player, User user, String ipAddress, String userAgent, String referrerUrl) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }

            PlayerView view = new PlayerView(player, user, ipAddress);
            view.setUserAgent(userAgent);
            view.setReferrerUrl(referrerUrl);

            playerViewRepository.save(view);

            // Update player view counts
            updatePlayerViewCounts(player);

            logger.debug("Recorded view for player {} by user {}",
                    player.getName(), user != null ? user.getUsername() : "anonymous");

        } catch (PlayerNotFoundException e) {
            logger.error("Error recording view: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error recording view for player {}: {}",
                    player != null ? player.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to record view", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getViewStatistics(Player player) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }

            logger.debug("Calculating view statistics for player: {}", player.getName());

            Map<String, Object> stats = new HashMap<>();

            // Total views
            Long totalViews = playerViewRepository.countByPlayer(player);
            stats.put("totalViews", totalViews);

            // Unique viewers (users + unique IPs for anonymous)
            Long uniqueUserViews = playerViewRepository.countDistinctUsersByPlayerId(player.getId());
            Long uniqueAnonymousViews = playerViewRepository.countDistinctAnonymousIpsByPlayerId(player.getId());
            stats.put("uniqueViewers", uniqueUserViews + uniqueAnonymousViews);

            // Views in last 24 hours
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            Long viewsLast24h = playerViewRepository.countByPlayerAndViewedAtAfter(player, yesterday);
            stats.put("viewsLast24Hours", viewsLast24h);

            // Views in last 7 days
            LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
            Long viewsLast7Days = playerViewRepository.countByPlayerAndViewedAtAfter(player, lastWeek);
            stats.put("viewsLast7Days", viewsLast7Days);

            // Views in last 30 days
            LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
            Long viewsLast30Days = playerViewRepository.countByPlayerAndViewedAtAfter(player, lastMonth);
            stats.put("viewsLast30Days", viewsLast30Days);

            // Daily view breakdown for last 7 days
            List<Object[]> dailyViews = playerViewRepository.findDailyViewsForPlayerLastWeek(player, lastWeek);
            stats.put("dailyViewsLastWeek", dailyViews);

            // Top viewing countries (if we have geolocation data)
            List<Object[]> topCountries = playerViewRepository.findTopViewingCountriesForPlayer(player.getId());
            stats.put("topViewingCountries", topCountries);

            logger.info("Calculated view statistics for player {}: {} total views, {} unique viewers",
                    player.getName(), totalViews, uniqueUserViews + uniqueAnonymousViews);

            return stats;

        } catch (PlayerNotFoundException e) {
            logger.error("Error getting view statistics: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error calculating view statistics for player {}: {}",
                    player != null ? player.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to get view statistics", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getViewHistoryForUser(User user) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            logger.debug("Retrieving view history for user: {}", user.getUsername());

            Map<String, Object> history = new HashMap<>();

            // Recent views (last 50)
            List<PlayerView> recentViews = playerViewRepository.findRecentViewsByUser(user.getId(), 50);
            history.put("recentViews", recentViews);

            // Total players viewed
            Long totalPlayersViewed = playerViewRepository.countDistinctPlayersByUserId(user.getId());
            history.put("totalPlayersViewed", totalPlayersViewed);

            // Views in last 24 hours
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            Long viewsLast24h = playerViewRepository.countByUserAndViewedAtAfter(user, yesterday);
            history.put("viewsLast24Hours", viewsLast24h);

            // Views in last 7 days
            LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
            Long viewsLast7Days = playerViewRepository.countByUserAndViewedAtAfter(user, lastWeek);
            history.put("viewsLast7Days", viewsLast7Days);

            // Most viewed players by this user
            List<Object[]> mostViewedPlayers = playerViewRepository.findMostViewedPlayersByUser(user.getId());
            history.put("mostViewedPlayers", mostViewedPlayers);

            // Daily view activity for last 30 days
            List<Object[]> dailyActivity = playerViewRepository.findDailyViewActivityForUser(user.getId());
            history.put("dailyActivity", dailyActivity);

            logger.info("Retrieved view history for user {}: {} total players viewed, {} recent views",
                    user.getUsername(), totalPlayersViewed, recentViews.size());

            return history;

        } catch (UserNotFoundException e) {
            logger.error("Error getting view history: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving view history for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to get view history", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Player> getTrendingPlayers(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            logger.debug("Calculating trending players on-the-fly");

            // Get players with most views in the last 7 days
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            Page<Player> trendingPlayers = playerRepository.findTrendingPlayersByWeeklyViews(oneWeekAgo, pageable);

            logger.info("Calculated {} trending players", trendingPlayers.getNumberOfElements());

            return trendingPlayers;

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for getting trending players: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error calculating trending players: {}", e.getMessage());
            throw new RuntimeException("Failed to get trending players", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPlayerViewAnalytics(Player player) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }

            Map<String, Object> analytics = new HashMap<>();

            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);

            analytics.put("totalViews", playerViewRepository.countByPlayer(player));
            analytics.put("weeklyViews", playerViewRepository.countByPlayerAndCreatedAtAfter(player, oneWeekAgo));
            analytics.put("monthlyViews", playerViewRepository.countByPlayerAndCreatedAtAfter(player, oneMonthAgo));
            analytics.put("uniqueViewers", playerViewRepository.countDistinctUsersByPlayer(player));
            analytics.put("anonymousViews", playerViewRepository.countByPlayerAndUserIsNull(player));

            // Daily view breakdown for the last 7 days
            Map<String, Long> dailyViews = new HashMap<>();
            for (int i = 0; i < 7; i++) {
                LocalDateTime dayStart = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime dayEnd = dayStart.plusDays(1);
                Long views = playerViewRepository.countByPlayerAndCreatedAtBetween(player, dayStart, dayEnd);
                dailyViews.put(dayStart.toLocalDate().toString(), views);
            }
            analytics.put("dailyViews", dailyViews);

            logger.info("Generated view analytics for player {}", player.getName());
            return analytics;

        } catch (PlayerNotFoundException e) {
            logger.error("Error getting player view analytics: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error generating view analytics for player {}: {}",
                    player != null ? player.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to get player view analytics", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<PlayerView> getRecentViews(Player player, int limit) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be positive");
            }

            return playerViewRepository.findByPlayerOrderByCreatedAtDesc(player,
                    PageRequest.of(0, limit)).getContent();

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting recent views: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving recent views for player {}: {}",
                    player != null ? player.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to get recent views", e);
        }
    }

    /**
     * Retrieves recent views for a specific player.
     */
    @Transactional(readOnly = true)
    public List<PlayerView> getRecentViewsForPlayer(Player player, int limit) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be positive");
            }

            Pageable pageable = PageRequest.of(0, limit);
            Page<PlayerView> viewsPage = playerViewRepository.findByPlayerOrderByCreatedAtDesc(player, pageable);

            logger.debug("Retrieved {} recent views for player: {}", viewsPage.getNumberOfElements(), player.getName());
            return viewsPage.getContent();

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting recent views for player: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving recent views for player {}: {}",
                    player != null ? player.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to get recent views for player", e);
        }
    }

    /**
     * Retrieves view history for a specific user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<PlayerView> getUserViewHistory(User user, Pageable pageable) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            Page<PlayerView> viewHistory = playerViewRepository.findByUserOrderByCreatedAtDesc(user, pageable);

            logger.debug("Retrieved {} view history entries for user: {}",
                    viewHistory.getNumberOfElements(), user.getUsername());
            return viewHistory;

        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting user view history: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving view history for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to get user view history", e);
        }
    }

    /**
     * Retrieves detailed view logs for a specific player with pagination.
     */
    @Transactional(readOnly = true)
    public Page<PlayerView> getPlayerViewLogs(Player player, Pageable pageable) {
        try {
            if (player == null) {
                throw new PlayerNotFoundException("Player cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            Page<PlayerView> viewLogs = playerViewRepository.findByPlayerOrderByCreatedAtDesc(player, pageable);

            logger.debug("Retrieved {} view log entries for player: {}",
                    viewLogs.getNumberOfElements(), player.getName());
            return viewLogs;

        } catch (PlayerNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting player view logs: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving view logs for player {}: {}",
                    player != null ? player.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to get player view logs", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private void updatePlayerViewCounts(Player player) {
        try {
            // Update total views
            Long totalViews = playerViewRepository.countByPlayer(player);
            player.setTotalViews(totalViews.intValue());

            // Update weekly views
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            Long weeklyViews = playerViewRepository.countByPlayerAndCreatedAtAfter(player, oneWeekAgo);
            player.setWeeklyViews(weeklyViews.intValue());

            // Update monthly views
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusDays(30);
            Long monthlyViews = playerViewRepository.countByPlayerAndCreatedAtAfter(player, oneMonthAgo);
            player.setMonthlyViews(monthlyViews.intValue());

            // Calculate trending score (weighted formula)
            BigDecimal trendingScore = calculateTrendingScore(weeklyViews, monthlyViews, totalViews);
            player.setTrendingScore(trendingScore.doubleValue());

            playerRepository.save(player);

            logger.debug("Updated view counts for player {}: {} total, {} weekly, {} monthly",
                    player.getName(), totalViews, weeklyViews, monthlyViews);

        } catch (Exception e) {
            logger.error("Error updating player view counts for player {}: {}",
                    player.getName(), e.getMessage());
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private BigDecimal calculateTrendingScore(Long weeklyViews, Long monthlyViews, Long totalViews) {
        try {
            // Formula: (weekly_views * 3) + (monthly_views * 1.5) + (total_views * 0.1)
            BigDecimal score = BigDecimal.valueOf(weeklyViews * 3)
                    .add(BigDecimal.valueOf(monthlyViews * 1.5))
                    .add(BigDecimal.valueOf(totalViews * 0.1));

            return score.setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            logger.error("Error calculating trending score: {}", e.getMessage());
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }
}

package com.talentradar.repository.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.enums.NotificationType;
import com.talentradar.model.notification.Notification;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing Notification entities. Provides data access
 * operations for notification management, user engagement tracking, read status
 * handling, and notification analytics.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /* Basic user notifications */
    // Find all notifications for a user
    List<Notification> findByUser(User user);

    // Find all notifications for a user with pagination
    Page<Notification> findByUser(User user, Pageable pageable);

    // Find all notifications for a user ordered by creation date
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Find unread notifications for a user
    List<Notification> findByUserAndIsReadFalse(User user);

    /* Unread notifications */
    // Find unread notifications for a user with pagination
    Page<Notification> findByUserAndIsReadFalse(User user, Pageable pageable);

    // Find unread notifications for a user ordered by creation date with pagination
    Page<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    // Find unread notifications for a user ordered by creation date
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    // Count unread notifications for a user
    long countByUserAndIsReadFalse(User user);

    /* Read notifications */
    // Find read notifications for a user
    List<Notification> findByUserAndIsReadTrue(User user);

    // Find read notifications for a user ordered by creation date with pagination
    Page<Notification> findByUserAndIsReadTrueOrderByCreatedAtDesc(User user, Pageable pageable);

    /* Notification type filtering */
    // Find notifications by user and type
    List<Notification> findByUserAndNotificationType(User user, NotificationType type);

    // Find notifications by user and type ordered by creation date with pagination
    Page<Notification> findByUserAndNotificationTypeOrderByCreatedAtDesc(User user, NotificationType type, Pageable pageable);

    // Find notifications by user and multiple types
    List<Notification> findByUserAndNotificationTypeIn(User user, List<NotificationType> types);

    /* Time-based queries */
    // Find notifications for a user created after specific time
    List<Notification> findByUserAndCreatedAtAfter(User user, LocalDateTime since);

    // Find all notifications created after specific time
    List<Notification> findByCreatedAtAfter(LocalDateTime since);

    // Find notifications created within date range
    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /* Recent notifications */
    // Find top 10 recent notifications for a user
    List<Notification> findTop10ByUserOrderByCreatedAtDesc(User user);

    // Find top 50 unread notifications for a user
    List<Notification> findTop50ByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    /* Entity-specific notifications */
    // Find notifications related to specific entity
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.relatedEntityType = :entityType AND n.relatedEntityId = :entityId")
    List<Notification> findByUserAndRelatedEntity(@Param("user") User user,
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId);

    /* Mark notifications as read */
    // Mark all unread notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.isRead = false")
    int markAllAsReadForUser(@Param("user") User user);

    // Mark specific notifications as read by IDs
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id IN :notificationIds")
    int markAsRead(@Param("notificationIds") List<Long> notificationIds);

    // Mark unread notifications as read by type for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.notificationType = :type AND n.isRead = false")
    int markAsReadByType(@Param("user") User user, @Param("type") NotificationType type);

    /* Delete old notifications */
    // Delete notifications older than cutoff date
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Delete read notifications older than cutoff date for a user
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.isRead = true AND n.createdAt < :cutoffDate")
    int deleteReadNotificationsOlderThan(@Param("user") User user, @Param("cutoffDate") LocalDateTime cutoffDate);

    /* Statistics */
    // Count total notifications for a user
    long countByUser(User user);

    // Count notifications by user and type
    long countByUserAndNotificationType(User user, NotificationType type);

    // Count notifications by type across all users
    long countByNotificationType(NotificationType type);

    // Get notification counts grouped by type for a user
    @Query("SELECT n.notificationType, COUNT(n) FROM Notification n WHERE n.user = :user GROUP BY n.notificationType")
    List<Object[]> getNotificationCountsByType(@Param("user") User user);

    /* Bulk operations */
    // Find unread notifications by multiple types for a user
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.notificationType IN :types AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByTypes(@Param("user") User user, @Param("types") List<NotificationType> types);

    /* System notifications */
    // Find system notifications since specific time
    @Query("SELECT n FROM Notification n WHERE n.notificationType = :systemType AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findSystemNotificationsSince(@Param("since") LocalDateTime since, @Param("systemType") NotificationType systemType);

    /* Duplicate checking */
    // Check for similar notifications to prevent duplicates
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.notificationType = :type AND n.relatedEntityType = :entityType AND n.relatedEntityId = :entityId AND n.createdAt >= :since")
    long countSimilarNotifications(@Param("user") User user,
            @Param("type") NotificationType type,
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("since") LocalDateTime since);

    /* User engagement metrics */
    // Count active users with notifications since specific time
    @Query("SELECT COUNT(DISTINCT n.user) FROM Notification n WHERE n.createdAt >= :since")
    long countActiveUsersWithNotifications(@Param("since") LocalDateTime since);

    // Get daily notification counts since specific time
    @Query("SELECT DATE(n.createdAt), COUNT(n) FROM Notification n WHERE n.createdAt >= :since GROUP BY DATE(n.createdAt) ORDER BY DATE(n.createdAt)")
    List<Object[]> getDailyNotificationCounts(@Param("since") LocalDateTime since);
}

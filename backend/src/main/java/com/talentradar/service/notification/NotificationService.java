package com.talentradar.service.notification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.NotificationNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.discussion.DiscussionReply;
import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.enums.GroupRole;
import com.talentradar.model.enums.NotificationType;
import com.talentradar.model.enums.ReportStatus;
import com.talentradar.model.group.UserGroup;
import com.talentradar.model.notification.Notification;
import com.talentradar.model.player.Player;
import com.talentradar.model.player.PlayerComment;
import com.talentradar.model.poll.Poll;
import com.talentradar.model.scouting.RatingCategory;
import com.talentradar.model.scouting.ScoutingReport;
import com.talentradar.model.user.User;
import com.talentradar.repository.group.GroupMemberRepository;
import com.talentradar.repository.notification.NotificationRepository;
import com.talentradar.repository.user.UserFollowRepository;

/**
 * Service responsible for managing notification operations. Handles creating,
 * retrieving, and managing notifications for various user activities.
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<Notification> findById(Long id) {
        try {
            if (id == null) {
                logger.warn("Attempted to find notification with null ID");
                return Optional.empty();
            }
            return notificationRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error finding notification by ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyUserFollowersOfNewRating(User user, Player player, RatingCategory category, BigDecimal rating) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (player == null) {
                throw new IllegalArgumentException("Player cannot be null");
            }
            if (category == null) {
                throw new IllegalArgumentException("Rating category cannot be null");
            }
            if (rating == null) {
                throw new IllegalArgumentException("Rating cannot be null");
            }

            List<User> followers = userFollowRepository.findFollowersByUser(user);

            for (User follower : followers) {
                try {
                    createNotification(
                            follower,
                            NotificationType.RATING,
                            "New Rating",
                            user.getDisplayName() + " rated " + player.getName() + " " + rating + "/10 for " + category.getName(),
                            "PLAYER",
                            player.getId(),
                            "/players/" + player.getId()
                    );
                } catch (Exception e) {
                    logger.error("Error creating rating notification for follower {}: {}", follower.getUsername(), e.getMessage());
                }
            }

            logger.info("Notified {} followers of new rating by {}", followers.size(), user.getUsername());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for rating notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying followers of new rating: {}", e.getMessage());
            throw new RuntimeException("Failed to notify followers of new rating", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyModeratorsOfReport(DiscussionReply reply, User reporter, String reason) {
        try {
            if (reply == null) {
                throw new IllegalArgumentException("Reply cannot be null");
            }
            if (reporter == null) {
                throw new IllegalArgumentException("Reporter cannot be null");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Reason cannot be null or empty");
            }

            List<User> moderators = userFollowRepository.findAllModerators();

            String title = "Reply Reported";
            String message = String.format("Reply ID %d was reported by %s. Reason: %s",
                    reply.getId(), reporter.getDisplayName(), reason.trim());

            for (User moderator : moderators) {
                try {
                    createNotification(
                            moderator,
                            NotificationType.REPORT,
                            title,
                            message,
                            "REPLY",
                            reply.getId(),
                            "/discussions/threads/" + reply.getThread().getId() + "#reply-" + reply.getId()
                    );
                } catch (Exception e) {
                    logger.error("Error creating report notification for moderator {}: {}", moderator.getUsername(), e.getMessage());
                }
            }

            logger.info("Notified {} moderators of reply report", moderators.size());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for moderator report notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying moderators of report: {}", e.getMessage());
            throw new RuntimeException("Failed to notify moderators of report", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyUserFollowersOfNewThread(User user, DiscussionThread thread) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (thread == null) {
                throw new IllegalArgumentException("Thread cannot be null");
            }

            List<User> followers = userFollowRepository.findFollowersByUser(user);

            for (User follower : followers) {
                try {
                    createNotification(
                            follower,
                            NotificationType.THREAD,
                            "New Discussion",
                            user.getDisplayName() + " started a new discussion: " + thread.getTitle(),
                            "THREAD",
                            thread.getId(),
                            "/discussions/threads/" + thread.getId()
                    );
                } catch (Exception e) {
                    logger.error("Error creating thread notification for follower {}: {}", follower.getUsername(), e.getMessage());
                }
            }

            logger.info("Notified {} followers of new thread by {}", followers.size(), user.getUsername());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for thread notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying followers of new thread: {}", e.getMessage());
            throw new RuntimeException("Failed to notify followers of new thread", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyUserFollowersOfNewPoll(User user, Poll poll) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (poll == null) {
                throw new IllegalArgumentException("Poll cannot be null");
            }

            List<User> followers = userFollowRepository.findFollowersByUser(user);

            for (User follower : followers) {
                try {
                    createNotification(
                            follower,
                            NotificationType.POLL,
                            "New Poll",
                            user.getDisplayName() + " created a poll: " + poll.getQuestion(),
                            "POLL",
                            poll.getId(),
                            "/polls/" + poll.getId()
                    );
                } catch (Exception e) {
                    logger.error("Error creating poll notification for follower {}: {}", follower.getUsername(), e.getMessage());
                }
            }

            logger.info("Notified {} followers of new poll by {}", followers.size(), user.getUsername());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for poll notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying followers of new poll: {}", e.getMessage());
            throw new RuntimeException("Failed to notify followers of new poll", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional
    public int markAllAsRead(User user) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalse(user);
            int count = unreadNotifications.size();

            unreadNotifications.forEach(notification -> {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
            });

            notificationRepository.saveAll(unreadNotifications);
            logger.info("Marked {} notifications as read for user {}", count, user.getUsername());

            return count;
        } catch (UserNotFoundException e) {
            logger.error("User not found for marking notifications as read: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error marking all notifications as read for user: {}", e.getMessage());
            throw new RuntimeException("Failed to mark notifications as read", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional
    public void deleteNotification(Notification notification) {
        try {
            if (notification == null) {
                throw new NotificationNotFoundException("Notification cannot be null");
            }

            notificationRepository.delete(notification);
            logger.info("Deleted notification with ID: {}", notification.getId());
        } catch (NotificationNotFoundException e) {
            logger.error("Notification not found for deletion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting notification {}: {}", notification != null ? notification.getId() : "null", e.getMessage());
            throw new RuntimeException("Failed to delete notification", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyOfNewReply(DiscussionThread thread, DiscussionReply reply, DiscussionReply parentReply) {
        try {
            if (thread == null) {
                throw new IllegalArgumentException("Thread cannot be null");
            }
            if (reply == null) {
                throw new IllegalArgumentException("Reply cannot be null");
            }

            // Notify thread author
            if (!thread.getAuthor().getId().equals(reply.getAuthor().getId())) {
                try {
                    createNotification(
                            thread.getAuthor(),
                            NotificationType.REPLY,
                            "New Reply",
                            reply.getAuthor().getDisplayName() + " replied to your thread: " + thread.getTitle(),
                            "THREAD",
                            thread.getId(),
                            "/discussions/threads/" + thread.getId() + "#reply-" + reply.getId()
                    );
                } catch (Exception e) {
                    logger.error("Error creating thread reply notification: {}", e.getMessage());
                }
            }

            // Notify parent reply author if it's a nested reply
            if (parentReply != null && !parentReply.getAuthor().getId().equals(reply.getAuthor().getId())) {
                try {
                    createNotification(
                            parentReply.getAuthor(),
                            NotificationType.REPLY,
                            "Reply to Your Comment",
                            reply.getAuthor().getDisplayName() + " replied to your comment",
                            "REPLY",
                            reply.getId(),
                            "/discussions/threads/" + thread.getId() + "#reply-" + reply.getId()
                    );
                } catch (Exception e) {
                    logger.error("Error creating parent reply notification: {}", e.getMessage());
                }
            }

            logger.info("Processed reply notifications for thread {}", thread.getId());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for reply notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying of new reply: {}", e.getMessage());
            throw new RuntimeException("Failed to notify of new reply", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyOfNewFollow(User follower, User followedUser) {
        try {
            if (follower == null) {
                throw new IllegalArgumentException("Follower cannot be null");
            }
            if (followedUser == null) {
                throw new IllegalArgumentException("Followed user cannot be null");
            }

            createNotification(
                    followedUser,
                    NotificationType.FOLLOW,
                    "New Follower",
                    follower.getDisplayName() + " started following you",
                    "USER",
                    follower.getId(),
                    "/users/" + follower.getId() + "/profile"
            );

            logger.info("Notified {} of new follower {}", followedUser.getUsername(), follower.getUsername());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for follow notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying of new follow: {}", e.getMessage());
            throw new RuntimeException("Failed to notify of new follow", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyGroupAdminsOfNewMember(UserGroup group, User newMember) {
        try {
            if (group == null) {
                throw new IllegalArgumentException("Group cannot be null");
            }
            if (newMember == null) {
                throw new IllegalArgumentException("New member cannot be null");
            }

            List<User> admins = groupMemberRepository.findAdminsByGroup(group);

            for (User admin : admins) {
                // Don't notify the new member if they're an admin
                if (!admin.getId().equals(newMember.getId())) {
                    try {
                        createNotification(
                                admin,
                                NotificationType.GROUP_INVITE,
                                "New Group Member",
                                newMember.getDisplayName() + " joined " + group.getName(),
                                "GROUP",
                                group.getId(),
                                "/groups/" + group.getId()
                        );
                    } catch (Exception e) {
                        logger.error("Error creating group member notification for admin {}: {}", admin.getUsername(), e.getMessage());
                    }
                }
            }

            logger.info("Notified {} group admins of new member {} in group {}", admins.size(), newMember.getUsername(), group.getName());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for group member notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying group admins of new member: {}", e.getMessage());
            throw new RuntimeException("Failed to notify group admins of new member", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyUserOfRoleChange(User user, UserGroup group, GroupRole newRole) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (group == null) {
                throw new IllegalArgumentException("Group cannot be null");
            }
            if (newRole == null) {
                throw new IllegalArgumentException("New role cannot be null");
            }

            createNotification(
                    user,
                    NotificationType.GROUP_ROLE,
                    "Role Changed",
                    "Your role in " + group.getName() + " has been changed to " + newRole.getDisplayName(),
                    "GROUP",
                    group.getId(),
                    "/groups/" + group.getId()
            );

            logger.info("Notified {} of role change to {} in group {}", user.getUsername(), newRole, group.getName());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for role change notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying user of role change: {}", e.getMessage());
            throw new RuntimeException("Failed to notify user of role change", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyUserOfGroupRemoval(User user, UserGroup group, User removedBy) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (group == null) {
                throw new IllegalArgumentException("Group cannot be null");
            }
            if (removedBy == null) {
                throw new IllegalArgumentException("Removed by user cannot be null");
            }

            createNotification(
                    user,
                    NotificationType.GROUP_REMOVAL,
                    "Removed from Group",
                    "You have been removed from " + group.getName() + " by " + removedBy.getDisplayName(),
                    "GROUP",
                    group.getId(),
                    "/groups"
            );

            logger.info("Notified {} of removal from group {} by {}", user.getUsername(), group.getName(), removedBy.getUsername());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for group removal notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying user of group removal: {}", e.getMessage());
            throw new RuntimeException("Failed to notify user of group removal", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    private void createNotification(User user, NotificationType type, String title, String message,
            String entityType, Long entityId, String actionUrl) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (type == null) {
                throw new IllegalArgumentException("Notification type cannot be null");
            }
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title cannot be null or empty");
            }
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Message cannot be null or empty");
            }

            Notification notification = new Notification();
            notification.setUser(user);
            notification.setNotificationType(type);
            notification.setTitle(title.trim());
            notification.setMessage(message.trim());
            notification.setRelatedEntityType(entityType);
            notification.setRelatedEntityId(entityId);
            notification.setActionUrl(actionUrl);

            notificationRepository.save(notification);
            logger.debug("Created notification for user {}: {}", user.getUsername(), title);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for creating notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating notification for user {}: {}", user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to create notification", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyOfNewScoutingReport(ScoutingReport report) {
        try {
            if (report == null) {
                throw new IllegalArgumentException("Scouting report cannot be null");
            }

            if (!Boolean.TRUE.equals(report.getIsPublic()) || report.getStatus() != ReportStatus.PUBLISHED) {
                logger.debug("Skipping notification for non-public or unpublished report {}", report.getId());
                return;
            }

            String title = "New Scouting Report";
            String message = String.format("New scouting report for %s by %s",
                    report.getPlayer().getName(),
                    report.getScout().getFullName());

            List<User> followers = userFollowRepository.findFollowersByUser(report.getScout());
            for (User follower : followers) {
                try {
                    createNotification(
                            follower,
                            NotificationType.SCOUTING_REPORT,
                            title,
                            message,
                            "SCOUTING_REPORT",
                            report.getId(),
                            "/scouting/reports/" + report.getId()
                    );
                } catch (Exception e) {
                    logger.error("Error creating scouting report notification for follower {}: {}", follower.getUsername(), e.getMessage());
                }
            }

            logger.info("Notified {} followers of new scouting report by {}", followers.size(), report.getScout().getUsername());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for scouting report notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying of new scouting report: {}", e.getMessage());
            throw new RuntimeException("Failed to notify of new scouting report", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void notifyOfCommentReply(PlayerComment parentComment, PlayerComment replyComment) {
        try {
            if (parentComment == null) {
                throw new IllegalArgumentException("Parent comment cannot be null");
            }
            if (replyComment == null) {
                throw new IllegalArgumentException("Reply comment cannot be null");
            }

            User parentCommentAuthor = parentComment.getAuthor();
            User replyAuthor = replyComment.getAuthor();

            // Don't notify if user is replying to their own comment
            if (parentCommentAuthor.getId().equals(replyAuthor.getId())) {
                logger.debug("Skipping self-reply notification for comment {}", parentComment.getId());
                return;
            }

            String title = "New Reply to Your Comment";
            String message = String.format("%s replied to your comment on %s",
                    replyAuthor.getFullName(),
                    parentComment.getPlayer().getName());

            createNotification(
                    parentCommentAuthor,
                    NotificationType.COMMENT_REPLY,
                    title,
                    message,
                    "PLAYER_COMMENT",
                    replyComment.getId(),
                    "/players/" + parentComment.getPlayer().getId() + "/comments#comment-" + replyComment.getId()
            );

            logger.info("Notified {} of comment reply by {}", parentCommentAuthor.getUsername(), replyAuthor.getUsername());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for comment reply notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error notifying of comment reply: {}", e.getMessage());
            throw new RuntimeException("Failed to notify of comment reply", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Invalid parameters for getting user notifications: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving notifications for user {}: {}", user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to retrieve user notifications", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Long getUnreadNotificationCount(User user) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            return notificationRepository.countByUserAndIsReadFalse(user);
        } catch (UserNotFoundException e) {
            logger.error("User not found for getting unread notification count: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error getting unread notification count for user {}: {}", user != null ? user.getUsername() : "null", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(User user, Pageable pageable) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return notificationRepository.findByUserAndIsReadFalse(user, pageable);
        } catch (UserNotFoundException | IllegalArgumentException e) {
            logger.error("Invalid parameters for getting unread notifications: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error retrieving unread notifications for user {}: {}", user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to retrieve unread notifications", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void markAsRead(Notification notification, User user) {
        try {
            if (notification == null) {
                throw new NotificationNotFoundException("Notification cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            if (!notification.getUser().getId().equals(user.getId())) {
                throw new IllegalStateException("Cannot mark another user's notification as read");
            }

            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);

            logger.info("Marked notification {} as read for user {}", notification.getId(), user.getUsername());
        } catch (NotificationNotFoundException | UserNotFoundException | IllegalStateException e) {
            logger.error("Error marking notification as read: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error marking notification {} as read for user {}: {}",
                    notification != null ? notification.getId() : "null",
                    user != null ? user.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to mark notification as read", e);
        }
    }
}

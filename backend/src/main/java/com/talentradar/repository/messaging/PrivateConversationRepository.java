package com.talentradar.repository.messaging;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.messaging.PrivateConversation;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing PrivateConversation entities. Provides data
 * access operations for conversation management, participant tracking, message
 * organisation, and activity monitoring.
 */
@Repository
public interface PrivateConversationRepository extends JpaRepository<PrivateConversation, Long> {

    /* Basic conversation finder methods */
    // Find conversations where user is a participant
    @Query("SELECT pc FROM PrivateConversation pc JOIN ConversationParticipant cp ON pc.id = cp.conversation.id WHERE cp.user = :user AND cp.isActive = true ORDER BY pc.lastMessageAt DESC")
    Page<PrivateConversation> findConversationsByUser(@Param("user") User user, Pageable pageable);

    // Find direct conversation between two users
    @Query("SELECT pc FROM PrivateConversation pc JOIN ConversationParticipant cp1 ON pc.id = cp1.conversation.id JOIN ConversationParticipant cp2 ON pc.id = cp2.conversation.id WHERE cp1.user = :user1 AND cp2.user = :user2 AND pc.isGroupConversation = false AND cp1.isActive = true AND cp2.isActive = true")
    Optional<PrivateConversation> findDirectConversationBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    /* User-specific conversation methods */
    // Find all conversations for a user
    @Query("SELECT DISTINCT pc FROM PrivateConversation pc "
            + "JOIN ConversationParticipant cp ON cp.conversation = pc "
            + "WHERE cp.user = :user AND cp.isActive = true "
            + "ORDER BY pc.lastMessageAt DESC")
    List<PrivateConversation> findConversationsForUser(@Param("user") User user);

    // Find conversations for a user with pagination
    @Query("SELECT DISTINCT pc FROM PrivateConversation pc "
            + "JOIN ConversationParticipant cp ON cp.conversation = pc "
            + "WHERE cp.user = :user AND cp.isActive = true "
            + "ORDER BY pc.lastMessageAt DESC")
    Page<PrivateConversation> findConversationsForUser(@Param("user") User user, Pageable pageable);

    /* Group conversation methods */
    // Find group conversations
    Page<PrivateConversation> findByIsGroupConversationTrueOrderByLastMessageAtDesc(Pageable pageable);

    // Find group conversations created by user
    @Query("SELECT pc FROM PrivateConversation pc WHERE pc.createdBy = :user AND pc.isGroupConversation = true")
    List<PrivateConversation> findGroupConversationsCreatedByUser(@Param("user") User user);

    // Find group conversations for user
    @Query("SELECT DISTINCT pc FROM PrivateConversation pc "
            + "JOIN ConversationParticipant cp ON cp.conversation = pc "
            + "WHERE cp.user = :user AND cp.isActive = true AND pc.isGroupConversation = true "
            + "ORDER BY pc.lastMessageAt DESC")
    List<PrivateConversation> findGroupConversationsForUser(@Param("user") User user);

    /* Direct conversation methods */
    // Find direct conversations for user
    @Query("SELECT DISTINCT pc FROM PrivateConversation pc "
            + "JOIN ConversationParticipant cp ON cp.conversation = pc "
            + "WHERE cp.user = :user AND cp.isActive = true AND pc.isGroupConversation = false "
            + "ORDER BY pc.lastMessageAt DESC")
    List<PrivateConversation> findDirectConversationsForUser(@Param("user") User user);

    /* Creator-based finder methods */
    // Find conversations created by user
    List<PrivateConversation> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    /* Search methods */
    // Search conversations for a user by name
    @Query("SELECT DISTINCT pc FROM PrivateConversation pc "
            + "JOIN ConversationParticipant cp ON cp.conversation = pc "
            + "WHERE cp.user = :user AND cp.isActive = true "
            + "AND LOWER(pc.conversationName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
            + "ORDER BY pc.lastMessageAt DESC")
    List<PrivateConversation> searchConversationsForUser(@Param("user") User user, @Param("searchTerm") String searchTerm);

    /* Unread and activity methods */
    // Find conversations with unread messages for user
    @Query("SELECT DISTINCT pc FROM PrivateConversation pc "
            + "JOIN ConversationParticipant cp ON cp.conversation = pc "
            + "WHERE cp.user = :user AND cp.isActive = true AND cp.unreadCount > 0 "
            + "ORDER BY pc.lastMessageAt DESC")
    List<PrivateConversation> findConversationsWithUnreadMessages(@Param("user") User user);

    // Find conversations updated after specific date
    @Query("SELECT DISTINCT pc FROM PrivateConversation pc "
            + "JOIN ConversationParticipant cp ON cp.conversation = pc "
            + "WHERE cp.user = :user AND cp.isActive = true "
            + "AND pc.lastMessageAt >= :since "
            + "ORDER BY pc.lastMessageAt DESC")
    List<PrivateConversation> findRecentConversationsForUser(@Param("user") User user, @Param("since") java.time.LocalDateTime since);

    // Find active conversations (with recent activity)
    @Query("SELECT pc FROM PrivateConversation pc WHERE pc.lastMessageAt >= :since ORDER BY pc.lastMessageAt DESC")
    List<PrivateConversation> findActiveConversations(@Param("since") java.time.LocalDateTime since);

    /* Count methods */
    // Count conversations for user
    @Query("SELECT COUNT(DISTINCT pc) FROM PrivateConversation pc "
            + "JOIN ConversationParticipant cp ON cp.conversation = pc "
            + "WHERE cp.user = :user AND cp.isActive = true")
    long countConversationsForUser(@Param("user") User user);
}

package com.talentradar.repository.messaging;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.messaging.PrivateConversation;
import com.talentradar.model.messaging.PrivateMessage;

/**
 * Repository interface for managing PrivateMessage entities. Provides data
 * access operations for message management, conversation tracking, search
 * functionality, and unread message counting.
 */
@Repository
public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    /* Basic conversation message methods */
    // Get messages for a conversation
    Page<PrivateMessage> findByConversationAndIsDeletedFalseOrderByCreatedAtDesc(
            PrivateConversation conversation, Pageable pageable);

    // Get latest message for a conversation
    PrivateMessage findTopByConversationAndIsDeletedFalseOrderByCreatedAtDesc(
            PrivateConversation conversation);

    /* Sender-based finder methods */
    // Get messages by sender
    List<PrivateMessage> findBySenderIdAndIsDeletedFalse(Long senderId);

    /* Count and unread methods */
    // Count unread messages for a user in a conversation
    @Query("SELECT COUNT(pm) FROM PrivateMessage pm WHERE pm.conversation = :conversation "
            + "AND pm.sender.id != :userId AND pm.isDeleted = false "
            + "AND pm.id NOT IN (SELECT mrs.message.id FROM MessageReadStatus mrs WHERE mrs.user.id = :userId)")
    long countUnreadMessagesForUserInConversation(
            @Param("conversation") PrivateConversation conversation,
            @Param("userId") Long userId);

    /* Search methods */
    // Search messages in conversation
    @Query("SELECT pm FROM PrivateMessage pm WHERE pm.conversation = :conversation "
            + "AND pm.isDeleted = false AND LOWER(pm.messageText) LIKE LOWER(CONCAT('%', :searchTerm, '%')) "
            + "ORDER BY pm.createdAt DESC")
    Page<PrivateMessage> searchMessagesInConversation(
            @Param("conversation") PrivateConversation conversation,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
}

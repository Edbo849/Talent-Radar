package com.talentradar.repository.messaging;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.talentradar.model.messaging.MessageReadStatus;
import com.talentradar.model.messaging.PrivateMessage;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing MessageReadStatus entities. Provides data
 * access operations for message read tracking, notification management, and
 * user activity monitoring.
 */
@Repository
public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, Long> {

    /* Basic finder methods */
    // Check if user has read a message
    Optional<MessageReadStatus> findByMessageAndUser(PrivateMessage message, User user);

    // Find all read statuses for a given message
    List<MessageReadStatus> findByMessageId(Long messageId);

    // Find all read statuses for a given conversation
    List<MessageReadStatus> findByConversationId(Long conversationId);

    /* Existence check methods */
    // Check if message is read by user
    boolean existsByMessageAndUser(PrivateMessage message, User user);
}

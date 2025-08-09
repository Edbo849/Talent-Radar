package com.talentradar.repository.messaging;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.talentradar.model.messaging.ConversationParticipant;
import com.talentradar.model.messaging.PrivateConversation;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing ConversationParticipant entities. Provides
 * data access operations for conversation membership management, participant
 * validation, and activity tracking.
 */
@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    /* Basic finder methods */
    // Find participant by conversation and user
    Optional<ConversationParticipant> findByConversationAndUser(PrivateConversation conversation, User user);

    // Get all participants of a conversation
    List<ConversationParticipant> findByConversationAndIsActiveTrue(PrivateConversation conversation);

    /* Existence and validation methods */
    // Check if user is participant
    boolean existsByConversationAndUserAndIsActiveTrue(PrivateConversation conversation, User user);

    /* Count methods */
    // Count active participants
    long countByConversationAndIsActiveTrue(PrivateConversation conversation);
}

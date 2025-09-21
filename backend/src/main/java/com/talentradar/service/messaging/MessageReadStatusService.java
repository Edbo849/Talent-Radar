package com.talentradar.service.messaging;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.model.messaging.MessageReadStatus;
import com.talentradar.model.messaging.PrivateMessage;
import com.talentradar.model.user.User;
import com.talentradar.repository.messaging.MessageReadStatusRepository;
import com.talentradar.repository.messaging.PrivateMessageRepository;

/**
 * Service responsible for managing message read status tracking. Handles
 * marking messages as read, checking read status, and managing read receipts.
 */
@Service
@Transactional
public class MessageReadStatusService {

    private static final Logger logger = LoggerFactory.getLogger(MessageReadStatusService.class);

    @Autowired
    private MessageReadStatusRepository readStatusRepository;

    @Autowired
    private PrivateMessageRepository messageRepository;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public MessageReadStatus markAsRead(PrivateMessage message, User user) {
        try {
            if (message == null) {
                throw new IllegalArgumentException("Message cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            // Don't mark own messages as read
            if (message.getSender().getId().equals(user.getId())) {
                logger.debug("Skipping read status for own message {} by user {}", message.getId(), user.getUsername());
                return null;
            }

            // Check if already marked as read
            Optional<MessageReadStatus> existing = readStatusRepository.findByMessageAndUser(message, user);
            if (existing.isPresent()) {
                logger.debug("Message {} already marked as read by user {}", message.getId(), user.getUsername());
                return existing.get();
            }

            // Create new read status
            MessageReadStatus readStatus = new MessageReadStatus();
            readStatus.setMessage(message);
            readStatus.setUser(user);
            readStatus.setReadAt(LocalDateTime.now());

            MessageReadStatus saved = readStatusRepository.save(readStatus);

            // Update message read count
            message.setReadCount(message.getReadCount() + 1);
            messageRepository.save(message);

            logger.debug("Marked message {} as read by user {}", message.getId(), user.getUsername());
            return saved;

        } catch (IllegalArgumentException e) {
            logger.error("Error marking message {} as read by user {}: {}",
                    message != null ? message.getId() : "null",
                    user != null ? user.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to mark message as read", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void markAsRead(Long messageId, User user) {
        try {
            if (messageId == null) {
                throw new IllegalArgumentException("Message ID cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            PrivateMessage message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

            markAsRead(message, user);

        } catch (IllegalArgumentException e) {
            logger.error("Error marking message {} as read by user {}: {}", messageId,
                    user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to mark message as read", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isMessageReadByUser(Long messageId, User user) {
        try {
            if (messageId == null) {
                throw new IllegalArgumentException("Message ID cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            PrivateMessage message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

            return readStatusRepository.existsByMessageAndUser(message, user);

        } catch (IllegalArgumentException e) {
            logger.error("Error checking read status for message {} by user {}: {}", messageId,
                    user != null ? user.getUsername() : "null", e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isMessageReadByUser(PrivateMessage message, User user) {
        try {
            if (message == null) {
                throw new IllegalArgumentException("Message cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            return readStatusRepository.existsByMessageAndUser(message, user);

        } catch (IllegalArgumentException e) {
            logger.error("Error checking read status for message {} by user {}: {}",
                    message != null ? message.getId() : "null",
                    user != null ? user.getUsername() : "null",
                    e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<LocalDateTime> getReadTime(Long messageId, User user) {
        try {
            if (messageId == null) {
                throw new IllegalArgumentException("Message ID cannot be null");
            }
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            PrivateMessage message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

            return readStatusRepository.findByMessageAndUser(message, user)
                    .map(MessageReadStatus::getReadAt);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting read time for message {} by user {}: {}", messageId,
                    user != null ? user.getUsername() : "null", e.getMessage());
            return Optional.empty();
        }
    }
}

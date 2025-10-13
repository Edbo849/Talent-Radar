package com.talentradar.service.discussion;

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

import com.talentradar.model.discussion.DiscussionCategory;
import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.enums.ThreadType;
import com.talentradar.model.player.Player;
import com.talentradar.model.thread.ThreadPlayer;
import com.talentradar.model.user.User;
import com.talentradar.repository.discussion.DiscussionCategoryRepository;
import com.talentradar.repository.discussion.DiscussionThreadRepository;
import com.talentradar.repository.thread.ThreadPlayerRepository;
import com.talentradar.service.notification.NotificationService;

@Service
@Transactional
public class DiscussionThreadService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussionThreadService.class);

    @Autowired
    private DiscussionThreadRepository threadRepository;

    @Autowired
    private DiscussionCategoryRepository categoryRepository;

    @Autowired
    private ThreadPlayerRepository threadPlayerRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Creates a new discussion thread with specified category, author, and
     * optional player associations.
     */
    public DiscussionThread createThread(Long categoryId, User author, String title, String content,
            ThreadType threadType, List<Player> players) {

        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }

        try {
            DiscussionCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

            DiscussionThread thread = new DiscussionThread(category, author, title, content);
            thread.setThreadType(threadType != null ? threadType : ThreadType.GENERAL);

            DiscussionThread savedThread = threadRepository.save(thread);

            // Link players to thread if provided
            if (players != null && !players.isEmpty()) {
                linkPlayersToThread(savedThread, players, author);
            }

            // Notify followers
            notificationService.notifyUserFollowersOfNewThread(author, savedThread);

            logger.info("Created new thread '{}' by user: {}", title, author.getUsername());
            return savedThread;
        } catch (Exception e) {
            logger.error("Error creating thread: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create thread: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves threads by category with pagination, ordered by last activity.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionThread> getThreadsByCategory(Long categoryId, Pageable pageable) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        try {
            DiscussionCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

            return threadRepository.findByCategoryOrderByLastActivityAtDesc(category, pageable);
        } catch (Exception e) {
            logger.error("Error retrieving threads for category {}: {}", categoryId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve threads by category: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all threads with pagination, ordered by last activity.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionThread> getAllThreads(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        try {
            return threadRepository.findAllByOrderByLastActivityAtDesc(pageable);
        } catch (Exception e) {
            logger.error("Error retrieving all threads: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve threads: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves pinned threads with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionThread> getPinnedThreads(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        try {
            return threadRepository.findByIsPinnedTrueOrderByLastActivityAtDesc(pageable);
        } catch (Exception e) {
            logger.error("Error retrieving pinned threads: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve pinned threads: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves trending threads from the last week with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionThread> getTrendingThreads(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        try {
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            return threadRepository.findTrendingThreadsSince(oneWeekAgo, pageable);
        } catch (Exception e) {
            logger.error("Error retrieving trending threads: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve trending threads: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves most active threads with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionThread> getMostActiveThreads(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        try {
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
            return threadRepository.findMostActiveThreadsSince(oneWeekAgo, pageable);
        } catch (Exception e) {
            logger.error("Error retrieving most active threads: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve most active threads: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves threads by thread type with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionThread> getThreadsByThreadType(ThreadType threadType, Pageable pageable) {
        if (threadType == null) {
            throw new IllegalArgumentException("Thread type cannot be null");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        try {
            return threadRepository.findByThreadTypeOrderByLastActivityAtDesc(threadType, pageable);
        } catch (Exception e) {
            logger.error("Error retrieving threads by type {}: {}", threadType, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve threads by type: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves threads created by a specific author with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionThread> getThreadsByAuthor(User author, Pageable pageable) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        try {
            return threadRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
        } catch (Exception e) {
            logger.error("Error retrieving threads by author {}: {}", author.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve threads by author: " + e.getMessage(), e);
        }
    }

    /**
     * Searches threads by title and content with pagination.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionThread> searchThreads(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        try {
            return threadRepository.findByFullTextSearch(searchTerm, pageable);
        } catch (Exception e) {
            logger.error("Error searching threads with term '{}': {}", searchTerm, e.getMessage(), e);
            throw new RuntimeException("Failed to search threads: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a thread by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<DiscussionThread> getThreadById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }

        try {
            return threadRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving thread with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve thread: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing thread's title and content.
     */
    public DiscussionThread updateThread(Long threadId, User requester, String title, String content) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        try {
            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

            // Check permissions - author can edit their own thread
            if (!thread.getAuthor().getId().equals(requester.getId()) && !requester.canModerate()) {
                throw new IllegalStateException("Cannot edit this thread");
            }

            if (title != null && !title.trim().isEmpty()) {
                thread.setTitle(title);
            }
            if (content != null && !content.trim().isEmpty()) {
                thread.setContent(content);
            }

            DiscussionThread updatedThread = threadRepository.save(thread);
            logger.info("Thread '{}' updated by user: {}", thread.getTitle(), requester.getUsername());
            return updatedThread;
        } catch (RuntimeException e) {
            logger.error("Error updating thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Failed to update thread: " + e.getMessage(), e);
        }
    }

    /**
     * Records a view for a thread and updates its activity.
     */
    public void viewThread(Long threadId, User user, String ipAddress) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }

        try {
            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

            thread.incrementViewCount();
            thread.updateLastActivity();
            threadRepository.save(thread);

        } catch (Exception e) {
            logger.error("Error recording view for thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Failed to record thread view: " + e.getMessage(), e);
        }
    }

    /**
     * Removes featured status from a thread.
     */
    public void unfeatureThread(Long threadId, User requester) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        // Check admin permissions
        if (!requester.getRole().hasAdminAccess()) {
            throw new IllegalStateException("Only administrators can unfeature threads");
        }

        try {
            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

            thread.setIsFeatured(false);
            threadRepository.save(thread);

            logger.info("Thread '{}' unfeatured by admin: {}", thread.getTitle(), requester.getUsername());
        } catch (Exception e) {
            logger.error("Error unfeaturing thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Failed to unfeature thread: " + e.getMessage(), e);
        }
    }

    /**
     * Pins a thread to the top of the category.
     */
    public void pinThread(Long threadId, User requester) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        if (!requester.canModerate()) {
            throw new IllegalStateException("Only moderators can pin threads");
        }

        try {
            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

            thread.setIsPinned(true);
            threadRepository.save(thread);

            logger.info("Thread '{}' pinned by moderator: {}", thread.getTitle(), requester.getUsername());
        } catch (Exception e) {
            logger.error("Error pinning thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Failed to pin thread: " + e.getMessage(), e);
        }
    }

    /**
     * Removes pin status from a thread.
     */
    public void unpinThread(Long threadId, User requester) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        if (!requester.canModerate()) {
            throw new IllegalStateException("Only moderators can unpin threads");
        }

        try {
            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

            thread.setIsPinned(false);
            threadRepository.save(thread);

            logger.info("Thread '{}' unpinned by moderator: {}", thread.getTitle(), requester.getUsername());
        } catch (Exception e) {
            logger.error("Error unpinning thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Failed to unpin thread: " + e.getMessage(), e);
        }
    }

    /**
     * Locks a thread to prevent new replies.
     */
    public void lockThread(Long threadId, User requester) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        if (!requester.canModerate()) {
            throw new IllegalStateException("Only moderators can lock threads");
        }

        try {
            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

            thread.setIsLocked(true);
            threadRepository.save(thread);

            logger.info("Thread '{}' locked by moderator: {}", thread.getTitle(), requester.getUsername());
        } catch (Exception e) {
            logger.error("Error locking thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Failed to lock thread: " + e.getMessage(), e);
        }
    }

    /**
     * Unlocks a thread to allow new replies.
     */
    public void unlockThread(Long threadId, User requester) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        if (!requester.canModerate()) {
            throw new IllegalStateException("Only moderators can unlock threads");
        }

        try {
            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

            thread.setIsLocked(false);
            threadRepository.save(thread);

            logger.info("Thread '{}' unlocked by moderator: {}", thread.getTitle(), requester.getUsername());
        } catch (Exception e) {
            logger.error("Error unlocking thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Failed to unlock thread: " + e.getMessage(), e);
        }
    }

    /**
     * Features a thread for prominent display.
     */
    public void featureThread(Long threadId, User requester) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        if (!requester.canModerate()) {
            throw new IllegalStateException("Only moderators can feature threads");
        }

        try {
            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

            thread.setIsFeatured(true);
            threadRepository.save(thread);

            logger.info("Thread '{}' featured by moderator: {}", thread.getTitle(), requester.getUsername());
        } catch (Exception e) {
            logger.error("Error featuring thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Failed to feature thread: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a thread permanently.
     */
    public void deleteThread(Long threadId, User requester) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }
        if (requester == null) {
            throw new IllegalArgumentException("Requester cannot be null");
        }

        try {
            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

            // Check permissions
            if (!thread.getAuthor().getId().equals(requester.getId()) && !requester.canModerate()) {
                throw new IllegalStateException("Cannot delete this thread");
            }

            threadRepository.delete(thread);
            logger.info("Thread '{}' deleted by user: {}", thread.getTitle(), requester.getUsername());
        } catch (RuntimeException e) {
            logger.error("Error deleting thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete thread: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all players associated with a thread.
     */
    @Transactional(readOnly = true)
    public List<Player> getThreadPlayers(Long threadId) {
        if (threadId == null) {
            throw new IllegalArgumentException("Thread ID cannot be null");
        }

        try {
            DiscussionThread thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new RuntimeException("Thread not found with ID: " + threadId));

            return threadPlayerRepository.findPlayersByThread(thread);
        } catch (Exception e) {
            logger.error("Error retrieving players for thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve thread players: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves threads associated with a specific player.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionThread> getThreadsByPlayer(Player player, Pageable pageable) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }

        try {
            return threadPlayerRepository.findThreadsByPlayer(player, pageable);
        } catch (Exception e) {
            logger.error("Error retrieving threads for player {}: {}", player.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve threads by player: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all featured threads.
     */
    @Transactional(readOnly = true)
    public List<DiscussionThread> getFeaturedThreads() {
        try {
            return threadRepository.findByIsFeaturedTrueOrderByLastActivityAtDesc();
        } catch (Exception e) {
            logger.error("Error retrieving featured threads: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve featured threads: " + e.getMessage(), e);
        }
    }

    /**
     * Counts the number of threads in a specific category.
     */
    @Transactional(readOnly = true)
    public long getThreadCountByCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }

        try {
            DiscussionCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

            return threadRepository.countByCategory(category);
        } catch (Exception e) {
            logger.error("Error counting threads for category {}: {}", categoryId, e.getMessage(), e);
            throw new RuntimeException("Failed to count threads by category: " + e.getMessage(), e);
        }
    }

    /**
     * Links players to a thread with display order and primary designation.
     */
    private void linkPlayersToThread(DiscussionThread thread, List<Player> players, User author) {
        try {
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                ThreadPlayer threadPlayer = new ThreadPlayer();
                threadPlayer.setThread(thread);
                threadPlayer.setPlayer(player);
                threadPlayer.setAddedByUser(author);
                threadPlayer.setDisplayOrder(i + 1);

                if (i == 0) {
                    threadPlayer.setIsPrimary(true); // First player is primary
                }

                threadPlayerRepository.save(threadPlayer);
            }
        } catch (Exception e) {
            logger.error("Error linking players to thread: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to link players to thread: " + e.getMessage(), e);
        }
    }
}

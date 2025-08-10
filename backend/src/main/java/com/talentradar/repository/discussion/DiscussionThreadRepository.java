package com.talentradar.repository.discussion;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.discussion.DiscussionCategory;
import com.talentradar.model.discussion.DiscussionThread;
import com.talentradar.model.enums.ThreadType;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing DiscussionThread entities. Provides data
 * access operations for thread management, category-based queries, search
 * functionality, and activity tracking.
 */
@Repository
public interface DiscussionThreadRepository extends JpaRepository<DiscussionThread, Long> {

    /* Basic finder methods */
    // Find all threads ordered by last activity
    Page<DiscussionThread> findAllByOrderByLastActivityAtDesc(Pageable pageable);

    // Find threads that haven't been locked
    Page<DiscussionThread> findByIsLockedFalseOrderByLastActivityAtDesc(Pageable pageable);

    /* Category-based finder methods */
    // Find threads by category
    Page<DiscussionThread> findByCategoryOrderByLastActivityAtDesc(DiscussionCategory category, Pageable pageable);

    // Find threads by category and type
    Page<DiscussionThread> findByCategoryAndThreadTypeOrderByLastActivityAtDesc(DiscussionCategory category, ThreadType threadType, Pageable pageable);

    // Find threads by multiple categories
    @Query("SELECT dt FROM DiscussionThread dt WHERE dt.category IN :categories ORDER BY dt.lastActivityAt DESC")
    Page<DiscussionThread> findByCategoriesOrderByLastActivityAtDesc(@Param("categories") List<DiscussionCategory> categories, Pageable pageable);

    /* Thread type finder methods */
    // Find threads by thread type ordered by last activity
    Page<DiscussionThread> findByThreadTypeOrderByLastActivityAtDesc(ThreadType threadType, Pageable pageable);

    /* Author-based finder methods */
    // Find threads by author
    Page<DiscussionThread> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    // Find threads by author with pagination ordered by last activity
    Page<DiscussionThread> findByAuthorOrderByLastActivityAtDesc(User author, Pageable pageable);

    // Find recent threads by author
    List<DiscussionThread> findByAuthorAndCreatedAtAfterOrderByCreatedAtDesc(User author, LocalDateTime since);

    /* Special thread finder methods */
    // Find pinned threads
    List<DiscussionThread> findByIsPinnedTrueOrderByCreatedAtDesc();

    // Find pinned threads with pagination
    Page<DiscussionThread> findByIsPinnedTrueOrderByLastActivityAtDesc(Pageable pageable);

    // Find featured threads (list)
    List<DiscussionThread> findByIsFeaturedTrueOrderByLastActivityAtDesc();

    // Find featured threads with pagination
    Page<DiscussionThread> findByIsFeaturedTrueOrderByLastActivityAtDesc(Pageable pageable);

    /* Search methods */
    // Search threads by title or content
    @Query("SELECT dt FROM DiscussionThread dt WHERE LOWER(dt.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(dt.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<DiscussionThread> searchByTitleOrContent(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Full text search
    @Query(
            "SELECT dt FROM DiscussionThread dt WHERE LOWER(dt.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(dt.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<DiscussionThread> findByFullTextSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    /* Trending and activity-based finder methods */
    // Find trending threads (high activity in recent period)
    @Query("SELECT dt FROM DiscussionThread dt WHERE dt.lastActivityAt >= :since ORDER BY dt.viewCount DESC, dt.lastActivityAt DESC")
    Page<DiscussionThread> findTrendingThreads(@Param("since") java.time.LocalDateTime since, Pageable pageable);

    // Find trending threads since specific time
    @Query("SELECT dt FROM DiscussionThread dt WHERE dt.lastActivityAt >= :since ORDER BY dt.viewCount DESC, dt.replyCount DESC")
    Page<DiscussionThread> findTrendingThreadsSince(@Param("since") LocalDateTime since, Pageable pageable);

    // Find most active threads by reply count
    @Query("SELECT dt FROM DiscussionThread dt WHERE dt.createdAt >= :since ORDER BY dt.replyCount DESC, dt.lastActivityAt DESC")
    Page<DiscussionThread> findMostActiveThreadsSince(@Param("since") LocalDateTime since, Pageable pageable);

    /* Count methods */
    // Count threads by category
    long countByCategory(DiscussionCategory category);

    // Count threads by author
    Long countByAuthor(User author);
}

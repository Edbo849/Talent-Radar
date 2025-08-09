package com.talentradar.repository.group;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.enums.GroupType;
import com.talentradar.model.group.UserGroup;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing UserGroup entities. Provides data access
 * operations for group management, membership tracking, search functionality,
 * and group statistics.
 */
@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    /* Basic finder methods */
    // Find active groups
    List<UserGroup> findByIsActiveTrue();

    // Find active groups with pagination ordered by creation date
    Page<UserGroup> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    // Find groups by creator
    List<UserGroup> findByCreatedByUser(User user);

    // Find groups by type
    List<UserGroup> findByGroupType(GroupType groupType);

    /* Creator-based finder methods */
    // Find groups created by user and active
    List<UserGroup> findByCreatedByUserAndIsActiveTrue(User user);

    // Find groups by creator
    List<UserGroup> findByCreatedByUserAndIsActiveTrueOrderByCreatedAtDesc(User createdByUser);

    /* Type-based finder methods */
    // Find groups by type and active status
    List<UserGroup> findByGroupTypeAndIsActiveTrue(GroupType groupType);

    // Find groups by type
    Page<UserGroup> findByGroupTypeAndIsActiveTrueOrderByMemberCountDesc(GroupType groupType, Pageable pageable);

    // Find public groups
    Page<UserGroup> findByGroupTypeAndIsActiveTrueOrderByCreatedAtDesc(GroupType groupType, Pageable pageable);

    /* Membership-based finder methods */
    // Find most active groups (by member count)
    Page<UserGroup> findByIsActiveTrueOrderByMemberCountDesc(Pageable pageable);

    // Find popular groups (top groups by member count)
    @Query("SELECT ug FROM UserGroup ug WHERE ug.isActive = true ORDER BY ug.memberCount DESC")
    List<UserGroup> findTopByIsActiveTrueOrderByMemberCountDesc();

    // Find groups that can accept new members
    @Query("SELECT ug FROM UserGroup ug WHERE ug.isActive = true AND ug.memberCount < ug.maxMembers ORDER BY ug.memberCount DESC")
    Page<UserGroup> findGroupsAcceptingMembers(Pageable pageable);

    // Find groups that are not full
    @Query("SELECT ug FROM UserGroup ug WHERE ug.isActive = true AND (ug.maxMembers IS NULL OR ug.memberCount < ug.maxMembers)")
    List<UserGroup> findActiveGroupsNotFull();

    // Find groups with member count greater than specified value
    @Query("SELECT ug FROM UserGroup ug WHERE ug.isActive = true AND ug.memberCount > :minMembers")
    List<UserGroup> findActiveGroupsWithMembersGreaterThan(@Param("minMembers") Integer minMembers);

    /* Search methods */
    // Search groups by name
    @Query("SELECT ug FROM UserGroup ug WHERE ug.name LIKE %:name% AND ug.isActive = true ORDER BY ug.memberCount DESC")
    Page<UserGroup> findByNameContainingAndIsActiveTrueOrderByMemberCountDesc(@Param("name") String name, Pageable pageable);

    // Search active groups by name or description
    @Query("SELECT ug FROM UserGroup ug WHERE ug.isActive = true AND (LOWER(ug.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(ug.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<UserGroup> searchActiveGroups(@Param("searchTerm") String searchTerm);

    /* Existence check methods */
    // Check if a group with the given name exists for a user
    boolean existsByNameAndCreatedBy(String name, User createdBy);

    /* Count methods */
    // Count groups created by user
    Long countByCreatedByUserAndIsActiveTrue(User createdByUser);

    // Count active groups
    long countByIsActiveTrue();
}

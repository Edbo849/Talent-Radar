package com.talentradar.repository.group;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.talentradar.model.enums.GroupRole;
import com.talentradar.model.group.GroupMember;
import com.talentradar.model.group.UserGroup;
import com.talentradar.model.user.User;

/**
 * Repository interface for managing GroupMember entities. Provides data access
 * operations for membership management, role-based queries, administrative
 * functions, and group participation tracking.
 */
@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    /* Basic finder methods */
    // Find member by group and user
    Optional<GroupMember> findByGroupAndUser(UserGroup group, User user);

    // Find all memberships for a specific user
    List<GroupMember> findByUser(User user);

    // Find groups a user belongs to
    List<GroupMember> findByUserOrderByJoinedAtDesc(User user);

    /* Group-based finder methods */
    // Find members by group ordered by join date (non-paginated)
    List<GroupMember> findByGroupOrderByJoinedAtAsc(UserGroup group);

    // Find members by group ordered by join date (paginated)
    Page<GroupMember> findByGroupOrderByJoinedAtAsc(UserGroup group, Pageable pageable);

    /* Role-based finder methods */
    // Find members by group and role
    List<GroupMember> findByGroupAndRole(UserGroup group, GroupRole role);

    // Find members by role
    List<GroupMember> findByRole(GroupRole role);

    // Find group admins and owners
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group AND (gm.role = 'OWNER' OR gm.role = 'ADMIN')")
    List<GroupMember> findGroupAdministrators(@Param("group") UserGroup group);

    // Find admin users by group
    @Query("SELECT gm.user FROM GroupMember gm WHERE gm.group = :group AND (gm.role = 'OWNER' OR gm.role = 'ADMIN') ORDER BY gm.role DESC")
    List<User> findAdminsByGroup(@Param("group") UserGroup group);

    // Find users by group and role
    @Query("SELECT gm.user FROM GroupMember gm WHERE gm.group = :group AND gm.role = :role")
    List<User> findUsersByGroupAndRole(@Param("group") UserGroup group, @Param("role") GroupRole role);

    /* Invitation-based finder methods */
    // Find members invited by a specific user
    List<GroupMember> findByInvitedBy(User invitedByUser);

    /* Existence and permission check methods */
    // Check if user is member of group
    boolean existsByGroupAndUser(UserGroup group, User user);

    // Check if user has admin privileges in group
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group = :group AND gm.user = :user AND (gm.role = 'OWNER' OR gm.role = 'ADMIN' OR gm.role = 'MODERATOR')")
    boolean hasAdminPrivileges(@Param("group") UserGroup group, @Param("user") User user);

    /* Count methods */
    // Count members in a group
    long countByGroup(UserGroup group);

    // Count groups a user belongs to
    long countByUser(User user);

    /* Deletion methods */
    // Delete all group members for a group
    void deleteByGroup(UserGroup group);
}

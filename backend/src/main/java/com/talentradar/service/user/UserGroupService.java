package com.talentradar.service.user;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.exception.GroupNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.enums.GroupRole;
import com.talentradar.model.enums.GroupType;
import com.talentradar.model.group.GroupMember;
import com.talentradar.model.group.UserGroup;
import com.talentradar.model.user.User;
import com.talentradar.repository.group.GroupMemberRepository;
import com.talentradar.repository.group.UserGroupRepository;
import com.talentradar.service.notification.NotificationService;

/**
 * Service responsible for managing user groups and group memberships. Handles
 * group creation, member management, and permission handling.
 */
@Service
@Transactional
public class UserGroupService {

    private static final Logger logger = LoggerFactory.getLogger(UserGroupService.class);

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public UserGroup createGroup(String name, String description, GroupType groupType,
            User creator, Integer maxMembers) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Group name cannot be null or empty");
            }
            if (creator == null) {
                throw new UserNotFoundException("Creator cannot be null");
            }
            if (groupType == null) {
                throw new IllegalArgumentException("Group type cannot be null");
            }

            String cleanName = name.trim();

            // Check if group name already exists for this user
            if (userGroupRepository.existsByNameAndCreatedBy(cleanName, creator)) {
                throw new IllegalArgumentException("Group name already exists for this user: " + cleanName);
            }

            UserGroup group = new UserGroup();
            group.setName(cleanName);
            group.setDescription(description != null ? description.trim() : null);
            group.setGroupType(groupType);
            group.setCreatedBy(creator);
            group.setMaxMembers(maxMembers);
            group.setIsActive(true);
            group.setMemberCount(1); // Creator is the first member

            UserGroup savedGroup = userGroupRepository.save(group);
            logger.info("Created new group: {} by user: {}", cleanName, creator.getUsername());

            // Add creator as owner member
            GroupMember creatorMember = new GroupMember();
            creatorMember.setGroup(savedGroup);
            creatorMember.setUser(creator);
            creatorMember.setRole(GroupRole.OWNER);
            groupMemberRepository.save(creatorMember);

            logger.info("Added creator {} as owner of group {}", creator.getUsername(), cleanName);
            return savedGroup;

        } catch (GroupNotFoundException | UserNotFoundException | IllegalArgumentException e) {
            logger.error("Error creating group: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error creating group '{}' by user {}: {}",
                    name, creator != null ? creator.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to create group", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public GroupMember joinGroup(UserGroup group, User user, User invitedBy) {
        try {
            if (group == null) {
                throw new GroupNotFoundException("Group cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            // Check if user is already a member
            if (groupMemberRepository.existsByGroupAndUser(group, user)) {
                throw new IllegalStateException("User is already a member of this group");
            }

            // Check if group can accept new members
            if (!group.canAcceptNewMembers()) {
                throw new IllegalStateException("Group has reached maximum capacity");
            }

            // Check if group is invite-only
            if (group.getGroupType() == GroupType.INVITE_ONLY && invitedBy == null) {
                throw new IllegalStateException("This group requires an invitation");
            }

            // Check if group is private
            if (group.getGroupType() == GroupType.PRIVATE) {
                throw new IllegalStateException("Cannot join private group without invitation");
            }

            GroupMember membership = new GroupMember();
            membership.setGroup(group);
            membership.setUser(user);
            membership.setRole(GroupRole.MEMBER);
            membership.setInvitedBy(invitedBy);

            GroupMember savedMembership = groupMemberRepository.save(membership);
            logger.info("User {} joined group {}", user.getUsername(), group.getName());

            // Update group member count
            group.incrementMemberCount();
            userGroupRepository.save(group);

            // Notify group admins
            try {
                notificationService.notifyGroupAdminsOfNewMember(group, user);
            } catch (RuntimeException e) {
                logger.warn("Failed to send notification for new member: {}", e.getMessage());
            }

            return savedMembership;

        } catch (GroupNotFoundException | UserNotFoundException | IllegalStateException e) {
            logger.error("Error joining group: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error joining group {} by user {}: {}",
                    group != null ? group.getName() : "null",
                    user != null ? user.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to join group", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void leaveGroup(UserGroup group, User user) {
        try {
            if (group == null) {
                throw new GroupNotFoundException("Group cannot be null");
            }
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            Optional<GroupMember> member = groupMemberRepository.findByGroupAndUser(group, user);
            if (member.isEmpty()) {
                throw new IllegalStateException("User is not a member of this group");
            }

            GroupMember memberEntity = member.get();

            // Don't allow the owner to leave if there are other members
            if (memberEntity.getRole() == GroupRole.OWNER && group.getMemberCount() > 1) {
                throw new IllegalStateException("Group owner cannot leave while there are other members. "
                        + "Transfer ownership or remove all members first.");
            }

            groupMemberRepository.delete(memberEntity);
            logger.info("User {} left group {}", user.getUsername(), group.getName());

            // Update member count
            group.setMemberCount(group.getMemberCount() - 1);
            userGroupRepository.save(group);

            // If this was the last member, deactivate the group
            if (group.getMemberCount() == 0) {
                group.setIsActive(false);
                userGroupRepository.save(group);
                logger.info("Deactivated group {} as it has no remaining members", group.getName());
            }

        } catch (GroupNotFoundException | UserNotFoundException | IllegalStateException e) {
            logger.error("Error leaving group: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error leaving group {} by user {}: {}",
                    group != null ? group.getName() : "null",
                    user != null ? user.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to leave group", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isGroupFull(Long groupId) {
        try {
            if (groupId == null) {
                throw new IllegalArgumentException("Group ID cannot be null");
            }

            Optional<UserGroup> groupOpt = userGroupRepository.findById(groupId);
            if (groupOpt.isEmpty()) {
                throw new GroupNotFoundException("Group not found with ID: " + groupId);
            }

            UserGroup group = groupOpt.get();
            Integer maxMembers = group.getMaxMembers();
            return maxMembers != null && group.getMemberCount() >= maxMembers;

        } catch (GroupNotFoundException | IllegalArgumentException e) {
            logger.error("Error checking if group is full: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error checking group capacity for ID {}: {}", groupId, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void incrementMemberCount(Long groupId) {
        try {
            if (groupId == null) {
                throw new IllegalArgumentException("Group ID cannot be null");
            }

            Optional<UserGroup> groupOpt = userGroupRepository.findById(groupId);
            if (groupOpt.isEmpty()) {
                throw new GroupNotFoundException("Group not found with ID: " + groupId);
            }

            UserGroup group = groupOpt.get();
            group.setMemberCount(group.getMemberCount() + 1);
            userGroupRepository.save(group);

        } catch (GroupNotFoundException | IllegalArgumentException e) {
            logger.error("Error incrementing member count: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error incrementing member count for group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to increment member count", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void decrementMemberCount(Long groupId) {
        try {
            if (groupId == null) {
                throw new IllegalArgumentException("Group ID cannot be null");
            }

            Optional<UserGroup> groupOpt = userGroupRepository.findById(groupId);
            if (groupOpt.isEmpty()) {
                throw new GroupNotFoundException("Group not found with ID: " + groupId);
            }

            UserGroup group = groupOpt.get();
            group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
            userGroupRepository.save(group);

        } catch (GroupNotFoundException | IllegalArgumentException e) {
            logger.error("Error decrementing member count: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error decrementing member count for group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to decrement member count", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void updateMemberRole(UserGroup group, User targetUser, GroupRole newRole, User requester) {
        try {
            if (group == null) {
                throw new GroupNotFoundException("Group cannot be null");
            }
            if (targetUser == null) {
                throw new UserNotFoundException("Target user cannot be null");
            }
            if (requester == null) {
                throw new UserNotFoundException("Requester cannot be null");
            }
            if (newRole == null) {
                throw new IllegalArgumentException("New role cannot be null");
            }

            // Check if requester has permission
            GroupMember requesterMembership = groupMemberRepository.findByGroupAndUser(group, requester)
                    .orElseThrow(() -> new IllegalStateException("Requester is not a member of this group"));

            if (!requesterMembership.getRole().canManageGroup()) {
                throw new IllegalStateException("Insufficient permissions to change member roles");
            }

            GroupMember targetMembership = groupMemberRepository.findByGroupAndUser(group, targetUser)
                    .orElseThrow(() -> new IllegalStateException("Target user is not a member of this group"));

            // Can't change the role of the group owner unless you are the owner
            if (targetMembership.getRole() == GroupRole.OWNER && requesterMembership.getRole() != GroupRole.OWNER) {
                throw new IllegalStateException("Only the group owner can change the owner's role");
            }

            // Can't promote someone to owner unless you are the owner
            if (newRole == GroupRole.OWNER && requesterMembership.getRole() != GroupRole.OWNER) {
                throw new IllegalStateException("Only the group owner can promote someone to owner");
            }

            GroupRole oldRole = targetMembership.getRole();
            targetMembership.setRole(newRole);
            groupMemberRepository.save(targetMembership);

            logger.info("Updated role for user {} in group {} from {} to {}",
                    targetUser.getUsername(), group.getName(), oldRole, newRole);

            // Notify the user
            try {
                notificationService.notifyUserOfRoleChange(targetUser, group, newRole);
            } catch (RuntimeException e) {
                logger.warn("Failed to send notification for role change: {}", e.getMessage());
            }

        } catch (GroupNotFoundException | UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error updating member role: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error updating role for user {} in group {}: {}",
                    targetUser != null ? targetUser.getUsername() : "null",
                    group != null ? group.getName() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to update member role", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void removeMember(UserGroup group, User targetUser, User requester) {
        try {
            if (group == null) {
                throw new GroupNotFoundException("Group cannot be null");
            }
            if (targetUser == null) {
                throw new UserNotFoundException("Target user cannot be null");
            }
            if (requester == null) {
                throw new UserNotFoundException("Requester cannot be null");
            }

            // Check permissions
            GroupMember requesterMembership = groupMemberRepository.findByGroupAndUser(group, requester)
                    .orElseThrow(() -> new IllegalStateException("Requester is not a member of this group"));

            if (!requesterMembership.getRole().canRemoveMembers()) {
                throw new IllegalStateException("Insufficient permissions to remove members");
            }

            GroupMember targetMembership = groupMemberRepository.findByGroupAndUser(group, targetUser)
                    .orElseThrow(() -> new IllegalStateException("Target user is not a member of this group"));

            // Can't remove the owner
            if (targetMembership.getRole() == GroupRole.OWNER) {
                throw new IllegalStateException("Cannot remove group owner");
            }

            // Admins can only be removed by owners
            if (targetMembership.getRole() == GroupRole.ADMIN && requesterMembership.getRole() != GroupRole.OWNER) {
                throw new IllegalStateException("Only group owner can remove administrators");
            }

            groupMemberRepository.delete(targetMembership);
            logger.info("User {} removed user {} from group {}",
                    requester.getUsername(), targetUser.getUsername(), group.getName());

            // Update group member count
            group.decrementMemberCount();
            userGroupRepository.save(group);

            // Notify the removed user
            try {
                notificationService.notifyUserOfGroupRemoval(targetUser, group, requester);
            } catch (RuntimeException e) {
                logger.warn("Failed to send notification for member removal: {}", e.getMessage());
            }

        } catch (GroupNotFoundException | UserNotFoundException | IllegalStateException e) {
            logger.error("Error removing member: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error removing user {} from group {} by {}: {}",
                    targetUser != null ? targetUser.getUsername() : "null",
                    group != null ? group.getName() : "null",
                    requester != null ? requester.getUsername() : "null",
                    e.getMessage());
            throw new RuntimeException("Failed to remove member", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<UserGroup> getPublicGroups(Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return userGroupRepository.findByGroupTypeAndIsActiveTrueOrderByMemberCountDesc(GroupType.PUBLIC, pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting public groups: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting public groups: {}", e.getMessage());
            throw new RuntimeException("Failed to get public groups", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public List<UserGroup> getUserGroups(User user) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            List<GroupMember> memberships = groupMemberRepository.findByUserOrderByJoinedAtDesc(user);
            return memberships.stream()
                    .map(GroupMember::getGroup)
                    .filter(group -> Boolean.TRUE.equals(group.getIsActive()))
                    .toList();

        } catch (UserNotFoundException e) {
            logger.error("Error getting user groups: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting groups for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to get user groups", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<GroupMember> getGroupMembers(UserGroup group, Pageable pageable) {
        try {
            if (group == null) {
                throw new GroupNotFoundException("Group cannot be null");
            }
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            return groupMemberRepository.findByGroupOrderByJoinedAtAsc(group, pageable);

        } catch (GroupNotFoundException | IllegalArgumentException e) {
            logger.error("Error getting group members: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting members for group {}: {}",
                    group != null ? group.getName() : "null", e.getMessage());
            throw new RuntimeException("Failed to get group members", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean isUserMember(UserGroup group, User user) {
        try {
            if (group == null || user == null) {
                return false;
            }

            return groupMemberRepository.existsByGroupAndUser(group, user);

        } catch (RuntimeException e) {
            logger.error("Runtime error checking membership for user {} in group {}: {}",
                    user != null ? user.getUsername() : "null",
                    group != null ? group.getName() : "null",
                    e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public boolean canUserManageGroup(UserGroup group, User user) {
        try {
            if (group == null || user == null) {
                return false;
            }

            return groupMemberRepository.hasAdminPrivileges(group, user);

        } catch (RuntimeException e) {
            logger.error("Runtime error checking management privileges for user {} in group {}: {}",
                    user != null ? user.getUsername() : "null",
                    group != null ? group.getName() : "null",
                    e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<UserGroup> searchGroups(String searchTerm, Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return userGroupRepository.findByIsActiveTrueOrderByMemberCountDesc(pageable);
            }

            return userGroupRepository.findByNameContainingAndIsActiveTrueOrderByMemberCountDesc(
                    searchTerm.trim(), pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error searching groups: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error searching groups with term '{}': {}", searchTerm, e.getMessage());
            throw new RuntimeException("Failed to search groups", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Optional<UserGroup> findById(Long id) {
        try {
            if (id == null) {
                logger.warn("Attempted to find group with null ID");
                return Optional.empty();
            }

            return userGroupRepository.findById(id);

        } catch (RuntimeException e) {
            logger.error("Runtime error finding group by ID {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public Page<UserGroup> getGroupsByType(GroupType type, Pageable pageable) {
        try {
            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            if (type == null) {
                return userGroupRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
            }

            return userGroupRepository.findByGroupTypeAndIsActiveTrueOrderByCreatedAtDesc(type, pageable);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting groups by type: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting groups by type {}: {}", type, e.getMessage());
            throw new RuntimeException("Failed to get groups by type", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getTotalGroupCount() {
        try {
            return userGroupRepository.countByIsActiveTrue();
        } catch (RuntimeException e) {
            logger.error("Runtime error getting total group count: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    @Transactional(readOnly = true)
    public long getUserGroupCount(User user) {
        try {
            if (user == null) {
                throw new UserNotFoundException("User cannot be null");
            }

            return groupMemberRepository.countByUser(user);

        } catch (UserNotFoundException e) {
            logger.error("Error getting user group count: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error getting group count for user {}: {}",
                    user != null ? user.getUsername() : "null", e.getMessage());
            return 0L;
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public void deleteGroup(Long groupId, User requester) {
        try {
            if (groupId == null) {
                throw new IllegalArgumentException("Group ID cannot be null");
            }
            if (requester == null) {
                throw new UserNotFoundException("Requester cannot be null");
            }

            UserGroup group = userGroupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

            // Check if requester is the owner
            GroupMember requesterMembership = groupMemberRepository.findByGroupAndUser(group, requester)
                    .orElseThrow(() -> new IllegalStateException("Requester is not a member of this group"));

            if (requesterMembership.getRole() != GroupRole.OWNER) {
                throw new IllegalStateException("Only group owner can delete the group");
            }

            // Delete all memberships first
            groupMemberRepository.deleteByGroup(group);

            // Delete the group
            userGroupRepository.delete(group);

            logger.info("Deleted group {} by owner {}", group.getName(), requester.getUsername());

        } catch (GroupNotFoundException | UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error deleting group: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error deleting group {} by user {}: {}",
                    groupId, requester != null ? requester.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to delete group", e);
        }
    }

    /**
     * Retrieves the current status of scheduled tasks.
     */
    public UserGroup updateGroup(Long groupId, User requester, String name, String description, Integer maxMembers) {
        try {
            if (groupId == null) {
                throw new IllegalArgumentException("Group ID cannot be null");
            }
            if (requester == null) {
                throw new UserNotFoundException("Requester cannot be null");
            }

            UserGroup group = userGroupRepository.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

            // Check if requester can manage the group
            if (!canUserManageGroup(group, requester)) {
                throw new IllegalStateException("Insufficient permissions to update group");
            }

            // Update fields if provided
            if (name != null && !name.trim().isEmpty()) {
                group.setName(name.trim());
            }
            if (description != null) {
                group.setDescription(description.trim());
            }
            if (maxMembers != null) {
                if (maxMembers > 0 && maxMembers < group.getMemberCount()) {
                    throw new IllegalArgumentException("Cannot set max members below current member count");
                }
                group.setMaxMembers(maxMembers);
            }

            UserGroup savedGroup = userGroupRepository.save(group);
            logger.info("Updated group {} by user {}", group.getName(), requester.getUsername());

            return savedGroup;

        } catch (GroupNotFoundException | UserNotFoundException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Error updating group: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Runtime error updating group {} by user {}: {}",
                    groupId, requester != null ? requester.getUsername() : "null", e.getMessage());
            throw new RuntimeException("Failed to update group", e);
        }
    }
}

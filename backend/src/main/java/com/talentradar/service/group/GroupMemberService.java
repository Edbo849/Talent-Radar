package com.talentradar.service.group;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentradar.model.enums.GroupRole;
import com.talentradar.model.group.GroupMember;
import com.talentradar.model.group.UserGroup;
import com.talentradar.model.user.User;
import com.talentradar.repository.group.GroupMemberRepository;
import com.talentradar.repository.group.UserGroupRepository;
import com.talentradar.repository.user.UserRepository;
import com.talentradar.service.user.UserGroupService;

/**
 * Service responsible for managing group membership operations. Handles
 * adding/removing members, role updates, and membership queries.
 */
@Service
@Transactional
public class GroupMemberService {

    private static final Logger logger = LoggerFactory.getLogger(GroupMemberService.class);

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupService userGroupService;

    /**
     * Adds a new member to a group with specified role and permissions.
     */
    public GroupMember addMember(Long groupId, Long userId, User invitedBy, GroupRole role) {
        try {
            UserGroup group = userGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with ID: " + groupId));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            // Check if group is full
            if (userGroupService.isGroupFull(groupId)) {
                throw new IllegalStateException("Group is full and cannot accept new members");
            }

            // Check if user is already a member
            if (groupMemberRepository.existsByGroupAndUser(group, user)) {
                throw new IllegalArgumentException("User is already a member of this group");
            }

            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setUser(user);
            member.setRole(role != null ? role : GroupRole.MEMBER);
            member.setJoinedAt(LocalDateTime.now());

            if (invitedBy != null) {
                member.setInvitedBy(invitedBy);
            }

            GroupMember savedMember = groupMemberRepository.save(member);

            // Update group member count
            userGroupService.incrementMemberCount(groupId);

            logger.info("Added user {} to group {} with role {}",
                    user.getUsername(), group.getName(), role);

            return savedMember;
        } catch (RuntimeException e) {
            logger.error("Error adding member to group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to add member to group", e);
        }
    }

    /**
     * Removes a member from a group with proper permission checks.
     */
    public void removeMember(Long groupId, Long userId, User requester) {
        try {
            UserGroup group = userGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with ID: " + groupId));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                    .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

            // Check permissions
            if (!canRemoveMember(group, member, requester)) {
                throw new IllegalArgumentException("You don't have permission to remove this member");
            }

            groupMemberRepository.delete(member);

            // Update group member count
            userGroupService.decrementMemberCount(groupId);

            logger.info("Removed user {} from group {} by {}",
                    user.getUsername(), group.getName(), requester.getUsername());
        } catch (RuntimeException e) {
            logger.error("Error removing member from group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to remove member from group", e);
        }
    }

    /**
     * Checks if a user has permission to remove a member from the group.
     */
    private boolean canRemoveMember(UserGroup group, GroupMember member, User requester) {
        try {
            // Group creator can remove anyone
            if (group.getCreatedBy().getId().equals(requester.getId())) {
                return true;
            }

            // Users can remove themselves
            if (member.getUser().getId().equals(requester.getId())) {
                return true;
            }

            // Admins can remove members (but not other admins or the creator)
            Optional<GroupMember> requesterMembership = groupMemberRepository
                    .findByGroupAndUser(group, requester);

            return requesterMembership.isPresent()
                    && requesterMembership.get().getRole() == GroupRole.ADMIN
                    && member.getRole() == GroupRole.MEMBER;
        } catch (Exception e) {
            logger.error("Error checking remove permissions for group {}: {}", group.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Updates a member's role within a group.
     */
    public void updateMemberRole(Long groupId, Long userId, GroupRole newRole, User requester) {
        try {
            UserGroup group = userGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with ID: " + groupId));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

            GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                    .orElseThrow(() -> new RuntimeException("User is not a member of this group"));

            // Only group creator can change roles
            if (!group.getCreatedBy().getId().equals(requester.getId())) {
                throw new IllegalArgumentException("Only the group creator can change member roles");
            }

            // Cannot change creator's role
            if (member.getUser().getId().equals(group.getCreatedBy().getId())) {
                throw new IllegalArgumentException("Cannot change the group creator's role");
            }

            member.setRole(newRole);
            groupMemberRepository.save(member);

            logger.info("Updated user {} role to {} in group {} by {}",
                    user.getUsername(), newRole, group.getName(), requester.getUsername());
        } catch (RuntimeException e) {
            logger.error("Error updating member role in group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to update member role", e);
        }
    }

    /**
     * Retrieves all members of a specific group ordered by join date.
     */
    @Transactional(readOnly = true)
    public List<GroupMember> getGroupMembers(Long groupId) {
        try {
            UserGroup group = userGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with ID: " + groupId));

            return groupMemberRepository.findByGroupOrderByJoinedAtAsc(group);
        } catch (Exception e) {
            logger.error("Error retrieving group members for group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to retrieve group members", e);
        }
    }

    /**
     * Retrieves all groups that a user is a member of.
     */
    @Transactional(readOnly = true)
    public List<GroupMember> getUserMemberships(User user) {
        try {
            return groupMemberRepository.findByUser(user);
        } catch (Exception e) {
            logger.error("Error retrieving user memberships for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to retrieve user memberships", e);
        }
    }

    /**
     * Checks if a user is a member of a specific group.
     */
    @Transactional(readOnly = true)
    public boolean isUserMember(Long groupId, User user) {
        try {
            UserGroup group = userGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with ID: " + groupId));

            return groupMemberRepository.existsByGroupAndUser(group, user);
        } catch (Exception e) {
            logger.error("Error checking user membership for group {}: {}", groupId, e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a user's membership information for a specific group.
     */
    @Transactional(readOnly = true)
    public Optional<GroupMember> getUserMembership(Long groupId, User user) {
        try {
            UserGroup group = userGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with ID: " + groupId));

            return groupMemberRepository.findByGroupAndUser(group, user);
        } catch (Exception e) {
            logger.error("Error retrieving user membership for group {}: {}", groupId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the total number of members in a group.
     */
    @Transactional(readOnly = true)
    public long getGroupMemberCount(Long groupId) {
        try {
            UserGroup group = userGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with ID: " + groupId));

            return groupMemberRepository.countByGroup(group);
        } catch (Exception e) {
            logger.error("Error getting member count for group {}: {}", groupId, e.getMessage());
            return 0;
        }
    }

    /**
     * Retrieves all members of a group with a specific role.
     */
    @Transactional(readOnly = true)
    public List<GroupMember> getGroupMembersByRole(Long groupId, GroupRole role) {
        try {
            UserGroup group = userGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with ID: " + groupId));

            return groupMemberRepository.findByGroupAndRole(group, role);
        } catch (Exception e) {
            logger.error("Error retrieving group members by role for group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to retrieve group members by role", e);
        }
    }
}

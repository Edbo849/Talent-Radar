package com.talentradar.controller.group;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.group.GroupMemberDTO;
import com.talentradar.dto.group.UserGroupCreateDTO;
import com.talentradar.dto.group.UserGroupDTO;
import com.talentradar.exception.GroupNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.enums.GroupRole;
import com.talentradar.model.enums.GroupType;
import com.talentradar.model.group.GroupMember;
import com.talentradar.model.group.UserGroup;
import com.talentradar.model.user.User;
import com.talentradar.service.user.UserGroupService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for managing user groups and memberships. Provides endpoints
 * for creating groups, managing members, and group permissions with proper
 * error handling and validation.
 */
@RestController
@RequestMapping("/api/groups")
public class UserGroupController {

    private static final Logger logger = LoggerFactory.getLogger(UserGroupController.class);

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves a paginated list of groups based on type and optional search
     * criteria.
     */
    @GetMapping
    public ResponseEntity<Page<UserGroupDTO>> getGroups(
            @RequestParam(defaultValue = "public") String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserGroup> groups;

            if (search != null && !search.trim().isEmpty()) {
                logger.debug("Searching groups with term: {}", search);
                groups = userGroupService.searchGroups(search, pageable);
            } else {
                GroupType groupType = GroupType.valueOf(type.toUpperCase());
                logger.debug("Retrieving groups of type: {}", groupType);
                groups = userGroupService.getGroupsByType(groupType, pageable);
            }

            Page<UserGroupDTO> groupDTOs = groups.map(this::convertToDTO);
            logger.info("Retrieved {} groups for page {} with size {}",
                    groupDTOs.getTotalElements(), page, size);

            return ResponseEntity.ok(groupDTOs);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid group type provided: {}", type, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving groups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates a new user group with the specified details.
     */
    @PostMapping
    public ResponseEntity<UserGroupDTO> createGroup(
            @Valid @RequestBody UserGroupCreateDTO createDTO,
            HttpServletRequest request) {

        try {
            User creator = userService.getCurrentUser(request);
            logger.info("User {} attempting to create group: {}",
                    creator.getUsername(), createDTO.getName());

            UserGroup group = userGroupService.createGroup(
                    createDTO.getName(),
                    createDTO.getDescription(),
                    GroupType.valueOf(createDTO.getGroupType()),
                    creator,
                    createDTO.getMaxMembers()
            );

            logger.info("Successfully created group {} with ID {}",
                    group.getName(), group.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(group));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid group type in create request: {}", createDTO.getGroupType(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating group", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves detailed information about a specific group.
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<UserGroupDTO> getGroup(@PathVariable Long groupId) {
        try {
            UserGroup group = userGroupService.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

            logger.debug("Retrieved group {} with ID {}", group.getName(), groupId);
            return ResponseEntity.ok(convertToDTO(group));
        } catch (GroupNotFoundException e) {
            logger.warn("Group not found: {}", groupId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving group with ID: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Allows a user to join an existing group.
     */
    @PostMapping("/{groupId}/join")
    public ResponseEntity<GroupMemberDTO> joinGroup(
            @PathVariable Long groupId,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);
            UserGroup group = userGroupService.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

            logger.info("User {} attempting to join group {}",
                    user.getUsername(), group.getName());

            GroupMember membership = userGroupService.joinGroup(group, user, null);

            logger.info("User {} successfully joined group {}",
                    user.getUsername(), group.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(convertMemberToDTO(membership));
        } catch (GroupNotFoundException e) {
            logger.warn("Group not found when joining: {}", groupId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("User cannot join group {}: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error joining group with ID: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Allows a user to leave a group they are currently a member of.
     */
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long groupId,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);
            UserGroup group = userGroupService.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

            logger.info("User {} attempting to leave group {}",
                    user.getUsername(), group.getName());

            userGroupService.leaveGroup(group, user);

            logger.info("User {} successfully left group {}",
                    user.getUsername(), group.getName());

            return ResponseEntity.noContent().build();
        } catch (GroupNotFoundException e) {
            logger.warn("Group not found when leaving: {}", groupId);
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            logger.warn("User cannot leave group {}: {}", groupId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error leaving group with ID: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a paginated list of members for a specific group.
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<Page<GroupMemberDTO>> getGroupMembers(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        try {
            UserGroup group = userGroupService.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

            Pageable pageable = PageRequest.of(page, size);
            Page<GroupMember> members = userGroupService.getGroupMembers(group, pageable);
            Page<GroupMemberDTO> memberDTOs = members.map(this::convertMemberToDTO);

            logger.debug("Retrieved {} members for group {}",
                    memberDTOs.getTotalElements(), group.getName());

            return ResponseEntity.ok(memberDTOs);
        } catch (GroupNotFoundException e) {
            logger.warn("Group not found when retrieving members: {}", groupId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving members for group ID: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates the role of a specific member within a group.
     */
    @PutMapping("/{groupId}/members/{userId}/role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam String role,
            HttpServletRequest request) {

        try {
            User requester = userService.getCurrentUser(request);
            User targetUser = userService.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
            UserGroup group = userGroupService.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

            GroupRole newRole = GroupRole.valueOf(role.toUpperCase());

            logger.info("User {} updating role of user {} in group {} to {}",
                    requester.getUsername(), targetUser.getUsername(),
                    group.getName(), newRole);

            userGroupService.updateMemberRole(group, targetUser, newRole, requester);

            logger.info("Successfully updated role of user {} in group {} to {}",
                    targetUser.getUsername(), group.getName(), newRole);

            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException | GroupNotFoundException e) {
            logger.warn("Resource not found when updating member role: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid role provided: {}", role, e);
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot update member role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error updating member role in group ID: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Removes a member from a group.
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            HttpServletRequest request) {

        try {
            User requester = userService.getCurrentUser(request);
            User targetUser = userService.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
            UserGroup group = userGroupService.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("Group not found with ID: " + groupId));

            logger.info("User {} attempting to remove user {} from group {}",
                    requester.getUsername(), targetUser.getUsername(), group.getName());

            userGroupService.removeMember(group, targetUser, requester);

            logger.info("User {} successfully removed user {} from group {}",
                    requester.getUsername(), targetUser.getUsername(), group.getName());

            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException | GroupNotFoundException e) {
            logger.warn("Resource not found when removing member: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot remove member: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error removing member from group ID: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all groups that the current user is a member of.
     */
    @GetMapping("/my-groups")
    public ResponseEntity<List<UserGroupDTO>> getMyGroups(HttpServletRequest request) {
        try {
            User user = userService.getCurrentUser(request);
            logger.debug("Retrieving groups for user: {}", user.getUsername());

            List<UserGroup> groups = userGroupService.getUserGroups(user);
            List<UserGroupDTO> groupDTOs = groups.stream()
                    .map(this::convertToDTO)
                    .toList();

            logger.info("Retrieved {} groups for user {}", groupDTOs.size(), user.getUsername());
            return ResponseEntity.ok(groupDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving user's groups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Converts a UserGroup entity to a UserGroupDTO for API responses.
     */
    private UserGroupDTO convertToDTO(UserGroup group) {
        if (group == null) {
            return null;
        }

        UserGroupDTO dto = new UserGroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setGroupType(group.getGroupType().toString());
        dto.setCreatedByUserId(group.getCreatedBy().getId());
        dto.setCreatedByUsername(group.getCreatedBy().getUsername());
        String fullName = null;
        if (group.getCreatedBy().getFirstName() != null || group.getCreatedBy().getLastName() != null) {
            fullName = (group.getCreatedBy().getFirstName() != null ? group.getCreatedBy().getFirstName() : "")
                    + " " + (group.getCreatedBy().getLastName() != null ? group.getCreatedBy().getLastName() : "");
            fullName = fullName.trim();
        }
        dto.setCreatedByFullName(fullName);
        dto.setMemberCount(group.getMemberCount());
        dto.setMaxMembers(group.getMaxMembers());
        dto.setGroupImageUrl(group.getGroupImageUrl());
        dto.setIsActive(group.getIsActive());
        dto.setLastActivityAt(group.getLastActivityAt());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());

        return dto;
    }

    /**
     * Converts a GroupMember entity to a GroupMemberDTO for API responses.
     */
    private GroupMemberDTO convertMemberToDTO(GroupMember member) {
        if (member == null) {
            return null;
        }

        GroupMemberDTO dto = new GroupMemberDTO();
        dto.setId(member.getId());
        dto.setGroupId(member.getGroup().getId());
        dto.setUserId(member.getUser().getId());
        dto.setUsername(member.getUser().getUsername());
        String fullName = null;
        if (member.getUser().getFirstName() != null || member.getUser().getLastName() != null) {
            fullName = (member.getUser().getFirstName() != null ? member.getUser().getFirstName() : "")
                    + " " + (member.getUser().getLastName() != null ? member.getUser().getLastName() : "");
            fullName = fullName.trim();
        }
        dto.setFullName(fullName);
        dto.setRole(member.getRole().toString());
        dto.setUserRole(member.getUser().getRole().toString());
        dto.setBadgeLevel(member.getUser().getBadgeLevel().toString());
        dto.setReputationScore(member.getUser().getReputationScore());
        dto.setIsVerified(member.getUser().getIsVerified());
        dto.setOrganisation(member.getUser().getOrganisation());
        dto.setProfileImageUrl(member.getUser().getProfileImageUrl());
        dto.setInvitedByUserId(member.getInvitedBy() != null ? member.getInvitedBy().getId() : null);
        dto.setInvitedByUsername(member.getInvitedBy() != null ? member.getInvitedBy().getUsername() : null);
        dto.setJoinedAt(member.getJoinedAt());
        dto.setCreatedAt(member.getCreatedAt());

        return dto;
    }
}

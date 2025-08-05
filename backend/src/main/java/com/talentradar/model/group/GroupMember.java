package com.talentradar.model.group;

import java.time.LocalDateTime;
import java.util.Objects;

import com.talentradar.model.enums.GroupRole;
import com.talentradar.model.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing membership relationships between users and groups. Tracks
 * group participation with roles, permissions, and membership status for
 * community management and access control.
 */
@Entity
@Table(name = "group_members")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @NotNull(message = "Group is required")
    private UserGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role = GroupRole.MEMBER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_user_id")
    private User invitedBy;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public GroupMember() {
    }

    public GroupMember(UserGroup group, User user, GroupRole role) {
        this.group = group;
        this.user = user;
        this.role = role;
    }

    public GroupMember(UserGroup group, User user, GroupRole role, User invitedBy) {
        this.group = group;
        this.user = user;
        this.role = role;
        this.invitedBy = invitedBy;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.joinedAt == null) {
            this.joinedAt = LocalDateTime.now();
        }
    }

    // Helper methods
    public boolean isOwner() {
        return role == GroupRole.OWNER;
    }

    public boolean isAdmin() {
        return role == GroupRole.OWNER || role == GroupRole.ADMIN;
    }

    public boolean canModerate() {
        return role == GroupRole.OWNER || role == GroupRole.ADMIN || role == GroupRole.MODERATOR;
    }

    public boolean canInvite() {
        return role == GroupRole.OWNER || role == GroupRole.ADMIN;
    }

    public boolean canRemoveMembers() {
        return role == GroupRole.OWNER || role == GroupRole.ADMIN;
    }

    public boolean canManageGroup() {
        return role == GroupRole.OWNER || role == GroupRole.ADMIN;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GroupRole getRole() {
        return role;
    }

    public void setRole(GroupRole role) {
        this.role = role;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupMember that = (GroupMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GroupMember{"
                + "id=" + id
                + ", role=" + role
                + ", joinedAt=" + joinedAt
                + '}';
    }
}

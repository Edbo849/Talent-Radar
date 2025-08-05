package com.talentradar.model.group;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.talentradar.model.enums.GroupType;
import com.talentradar.model.user.User;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entity representing user-created groups within the platform. Enables users to
 * form communities around specific interests such as favorite teams, scouting
 * networks, or discussion topics.
 */
@Entity
@Table(name = "user_groups")
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String name;

    @Column(columnDefinition = "TEXT")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false)
    private GroupType groupType = GroupType.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "member_count")
    private Integer memberCount = 0;

    @Column(name = "max_members")
    private Integer maxMembers;

    @Column(name = "group_image_url")
    private String groupImageUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<GroupMember> members = new HashSet<>();

    // Constructors
    public UserGroup() {
    }

    public UserGroup(String name, User createdBy, GroupType groupType) {
        this.name = name;
        this.createdBy = createdBy;
        this.groupType = groupType;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public boolean canAcceptNewMembers() {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }
        if (maxMembers != null && memberCount != null) {
            return memberCount < maxMembers;
        }
        return true;
    }

    public void incrementMemberCount() {
        this.memberCount = java.util.Objects.requireNonNullElse(this.memberCount, 0) + 1;
    }

    public void decrementMemberCount() {
        this.memberCount = Math.max(0, java.util.Objects.requireNonNullElse(this.memberCount, 0) - 1);
    }

    public boolean isPublic() {
        return groupType == GroupType.PUBLIC;
    }

    public boolean isPrivate() {
        return groupType == GroupType.PRIVATE;
    }

    public boolean isInviteOnly() {
        return groupType == GroupType.INVITE_ONLY;
    }

    public boolean isFull() {
        return maxMembers != null && memberCount != null && memberCount >= maxMembers;
    }

    public boolean canJoin() {
        return isActive && !isFull() && (isPublic() || isInviteOnly());
    }

    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public String getGroupImageUrl() {
        return groupImageUrl;
    }

    public void setGroupImageUrl(String groupImageUrl) {
        this.groupImageUrl = groupImageUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<GroupMember> getMembers() {
        return members;
    }

    public void setMembers(Set<GroupMember> members) {
        this.members = members;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserGroup userGroup = (UserGroup) o;
        return Objects.equals(id, userGroup.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserGroup{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", groupType=" + groupType
                + ", memberCount=" + memberCount
                + ", isActive=" + isActive
                + '}';
    }
}

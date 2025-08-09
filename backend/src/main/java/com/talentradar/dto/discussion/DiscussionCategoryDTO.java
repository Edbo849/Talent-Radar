package com.talentradar.dto.discussion;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for DiscussionCategory entities. Used to transfer
 * discussion category information between layers including name, description,
 * styling properties, and derived statistics.
 */
public class DiscussionCategoryDTO {

    // Primary identifier
    private Long id;

    // Category name (required, max 100 characters, unique)
    private String name;

    // Category description
    private String description;

    // Hex color code for category styling (max 7 characters)
    private String color;

    // Icon identifier for category display (max 50 characters)
    private String icon;

    // Display order for category sorting
    private Integer displayOrder;

    // Whether the category is currently active
    private Boolean isActive;

    // Timestamp when category was created
    private LocalDateTime createdAt;

    // Derived fields (not directly from model)
    // Number of threads in this category (calculated field)
    private Integer threadCount;

    // Constructors
    public DiscussionCategoryDTO() {
    }

    public DiscussionCategoryDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters and setters
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Derived field getters and setters
    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }
}

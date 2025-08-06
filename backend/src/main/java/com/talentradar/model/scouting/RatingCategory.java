package com.talentradar.model.scouting;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.io.IOException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Entity representing categories for player rating and evaluation. Defines
 * standardized rating criteria such as technical skills, physical attributes,
 * and mental qualities used in scouting assessments.
 */
@Entity
@Table(name = "rating_categories")
public class RatingCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    @NotBlank(message = "Category name is required")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "position_specific")
    private Boolean positionSpecific = false;

    @Column(name = "applicable_positions", columnDefinition = "JSON")
    private String applicablePositionsJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RatingCategory() {
    }

    public RatingCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods for JSON fields
    public List<String> getApplicablePositions() {
        if (applicablePositionsJson == null) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(applicablePositionsJson, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void setApplicablePositions(List<String> positions) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.applicablePositionsJson = mapper.writeValueAsString(positions);
        } catch (IOException e) {
            this.applicablePositionsJson = "[]";
        }
    }

    public boolean isApplicableForPosition(String position) {
        return getApplicablePositions().contains(position);
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

    public Boolean getPositionSpecific() {
        return positionSpecific;
    }

    public void setPositionSpecific(Boolean positionSpecific) {
        this.positionSpecific = positionSpecific;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RatingCategory that = (RatingCategory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RatingCategory{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", positionSpecific=" + positionSpecific
                + '}';
    }
}

package com.talentradar.dto.poll;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating new Poll entities. Used to transfer poll
 * creation data from client to server with validation constraints matching the
 * model requirements.
 */
public class PollCreateDTO {

    // Required fields matching model constraints
    // Poll question (required, max 300 characters to match model)
    @NotBlank(message = "Question is required")
    @Size(max = 300, message = "Question cannot exceed 300 characters")
    private String question;

    // Poll description (optional)
    private String description;

    // Poll type (required, will be converted to PollType enum)
    @NotBlank(message = "Poll type is required")
    private String pollType;

    // Poll options (required, between 2 and 10 options)
    @NotEmpty(message = "At least two options are required")
    @Size(min = 2, max = 10, message = "Poll must have between 2 and 10 options")
    private List<String> options;

    // Optional associations
    private Long threadId;
    private Long playerId;

    // Optional configuration
    private Integer expiresInHours;
    private Boolean isAnonymous = false;

    // Constructors
    public PollCreateDTO() {
    }

    public PollCreateDTO(String question, String pollType, List<String> options) {
        this.question = question;
        this.pollType = pollType;
        this.options = options;
    }

    // Getters and setters
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPollType() {
        return pollType;
    }

    public void setPollType(String pollType) {
        this.pollType = pollType;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Integer getExpiresInHours() {
        return expiresInHours;
    }

    public void setExpiresInHours(Integer expiresInHours) {
        this.expiresInHours = expiresInHours;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    @Override
    public String toString() {
        return "PollCreateDTO{"
                + "question='" + question + '\''
                + ", pollType='" + pollType + '\''
                + ", optionsCount=" + (options != null ? options.size() : 0)
                + ", isAnonymous=" + isAnonymous
                + '}';
    }
}

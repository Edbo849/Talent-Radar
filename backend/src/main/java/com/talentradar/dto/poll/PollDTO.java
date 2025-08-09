package com.talentradar.dto.poll;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Poll entities. Used to transfer poll information
 * between layers including poll details, options, voting statistics, and user
 * interaction context.
 */
public class PollDTO {

    // Primary identifier
    private Long id;

    // Poll content
    private String question;
    private String description;

    // Poll configuration
    private String pollType; // SINGLE_CHOICE, MULTIPLE_CHOICE, RATING, YES_NO
    private Boolean isAnonymous;
    private LocalDateTime expiresAt;
    private Boolean isActive;

    // Vote statistics
    private Integer totalVotes;

    // Timestamp fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Author information (from User relationship)
    private Long authorId;
    private String authorName;
    private String authorProfileImageUrl;
    private String authorBadgeLevel;

    // Thread/Player association
    private Long threadId;
    private String threadTitle;
    private Long playerId;
    private String playerName;

    // Poll options
    private List<PollOptionDTO> options;

    // User context (derived fields)
    private Boolean hasUserVoted;
    private List<Long> userVotedOptionIds;
    private Boolean canUserVote;

    // Constructors
    public PollDTO() {
    }

    public PollDTO(Long id, String question, String pollType, String authorName) {
        this.id = id;
        this.question = question;
        this.pollType = pollType;
        this.authorName = authorName;
    }

    // Helper methods
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canVote() {
        return Boolean.TRUE.equals(isActive) && !isExpired() && !Boolean.TRUE.equals(hasUserVoted);
    }

    public boolean isSingleChoice() {
        return "SINGLE_CHOICE".equals(pollType) || "YES_NO".equals(pollType);
    }

    public boolean isMultipleChoice() {
        return "MULTIPLE_CHOICE".equals(pollType);
    }

    public boolean isRating() {
        return "RATING".equals(pollType);
    }

    public boolean isYesNo() {
        return "YES_NO".equals(pollType);
    }

    public String getTimeUntilExpiry() {
        if (expiresAt == null) {
            return "No expiry";
        }
        if (isExpired()) {
            return "Expired";
        }

        LocalDateTime now = LocalDateTime.now();
        long hours = java.time.Duration.between(now, expiresAt).toHours();

        if (hours < 1) {
            long minutes = java.time.Duration.between(now, expiresAt).toMinutes();
            return minutes + " minutes remaining";
        } else if (hours < 24) {
            return hours + " hours remaining";
        } else {
            long days = java.time.Duration.between(now, expiresAt).toDays();
            return days + " days remaining";
        }
    }

    // Calculate percentages for all options
    public void calculatePercentages() {
        if (options != null && totalVotes != null) {
            options.forEach(option -> option.calculatePercentage(totalVotes));
        }
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(Integer totalVotes) {
        this.totalVotes = totalVotes;
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

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorProfileImageUrl() {
        return authorProfileImageUrl;
    }

    public void setAuthorProfileImageUrl(String authorProfileImageUrl) {
        this.authorProfileImageUrl = authorProfileImageUrl;
    }

    public String getAuthorBadgeLevel() {
        return authorBadgeLevel;
    }

    public void setAuthorBadgeLevel(String authorBadgeLevel) {
        this.authorBadgeLevel = authorBadgeLevel;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getThreadTitle() {
        return threadTitle;
    }

    public void setThreadTitle(String threadTitle) {
        this.threadTitle = threadTitle;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public List<PollOptionDTO> getOptions() {
        return options;
    }

    public void setOptions(List<PollOptionDTO> options) {
        this.options = options;
    }

    public Boolean getHasUserVoted() {
        return hasUserVoted;
    }

    public void setHasUserVoted(Boolean hasUserVoted) {
        this.hasUserVoted = hasUserVoted;
    }

    public List<Long> getUserVotedOptionIds() {
        return userVotedOptionIds;
    }

    public void setUserVotedOptionIds(List<Long> userVotedOptionIds) {
        this.userVotedOptionIds = userVotedOptionIds;
    }

    public Boolean getCanUserVote() {
        return canUserVote;
    }

    public void setCanUserVote(Boolean canUserVote) {
        this.canUserVote = canUserVote;
    }

    @Override
    public String toString() {
        return "PollDTO{"
                + "id=" + id
                + ", question='" + question + '\''
                + ", pollType='" + pollType + '\''
                + ", totalVotes=" + totalVotes
                + ", isExpired=" + isExpired()
                + '}';
    }
}

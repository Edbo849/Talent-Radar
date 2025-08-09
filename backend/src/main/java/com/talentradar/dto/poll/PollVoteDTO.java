package com.talentradar.dto.poll;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for PollVote entities. Used to transfer poll vote
 * information between layers including vote details, user information,
 * anonymity settings, and voting metadata.
 */
public class PollVoteDTO {

    // Primary identifier
    private Long id;

    // Poll and option references
    private Long pollId;
    private Long optionId;
    private String optionText;

    // User information (from User relationship)
    private Long userId;
    private String username;
    private String voterName;

    // Anonymous voting support
    private String ipAddress; // For anonymous polls
    private String userAgent;
    private Boolean isAnonymous;

    // Timestamp field
    private LocalDateTime createdAt;

    // Constructors
    public PollVoteDTO() {
    }

    public PollVoteDTO(Long id, Long pollId, Long optionId, String optionText) {
        this.id = id;
        this.pollId = pollId;
        this.optionId = optionId;
        this.optionText = optionText;
    }

    // Helper methods
    public String getDisplayName() {
        if (Boolean.TRUE.equals(isAnonymous)) {
            return "Anonymous";
        }
        return voterName != null ? voterName : username;
    }

    public boolean isUserVote(Long currentUserId) {
        return userId != null && userId.equals(currentUserId);
    }

    public boolean isAnonymousVote() {
        return userId == null || Boolean.TRUE.equals(isAnonymous);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPollId() {
        return pollId;
    }

    public void setPollId(Long pollId) {
        this.pollId = pollId;
    }

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVoterName() {
        return voterName;
    }

    public void setVoterName(String voterName) {
        this.voterName = voterName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PollVoteDTO{"
                + "id=" + id
                + ", pollId=" + pollId
                + ", optionId=" + optionId
                + ", username='" + username + '\''
                + ", isAnonymous=" + isAnonymous
                + '}';
    }
}

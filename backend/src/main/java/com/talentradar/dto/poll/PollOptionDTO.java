package com.talentradar.dto.poll;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for PollOption entities. Used to transfer poll option
 * information between layers including option text, vote statistics, display
 * order, and user voting status.
 */
public class PollOptionDTO {

    // Primary identifier
    private Long id;

    // Option content
    private String optionText;

    // Display and ordering
    private Integer displayOrder;

    // Vote statistics
    private Integer voteCount;
    private Double percentage;

    // Timestamp field
    private LocalDateTime createdAt;

    // User context (derived fields)
    private Boolean hasUserVoted; // Whether current user voted for this option

    // Constructors
    public PollOptionDTO() {
    }

    public PollOptionDTO(Long id, String optionText, Integer voteCount) {
        this.id = id;
        this.optionText = optionText;
        this.voteCount = voteCount;
    }

    // Helper methods
    public void calculatePercentage(Integer totalVotes) {
        if (totalVotes == null || totalVotes == 0) {
            this.percentage = 0.0;
        } else {
            this.percentage = (voteCount.doubleValue() / totalVotes.doubleValue()) * 100.0;
        }
    }

    public boolean isWinning(Integer highestVoteCount) {
        return voteCount != null && voteCount.equals(highestVoteCount) && voteCount > 0;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getHasUserVoted() {
        return hasUserVoted;
    }

    public void setHasUserVoted(Boolean hasUserVoted) {
        this.hasUserVoted = hasUserVoted;
    }

    @Override
    public String toString() {
        return "PollOptionDTO{"
                + "id=" + id
                + ", optionText='" + optionText + '\''
                + ", voteCount=" + voteCount
                + ", percentage=" + percentage
                + '}';
    }
}

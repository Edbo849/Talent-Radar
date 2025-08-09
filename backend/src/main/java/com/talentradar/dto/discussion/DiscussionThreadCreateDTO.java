package com.talentradar.dto.discussion;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating new DiscussionThread entities. Used to
 * transfer thread creation data from client to server with validation
 * constraints and optional metadata for different thread types.
 */
public class DiscussionThreadCreateDTO {

    // Required fields matching model constraints
    // Category reference (required)
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    // Thread title (required, max 200 characters to match model)
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    // Thread content (required, text field)
    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    // Thread type (will be converted to ThreadType enum)
    @NotBlank(message = "Thread type is required")
    private String threadType;

    // Optional status flags (matching model defaults)
    private Boolean isPinned = false;
    private Boolean isFeatured = false;

    // Optional derived/metadata fields
    // For player-related discussions
    private List<Long> playerIds;

    // For match performance threads
    private String matchDate;
    private String opponentTeam;

    // For transfer speculation threads
    private String transferWindow;
    private Long fromClubId;
    private Long toClubId;

    // Constructors
    public DiscussionThreadCreateDTO() {
    }

    public DiscussionThreadCreateDTO(Long categoryId, String title, String content, String threadType) {
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.threadType = threadType;
    }

    // Getters and setters
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getThreadType() {
        return threadType;
    }

    public void setThreadType(String threadType) {
        this.threadType = threadType;
    }

    public Boolean getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public List<Long> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<Long> playerIds) {
        this.playerIds = playerIds;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }

    public String getOpponentTeam() {
        return opponentTeam;
    }

    public void setOpponentTeam(String opponentTeam) {
        this.opponentTeam = opponentTeam;
    }

    public String getTransferWindow() {
        return transferWindow;
    }

    public void setTransferWindow(String transferWindow) {
        this.transferWindow = transferWindow;
    }

    public Long getFromClubId() {
        return fromClubId;
    }

    public void setFromClubId(Long fromClubId) {
        this.fromClubId = fromClubId;
    }

    public Long getToClubId() {
        return toClubId;
    }

    public void setToClubId(Long toClubId) {
        this.toClubId = toClubId;
    }

    @Override
    public String toString() {
        return "DiscussionThreadCreateDTO{"
                + "categoryId=" + categoryId
                + ", title='" + title + '\''
                + ", threadType='" + threadType + '\''
                + ", playerIds=" + playerIds
                + '}';
    }
}

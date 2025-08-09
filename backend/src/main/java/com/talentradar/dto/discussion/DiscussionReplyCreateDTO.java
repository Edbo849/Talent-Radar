package com.talentradar.dto.discussion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating new DiscussionReply entities. Used to
 * transfer reply creation data from client to server with validation
 * constraints and optional features for enhanced functionality.
 */
public class DiscussionReplyCreateDTO {

    // Required fields matching model constraints
    // Reply content (required, text field to match model)
    @NotBlank(message = "Reply content is required")
    @Size(max = 5000, message = "Reply content must not exceed 5000 characters")
    private String content;

    // Optional parent reply for nested threading
    private Long parentReplyId;

    // Optional enhancement fields (not in model but useful for functionality)
    // For mentioning other users
    private String mentionedUsernames;

    // For attaching files or images
    private String attachmentUrl;
    private String attachmentType; // IMAGE, FILE, LINK

    // Constructors
    public DiscussionReplyCreateDTO() {
    }

    public DiscussionReplyCreateDTO(String content) {
        this.content = content;
    }

    public DiscussionReplyCreateDTO(String content, Long parentReplyId) {
        this.content = content;
        this.parentReplyId = parentReplyId;
    }

    // Helper methods
    public boolean isNestedReply() {
        return parentReplyId != null;
    }

    public boolean hasAttachment() {
        return attachmentUrl != null && !attachmentUrl.trim().isEmpty();
    }

    public boolean hasMentions() {
        return mentionedUsernames != null && !mentionedUsernames.trim().isEmpty();
    }

    // Getters and setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentReplyId() {
        return parentReplyId;
    }

    public void setParentReplyId(Long parentReplyId) {
        this.parentReplyId = parentReplyId;
    }

    public String getMentionedUsernames() {
        return mentionedUsernames;
    }

    public void setMentionedUsernames(String mentionedUsernames) {
        this.mentionedUsernames = mentionedUsernames;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    @Override
    public String toString() {
        return "DiscussionReplyCreateDTO{"
                + "contentLength=" + (content != null ? content.length() : 0)
                + ", parentReplyId=" + parentReplyId
                + ", hasAttachment=" + hasAttachment()
                + ", hasMentions=" + hasMentions()
                + '}';
    }
}

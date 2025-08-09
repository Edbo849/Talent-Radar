package com.talentradar.dto.messaging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating new PrivateMessage entities. Used to
 * transfer message creation data from client to server with validation
 * constraints matching the model requirements.
 */
public class MessageCreateDTO {

    // Required fields matching model constraints
    // Conversation reference (required)
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    // Message content (required)
    @NotBlank(message = "Message text is required")
    private String messageText;

    // Message type (defaults to TEXT)
    private String messageType = "TEXT";

    // Optional attachment URL
    private String attachmentUrl;

    // Optional reply reference for threaded messaging
    private Long replyToMessageId;

    // Constructors
    public MessageCreateDTO() {
    }

    public MessageCreateDTO(Long conversationId, String messageText) {
        this.conversationId = conversationId;
        this.messageText = messageText;
    }

    public MessageCreateDTO(Long conversationId, String messageText, String messageType) {
        this.conversationId = conversationId;
        this.messageText = messageText;
        this.messageType = messageType;
    }

    // Getters and setters
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public Long getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(Long replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    @Override
    public String toString() {
        return "MessageCreateDTO{"
                + "conversationId=" + conversationId
                + ", messageType='" + messageType + '\''
                + ", hasAttachment=" + (attachmentUrl != null)
                + ", isReply=" + (replyToMessageId != null)
                + '}';
    }
}

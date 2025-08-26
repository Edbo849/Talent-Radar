package com.talentradar.controller.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.dto.player.PlayerCommentCreateDTO;
import com.talentradar.dto.player.PlayerCommentDTO;
import com.talentradar.exception.PlayerNotFoundException;
import com.talentradar.exception.UserNotFoundException;
import com.talentradar.model.enums.VoteType;
import com.talentradar.model.player.PlayerComment;
import com.talentradar.model.user.User;
import com.talentradar.service.player.PlayerCommentService;
import com.talentradar.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST controller for managing player comments and discussions. Provides
 * endpoints for creating, retrieving, voting on, and managing player comments
 * with proper error handling and validation.
 */
@RestController
@RequestMapping("/api/players/{playerId}/comments")
@CrossOrigin(origins = "http://localhost:3000")
public class PlayerCommentController {

    private static final Logger logger = LoggerFactory.getLogger(PlayerCommentController.class);

    @Autowired
    private PlayerCommentService commentService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves paginated comments for a specific player.
     */
    @GetMapping
    public ResponseEntity<Page<PlayerCommentDTO>> getPlayerComments(
            @PathVariable Long playerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {

        try {
            Pageable pageable = PageRequest.of(page, size);

            logger.debug("Retrieving comments for player {} (page: {}, size: {})", playerId, page, size);

            Page<PlayerComment> comments = commentService.getPlayerComments(playerId, pageable);
            Page<PlayerCommentDTO> commentDTOs = comments.map(this::convertToDTO);

            logger.info("Retrieved {} comments for player {}", commentDTOs.getTotalElements(), playerId);
            return ResponseEntity.ok(commentDTOs);
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when retrieving comments: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid pagination parameters: page={}, size={}", page, size);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving comments for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates a new comment for a specific player.
     */
    @PostMapping
    public ResponseEntity<PlayerCommentDTO> createComment(
            @PathVariable Long playerId,
            @Valid @RequestBody PlayerCommentCreateDTO createDTO,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);

            logger.info("User {} creating comment for player {}", user.getUsername(), playerId);

            PlayerComment comment = commentService.createComment(
                    playerId,
                    user,
                    createDTO.getContent(),
                    createDTO.getParentCommentId()
            );

            logger.info("Successfully created comment with ID: {}", comment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(comment));
        } catch (UserNotFoundException e) {
            logger.warn("User not found when creating comment");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (PlayerNotFoundException e) {
            logger.warn("Player not found when creating comment: {}", playerId);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid comment data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating comment for player: {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates an existing comment.
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<PlayerCommentDTO> updateComment(
            @PathVariable Long playerId,
            @PathVariable Long commentId,
            @Valid @RequestBody PlayerCommentCreateDTO updateDTO,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);

            logger.info("User {} updating comment {} for player {}", user.getUsername(), commentId, playerId);

            PlayerComment updatedComment = commentService.updateComment(
                    commentId,
                    user,
                    updateDTO.getContent()
            );

            logger.info("Successfully updated comment: {}", commentId);
            return ResponseEntity.ok(convertToDTO(updatedComment));
        } catch (UserNotFoundException e) {
            logger.warn("User not found when updating comment");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot update comment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error updating comment: {}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a specific comment.
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long playerId,
            @PathVariable Long commentId,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);

            logger.info("User {} deleting comment {} for player {}", user.getUsername(), commentId, playerId);

            commentService.deleteComment(commentId, user);

            logger.info("Successfully deleted comment: {}", commentId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            logger.warn("User not found when deleting comment");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalStateException e) {
            logger.warn("Cannot delete comment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error deleting comment: {}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Votes on a specific comment.
     */
    @PostMapping("/{commentId}/vote")
    public ResponseEntity<Void> voteOnComment(
            @PathVariable Long playerId,
            @PathVariable Long commentId,
            @RequestParam String voteType,
            HttpServletRequest request) {

        try {
            User user = userService.getCurrentUser(request);
            VoteType vote = VoteType.valueOf(voteType.toUpperCase());

            logger.debug("User {} voting {} on comment {}", user.getUsername(), vote, commentId);

            commentService.voteOnComment(commentId, user, vote);

            logger.info("Successfully processed vote on comment: {}", commentId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            logger.warn("User not found when voting on comment");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid vote type: {}", voteType);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error voting on comment: {}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Converts a PlayerComment entity to a PlayerCommentDTO for API responses.
     */
    private PlayerCommentDTO convertToDTO(PlayerComment comment) {
        if (comment == null) {
            return null;
        }

        PlayerCommentDTO dto = new PlayerCommentDTO();
        dto.setId(comment.getId());
        dto.setPlayerId(comment.getPlayer().getId());
        dto.setPlayerName(comment.getPlayer().getName());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getUsername());

        if (comment.getAuthor().getProfileImageUrl() != null) {
            dto.setAuthorProfileImage(comment.getAuthor().getProfileImageUrl());
        }

        dto.setContent(comment.getContent());
        dto.setUpvotes(comment.getUpvotes());
        dto.setDownvotes(comment.getDownvotes());
        dto.setIsFeatured(comment.getIsFeatured());
        dto.setIsDeleted(comment.getIsDeleted());
        dto.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null);
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        return dto;
    }
}

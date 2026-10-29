package com.taskflow.controller;

import com.taskflow.dto.CommentRequest;
import com.taskflow.dto.CommentResponse;
import com.taskflow.dto.PagedResponse;
import com.taskflow.entity.Role;
import com.taskflow.security.SecurityUser;
import com.taskflow.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Comments", description = "Task discussion comments management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/api/tasks/{taskId}/comments")
    @Operation(summary = "Post a comment on a task")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal SecurityUser principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        CommentResponse response = commentService.createComment(taskId, request, principal.getUserId(), isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/tasks/{taskId}/comments")
    @Operation(summary = "List comments for a task in chronological order (paginated)")
    public ResponseEntity<PagedResponse<CommentResponse>> getComments(
            @PathVariable Long taskId,
            @AuthenticationPrincipal SecurityUser principal,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        return ResponseEntity.ok(commentService.getCommentsByTaskId(taskId, principal.getUserId(), isAdmin, pageable));
    }

    @DeleteMapping("/api/comments/{commentId}")
    @Operation(summary = "Delete a comment (Author, Project Owner, or Admin)")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal SecurityUser principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        commentService.deleteComment(commentId, principal.getUserId(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}

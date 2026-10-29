package com.taskflow.dto;

import java.time.LocalDateTime;

/**
 * Returned to the client representing a task comment.
 */
public record CommentResponse(
        Long id,
        Long taskId,
        Long authorId,
        String authorName,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

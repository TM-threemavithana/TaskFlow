package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sent by the client to post a comment on a task.
 */
public record CommentRequest(
        @NotBlank(message = "Comment content cannot be blank")
        @Size(min = 1, max = 2000, message = "Comment content must be between 1 and 2000 characters")
        String content
) {}

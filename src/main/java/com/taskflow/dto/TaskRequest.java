package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Sent by the client to create or update a task.
 * Notice: status, priority, assigneeId, dueDate are all optional - they have defaults.
 */
public record TaskRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 2, max = 200, message = "Title must be 2-200 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        /** Must reference an existing project. */
        @NotNull(message = "Project ID is required")
        Long projectId,

        /** null = TODO (default handled in the service). */
        String status,

        /** null = MEDIUM (default). */
        String priority,

        /** null = no due date. */
        LocalDate dueDate,

        /** null = unassigned. */
        Long assigneeId
) {}

package com.taskflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Returned to the client when reading a task. */
public record TaskResponse(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        LocalDate dueDate,
        Long projectId,
        String projectName,
        Long assigneeId,
        String assigneeName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

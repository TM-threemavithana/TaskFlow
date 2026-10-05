package com.taskflow.dto;

import java.time.LocalDateTime;

/** Returned to the client when reading a project. */
public record ProjectResponse(
        Long id,
        String name,
        String description,
        Long ownerId,
        String ownerName,
        int taskCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

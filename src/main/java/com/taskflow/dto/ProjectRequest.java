package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Sent by the client to create or update a project. */
public record ProjectRequest(
        @NotBlank(message = "Project name is required")
        @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
        String name,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description
) {}

package com.taskflow.dto;

import jakarta.validation.constraints.NotNull;

public record ProjectMemberRequest(
        @NotNull(message = "User ID is required")
        Long userId
) {}


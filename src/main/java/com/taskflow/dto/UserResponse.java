package com.taskflow.dto;

import java.time.LocalDateTime;

/**
 * RESPONSE DTO for User. Notice it does NOT include the password hash.
 * Records are immutable, so the password can never be accidentally added after construction.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        LocalDateTime createdAt
) {}

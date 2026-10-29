package com.taskflow.dto;

import java.time.LocalDateTime;

public record ProjectMemberResponse(
        Long id,
        Long projectId,
        Long userId,
        String userName,
        String userEmail,
        String role,
        LocalDateTime joinedAt
) {}


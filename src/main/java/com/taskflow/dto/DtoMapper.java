package com.taskflow.dto;

import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;

/**
 * MAPPER: converts between ENTITIES ↔ DTOs.
 *
 * <p>DESIGN PATTERN: ADAPTER (structural).
 * The Adapter pattern converts the interface of one class (Entity with all internal fields)
 * into another interface (DTO with only public-facing fields) that the client expects.
 * This class "adapts" entities to DTOs and vice versa.
 *
 * <p>Libraries like MapStruct can generate mappers automatically. We do it by hand to understand it.
 */
public final class DtoMapper {

    /** Private constructor: this class has only static methods, so nobody should instantiate it. */
    private DtoMapper() {
    }

    // ======================== User ========================

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),        // Enum → String
                user.getCreatedAt()
        );
    }

    // ======================== Project ========================

    public static ProjectResponse toProjectResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getId(),
                project.getOwner().getName(),
                project.getTasks().size(),     // Hibernate loads the collection lazily here
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    // ======================== Task ========================

    public static TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getPriority().name(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getName() : null,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}

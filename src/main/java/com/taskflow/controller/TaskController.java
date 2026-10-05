package com.taskflow.controller;

import com.taskflow.dto.PagedResponse;
import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.entity.Role;
import com.taskflow.security.SecurityUser;
import com.taskflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Task CRUD + filtering/sorting/pagination endpoints.
 *
 * <p>Filtering demo: {@code GET /api/tasks?projectId=1&status=TODO&priority=HIGH&search=login&page=0&size=5&sort=dueDate,asc}
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponse> create(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal SecurityUser principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        TaskResponse response = taskService.createTask(request, principal.getUserId(), isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTask(id));
    }

    @GetMapping
    @Operation(summary = "List tasks with optional filters")
    public ResponseEntity<PagedResponse<TaskResponse>> getTasks(
            @Parameter(description = "Filter by project ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "Filter by status (TODO, IN_PROGRESS, COMPLETED)") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by priority (LOW, MEDIUM, HIGH)") @RequestParam(required = false) String priority,
            @Parameter(description = "Filter by assignee user ID") @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "Search in task title") @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasks(projectId, status, priority, assigneeId, search, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal SecurityUser principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        return ResponseEntity.ok(taskService.updateTask(id, request, principal.getUserId(), isAdmin));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        taskService.deleteTask(id, principal.getUserId(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}

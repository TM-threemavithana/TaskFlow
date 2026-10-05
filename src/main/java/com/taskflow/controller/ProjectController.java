package com.taskflow.controller;

import com.taskflow.dto.PagedResponse;
import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.entity.Role;
import com.taskflow.security.SecurityUser;
import com.taskflow.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Project CRUD endpoints.
 *
 * <p>PAGINATION: Spring's {@link Pageable} is auto-populated from query parameters:
 * {@code ?page=0&size=10&sort=name,asc}. No manual parsing needed!
 */
@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal SecurityUser principal) {
        ProjectResponse response = projectService.createProject(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by ID")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    /**
     * List the current user's projects.
     * {@code @PageableDefault(size = 10, sort = "createdAt")}: if the client doesn't send
     * pagination params, use these defaults.
     */
    @GetMapping
    @Operation(summary = "List current user's projects (paginated)")
    public ResponseEntity<PagedResponse<ProjectResponse>> getMyProjects(
            @AuthenticationPrincipal SecurityUser principal,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectsByOwner(principal.getUserId(), pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal SecurityUser principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        return ResponseEntity.ok(projectService.updateProject(id, request, principal.getUserId(), isAdmin));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        projectService.deleteProject(id, principal.getUserId(), isAdmin);
        return ResponseEntity.noContent().build();   // 204 No Content
    }
}

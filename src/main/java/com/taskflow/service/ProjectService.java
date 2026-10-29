package com.taskflow.service;

import com.taskflow.dto.DtoMapper;
import com.taskflow.dto.PagedResponse;
import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import com.taskflow.exception.AccessDeniedException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SERVICE LAYER: contains the business logic.
 *
 * <p>LAYERED ARCHITECTURE:
 * <pre>
 *   Controller  →  Service  →  Repository  →  Database
 *       ↑                          ↑
 *      DTOs                     Entities
 * </pre>
 *
 * <p>Rules:
 * <ul>
 *   <li>Controllers validate input (via DTOs) and delegate to services.</li>
 *   <li>Services contain business rules and call repositories.</li>
 *   <li>Repositories handle database access.</li>
 *   <li>Each layer only talks to the layer directly below it.</li>
 * </ul>
 *
 * <p>{@code @Service}: makes this a Spring bean. {@code @Transactional}: every public method
 * runs inside a database transaction - if anything throws, all changes are rolled back automatically.
 */
@Service
@Transactional
public class ProjectService {

    // DEPENDENCY INJECTION via constructor (preferred over field injection).
    // Spring sees these constructor parameters and passes in the beans it already created.
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    /** Create a project owned by the given user. */
    public ProjectResponse createProject(ProjectRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        Project project = new Project(request.name(), request.description(), owner);
        Project saved = projectRepository.save(project);

        return DtoMapper.toProjectResponse(saved);
    }

    /** Read one project with authorization check. */
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id, Long currentUserId, boolean isAdmin) {
        Project project = findProjectOrThrow(id);
        checkProjectAccess(project, currentUserId, isAdmin);
        return DtoMapper.toProjectResponse(project);
    }

    /** Read one project without user context (internal / legacy use). */
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {
        Project project = findProjectOrThrow(id);
        return DtoMapper.toProjectResponse(project);
    }

    /** Read all projects for a user, paginated. */
    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> getProjectsByOwner(Long ownerId, Pageable pageable) {
        Page<Project> page = projectRepository.findByOwnerId(ownerId, pageable);
        return toPagedResponse(page);
    }

    /** Update a project. Only the owner or an ADMIN can update. */
    public ProjectResponse updateProject(Long projectId, ProjectRequest request, Long currentUserId, boolean isAdmin) {
        Project project = findProjectOrThrow(projectId);
        checkOwnership(project, currentUserId, isAdmin);

        project.setName(request.name());
        project.setDescription(request.description());
        // No need to call save(): inside a @Transactional method, Hibernate detects changes
        // to managed entities and auto-flushes them at commit time ("dirty checking").
        return DtoMapper.toProjectResponse(project);
    }

    /** Delete a project. Tasks are cascade-deleted by the database FK constraint. */
    public void deleteProject(Long projectId, Long currentUserId, boolean isAdmin) {
        Project project = findProjectOrThrow(projectId);
        checkOwnership(project, currentUserId, isAdmin);
        projectRepository.delete(project);
    }

    // ---- Private helpers ----

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    public void checkProjectAccess(Project project, Long userId, boolean isAdmin) {
        if (!isAdmin && !project.isOwnedBy(userId)) {
            throw new AccessDeniedException("You do not have access to this project");
        }
    }

    private void checkOwnership(Project project, Long userId, boolean isAdmin) {
        if (!isAdmin && !project.isOwnedBy(userId)) {
            throw new AccessDeniedException("You do not own this project");
        }
    }

    private PagedResponse<ProjectResponse> toPagedResponse(Page<Project> page) {
        return new PagedResponse<>(
                page.getContent().stream().map(DtoMapper::toProjectResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}

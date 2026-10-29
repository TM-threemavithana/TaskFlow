package com.taskflow.service;

import com.taskflow.dto.DtoMapper;
import com.taskflow.dto.PagedResponse;
import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.event.TaskEvent;
import com.taskflow.exception.AccessDeniedException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.specification.TaskSpecifications;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Task service. Uses:
 * <ul>
 *   <li>BUILDER PATTERN when constructing tasks.</li>
 *   <li>OBSERVER PATTERN: publishes {@link TaskEvent}s via Spring's event system.</li>
 *   <li>STRATEGY PATTERN: builds dynamic queries with {@link TaskSpecifications}.</li>
 * </ul>
 */
@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;   // Spring injects this automatically

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       ApplicationEventPublisher eventPublisher) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public TaskResponse createTask(TaskRequest request, Long currentUserId, boolean isAdmin) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.projectId()));

        // Only the project owner or an admin can add tasks to a project
        if (!isAdmin && !project.isOwnedBy(currentUserId)) {
            throw new AccessDeniedException("You do not own this project");
        }

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assigneeId()));
        }

        // BUILDER PATTERN in action
        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(parseStatus(request.status()))
                .priority(parsePriority(request.priority()))
                .dueDate(request.dueDate())
                .project(project)
                .assignee(assignee)
                .build();

        Task saved = taskRepository.save(task);

        // OBSERVER PATTERN: publish event. All @EventListener beans will be notified.
        eventPublisher.publishEvent(new TaskEvent(TaskEvent.Type.CREATED, saved));

        return DtoMapper.toTaskResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id, Long currentUserId, boolean isAdmin) {
        Task task = findTaskOrThrow(id);
        checkTaskReadAccess(task, currentUserId, isAdmin);
        return DtoMapper.toTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        Task task = findTaskOrThrow(id);
        return DtoMapper.toTaskResponse(task);
    }

    /**
     * Filtered, sorted, paginated task listing scoped to user authorization.
     * STRATEGY PATTERN: each filter parameter adds a Specification to the WHERE clause.
     */
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasks(Long projectId, String status, String priority,
                                                 Long assigneeId, String search,
                                                 Long currentUserId, boolean isAdmin, Pageable pageable) {
        // If filtering by a specific project, verify the user has access to that project
        if (projectId != null && !isAdmin && currentUserId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
            if (!project.isOwnedBy(currentUserId)) {
                throw new AccessDeniedException("You do not have access to this project's tasks");
            }
        }

        Specification<Task> spec = Specification.where(null);

        // If not admin, scope query at the database level to tasks accessible to the user
        if (!isAdmin && currentUserId != null) {
            if (projectId == null) {
                spec = spec.and(TaskSpecifications.isAccessibleByUser(currentUserId, java.util.Collections.emptySet()));
            }
        }

        // Dynamically compose filters only for parameters that are present
        if (projectId != null) spec = spec.and(TaskSpecifications.hasProject(projectId));
        if (status != null && !status.isBlank())    spec = spec.and(TaskSpecifications.hasStatus(parseStatus(status)));
        if (priority != null && !priority.isBlank())  spec = spec.and(TaskSpecifications.hasPriority(parsePriority(priority)));
        if (assigneeId != null) spec = spec.and(TaskSpecifications.hasAssignee(assigneeId));
        if (search != null && !search.isBlank()) spec = spec.and(TaskSpecifications.titleContains(search));

        Page<Task> page = taskRepository.findAll(spec, pageable);

        return toPagedResponse(page);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasks(Long projectId, String status, String priority,
                                                 Long assigneeId, String search, Pageable pageable) {
        return getTasks(projectId, status, priority, assigneeId, search, null, true, pageable);
    }

    public TaskResponse updateTask(Long taskId, TaskRequest request, Long currentUserId, boolean isAdmin) {
        Task task = findTaskOrThrow(taskId);
        checkTaskAccess(task, currentUserId, isAdmin);

        boolean isProjectOwner = task.getProject().isOwnedBy(currentUserId);

        // Field-level authorization:
        // Assignees who do NOT own the project may ONLY update task status
        if (!isAdmin && !isProjectOwner) {
            boolean titleChanged = request.title() != null && !request.title().equals(task.getTitle());
            boolean descChanged = request.description() != null && !request.description().equals(task.getDescription());
            boolean priorityChanged = request.priority() != null && !request.priority().equalsIgnoreCase(task.getPriority().name());
            boolean dueDateChanged = (request.dueDate() != null && !request.dueDate().equals(task.getDueDate())) ||
                    (request.dueDate() == null && task.getDueDate() != null);
            boolean assigneeChanged = (request.assigneeId() != null &&
                    (task.getAssignee() == null || !task.getAssignee().getId().equals(request.assigneeId())));

            if (titleChanged || descChanged || priorityChanged || dueDateChanged || assigneeChanged) {
                throw new AccessDeniedException("Assignees are only permitted to update task status");
            }
            task.setStatus(parseStatus(request.status()));
        } else {
            // Project owner or admin can update all fields
            task.setTitle(request.title());
            task.setDescription(request.description());
            task.setStatus(parseStatus(request.status()));
            task.setPriority(parsePriority(request.priority()));
            task.setDueDate(request.dueDate());

            if (request.assigneeId() != null) {
                User assignee = userRepository.findById(request.assigneeId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assigneeId()));
                task.setAssignee(assignee);
            } else {
                task.setAssignee(null);
            }
        }

        TaskEvent.Type eventType = task.getStatus() == TaskStatus.COMPLETED
                ? TaskEvent.Type.COMPLETED
                : TaskEvent.Type.UPDATED;
        eventPublisher.publishEvent(new TaskEvent(eventType, task));

        return DtoMapper.toTaskResponse(task);
    }

    public void deleteTask(Long taskId, Long currentUserId, boolean isAdmin) {
        Task task = findTaskOrThrow(taskId);
        boolean isProjectOwner = task.getProject().isOwnedBy(currentUserId);
        if (!isAdmin && !isProjectOwner) {
            throw new AccessDeniedException("Only the project owner or an admin can delete a task");
        }

        eventPublisher.publishEvent(new TaskEvent(TaskEvent.Type.DELETED, task));
        taskRepository.delete(task);
    }

    // ---- Private helpers ----

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
    }

    public void checkTaskReadAccess(Task task, Long userId, boolean isAdmin) {
        if (isAdmin) return;
        boolean isProjectOwner = task.getProject().isOwnedBy(userId);
        boolean isAssignee = task.isAssignedTo(userId);
        if (!isProjectOwner && !isAssignee) {
            throw new AccessDeniedException("You don't have access to this task");
        }
    }

    private void checkTaskAccess(Task task, Long userId, boolean isAdmin) {
        boolean isProjectOwner = task.getProject().isOwnedBy(userId);
        boolean isAssignee = task.isAssignedTo(userId);
        if (!isAdmin && !isProjectOwner && !isAssignee) {
            throw new AccessDeniedException("You don't have access to this task");
        }
    }

    private PagedResponse<TaskResponse> toPagedResponse(Page<Task> page) {
        return new PagedResponse<>(
                page.getContent().stream().map(DtoMapper::toTaskResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private TaskStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return TaskStatus.TODO;
        return TaskStatus.valueOf(status.toUpperCase());
    }

    private Priority parsePriority(String priority) {
        if (priority == null || priority.isBlank()) return Priority.MEDIUM;
        return Priority.valueOf(priority.toUpperCase());
    }
}

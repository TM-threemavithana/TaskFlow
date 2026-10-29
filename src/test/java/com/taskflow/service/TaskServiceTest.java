package com.taskflow.service;

import com.taskflow.dto.PagedResponse;
import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.Role;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.event.TaskEvent;
import com.taskflow.exception.AccessDeniedException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    private User owner;
    private User assignee;
    private User member;
    private User stranger;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        owner = new User("Alice Owner", "alice@example.com", "hash", Role.USER);
        owner.setId(1L);

        assignee = new User("Bob Assignee", "bob@example.com", "hash", Role.USER);
        assignee.setId(2L);

        member = new User("Charlie Member", "charlie@example.com", "hash", Role.USER);
        member.setId(3L);

        stranger = new User("David Stranger", "david@example.com", "hash", Role.USER);
        stranger.setId(4L);

        project = new Project("TaskFlow Project", "Project Description", owner);
        project.setId(10L);

        task = Task.builder()
                .title("Initial Task")
                .description("Task description")
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .project(project)
                .assignee(assignee)
                .build();
        task.setId(100L);
    }

    @Nested
    @DisplayName("createTask")
    class CreateTaskTests {

        @Test
        @DisplayName("should create task when caller is project owner")
        void shouldCreateTaskByOwner() {
            TaskRequest request = new TaskRequest("New Task", "Description", 10L, "TODO", "HIGH", LocalDate.now().plusDays(3), 2L);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
                Task t = inv.getArgument(0);
                t.setId(101L);
                return t;
            });

            TaskResponse response = taskService.createTask(request, 1L, false);

            assertNotNull(response);
            assertEquals("New Task", response.title());
            assertEquals("HIGH", response.priority());
            assertEquals(10L, response.projectId());
            assertEquals(2L, response.assigneeId());

            ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
            assertEquals(TaskEvent.Type.CREATED, eventCaptor.getValue().type());
        }

        @Test
        @DisplayName("should create task when caller is admin")
        void shouldCreateTaskByAdmin() {
            TaskRequest request = new TaskRequest("Admin Task", null, 10L, null, null, null, null);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
                Task t = inv.getArgument(0);
                t.setId(102L);
                return t;
            });

            TaskResponse response = taskService.createTask(request, 999L, true);

            assertNotNull(response);
            assertEquals("Admin Task", response.title());
            assertEquals("TODO", response.status());
            assertEquals("MEDIUM", response.priority());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when project not found")
        void shouldThrowWhenProjectNotFound() {
            TaskRequest request = new TaskRequest("Task", null, 999L, null, null, null, null);
            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    taskService.createTask(request, 1L, false));
            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reject task creation when caller is stranger or non-owner")
        void shouldRejectTaskCreationByStranger() {
            TaskRequest request = new TaskRequest("Task", null, 10L, null, null, null, null);
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

            assertThrows(AccessDeniedException.class, () ->
                    taskService.createTask(request, 4L, false));
            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when assignee does not exist")
        void shouldThrowWhenAssigneeNotFound() {
            TaskRequest request = new TaskRequest("Task", null, 10L, null, null, null, 999L);
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    taskService.createTask(request, 1L, false));
        }
    }

    @Nested
    @DisplayName("getTask")
    class GetTaskTests {

        @Test
        @DisplayName("should return task when requested by project owner")
        void shouldGetTaskByOwner() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.getTask(100L, 1L, false);

            assertNotNull(response);
            assertEquals(100L, response.id());
            assertEquals("Initial Task", response.title());
        }

        @Test
        @DisplayName("should return task when requested by assignee")
        void shouldGetTaskByAssignee() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.getTask(100L, 2L, false);

            assertNotNull(response);
            assertEquals(100L, response.id());
        }

        @Test
        @DisplayName("should return task when requested by project member")
        void shouldGetTaskByMember() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 3L)).thenReturn(true);

            TaskResponse response = taskService.getTask(100L, 3L, false);

            assertNotNull(response);
            assertEquals(100L, response.id());
        }

        @Test
        @DisplayName("should return task when requested by admin")
        void shouldGetTaskByAdmin() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.getTask(100L, 999L, true);

            assertNotNull(response);
            assertEquals(100L, response.id());
        }

        @Test
        @DisplayName("should deny access when requested by stranger")
        void shouldDenyGetTaskByStranger() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 4L)).thenReturn(false);

            assertThrows(AccessDeniedException.class, () ->
                    taskService.getTask(100L, 4L, false));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when task does not exist")
        void shouldThrowWhenTaskNotFound() {
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    taskService.getTask(999L, 1L, false));
        }

        @Test
        @DisplayName("should get task using legacy method")
        void shouldGetTaskLegacy() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.getTask(100L);

            assertNotNull(response);
            assertEquals(100L, response.id());
        }
    }

    @Nested
    @DisplayName("getTasks (filtering & pagination)")
    class GetTasksTests {

        @Test
        @DisplayName("should return paginated tasks for project when user has access")
        void shouldGetTasksForProject() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            Page<Task> page = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1);
            when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PagedResponse<TaskResponse> response = taskService.getTasks(10L, null, null, null, null, 1L, false, PageRequest.of(0, 10));

            assertNotNull(response);
            assertEquals(1, response.content().size());
            assertEquals("Initial Task", response.content().get(0).title());
        }

        @Test
        @DisplayName("should reject project task listing when user has no project access")
        void shouldRejectTasksWhenUserHasNoAccess() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 4L)).thenReturn(false);

            assertThrows(AccessDeniedException.class, () ->
                    taskService.getTasks(10L, null, null, null, null, 4L, false, PageRequest.of(0, 10)));
        }

        @Test
        @DisplayName("should query accessible member projects when projectId is null")
        void shouldIncludeMemberProjectsWhenListingAll() {
            when(projectMemberRepository.findProjectIdsByUserId(3L)).thenReturn(Set.of(10L));
            Page<Task> page = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1);
            when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            PagedResponse<TaskResponse> response = taskService.getTasks(null, "TODO", "MEDIUM", null, "test", 3L, false, PageRequest.of(0, 10));

            assertNotNull(response);
            verify(projectMemberRepository, times(1)).findProjectIdsByUserId(3L);
        }
    }

    @Nested
    @DisplayName("updateTask")
    class UpdateTaskTests {

        @Test
        @DisplayName("project owner can update all fields of a task")
        void shouldAllowOwnerToUpdateAllFields() {
            User newAssignee = new User("New Assignee", "new@example.com", "hash", Role.USER);
            newAssignee.setId(5L);

            LocalDate newDueDate = LocalDate.now().plusDays(14);
            TaskRequest request = new TaskRequest("Updated Title", "Updated Description", 10L, "IN_PROGRESS", "HIGH", newDueDate, 5L);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
            when(userRepository.findById(5L)).thenReturn(Optional.of(newAssignee));

            TaskResponse response = taskService.updateTask(100L, request, 1L, false);

            assertEquals("Updated Title", response.title());
            assertEquals("Updated Description", response.description());
            assertEquals("IN_PROGRESS", response.status());
            assertEquals("HIGH", response.priority());
            assertEquals(5L, response.assigneeId());

            ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
            assertEquals(TaskEvent.Type.UPDATED, eventCaptor.getValue().type());
        }

        @Test
        @DisplayName("completing task emits COMPLETED event")
        void shouldPublishCompletedEvent() {
            TaskRequest request = new TaskRequest(task.getTitle(), task.getDescription(), 10L, "COMPLETED", task.getPriority().name(), task.getDueDate(), 2L);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
            when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));

            taskService.updateTask(100L, request, 1L, false);

            ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
            assertEquals(TaskEvent.Type.COMPLETED, eventCaptor.getValue().type());
        }

        @Test
        @DisplayName("assignee can update status only")
        void shouldAllowAssigneeToUpdateStatusOnly() {
            TaskRequest request = new TaskRequest(task.getTitle(), task.getDescription(), 10L, "IN_PROGRESS", task.getPriority().name(), task.getDueDate(), 2L);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.updateTask(100L, request, 2L, false);

            assertEquals("IN_PROGRESS", response.status());
            assertEquals("Initial Task", response.title());
        }

        @Test
        @DisplayName("assignee is rejected when trying to change title")
        void shouldRejectAssigneeChangingTitle() {
            TaskRequest request = new TaskRequest("Hacked Title", task.getDescription(), 10L, "IN_PROGRESS", task.getPriority().name(), task.getDueDate(), 2L);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            assertThrows(AccessDeniedException.class, () ->
                    taskService.updateTask(100L, request, 2L, false));
        }

        @Test
        @DisplayName("assignee is rejected when trying to change priority")
        void shouldRejectAssigneeChangingPriority() {
            TaskRequest request = new TaskRequest(task.getTitle(), task.getDescription(), 10L, "IN_PROGRESS", "HIGH", task.getDueDate(), 2L);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            assertThrows(AccessDeniedException.class, () ->
                    taskService.updateTask(100L, request, 2L, false));
        }

        @Test
        @DisplayName("stranger is rejected from updating task")
        void shouldRejectStrangerFromUpdatingTask() {
            TaskRequest request = new TaskRequest(task.getTitle(), task.getDescription(), 10L, "COMPLETED", task.getPriority().name(), task.getDueDate(), 2L);

            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            assertThrows(AccessDeniedException.class, () ->
                    taskService.updateTask(100L, request, 4L, false));
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTaskTests {

        @Test
        @DisplayName("project owner can delete task")
        void shouldAllowOwnerToDeleteTask() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            taskService.deleteTask(100L, 1L, false);

            verify(taskRepository, times(1)).delete(task);
            ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
            assertEquals(TaskEvent.Type.DELETED, eventCaptor.getValue().type());
        }

        @Test
        @DisplayName("admin can delete task")
        void shouldAllowAdminToDeleteTask() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            taskService.deleteTask(100L, 999L, true);

            verify(taskRepository, times(1)).delete(task);
        }

        @Test
        @DisplayName("assignee cannot delete task")
        void shouldRejectAssigneeDeletingTask() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            assertThrows(AccessDeniedException.class, () ->
                    taskService.deleteTask(100L, 2L, false));
            verify(taskRepository, never()).delete(any(Task.class));
        }

        @Test
        @DisplayName("stranger cannot delete task")
        void shouldRejectStrangerDeletingTask() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            assertThrows(AccessDeniedException.class, () ->
                    taskService.deleteTask(100L, 4L, false));
            verify(taskRepository, never()).delete(any(Task.class));
        }
    }
}

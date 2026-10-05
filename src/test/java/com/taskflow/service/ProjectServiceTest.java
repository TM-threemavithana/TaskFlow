package com.taskflow.service;

import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.exception.AccessDeniedException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TESTS with JUnit 5 + Mockito.
 *
 * <p>UNIT TEST PHILOSOPHY:
 * <ul>
 *   <li>Test ONE class in isolation. All dependencies are MOCKED (faked).</li>
 *   <li>Tests are FAST (no database, no network, no Spring context).</li>
 *   <li>Tests verify BEHAVIOR: "when I call X with Y, it should return Z" or "it should throw".</li>
 * </ul>
 *
 * <p>MOCKITO explained:
 * <ul>
 *   <li>{@code @Mock}: creates a fake object. All methods return null/0/false by default.</li>
 *   <li>{@code when(...).thenReturn(...)}: program the fake to return a specific value.</li>
 *   <li>{@code verify(mock).method(...)}: assert that a method WAS called on the mock.</li>
 *   <li>{@code @InjectMocks}: creates the real class and injects all @Mocks into its constructor.</li>
 * </ul>
 *
 * <p>{@code @ExtendWith(MockitoExtension.class)}: tells JUnit to process Mockito annotations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Unit Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    // ---- Test fixtures (reusable test data) ----
    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testUser = new User("Alice", "alice@example.com", "hashed", Role.USER);
        testUser.setId(1L);

        testProject = new Project("Test Project", "A project for testing", testUser);
        testProject.setId(1L);
    }

    // ---- Nested test classes group related tests for readability ----

    @Nested
    @DisplayName("createProject()")
    class CreateProject {

        @Test
        @DisplayName("should create a project when user exists")
        void shouldCreateProject() {
            // ARRANGE: set up the test scenario
            ProjectRequest request = new ProjectRequest("New Project", "Description");
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
                Project saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // ACT: call the method under test
            ProjectResponse response = projectService.createProject(request, 1L);

            // ASSERT: verify the result
            assertNotNull(response);
            assertEquals("New Project", response.name());
            assertEquals("Description", response.description());

            // VERIFY: the mock was called exactly once
            verify(projectRepository, times(1)).save(any(Project.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            ProjectRequest request = new ProjectRequest("Project", "Desc");
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // assertThrows: verifies that the lambda throws the expected exception
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> projectService.createProject(request, 999L)
            );
            assertTrue(ex.getMessage().contains("User"));
        }
    }

    @Nested
    @DisplayName("getProject()")
    class GetProject {

        @Test
        @DisplayName("should return project when it exists")
        void shouldReturnProject() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

            ProjectResponse response = projectService.getProject(1L);

            assertEquals("Test Project", response.name());
        }

        @Test
        @DisplayName("should throw when project does not exist")
        void shouldThrowWhenNotFound() {
            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> projectService.getProject(999L));
        }
    }

    @Nested
    @DisplayName("updateProject()")
    class UpdateProject {

        @Test
        @DisplayName("should update when user is the owner")
        void shouldUpdateWhenOwner() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            ProjectRequest request = new ProjectRequest("Updated", "New desc");

            ProjectResponse response = projectService.updateProject(1L, request, 1L, false);

            assertEquals("Updated", response.name());
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not the owner")
        void shouldThrowWhenNotOwner() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            ProjectRequest request = new ProjectRequest("Hacked", "Nope");

            assertThrows(AccessDeniedException.class,
                    () -> projectService.updateProject(1L, request, 999L, false));
        }

        @Test
        @DisplayName("should allow update when user is admin (even if not owner)")
        void shouldAllowAdminUpdate() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            ProjectRequest request = new ProjectRequest("Admin Update", "By admin");

            ProjectResponse response = projectService.updateProject(1L, request, 999L, true);

            assertEquals("Admin Update", response.name());
        }
    }

    @Nested
    @DisplayName("deleteProject()")
    class DeleteProject {

        @Test
        @DisplayName("should delete when user is the owner")
        void shouldDeleteWhenOwner() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

            projectService.deleteProject(1L, 1L, false);

            verify(projectRepository).delete(testProject);
        }

        @Test
        @DisplayName("should throw when non-owner tries to delete")
        void shouldThrowWhenNotOwner() {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

            assertThrows(AccessDeniedException.class,
                    () -> projectService.deleteProject(1L, 999L, false));
            // verify delete was NEVER called
            verify(projectRepository, never()).delete(any());
        }
    }
}

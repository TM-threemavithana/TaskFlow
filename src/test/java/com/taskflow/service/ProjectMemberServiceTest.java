package com.taskflow.service;

import com.taskflow.dto.ProjectMemberRequest;
import com.taskflow.dto.ProjectMemberResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.ProjectMember;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.exception.AccessDeniedException;
import com.taskflow.exception.DuplicateResourceException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ProjectMemberRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService - Membership Management Unit Tests")
class ProjectMemberServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectService projectService;

    private User owner;
    private User memberUser;
    private User stranger;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = new User("Owner", "owner@example.com", "hash", Role.USER);
        owner.setId(1L);

        memberUser = new User("Member", "member@example.com", "hash", Role.USER);
        memberUser.setId(2L);

        stranger = new User("Stranger", "stranger@example.com", "hash", Role.USER);
        stranger.setId(3L);

        project = new Project("Test Project", "Description", owner);
        project.setId(10L);
    }

    @Nested
    @DisplayName("addMember tests")
    class AddMemberTests {

        @Test
        @DisplayName("should add member successfully when caller is project owner")
        void shouldAddMemberByOwner() {
            ProjectMemberRequest req = new ProjectMemberRequest(2L);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(false);
            when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(inv -> {
                ProjectMember pm = inv.getArgument(0);
                pm.setId(100L);
                return pm;
            });

            ProjectMemberResponse resp = projectService.addMember(10L, req, 1L, false);

            assertNotNull(resp);
            assertEquals(10L, resp.projectId());
            assertEquals(2L, resp.userId());
            assertEquals("Member", resp.userName());
            verify(projectMemberRepository, times(1)).save(any(ProjectMember.class));
        }

        @Test
        @DisplayName("should add member successfully when caller is admin")
        void shouldAddMemberByAdmin() {
            ProjectMemberRequest req = new ProjectMemberRequest(2L);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(false);
            when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(inv -> {
                ProjectMember pm = inv.getArgument(0);
                pm.setId(101L);
                return pm;
            });

            ProjectMemberResponse resp = projectService.addMember(10L, req, 999L, true);

            assertNotNull(resp);
            assertEquals(2L, resp.userId());
        }

        @Test
        @DisplayName("should reject addMember when caller is stranger or non-owner")
        void shouldRejectAddMemberByStranger() {
            ProjectMemberRequest req = new ProjectMemberRequest(2L);
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

            assertThrows(AccessDeniedException.class, () ->
                    projectService.addMember(10L, req, 3L, false));
            verify(projectMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reject adding the owner as a member")
        void shouldRejectAddingOwnerAsMember() {
            ProjectMemberRequest req = new ProjectMemberRequest(1L);
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

            assertThrows(DuplicateResourceException.class, () ->
                    projectService.addMember(10L, req, 1L, false));
            verify(projectMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reject duplicate member addition")
        void shouldRejectDuplicateMember() {
            ProjectMemberRequest req = new ProjectMemberRequest(2L);
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () ->
                    projectService.addMember(10L, req, 1L, false));
            verify(projectMemberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user to add does not exist")
        void shouldThrowWhenUserNotFound() {
            ProjectMemberRequest req = new ProjectMemberRequest(999L);
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    projectService.addMember(10L, req, 1L, false));
        }
    }

    @Nested
    @DisplayName("getMembers tests")
    class GetMembersTests {

        @Test
        @DisplayName("should return member list when caller is project owner")
        void shouldGetMembersByOwner() {
            ProjectMember pm = new ProjectMember(project, memberUser);
            pm.setId(100L);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(pm));

            List<ProjectMemberResponse> list = projectService.getMembers(10L, 1L, false);

            assertEquals(1, list.size());
            assertEquals(2L, list.get(0).userId());
        }

        @Test
        @DisplayName("should return member list when caller is member")
        void shouldGetMembersByMember() {
            ProjectMember pm = new ProjectMember(project, memberUser);
            pm.setId(100L);

            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).thenReturn(true);
            when(projectMemberRepository.findByProjectIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(pm));

            List<ProjectMemberResponse> list = projectService.getMembers(10L, 2L, false);

            assertEquals(1, list.size());
        }

        @Test
        @DisplayName("should reject getMembers when caller is stranger")
        void shouldRejectGetMembersByStranger() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 3L)).thenReturn(false);

            assertThrows(AccessDeniedException.class, () ->
                    projectService.getMembers(10L, 3L, false));
        }
    }

    @Nested
    @DisplayName("removeMember tests")
    class RemoveMemberTests {

        @Test
        @DisplayName("should remove member successfully by owner")
        void shouldRemoveMemberByOwner() {
            ProjectMember pm = new ProjectMember(project, memberUser);
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 2L)).thenReturn(Optional.of(pm));

            projectService.removeMember(10L, 2L, 1L, false);

            verify(projectMemberRepository, times(1)).delete(pm);
        }

        @Test
        @DisplayName("should remove member successfully by admin")
        void shouldRemoveMemberByAdmin() {
            ProjectMember pm = new ProjectMember(project, memberUser);
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 2L)).thenReturn(Optional.of(pm));

            projectService.removeMember(10L, 2L, 999L, true);

            verify(projectMemberRepository, times(1)).delete(pm);
        }

        @Test
        @DisplayName("should reject removing member by stranger")
        void shouldRejectRemoveMemberByStranger() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

            assertThrows(AccessDeniedException.class, () ->
                    projectService.removeMember(10L, 2L, 3L, false));
            verify(projectMemberRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should reject removing project owner as a member")
        void shouldRejectRemovingOwner() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

            assertThrows(AccessDeniedException.class, () ->
                    projectService.removeMember(10L, 1L, 1L, false));
            verify(projectMemberRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when member not found")
        void shouldThrowWhenMemberNotFound() {
            when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
            when(projectMemberRepository.findByProjectIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    projectService.removeMember(10L, 2L, 1L, false));
        }
    }
}


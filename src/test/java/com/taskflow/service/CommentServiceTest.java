package com.taskflow.service;

import com.taskflow.dto.CommentRequest;
import com.taskflow.dto.CommentResponse;
import com.taskflow.entity.Comment;
import com.taskflow.entity.Project;
import com.taskflow.entity.Role;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.AccessDeniedException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.TaskRepository;
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
@DisplayName("CommentService Unit Tests")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private CommentService commentService;

    private User owner;
    private User assignee;
    private User stranger;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        owner = new User("Owner", "owner@example.com", "pass", Role.USER);
        owner.setId(1L);

        assignee = new User("Assignee", "assignee@example.com", "pass", Role.USER);
        assignee.setId(2L);

        stranger = new User("Stranger", "stranger@example.com", "pass", Role.USER);
        stranger.setId(3L);

        project = new Project("Project Alpha", "Description", owner);
        project.setId(10L);

        task = Task.builder()
                .title("Database Schema")
                .description("Create initial DDL")
                .project(project)
                .assignee(assignee)
                .build();
        task.setId(100L);
    }

    @Nested
    @DisplayName("createComment()")
    class CreateComment {

        @Test
        @DisplayName("should create comment when author is project owner")
        void shouldCreateCommentByOwner() {
            CommentRequest req = new CommentRequest("Looks good!");
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
            when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
            when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
                Comment c = inv.getArgument(0);
                c.setId(500L);
                return c;
            });

            CommentResponse resp = commentService.createComment(100L, req, 1L, false);

            assertNotNull(resp);
            assertEquals("Looks good!", resp.content());
            assertEquals(1L, resp.authorId());
            assertEquals(100L, resp.taskId());
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("should create comment when author is task assignee")
        void shouldCreateCommentByAssignee() {
            CommentRequest req = new CommentRequest("Working on it.");
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
            when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
            when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
                Comment c = inv.getArgument(0);
                c.setId(501L);
                return c;
            });

            CommentResponse resp = commentService.createComment(100L, req, 2L, false);

            assertNotNull(resp);
            assertEquals("Working on it.", resp.content());
            assertEquals(2L, resp.authorId());
        }

        @Test
        @DisplayName("should create comment when author is project member")
        void shouldCreateCommentByProjectMember() {
            CommentRequest req = new CommentRequest("Member update");
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
            when(projectMemberRepository.existsByProjectIdAndUserId(10L, 3L)).thenReturn(true);
            when(userRepository.findById(3L)).thenReturn(Optional.of(stranger));
            when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
                Comment c = inv.getArgument(0);
                c.setId(502L);
                return c;
            });

            CommentResponse resp = commentService.createComment(100L, req, 3L, false);

            assertNotNull(resp);
            assertEquals("Member update", resp.content());
            assertEquals(3L, resp.authorId());
        }

        @Test
        @DisplayName("should deny comment creation when user has no access to task")
        void shouldDenyCommentByStranger() {
            CommentRequest req = new CommentRequest("Spam");
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            assertThrows(AccessDeniedException.class, () ->
                    commentService.createComment(100L, req, 3L, false));
            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when task does not exist")
        void shouldThrowWhenTaskNotFound() {
            CommentRequest req = new CommentRequest("Comment");
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                    commentService.createComment(999L, req, 1L, false));
        }
    }

    @Nested
    @DisplayName("getCommentsByTaskId()")
    class GetComments {

        @Test
        @DisplayName("should return comments when authorized")
        void shouldReturnComments() {
            Comment c1 = new Comment(task, owner, "Comment 1");
            c1.setId(1L);
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));
            when(commentRepository.findByTaskIdOrderByCreatedAtAscIdAsc(100L)).thenReturn(List.of(c1));

            List<CommentResponse> comments = commentService.getCommentsByTaskId(100L, 1L, false);

            assertEquals(1, comments.size());
            assertEquals("Comment 1", comments.get(0).content());
        }

        @Test
        @DisplayName("should deny comments access for stranger")
        void shouldDenyCommentsForStranger() {
            when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

            assertThrows(AccessDeniedException.class, () ->
                    commentService.getCommentsByTaskId(100L, 3L, false));
        }
    }

    @Nested
    @DisplayName("deleteComment()")
    class DeleteComment {

        @Test
        @DisplayName("author can delete their own comment")
        void authorCanDelete() {
            Comment comment = new Comment(task, assignee, "My comment");
            comment.setId(50L);
            when(commentRepository.findById(50L)).thenReturn(Optional.of(comment));

            commentService.deleteComment(50L, 2L, false);

            verify(commentRepository, times(1)).delete(comment);
        }

        @Test
        @DisplayName("project owner can delete any comment on their project task (moderation)")
        void projectOwnerCanModerate() {
            Comment comment = new Comment(task, assignee, "Assignee comment");
            comment.setId(51L);
            when(commentRepository.findById(51L)).thenReturn(Optional.of(comment));

            commentService.deleteComment(51L, 1L, false);

            verify(commentRepository, times(1)).delete(comment);
        }

        @Test
        @DisplayName("unrelated user cannot delete someone else's comment")
        void strangerCannotDelete() {
            Comment comment = new Comment(task, assignee, "Assignee comment");
            comment.setId(52L);
            when(commentRepository.findById(52L)).thenReturn(Optional.of(comment));

            assertThrows(AccessDeniedException.class, () ->
                    commentService.deleteComment(52L, 3L, false));
            verify(commentRepository, never()).delete(any());
        }
    }
}

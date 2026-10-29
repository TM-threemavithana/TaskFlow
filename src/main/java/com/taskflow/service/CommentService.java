package com.taskflow.service;

import com.taskflow.dto.CommentRequest;
import com.taskflow.dto.CommentResponse;
import com.taskflow.dto.DtoMapper;
import com.taskflow.dto.PagedResponse;
import com.taskflow.entity.Comment;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.AccessDeniedException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          TaskRepository taskRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /**
     * Post a comment on a task.
     * Allowed: Project Owner, Task Assignee, or Admin.
     */
    public CommentResponse createComment(Long taskId, CommentRequest request, Long currentUserId, boolean isAdmin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        checkCommentAccess(task, currentUserId, isAdmin);

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Comment comment = new Comment(task, author, request.content());
        Comment saved = commentRepository.save(comment);

        return DtoMapper.toCommentResponse(saved);
    }

    /**
     * Retrieve all comments for a task in stable chronological order.
     * Allowed: Project Owner, Task Assignee, or Admin.
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTaskId(Long taskId, Long currentUserId, boolean isAdmin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        checkCommentAccess(task, currentUserId, isAdmin);

        return commentRepository.findByTaskIdOrderByCreatedAtAscIdAsc(taskId).stream()
                .map(DtoMapper::toCommentResponse)
                .toList();
    }

    /**
     * Paginated retrieval of comments for a task.
     */
    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getCommentsByTaskId(Long taskId, Long currentUserId, boolean isAdmin, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        checkCommentAccess(task, currentUserId, isAdmin);

        Page<Comment> page = commentRepository.findByTaskId(taskId, pageable);
        return new PagedResponse<>(
                page.getContent().stream().map(DtoMapper::toCommentResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /**
     * Delete a comment.
     * Allowed: Comment Author, Project Owner (moderation), or Admin.
     */
    public void deleteComment(Long commentId, Long currentUserId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        boolean isAuthor = comment.isAuthoredBy(currentUserId);
        boolean isProjectOwner = comment.getTask().getProject().isOwnedBy(currentUserId);

        if (!isAdmin && !isAuthor && !isProjectOwner) {
            throw new AccessDeniedException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    // ---- Private helpers ----

    private void checkCommentAccess(Task task, Long userId, boolean isAdmin) {
        if (isAdmin) return;
        boolean isProjectOwner = task.getProject().isOwnedBy(userId);
        boolean isAssignee = task.isAssignedTo(userId);
        if (!isProjectOwner && !isAssignee) {
            throw new AccessDeniedException("You do not have access to this task's comments");
        }
    }
}

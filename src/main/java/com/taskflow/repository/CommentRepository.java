package com.taskflow.repository;

import com.taskflow.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** Returns all comments for a task in stable chronological order with deterministic tie-breaking. */
    List<Comment> findByTaskIdOrderByCreatedAtAscIdAsc(Long taskId);

    /** Paginated comments for a task. */
    Page<Comment> findByTaskId(Long taskId, Pageable pageable);
}

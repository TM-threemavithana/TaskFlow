package com.taskflow.repository;

import com.taskflow.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Task repository.
 *
 * <p>Extends {@code JpaSpecificationExecutor<Task>}: this adds the method
 * {@code findAll(Specification<Task>, Pageable)} which lets us build dynamic WHERE clauses
 * at runtime (see {@link com.taskflow.specification.TaskSpecifications}).
 * It's the Spring Data way of implementing the SPECIFICATION pattern (a close relative of the
 * Strategy pattern).
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findByProjectId(Long projectId, Pageable pageable);
}

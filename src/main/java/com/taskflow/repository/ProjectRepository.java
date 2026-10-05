package com.taskflow.repository;

import com.taskflow.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Project repository. Demonstrates JPQL (Java Persistence Query Language) and
 * pagination with {@link Pageable}.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** Finds all projects owned by a user, paginated. */
    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Custom JPQL query. JPQL operates on ENTITIES (not tables), so we write "Project p"
     * instead of "projects p". Spring fills in :ownerId from the method parameter.
     */
    @Query("SELECT p FROM Project p WHERE p.owner.id = :ownerId ORDER BY p.createdAt DESC")
    Page<Project> findByOwnerIdSorted(@Param("ownerId") Long ownerId, Pageable pageable);
}

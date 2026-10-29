package com.taskflow.repository;

import com.taskflow.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectMember> findByProjectIdOrderByCreatedAtAsc(Long projectId);

    List<ProjectMember> findByUserId(Long userId);

    @Query("SELECT pm.project.id FROM ProjectMember pm WHERE pm.user.id = :userId")
    Set<Long> findProjectIdsByUserId(@Param("userId") Long userId);

    void deleteByProjectIdAndUserId(Long projectId, Long userId);
}


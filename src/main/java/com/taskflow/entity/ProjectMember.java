package com.taskflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * PROJECT MEMBER ENTITY (Explicit Join Model).
 *
 * <p>Represents membership/collaboration of a User in a Project.
 * The Project Owner remains independent in the Project entity.
 */
@Entity
@Table(
    name = "project_members",
    uniqueConstraints = @UniqueConstraint(name = "uk_project_members_project_user", columnNames = {"project_id", "user_id"})
)
public class ProjectMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String role = "MEMBER";

    protected ProjectMember() {
    }

    public ProjectMember(Project project, User user) {
        this(project, user, "MEMBER");
    }

    public ProjectMember(Project project, User user, String role) {
        this.project = project;
        this.user = user;
        this.role = (role != null && !role.isBlank()) ? role : "MEMBER";
    }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

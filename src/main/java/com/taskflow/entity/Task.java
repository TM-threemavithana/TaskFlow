package com.taskflow.entity;

import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * TASK ENTITY + BUILDER PATTERN.
 *
 * <p>Notice we REUSE the Phase 1 enums {@link TaskStatus} and {@link Priority} from
 * {@code com.taskflow.model} - good code is reusable across layers.
 *
 * <h2>DESIGN PATTERN: BUILDER (creational)</h2>
 * A task has many optional fields (description, priority, due date, assignee). Constructors with many
 * parameters are hard to read:  {@code new Task("Fix", null, HIGH, null, project, null)} - which null is what?
 * The Builder gives a readable, step-by-step "fluent" API:
 * <pre>
 *   Task task = Task.builder()
 *                   .title("Fix login bug")
 *                   .priority(Priority.HIGH)
 *                   .project(project)
 *                   .build();
 * </pre>
 * {@code build()} is also the single place to validate the object before it is created.
 * (Libraries like Lombok can generate builders with {@code @Builder}; we write it by hand to learn it.)
 */
@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "due_date")
    private LocalDate dueDate;

    /** The OWNING side of the Project <-> Task relationship: this field holds the project_id foreign key. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Optional: a task may be unassigned (assignee_id is NULL). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    /** Required by JPA. */
    protected Task() {
    }

    /** Private: the ONLY way to create a Task from outside is through the Builder. */
    private Task(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.status = builder.status;
        this.priority = builder.priority;
        this.dueDate = builder.dueDate;
        this.project = builder.project;
        this.assignee = builder.assignee;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Business rule that belongs to the Task itself (ENCAPSULATION, like Phase 1's complete()). */
    public boolean isOverdue(LocalDate today) {
        return dueDate != null && status != TaskStatus.COMPLETED && dueDate.isBefore(today);
    }

    public boolean isAssignedTo(Long userId) {
        return assignee != null && assignee.getId() != null && assignee.getId().equals(userId);
    }

    // ---------------- getters / setters ----------------
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getAssignee() { return assignee; }
    public void setAssignee(User assignee) { this.assignee = assignee; }

    @Override
    public String toString() {
        return "Task{id=" + getId() + ", title='" + title + "', status=" + status + ", priority=" + priority + '}';
    }

    /**
     * The BUILDER. A static nested class: it does not need an outer Task instance to exist.
     * Each "setter" returns {@code this}, which is what allows method chaining.
     */
    public static class Builder {
        private String title;
        private String description;
        private TaskStatus status = TaskStatus.TODO;     // sensible defaults, same as Phase 1
        private Priority priority = Priority.MEDIUM;
        private LocalDate dueDate;
        private Project project;
        private User assignee;

        private Builder() {
        }

        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(TaskStatus status) { this.status = status; return this; }
        public Builder priority(Priority priority) { this.priority = priority; return this; }
        public Builder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public Builder project(Project project) { this.project = project; return this; }
        public Builder assignee(User assignee) { this.assignee = assignee; return this; }

        /** Validates and creates the object. Invalid objects can never be built. */
        public Task build() {
            if (title == null || title.isBlank()) {
                throw new IllegalStateException("A task must have a title");
            }
            if (status == null) status = TaskStatus.TODO;
            if (priority == null) priority = Priority.MEDIUM;
            return new Task(this);
        }
    }
}

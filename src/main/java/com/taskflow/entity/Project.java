package com.taskflow.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * PROJECT ENTITY.
 *
 * <p>RELATIONSHIPS (the "R" in relational databases):
 * <ul>
 *   <li>Many projects belong to one owner          -> {@code @ManyToOne User owner}  (column owner_id)</li>
 *   <li>One project contains many tasks             -> {@code @OneToMany List<Task> tasks}</li>
 * </ul>
 *
 * <p>COMPOSITION: a Project "HAS-A" list of tasks. Tasks cannot exist without their project:
 * {@code cascade = ALL} + {@code orphanRemoval = true} means deleting a project deletes its tasks too.
 */
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    /**
     * LAZY loading: the owner row is only fetched from the database when we actually call
     * getOwner().getName(). This avoids loading data we don't need. (Default for @ManyToOne is EAGER,
     * which is a common performance trap, so we change it.)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * The inverse side of Task.project. {@code mappedBy = "project"} says "the foreign key lives in the
     * Task table, in the field called 'project'". This side does not own a column.
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    protected Project() {
    }

    public Project(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    /**
     * Helper that keeps BOTH sides of the bidirectional relationship in sync.
     * If you only did tasks.add(task) the task's project field would still be null in memory.
     */
    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }

    public boolean isOwnedBy(Long userId) {
        return owner != null && owner.getId() != null && owner.getId().equals(userId);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public List<Task> getTasks() { return tasks; }
}

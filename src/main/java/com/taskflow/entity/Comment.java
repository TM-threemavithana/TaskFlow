package com.taskflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * COMMENT ENTITY.
 *
 * <p>Represents a discussion message posted on a task.
 * Relationships:
 * <ul>
 *   <li>Many comments belong to one task   -> {@code @ManyToOne Task task}</li>
 *   <li>Many comments belong to one author -> {@code @ManyToOne User author}</li>
 * </ul>
 */
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 2000)
    private String content;

    protected Comment() {
    }

    public Comment(Task task, User author, String content) {
        this.task = task;
        this.author = author;
        this.content = content;
    }

    public boolean isAuthoredBy(Long userId) {
        return author != null && author.getId() != null && author.getId().equals(userId);
    }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

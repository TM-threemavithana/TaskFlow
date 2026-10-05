package com.taskflow.model;

/**
 * The Task class demonstrates the core OOP concept of an Object managing its own state.
 */
public class Task {
    // ENCAPSULATION: Data hiding. External code cannot modify these directly.
    private Long id;
    private String title;
    private String description;
    
    // Using Enums restricts these variables to specific predefined values,
    // which prevents invalid states (e.g. someone typing status = "ALMOST_DONE").
    private TaskStatus status;
    private Priority priority;

    /**
     * CONSTRUCTOR
     * Note that we only require 'id' and 'title' to create a Task.
     * The other fields get sensible default values automatically.
     */
    public Task(Long id, String title) {
        this.id = id;
        this.title = title;
        // Business Logic: Every new task starts as TODO and MEDIUM priority.
        this.status = TaskStatus.TODO;
        this.priority = Priority.MEDIUM;
    }

    /**
     * BEHAVIOR (Methods)
     * This is a great example of Encapsulation. Instead of the outside world saying:
     *   task.setStatus(TaskStatus.COMPLETED);
     * The outside world simply tells the task to complete itself:
     *   task.complete();
     * This keeps the business rules inside the Task class where they belong.
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
    }

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                '}';
    }
}

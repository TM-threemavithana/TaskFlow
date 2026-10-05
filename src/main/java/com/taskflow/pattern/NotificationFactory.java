package com.taskflow.pattern;

import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;

/**
 * DESIGN PATTERN: FACTORY METHOD (creational) - standalone demo.
 *
 * <p>PROBLEM: creating notification messages is complex and varies by task event type.
 * Scattering {@code if/else} or {@code switch} blocks throughout the code violates
 * the Open/Closed Principle (OCP): adding a new event type means editing existing code.
 *
 * <p>FACTORY SOLUTION: centralize object creation in ONE place. The caller says WHAT it wants
 * (the type), and the factory decides HOW to create it. To add a new type, add a new case -
 * no existing client code changes.
 *
 * <p>Java has a built-in factory method: {@code List.of()}, {@code Map.of()}, {@code Optional.of()}.
 * Those are all static factory methods.
 */
public final class NotificationFactory {

    private NotificationFactory() {
    }

    /**
     * A simple notification POJO created by the factory.
     */
    public record Notification(String subject, String body, NotificationType type) {
        public enum NotificationType { EMAIL, IN_APP }
    }

    /**
     * FACTORY METHOD: creates the right Notification based on the event type.
     * The caller doesn't know the construction details.
     */
    public static Notification createTaskNotification(String eventType, String taskTitle,
                                                       Priority priority, TaskStatus status) {
        return switch (eventType.toUpperCase()) {
            case "CREATED" -> new Notification(
                    "New Task: " + taskTitle,
                    String.format("A new %s-priority task '%s' has been created.", priority, taskTitle),
                    Notification.NotificationType.IN_APP
            );
            case "COMPLETED" -> new Notification(
                    "Task Completed: " + taskTitle,
                    String.format("Task '%s' has been marked as completed! 🎉", taskTitle),
                    Notification.NotificationType.EMAIL
            );
            case "OVERDUE" -> new Notification(
                    "⚠️ Overdue Task: " + taskTitle,
                    String.format("Task '%s' (priority: %s) is past its due date!", taskTitle, priority),
                    Notification.NotificationType.EMAIL
            );
            default -> new Notification(
                    "Task Update: " + taskTitle,
                    String.format("Task '%s' status: %s, priority: %s", taskTitle, status, priority),
                    Notification.NotificationType.IN_APP
            );
        };
    }
}

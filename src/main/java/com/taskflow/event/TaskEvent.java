package com.taskflow.event;

import com.taskflow.entity.Task;

/**
 * DESIGN PATTERN: OBSERVER (behavioral) - The Event.
 *
 * <p>The OBSERVER pattern lets objects (observers/listeners) subscribe to events published by
 * another object (the subject/publisher) without tight coupling. In Spring this is built in:
 * <ol>
 *   <li>Define an event class (this class).</li>
 *   <li>PUBLISH it from the service: {@code applicationEventPublisher.publishEvent(event)}</li>
 *   <li>LISTEN with {@code @EventListener} on any bean method.</li>
 * </ol>
 *
 * <p>Benefit: the TaskService doesn't know or care WHO listens. We can add email notifications,
 * Slack notifications, audit logs, etc. without changing TaskService at all (Open/Closed Principle).
 *
 * <p>A record is a good fit here: events are immutable snapshots of something that happened.
 */
public record TaskEvent(
        /** What happened. */
        Type type,
        /** The task it happened to (a snapshot at the time of the event). */
        Task task
) {
    public enum Type {
        CREATED,
        UPDATED,
        COMPLETED,
        DELETED
    }
}

package com.taskflow.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * OBSERVER: listens for {@link TaskEvent}s and "notifies" (here: logs a message).
 *
 * <p>In a real app this would send emails, push notifications, or write to a message queue.
 * The service that publishes events does not know this class exists - they are decoupled.
 */
@Component
public class TaskEventListener {

    private static final Logger log = LoggerFactory.getLogger(TaskEventListener.class);

    /**
     * {@code @EventListener}: Spring calls this method whenever a {@code TaskEvent} is published.
     * No interface to implement, no registration boilerplate - just annotate.
     */
    @EventListener
    public void onTaskEvent(TaskEvent event) {
        switch (event.type()) {
            case CREATED   -> log.info("📋 Task created: '{}'", event.task().getTitle());
            case UPDATED   -> log.info("✏️  Task updated: '{}'", event.task().getTitle());
            case COMPLETED -> log.info("✅ Task completed: '{}'", event.task().getTitle());
            case DELETED   -> log.info("🗑️  Task deleted: '{}'", event.task().getTitle());
        }
    }
}

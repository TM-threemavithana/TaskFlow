package com.taskflow.pattern;

import com.taskflow.entity.Task;
import com.taskflow.model.Priority;

import java.util.Comparator;

/**
 * DESIGN PATTERN: STRATEGY (behavioral) - standalone demo.
 *
 * <p>PROBLEM: We need to sort tasks by different criteria (priority, due date, title).
 * A naive approach: write a separate sorting method for each criterion.
 * If a new criterion is added, you must change the sorting code.
 *
 * <p>STRATEGY PATTERN SOLUTION: Define a FAMILY of algorithms (each sort order is a "strategy"),
 * make them interchangeable, and let the client choose which one to use at runtime.
 *
 * <p>In Java, the STRATEGY pattern is naturally represented by INTERFACES:
 * {@link Comparator} is a strategy interface. Each implementation is a concrete strategy.
 * Java 8+ lambdas make this even more concise.
 *
 * <p>USED IN THE REAL APP: {@link com.taskflow.specification.TaskSpecifications} is a strategy
 * pattern for building query filters.
 */
public final class SortingStrategyDemo {

    private SortingStrategyDemo() {
    }

    // ---- Concrete Strategies (as Comparator implementations) ----

    /** Sort by priority: HIGH first, then MEDIUM, then LOW. */
    public static final Comparator<Task> BY_PRIORITY =
            Comparator.comparing(Task::getPriority, (a, b) -> {
                // Custom ordering: HIGH (0) < MEDIUM (1) < LOW (2)
                return Integer.compare(priorityRank(a), priorityRank(b));
            });

    /** Sort by due date (nulls last). */
    public static final Comparator<Task> BY_DUE_DATE =
            Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));

    /** Sort alphabetically by title. */
    public static final Comparator<Task> BY_TITLE =
            Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER);

    private static int priorityRank(Priority p) {
        return switch (p) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }
}

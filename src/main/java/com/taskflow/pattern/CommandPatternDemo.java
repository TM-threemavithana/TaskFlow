package com.taskflow.pattern;

/**
 * DESIGN PATTERN: COMMAND (behavioral) - standalone demo.
 *
 * <p>PROBLEM: We want to support undo/redo for task operations. Without the Command pattern,
 * the undo logic would be scattered across the UI/controller code.
 *
 * <p>COMMAND SOLUTION: Encapsulate each action as an OBJECT. Each command object knows:
 * <ul>
 *   <li>HOW to execute the action ({@code execute()}).</li>
 *   <li>HOW to undo it ({@code undo()}).</li>
 * </ul>
 * Commands can be stored in a history list for undo/redo, queued for later execution,
 * or logged for auditing.
 *
 * <p>Real-world examples:
 * <ul>
 *   <li>Ctrl+Z in any text editor (undo stack of Command objects).</li>
 *   <li>Database transactions (each SQL statement is a command that can be rolled back).</li>
 * </ul>
 */
public final class CommandPatternDemo {

    private CommandPatternDemo() {
    }

    // ---- The Command interface ----
    public interface Command {
        void execute();
        void undo();
        String describe();
    }

    // ---- A simple "receiver" (the object being acted on) ----
    /** Mutable task-like object for demo purposes. */
    public static class MutableTask {
        private String title;
        private String status;

        public MutableTask(String title, String status) {
            this.title = title;
            this.status = status;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() { return "Task{title='" + title + "', status='" + status + "'}"; }
    }

    // ---- Concrete commands ----

    /** Command to change a task's status. Remembers the old status for undo. */
    public static class ChangeStatusCommand implements Command {
        private final MutableTask task;
        private final String newStatus;
        private String oldStatus;   // saved for undo

        public ChangeStatusCommand(MutableTask task, String newStatus) {
            this.task = task;
            this.newStatus = newStatus;
        }

        @Override
        public void execute() {
            this.oldStatus = task.getStatus();   // save current state
            task.setStatus(newStatus);
        }

        @Override
        public void undo() {
            task.setStatus(oldStatus);           // restore saved state
        }

        @Override
        public String describe() {
            return "Change status from '" + oldStatus + "' to '" + newStatus + "'";
        }
    }

    /** Command to rename a task. */
    public static class RenameCommand implements Command {
        private final MutableTask task;
        private final String newTitle;
        private String oldTitle;

        public RenameCommand(MutableTask task, String newTitle) {
            this.task = task;
            this.newTitle = newTitle;
        }

        @Override
        public void execute() {
            this.oldTitle = task.getTitle();
            task.setTitle(newTitle);
        }

        @Override
        public void undo() {
            task.setTitle(oldTitle);
        }

        @Override
        public String describe() {
            return "Rename from '" + oldTitle + "' to '" + newTitle + "'";
        }
    }
}

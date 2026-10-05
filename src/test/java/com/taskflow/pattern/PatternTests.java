package com.taskflow.pattern;

import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;
import com.taskflow.pattern.CommandPatternDemo.ChangeStatusCommand;
import com.taskflow.pattern.CommandPatternDemo.MutableTask;
import com.taskflow.pattern.CommandPatternDemo.RenameCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the design pattern demos. Proves they work correctly.
 */
@DisplayName("Design Pattern Tests")
class PatternTests {

    @Test
    @DisplayName("Factory should create correct notification for COMPLETED event")
    void factoryCompletedNotification() {
        var notification = NotificationFactory.createTaskNotification(
                "COMPLETED", "Fix bug", Priority.HIGH, TaskStatus.COMPLETED);

        assertTrue(notification.subject().contains("Completed"));
        assertEquals(NotificationFactory.Notification.NotificationType.EMAIL, notification.type());
    }

    @Test
    @DisplayName("Decorator should stack formatters correctly")
    void decoratorStacking() {
        String result = TextFormatterDemo.demo();
        assertEquals("[TASK] **FIX LOGIN BUG**", result);
    }

    @Test
    @DisplayName("Command should support execute and undo")
    void commandExecuteAndUndo() {
        MutableTask task = new MutableTask("Original", "TODO");

        ChangeStatusCommand cmd = new ChangeStatusCommand(task, "DONE");
        cmd.execute();
        assertEquals("DONE", task.getStatus());

        cmd.undo();
        assertEquals("TODO", task.getStatus());
    }

    @Test
    @DisplayName("Command rename should support undo")
    void commandRenameUndo() {
        MutableTask task = new MutableTask("Old Title", "TODO");

        RenameCommand cmd = new RenameCommand(task, "New Title");
        cmd.execute();
        assertEquals("New Title", task.getTitle());

        cmd.undo();
        assertEquals("Old Title", task.getTitle());
    }

    @Test
    @DisplayName("Template Method should generate text report with correct structure")
    void templateMethodTextReport() {
        ReportGenerator gen = new ReportGenerator.TextReportGenerator();
        Map<String, Object> data = Map.of("Total", 10);

        String report = gen.generateReport("My Report", data);

        assertTrue(report.startsWith("=== My Report ==="));
        assertTrue(report.contains("Total: 10"));
        assertTrue(report.endsWith("=== End of Report ===\n"));
    }

    @Test
    @DisplayName("Template Method should generate HTML report with correct tags")
    void templateMethodHtmlReport() {
        ReportGenerator gen = new ReportGenerator.HtmlReportGenerator();
        Map<String, Object> data = Map.of("Tasks", 5);

        String report = gen.generateReport("Summary", data);

        assertTrue(report.contains("<h1>Summary</h1>"));
        assertTrue(report.contains("<li>"));
        assertTrue(report.endsWith("</body></html>\n"));
    }
}

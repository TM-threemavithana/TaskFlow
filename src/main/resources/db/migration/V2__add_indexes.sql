-- =====================================================================================
-- FLYWAY MIGRATION V2 - INDEXES
--
-- Shows how a schema EVOLVES: V1 already ran, so we add a new versioned file instead of editing V1.
--
-- An INDEX is like the index at the back of a book: it lets the database find rows quickly
-- without scanning the whole table. We index the columns we filter / join on most often
-- (see TaskSpecifications: project, assignee, status, priority, due date).
-- Trade-off: indexes speed up reads but slightly slow down writes and use disk space.
-- =====================================================================================

CREATE INDEX idx_projects_owner   ON projects (owner_id);
CREATE INDEX idx_tasks_project    ON tasks (project_id);
CREATE INDEX idx_tasks_assignee   ON tasks (assignee_id);
CREATE INDEX idx_tasks_status     ON tasks (status);
CREATE INDEX idx_tasks_priority   ON tasks (priority);
CREATE INDEX idx_tasks_due_date   ON tasks (due_date);

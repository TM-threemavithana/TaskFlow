package com.taskflow.specification;

import com.taskflow.entity.Task;
import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * DYNAMIC QUERY BUILDER using the JPA Criteria API + Spring Data Specifications.
 *
 * <p>DESIGN PATTERN: STRATEGY (behavioral).
 * Each method returns a {@code Specification<Task>}, which is a strategy for adding a WHERE clause.
 * They can be COMBINED with {@code .and()} / {@code .or()}, so the service layer builds
 * the query dynamically based on which filter parameters the user actually sent.
 *
 * <p>This replaces the need for many separate repository methods like:
 *   findByStatus, findByPriority, findByStatusAndPriority, findByStatusAndPriorityAndAssigneeId...
 * (which would explode combinatorially).
 */
public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> hasProject(Long projectId) {
        // The lambda receives (root, query, cb).
        //   root  = the Task entity (root.get("project").get("id") means task.project.id)
        //   query = the overall query (unused here)
        //   cb    = CriteriaBuilder, used to create comparisons: equal, like, greaterThan...
        return (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(Priority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasAssignee(Long assigneeId) {
        return (root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    /** Search title case-insensitively (SQL LIKE '%keyword%'). */
    public static Specification<Task> titleContains(String keyword) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    /**
     * Scopes task visibility to resources the given user has legitimate access to:
     * - The user is the owner of the project (task.project.owner.id == userId), OR
     * - The user is assigned to the task (task.assignee.id == userId), OR
     * - The project is in the set of projects the user is a member of.
     */
    public static Specification<Task> isAccessibleByUser(Long userId, java.util.Collection<Long> memberProjectIds) {
        return (root, query, cb) -> {
            var ownerPredicate = cb.equal(root.get("project").get("owner").get("id"), userId);
            var assigneePredicate = cb.equal(root.get("assignee").get("id"), userId);
            if (memberProjectIds != null && !memberProjectIds.isEmpty()) {
                var memberPredicate = root.get("project").get("id").in(memberProjectIds);
                return cb.or(ownerPredicate, assigneePredicate, memberPredicate);
            }
            return cb.or(ownerPredicate, assigneePredicate);
        };
    }
}

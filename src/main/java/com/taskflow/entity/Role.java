package com.taskflow.entity;

/**
 * Security roles. Stored in the database as text ("USER" / "ADMIN") thanks to {@code @Enumerated(STRING)}.
 *
 * <p>Spring Security expects role authorities to start with "ROLE_", so a user with {@code ADMIN}
 * gets the authority "ROLE_ADMIN" (see {@code SecurityUser#getAuthorities()}), and
 * {@code @PreAuthorize("hasRole('ADMIN')")} checks for exactly that.
 */
public enum Role {
    /** Regular user: manages their own projects and the tasks assigned to them. */
    USER,
    /** Administrator: can see everything and change other users' roles. */
    ADMIN
}

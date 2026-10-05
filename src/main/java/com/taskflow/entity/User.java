package com.taskflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * USER ENTITY - the database version of Phase 1's {@code com.taskflow.model.User}.
 *
 * <p>Phase 1 classes were "plain old Java objects" living only in memory. An ENTITY is a class
 * that JPA/Hibernate maps to a database table: each object = one row, each field = one column.
 *
 * <p>{@code @Table(name = "users")}: "user" is a reserved word in PostgreSQL, so we use "users".
 * The table itself is created by the Flyway script {@code V1__create_core_tables.sql}, not by Hibernate.
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * NEVER store plain-text passwords! This column holds a BCrypt HASH such as
     * "$2a$10$N9qo8uLOickgx2ZMRZoMye...". BCrypt is one-way and salted, so even with database access
     * an attacker cannot read the original password.
     */
    @Column(nullable = false)
    private String password;

    /** EnumType.STRING stores "ADMIN" instead of the ordinal number 1 - safer if enum order changes. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    /** JPA REQUIREMENT: a no-argument constructor. 'protected' so normal code can't create half-empty users. */
    protected User() {
    }

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    /** Note: the password is deliberately left out so it never ends up in log files. */
    @Override
    public String toString() {
        return "User{id=" + getId() + ", email='" + email + "', role=" + role + '}';
    }
}

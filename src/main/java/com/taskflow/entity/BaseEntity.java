package com.taskflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * BASE CLASS FOR ALL DATABASE ENTITIES  (INHERITANCE + DRY: "Don't Repeat Yourself").
 *
 * <p>Every table has an id, created_at and updated_at column. Instead of copy-pasting these fields
 * into User, Project and Task, we put them here once.
 *
 * <p>{@code @MappedSuperclass} tells JPA: "this class is NOT a table by itself, but its fields
 * should be added as columns to the tables of the sub-classes".
 *
 * <p>It is {@code abstract} because it makes no sense to create a plain "BaseEntity" object.
 */
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * PRIMARY KEY.
     * {@code IDENTITY} = the database generates the id (auto-increment / identity column).
     * We use the wrapper type {@code Long} (not {@code long}) so that "no id yet" can be represented as null,
     * which is how JPA knows an entity is new and must be INSERTed instead of UPDATEd.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA LIFECYCLE CALLBACK: Hibernate calls this automatically right before the INSERT statement.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** Called automatically right before an UPDATE statement. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    /** Normally the database assigns ids. This setter exists so unit tests can build entities with ids. */
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /**
     * EQUALS / HASHCODE FOR ENTITIES.
     * Two entity objects represent the same database row if they are the same type and have the same id.
     * A new (unsaved) entity has id == null, so it is only equal to itself.
     * hashCode is constant per class so it does not change when the id gets assigned after saving
     * (otherwise the object would get "lost" inside a HashSet).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity other = (BaseEntity) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

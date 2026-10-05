package com.taskflow.repository;

import com.taskflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REPOSITORY LAYER  (Spring Data JPA).
 *
 * <p>This is an INTERFACE, not a class. We do NOT write any implementation code.
 * Spring Data JPA creates a proxy object at runtime that provides:
 * <ul>
 *   <li>CRUD: {@code save()}, {@code findById()}, {@code deleteById()}, {@code findAll()} ...</li>
 *   <li>Pagination: {@code findAll(Pageable)} returns a Page object.</li>
 *   <li>"Query derivation": just NAME a method and Spring generates the SQL.
 *       {@code findByEmail(String email)} → {@code SELECT * FROM users WHERE email = ?}</li>
 * </ul>
 *
 * <p>{@code JpaRepository<User, Long>}: first generic = the entity type, second = the primary key type.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * DERIVED QUERY: Spring reads the method name and generates the query.
     * Returns {@link Optional} because the user might not exist.
     *
     * <p>OPTIONAL: Java's way of saying "this might be absent". Forces callers to handle the
     * absent case explicitly instead of risking a NullPointerException.
     */
    Optional<User> findByEmail(String email);

    /** Used during registration to check for duplicate emails. */
    boolean existsByEmail(String email);
}

package com.taskflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO = DATA TRANSFER OBJECT.
 *
 * <p>WHY DTOs instead of entities?
 * <ol>
 *   <li>SECURITY: never expose the password hash, internal ids, or lazy-loaded collections to the API.</li>
 *   <li>VALIDATION: DTOs carry {@code @NotBlank}, {@code @Email}, etc. Spring validates before reaching the controller.</li>
 *   <li>DECOUPLING: the API shape (what the frontend sees) is independent of the database schema.</li>
 *   <li>VERSIONING: you can change the database without breaking existing API clients.</li>
 * </ol>
 *
 * <p>We use Java 17 RECORDS. A record is an immutable data carrier that auto-generates:
 * constructor, getters (name(), email()...), equals(), hashCode(), toString().
 * Records are perfect for DTOs because DTOs should be read-only value objects.
 */

// ======================== AUTH DTOs ========================

/**
 * Sent by the client to register a new account.
 * {@code @Valid} on the controller parameter triggers the constraint checks.
 */
public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
        String password
) {}

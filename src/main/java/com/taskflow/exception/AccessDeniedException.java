package com.taskflow.exception;

/**
 * CUSTOM EXCEPTION for "403 Forbidden".
 * Thrown when a user tries to access/modify a resource they don't own.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}

package com.taskflow.exception;

/**
 * CUSTOM EXCEPTION for "409 Conflict" scenarios.
 * Example: trying to register with an email that already exists.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

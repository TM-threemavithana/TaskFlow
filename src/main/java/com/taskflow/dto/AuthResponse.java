package com.taskflow.dto;

/** Returned to the client after a successful login. */
public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) {
    /** Convenience constructor: tokenType defaults to "Bearer" (the OAuth 2 standard). */
    public AuthResponse(String token, UserResponse user) {
        this(token, "Bearer", user);
    }
}

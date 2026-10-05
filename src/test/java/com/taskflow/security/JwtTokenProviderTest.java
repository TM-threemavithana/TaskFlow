package com.taskflow.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JWT token provider.
 * Tests token generation, validation, and claim extraction.
 */
@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        // Base64-encoded secret (at least 32 bytes for HMAC-SHA-256)
        props.setSecret("dGVzdC1zZWNyZXQta2V5LWZvci1qdW5pdC10ZXN0cy1vbmx5LW5vdC1wcm9kdWN0aW9u");
        props.setExpirationMs(3600000L); // 1 hour
        jwtTokenProvider = new JwtTokenProvider(props);
    }

    @Test
    @DisplayName("should generate a valid JWT token")
    void shouldGenerateToken() {
        String token = jwtTokenProvider.generateToken(1L, "test@example.com", "USER");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT has 3 parts separated by dots
        assertEquals(3, token.split("\\.").length, "JWT should have 3 parts: header.payload.signature");
    }

    @Test
    @DisplayName("should extract user ID from token")
    void shouldExtractUserId() {
        String token = jwtTokenProvider.generateToken(42L, "user@test.com", "ADMIN");

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertEquals(42L, userId);
    }

    @Test
    @DisplayName("should extract role from token")
    void shouldExtractRole() {
        String token = jwtTokenProvider.generateToken(1L, "admin@test.com", "ADMIN");

        String role = jwtTokenProvider.getRoleFromToken(token);

        assertEquals("ADMIN", role);
    }

    @Test
    @DisplayName("should validate a correctly signed token")
    void shouldValidateCorrectToken() {
        String token = jwtTokenProvider.generateToken(1L, "test@test.com", "USER");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("should reject a tampered token")
    void shouldRejectTamperedToken() {
        String token = jwtTokenProvider.generateToken(1L, "test@test.com", "USER");
        String tampered = token + "TAMPERED";

        assertFalse(jwtTokenProvider.validateToken(tampered));
    }

    @Test
    @DisplayName("should reject null/empty token")
    void shouldRejectNullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    @DisplayName("should reject a completely invalid token string")
    void shouldRejectInvalidString() {
        assertFalse(jwtTokenProvider.validateToken("not.a.jwt"));
    }
}

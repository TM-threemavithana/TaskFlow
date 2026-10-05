package com.taskflow.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CONFIGURATION PROPERTIES: binds the {@code app.jwt.*} keys from application.properties
 * to typed Java fields. Much safer than scattering {@code @Value("${app.jwt.secret}")} everywhere.
 *
 * <p>If you mistype a property name, the app fails to start (fail-fast) instead of silently
 * using a default value.
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** The signing key (Base64 encoded). In production, inject via environment variable. */
    private String secret;

    /** Token validity in milliseconds (default: 24 hours = 86_400_000 ms). */
    private long expirationMs = 86_400_000L;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getExpirationMs() { return expirationMs; }
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
}

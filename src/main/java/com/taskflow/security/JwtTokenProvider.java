package com.taskflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT TOKEN PROVIDER - creates and validates JSON Web Tokens.
 *
 * <p>JWT explained for beginners:
 * <ol>
 *   <li>User sends email + password to {@code /api/auth/login}.</li>
 *   <li>Server verifies the password, then creates a JWT containing the user's id and role.</li>
 *   <li>Server sends the JWT back. Client stores it (e.g. localStorage or a cookie).</li>
 *   <li>For every subsequent request, client sends the JWT in the header:
 *       {@code Authorization: Bearer eyJhbGciOi...}</li>
 *   <li>{@link JwtAuthenticationFilter} intercepts the request, calls this class to VERIFY the token
 *       (is the signature valid? is it expired?) and extracts the user id.</li>
 *   <li>If valid, the request proceeds authenticated. If not, 401 Unauthorized.</li>
 * </ol>
 *
 * <p>A JWT has 3 parts separated by dots:
 * <pre>
 *   HEADER.PAYLOAD.SIGNATURE
 *   eyJhbGciOi...  .  eyJzdWIiOi...  .  SflKxwRJSM...
 * </pre>
 * The SIGNATURE is created with a SECRET KEY that only the server knows. If anyone tampers with the
 * payload, the signature won't match and verification fails. That's how stateless auth works:
 * no session on the server, the token IS the proof.
 *
 * <p>DESIGN PATTERN: FACADE (structural).
 * This class hides the complexity of the JJWT library behind a simple interface:
 * {@code generateToken()}, {@code validateToken()}, {@code getUserIdFromToken()}.
 * Controllers and filters only call these three methods, they never see the JJWT API directly.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(JwtProperties props) {
        // Decode the Base64-encoded secret into raw bytes, then create an HMAC-SHA key.
        byte[] keyBytes = Base64.getDecoder().decode(props.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = props.getExpirationMs();
    }

    /** Create a new JWT for the given user. */
    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))      // "sub" claim = who this token is about
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)                         // "iat" claim
                .expiration(expiry)                    // "exp" claim
                .signWith(key)                         // sign with HMAC-SHA-256
                .compact();                            // serialize to the final "xxx.yyy.zzz" string
    }

    /** Extract the user id from a valid token's "sub" claim. */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /** Extract the role claim. */
    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    /** Returns true if the token is well-formed, correctly signed, and not expired. */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Covers: ExpiredJwtException, MalformedJwtException, SignatureException, etc.
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

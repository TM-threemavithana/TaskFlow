package com.taskflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT AUTHENTICATION FILTER.
 *
 * <p>This filter runs ONCE per HTTP request (guaranteed by {@link OncePerRequestFilter}).
 * It intercepts every request BEFORE it reaches the controller:
 * <pre>
 *   Client → JwtAuthenticationFilter → SecurityFilterChain → Controller
 * </pre>
 *
 * <p>DESIGN PATTERN: CHAIN OF RESPONSIBILITY (behavioral).
 * In the Servlet / Spring Security world, "filters" form a chain. Each filter inspects the
 * request, optionally modifies it, and calls {@code filterChain.doFilter()} to pass it to the
 * next filter. If a filter decides the request is unauthorized, it can short-circuit the chain.
 *
 * <p>DESIGN PATTERN: TEMPLATE METHOD (behavioral).
 * {@link OncePerRequestFilter} is the abstract base class that handles duplicate-invocation
 * protection and lifecycle management. We only override the ONE step that varies:
 * {@code doFilterInternal()}. The base class defines the "template" of the algorithm
 * (when and how filters run), and we fill in the specific step.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extract the JWT from the "Authorization: Bearer <token>" header
        String token = extractToken(request);

        // 2. If a token exists and is valid, authenticate the request
        if (token != null && jwtTokenProvider.validateToken(token)) {

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserById(userId);

            // Create an Authentication object and set it in the SecurityContext.
            // After this, @PreAuthorize, hasRole(), and SecurityContextHolder.getContext()
            // all see this user as "logged in" for the remainder of this request.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,           // the "principal"
                            null,                  // credentials (not needed, JWT already verified)
                            userDetails.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. Pass the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the token from the Authorization header.
     * @return the raw JWT string, or null if the header is missing/malformed
     */
    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);  // strip "Bearer " prefix
        }
        return null;
    }
}

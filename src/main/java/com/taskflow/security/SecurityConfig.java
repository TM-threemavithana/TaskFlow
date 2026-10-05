package com.taskflow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SPRING SECURITY CONFIGURATION.
 *
 * <p>This is a {@code @Configuration} class: it defines {@code @Bean} methods whose return values
 * become managed objects in the Spring container. Other classes can then inject them.
 *
 * <p>Security flow per request:
 * <pre>
 *   HTTP Request
 *     → JwtAuthenticationFilter (our custom filter: extracts and validates JWT)
 *     → SecurityFilterChain (checks URL permissions below)
 *     → Controller (only if authorized)
 * </pre>
 */
@Configuration
@EnableWebSecurity          // activates Spring Security's filter chain
@EnableMethodSecurity       // enables @PreAuthorize("hasRole('ADMIN')") on methods
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          CustomUserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * The main security configuration. Defines WHICH URLs are public, which require authentication,
     * and which require specific roles.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection disabled: it's for browser-based form submissions with cookies.
            // Our API uses JWT in headers, so CSRF doesn't apply (and would block Postman/Swagger).
            .csrf(AbstractHttpConfigurer::disable)

            // Allow H2 console frames to load (only active when the h2 profile is used)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

            // STATELESS sessions: the server stores NO session. Every request must carry a JWT.
            // This is what makes the API truly RESTful (stateless).
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL-based authorization rules (evaluated top to bottom, first match wins)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints: anyone can register, log in, or browse Swagger
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // Admin-only endpoints
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")

                // Everything else requires authentication (any role)
                .anyRequest().authenticated()
            )

            // Our custom JWT filter before the default UsernamePasswordAuthenticationFilter
            .authenticationProvider(daoAuthenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * PASSWORD ENCODER BEAN.
     * BCrypt is the gold standard for password hashing:
     * - It's intentionally SLOW (cost factor 10 by default ≈ ~100ms per hash), which makes
     *   brute-force attacks extremely expensive.
     * - It generates a random salt per password, so identical passwords produce different hashes.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AUTHENTICATION MANAGER: used by AuthService.login() to verify credentials.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Tells Spring Security: "use our CustomUserDetailsService to load users, and BCrypt to verify passwords."
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}

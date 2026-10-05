package com.taskflow.security;

import com.taskflow.dto.AuthResponse;
import com.taskflow.dto.DtoMapper;
import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.exception.DuplicateResourceException;
import com.taskflow.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service: handles registration and login.
 *
 * <p>DESIGN PATTERN: FACADE (structural).
 * The caller (AuthController) only sees two methods: register() and login().
 * Behind the scenes, this class coordinates multiple subsystems:
 * UserRepository, PasswordEncoder (BCrypt), AuthenticationManager, JwtTokenProvider.
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user.
     * <ol>
     *   <li>Check for duplicate email.</li>
     *   <li>Hash the password with BCrypt (NEVER store plain text!).</li>
     *   <li>Save the user.</li>
     *   <li>Generate a JWT so the user is immediately logged in.</li>
     * </ol>
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }

        // BCrypt automatically generates a random salt and embeds it in the hash.
        // The same password produces a DIFFERENT hash each time (because of the random salt).
        // This protects against rainbow table attacks.
        String hashedPassword = passwordEncoder.encode(request.password());

        User user = new User(request.name(), request.email(), hashedPassword, Role.USER);
        User saved = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());
        return new AuthResponse(token, DtoMapper.toUserResponse(saved));
    }

    /**
     * Log in an existing user.
     * <ol>
     *   <li>AuthenticationManager delegates to CustomUserDetailsService to load the user,
     *       then uses PasswordEncoder to compare the raw password with the stored hash.</li>
     *   <li>If it matches, we get an authenticated Authentication object.</li>
     *   <li>We generate a JWT from it.</li>
     * </ol>
     */
    public AuthResponse login(LoginRequest request) {
        // Spring Security handles password verification internally.
        // If the password is wrong, it throws BadCredentialsException (caught by GlobalExceptionHandler).
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = securityUser.getUser();

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, DtoMapper.toUserResponse(user));
    }
}

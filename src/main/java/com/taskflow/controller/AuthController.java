package com.taskflow.controller;

import com.taskflow.dto.AuthResponse;
import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST CONTROLLER: maps HTTP requests to Java methods.
 *
 * <p>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}.
 * Every return value is automatically serialized to JSON by Jackson.
 *
 * <p>REST API URL design:
 * <ul>
 *   <li>Nouns, not verbs: {@code /api/auth/register} (not {@code /api/registerUser})</li>
 *   <li>HTTP methods convey the action: POST = create, GET = read, PUT = update, DELETE = delete</li>
 *   <li>Status codes convey the result: 201 = created, 200 = ok, 404 = not found, 401 = unauthorized</li>
 * </ul>
 *
 * <p>{@code @Valid}: triggers Bean Validation on the request body. If a field fails
 * (e.g. blank name), Spring throws MethodArgumentNotValidException BEFORE this method is called,
 * and our GlobalExceptionHandler returns a 400 with field-level error messages.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        // 201 CREATED: the standard status code when a new resource is successfully created.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

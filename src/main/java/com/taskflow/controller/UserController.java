package com.taskflow.controller;

import com.taskflow.dto.UserResponse;
import com.taskflow.security.SecurityUser;
import com.taskflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User endpoints.
 *
 * <p>{@code @AuthenticationPrincipal}: Spring injects the currently authenticated user's
 * SecurityUser object. This avoids calling SecurityContextHolder.getContext()... manually.
 *
 * <p>{@code @PreAuthorize("hasRole('ADMIN')")}: method-level security. Only users with
 * ROLE_ADMIN authority can call this method. Others get 403 Forbidden.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Get the currently authenticated user's profile. */
    @GetMapping("/me")
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal SecurityUser principal) {
        return ResponseEntity.ok(userService.getUserById(principal.getUserId()));
    }

    /** Get any user by id. */
    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /** List all users (admin only). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}

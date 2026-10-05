package com.taskflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.AuthResponse;
import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * INTEGRATION TEST.
 *
 * <p>Unlike unit tests, integration tests start the FULL Spring Boot application (in-memory H2)
 * and test the entire request flow: Controller → Service → Repository → Database.
 *
 * <p>KEY ANNOTATIONS:
 * <ul>
 *   <li>{@code @SpringBootTest}: starts the complete Spring context (all beans, JPA, Flyway).</li>
 *   <li>{@code @AutoConfigureMockMvc}: provides MockMvc, which simulates HTTP requests without
 *       actually starting a web server (faster than a real HTTP client).</li>
 *   <li>{@code @ActiveProfiles("test")}: uses application-test.properties (H2 database).</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("API Integration Tests")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  // Jackson: Java ↔ JSON

    private static String jwtToken;     // shared across ordered tests

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register → 201 Created")
    void registerUser() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())                          // HTTP 201
                .andExpect(jsonPath("$.token").isNotEmpty())             // JWT present
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andReturn();

        // Save the token for subsequent tests
        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        jwtToken = response.token();
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register with duplicate email → 409 Conflict")
    void registerDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("Another", "test@example.com", "pass456");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());                        // HTTP 409
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/login → 200 OK with JWT")
    void loginUser() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        jwtToken = response.token();
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/register with invalid data → 400 Bad Request with field errors")
    void registerWithInvalidData() throws Exception {
        RegisterRequest request = new RegisterRequest("", "not-an-email", "12");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/users/me → 200 OK (authenticated)")
    void getCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/users/me without token → 403 Forbidden")
    void getCurrentUserUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/projects → 201 Created")
    void createProject() throws Exception {
        ProjectRequest request = new ProjectRequest("Integration Test Project", "Testing the full flow");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Project"))
                .andExpect(jsonPath("$.ownerId").isNumber());
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/projects → 200 OK with pagination")
    void listProjects() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/projects/999 → 404 Not Found")
    void getProjectNotFound() throws Exception {
        mockMvc.perform(get("/api/projects/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project not found with id : '999'"));
    }
}

package com.taskflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.*;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security & Authorization Boundary Tests")
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userAToken;
    private Long userAId;

    private String userBToken;
    private Long userBId;

    private String adminToken;
    private Long adminId;

    private Long projectAId;
    private Long taskAId;

    @BeforeEach
    void setUp() throws Exception {
        // Register or retrieve User A
        User userA = userRepository.findByEmail("user.a@example.com").orElseGet(() ->
                userRepository.save(new User("User A", "user.a@example.com", passwordEncoder.encode("password123"), Role.USER)));
        userAId = userA.getId();
        userAToken = jwtTokenProvider.generateToken(userAId, userA.getEmail(), userA.getRole().name());

        // Register or retrieve User B
        User userB = userRepository.findByEmail("user.b@example.com").orElseGet(() ->
                userRepository.save(new User("User B", "user.b@example.com", passwordEncoder.encode("password123"), Role.USER)));
        userBId = userB.getId();
        userBToken = jwtTokenProvider.generateToken(userBId, userB.getEmail(), userB.getRole().name());

        // Register or retrieve Admin
        User admin = userRepository.findByEmail("admin.sec@example.com").orElseGet(() ->
                userRepository.save(new User("Admin Sec", "admin.sec@example.com", passwordEncoder.encode("password123"), Role.ADMIN)));
        adminId = admin.getId();
        adminToken = jwtTokenProvider.generateToken(adminId, admin.getEmail(), admin.getRole().name());

        // User A creates Project A
        ProjectRequest projReq = new ProjectRequest("Private Project A", "Confidential to User A");
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projReq)))
                .andExpect(status().isCreated())
                .andReturn();
        ProjectResponse projResp = objectMapper.readValue(projResult.getResponse().getContentAsString(), ProjectResponse.class);
        projectAId = projResp.id();

        // User A creates Task A in Project A
        TaskRequest taskReq = new TaskRequest("Confidential Task", "Task details", projectAId, "TODO", "HIGH", LocalDate.now().plusDays(7), null);
        MvcResult taskResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn();
        TaskResponse taskResp = objectMapper.readValue(taskResult.getResponse().getContentAsString(), TaskResponse.class);
        taskAId = taskResp.id();
    }

    @Test
    @DisplayName("User B cannot access User A's private project (GET /api/projects/{id} -> 403 Forbidden)")
    void userBCannotAccessUserAProject() throws Exception {
        mockMvc.perform(get("/api/projects/" + projectAId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User B cannot read User A's private task (GET /api/tasks/{id} -> 403 Forbidden)")
    void userBCannotReadUserATask() throws Exception {
        mockMvc.perform(get("/api/tasks/" + taskAId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User B cannot enumerate tasks of User A's project (GET /api/tasks?projectId=X -> 403 Forbidden)")
    void userBCannotQueryTasksOfUserAProject() throws Exception {
        mockMvc.perform(get("/api/tasks?projectId=" + projectAId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User B general task listing does not leak User A's tasks")
    void userBListingDoesNotLeakUserATasks() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andReturn();

        PagedResponse<TaskResponse> paged = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructParametricType(PagedResponse.class, TaskResponse.class));

        // Ensure Task A is NOT present in User B's result set
        boolean containsTaskA = paged.content().stream().anyMatch(t -> t.id().equals(taskAId));
        assertFalse(containsTaskA, "User B should not see User A's task in general query listing");
    }

    @Test
    @DisplayName("Assignee can read and update status, but cannot modify other fields or delete task")
    void assigneePermissionsEnforced() throws Exception {
        // 1. User A assigns Task A to User B
        TaskRequest assignReq = new TaskRequest("Confidential Task", "Task details", projectAId, "TODO", "HIGH", LocalDate.now().plusDays(7), userBId);
        mockMvc.perform(put("/api/tasks/" + taskAId)
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk());

        // 2. User B can now read Task A
        mockMvc.perform(get("/api/tasks/" + taskAId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Confidential Task"));

        // 3. User B updates status to IN_PROGRESS -> 200 OK
        TaskRequest statusUpdateReq = new TaskRequest("Confidential Task", "Task details", projectAId, "IN_PROGRESS", "HIGH", LocalDate.now().plusDays(7), userBId);
        mockMvc.perform(put("/api/tasks/" + taskAId)
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 4. User B attempts to change title to 'Hacked' -> 403 Forbidden
        TaskRequest titleHackedReq = new TaskRequest("Hacked Title", "Task details", projectAId, "IN_PROGRESS", "HIGH", LocalDate.now().plusDays(7), userBId);
        mockMvc.perform(put("/api/tasks/" + taskAId)
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(titleHackedReq)))
                .andExpect(status().isForbidden());

        // 5. User B attempts to delete Task A -> 403 Forbidden
        mockMvc.perform(delete("/api/tasks/" + taskAId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin retains full read and delete access across projects and tasks")
    void adminRetainsFullAccess() throws Exception {
        // Admin can read Project A
        mockMvc.perform(get("/api/projects/" + projectAId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Admin can read Task A
        mockMvc.perform(get("/api/tasks/" + taskAId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Admin can delete Task A
        mockMvc.perform(delete("/api/tasks/" + taskAId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}

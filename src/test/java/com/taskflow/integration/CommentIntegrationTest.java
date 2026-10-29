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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Comment Subsystem Integration Tests")
class CommentIntegrationTest {

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

    private String ownerToken;
    private Long ownerId;

    private String strangerToken;
    private Long strangerId;

    private Long taskId;

    @BeforeEach
    void setUp() throws Exception {
        User owner = userRepository.findByEmail("comment.owner@example.com").orElseGet(() ->
                userRepository.save(new User("Comment Owner", "comment.owner@example.com", passwordEncoder.encode("password123"), Role.USER)));
        ownerId = owner.getId();
        ownerToken = jwtTokenProvider.generateToken(ownerId, owner.getEmail(), owner.getRole().name());

        User stranger = userRepository.findByEmail("comment.stranger@example.com").orElseGet(() ->
                userRepository.save(new User("Comment Stranger", "comment.stranger@example.com", passwordEncoder.encode("password123"), Role.USER)));
        strangerId = stranger.getId();
        strangerToken = jwtTokenProvider.generateToken(strangerId, stranger.getEmail(), stranger.getRole().name());

        // Create Project
        ProjectRequest projReq = new ProjectRequest("Comments Test Project", "Project for comments");
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projReq)))
                .andExpect(status().isCreated())
                .andReturn();
        ProjectResponse projResp = objectMapper.readValue(projResult.getResponse().getContentAsString(), ProjectResponse.class);

        // Create Task
        TaskRequest taskReq = new TaskRequest("Task for Comments", "Discuss below", projResp.id(), "TODO", "MEDIUM", LocalDate.now().plusDays(5), null);
        MvcResult taskResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn();
        TaskResponse taskResp = objectMapper.readValue(taskResult.getResponse().getContentAsString(), TaskResponse.class);
        taskId = taskResp.id();
    }

    @Test
    @DisplayName("POST /api/tasks/{taskId}/comments → 201 Created, GET comments in order, DELETE → 204")
    void fullCommentLifecycle() throws Exception {
        // 1. Post first comment
        CommentRequest req1 = new CommentRequest("First comment by owner");
        MvcResult postRes1 = mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("First comment by owner"))
                .andExpect(jsonPath("$.authorName").value("Comment Owner"))
                .andReturn();
        CommentResponse c1 = objectMapper.readValue(postRes1.getResponse().getContentAsString(), CommentResponse.class);

        // 2. Post second comment
        CommentRequest req2 = new CommentRequest("Second comment");
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // 3. GET comments paginated
        MvcResult getRes = mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andReturn();

        // 4. DELETE first comment
        mockMvc.perform(delete("/api/comments/" + c1.id())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        // 5. Verify 1 comment remains
        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("POST comment with blank content → 400 Bad Request")
    void rejectBlankComment() throws Exception {
        CommentRequest blankReq = new CommentRequest("   ");
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.content").exists());
    }

    @Test
    @DisplayName("Stranger cannot post or read comments on private task → 403 Forbidden")
    void strangerDeniedCommentAccess() throws Exception {
        CommentRequest req = new CommentRequest("Unauthorized comment");
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + strangerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Post comment to non-existent task → 404 Not Found")
    void commentOnNonExistentTask() throws Exception {
        CommentRequest req = new CommentRequest("Comment on void");
        mockMvc.perform(post("/api/tasks/99999/comments")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}

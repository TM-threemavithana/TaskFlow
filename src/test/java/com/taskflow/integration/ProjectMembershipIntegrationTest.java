package com.taskflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.CommentRequest;
import com.taskflow.dto.ProjectMemberRequest;
import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.TaskRequest;
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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Project Membership Integration Tests")
class ProjectMembershipIntegrationTest {

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

    private String colleagueToken;
    private Long colleagueId;

    private String strangerToken;
    private Long strangerId;

    private Long projectId;
    private Long taskId;

    @BeforeEach
    void setUp() throws Exception {
        User owner = userRepository.findByEmail("pm.owner@example.com").orElseGet(() ->
                userRepository.save(new User("PM Owner", "pm.owner@example.com", passwordEncoder.encode("password123"), Role.USER)));
        ownerId = owner.getId();
        ownerToken = "Bearer " + jwtTokenProvider.generateToken(ownerId, owner.getEmail(), owner.getRole().name());

        User colleague = userRepository.findByEmail("pm.colleague@example.com").orElseGet(() ->
                userRepository.save(new User("PM Colleague", "pm.colleague@example.com", passwordEncoder.encode("password123"), Role.USER)));
        colleagueId = colleague.getId();
        colleagueToken = "Bearer " + jwtTokenProvider.generateToken(colleagueId, colleague.getEmail(), colleague.getRole().name());

        User stranger = userRepository.findByEmail("pm.stranger@example.com").orElseGet(() ->
                userRepository.save(new User("PM Stranger", "pm.stranger@example.com", passwordEncoder.encode("password123"), Role.USER)));
        strangerId = stranger.getId();
        strangerToken = "Bearer " + jwtTokenProvider.generateToken(strangerId, stranger.getEmail(), stranger.getRole().name());

        // Owner creates project
        ProjectRequest projReq = new ProjectRequest("Collab Project", "Desc for collaboration");
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projReq)))
                .andExpect(status().isCreated())
                .andReturn();
        projectId = objectMapper.readTree(projResult.getResponse().getContentAsString()).get("id").asLong();

        // Owner creates task in project
        TaskRequest taskReq = new TaskRequest("Collab Task", "Task details", projectId, "TODO", "HIGH", LocalDate.now().plusDays(5), null);
        MvcResult taskResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn();
        taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("Complete project membership lifecycle with authorization enforcement")
    void testProjectMembershipLifecycleAndAccessControl() throws Exception {
        // 1. Before being added, colleague cannot read project or task
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", colleagueToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", colleagueToken))
                .andExpect(status().isForbidden());

        // 2. Stranger cannot add members to the project
        ProjectMemberRequest memberReq = new ProjectMemberRequest(colleagueId);
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", strangerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberReq)))
                .andExpect(status().isForbidden());

        // 3. Project owner adds colleague as member
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.userId").value(colleagueId))
                .andExpect(jsonPath("$.userName").value("PM Colleague"));

        // 4. Duplicate addition returns 409 Conflict
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberReq)))
                .andExpect(status().isConflict());

        // 5. Adding owner as member returns 409 Conflict
        ProjectMemberRequest ownerAsMemberReq = new ProjectMemberRequest(ownerId);
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerAsMemberReq)))
                .andExpect(status().isConflict());

        // 6. Member can now read the project
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", colleagueToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Collab Project"));

        // 7. Member can list project members
        mockMvc.perform(get("/api/projects/" + projectId + "/members")
                        .header("Authorization", colleagueToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(colleagueId));

        // 8. Member sees project in GET /api/projects
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", colleagueToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(projectId));

        // 9. Member can read task in project
        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", colleagueToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId));

        // 10. Member can list tasks in project
        mockMvc.perform(get("/api/tasks?projectId=" + projectId)
                        .header("Authorization", colleagueToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(taskId));

        // 11. Member can add comments to the task
        CommentRequest commentReq = new CommentRequest("Member comment on task");
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", colleagueToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Member comment on task"))
                .andExpect(jsonPath("$.authorName").value("PM Colleague"));

        // 12. Colleague (ordinary member) cannot remove themselves or others via member management
        mockMvc.perform(delete("/api/projects/" + projectId + "/members/" + colleagueId)
                        .header("Authorization", colleagueToken))
                .andExpect(status().isForbidden());

        // 13. Owner cannot remove themselves via member endpoint
        mockMvc.perform(delete("/api/projects/" + projectId + "/members/" + ownerId)
                        .header("Authorization", ownerToken))
                .andExpect(status().isForbidden());

        // 14. Owner removes colleague
        mockMvc.perform(delete("/api/projects/" + projectId + "/members/" + colleagueId)
                        .header("Authorization", ownerToken))
                .andExpect(status().isNoContent());

        // 15. After removal, colleague cannot access project or task again
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", colleagueToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", colleagueToken))
                .andExpect(status().isForbidden());
    }
}

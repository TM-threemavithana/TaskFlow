package com.taskflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.CommentRequest;
import com.taskflow.dto.ProjectMemberRequest;
import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.TaskRequest;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Real PostgreSQL integration tests using Testcontainers.
 *
 * <p>Verifies against an isolated PostgreSQL instance:
 * <ul>
 *   <li>PostgreSQL database startup</li>
 *   <li>Full Flyway migrations execution (V1, V2, V3, V4)</li>
 *   <li>JPA entity relationships and foreign key integrity</li>
 *   <li>Repository queries and custom JPQL/Specifications</li>
 *   <li>Comments and Project Membership collaboration subsystems</li>
 *   <li>End-to-end API persistence and authorization behavior</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("PostgreSQL Testcontainers Integration Tests")
class PostgresContainerIntegrationTest {

    private static boolean dockerAvailable;

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("taskflow_tc")
            .withUsername("testuser")
            .withPassword("testpass");

    @BeforeAll
    static void checkDocker() {
        try {
            dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            dockerAvailable = false;
        }
        assumeTrue(dockerAvailable, "Docker daemon is not running. Skipping PostgreSQL Testcontainers integration test.");
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (postgresContainer.isRunning()) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
            registry.add("spring.datasource.username", postgresContainer::getUsername);
            registry.add("spring.datasource.password", postgresContainer::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
            registry.add("spring.flyway.enabled", () -> "true");
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String ownerToken;
    private Long ownerId;

    private String memberToken;
    private Long memberId;

    @BeforeEach
    void setUp() {
        assumeTrue(dockerAvailable, "Docker daemon is not running.");

        User owner = userRepository.findByEmail("tc.owner@example.com").orElseGet(() ->
                userRepository.save(new User("TC Owner", "tc.owner@example.com", passwordEncoder.encode("password123"), Role.USER)));
        ownerId = owner.getId();
        ownerToken = "Bearer " + jwtTokenProvider.generateToken(ownerId, owner.getEmail(), owner.getRole().name());

        User member = userRepository.findByEmail("tc.member@example.com").orElseGet(() ->
                userRepository.save(new User("TC Member", "tc.member@example.com", passwordEncoder.encode("password123"), Role.USER)));
        memberId = member.getId();
        memberToken = "Bearer " + jwtTokenProvider.generateToken(memberId, member.getEmail(), member.getRole().name());
    }

    @Test
    @DisplayName("Verify real PostgreSQL end-to-end entity persistence, foreign keys, comments, and memberships")
    void testRealPostgreSQLEndToEndWorkflow() throws Exception {
        assumeTrue(dockerAvailable, "Docker daemon is not running.");

        // 1. Create Project via API
        ProjectRequest projReq = new ProjectRequest("PostgreSQL Project", "Testing with real Postgres");
        MvcResult projResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long projectId = objectMapper.readTree(projResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Add member via API
        ProjectMemberRequest memberReq = new ProjectMemberRequest(memberId);
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberReq)))
                .andExpect(status().isCreated());

        // Verify membership persisted in PostgreSQL
        assertTrue(projectMemberRepository.existsByProjectIdAndUserId(projectId, memberId));

        // 3. Create Task via API
        TaskRequest taskReq = new TaskRequest("Postgres Task", "Task on PostgreSQL", projectId, "TODO", "HIGH", LocalDate.now().plusDays(10), memberId);
        MvcResult taskResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        // Verify task persisted in PostgreSQL
        assertTrue(taskRepository.findById(taskId).isPresent());

        // 4. Member adds Comment via API
        CommentRequest commentReq = new CommentRequest("Comment stored in PostgreSQL");
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Comment stored in PostgreSQL"));

        // Verify comment persisted in PostgreSQL
        var comments = commentRepository.findByTaskIdOrderByCreatedAtAscIdAsc(taskId);
        assertEquals(1, comments.size());
        assertEquals("Comment stored in PostgreSQL", comments.get(0).getContent());

        // 5. Member reads project and task
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", memberToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", memberToken))
                .andExpect(status().isOk());

        // 6. Verify foreign key / cascade constraint behavior
        // Deleting project should cascade delete tasks, comments, and members in PostgreSQL
        mockMvc.perform(delete("/api/projects/" + projectId)
                        .header("Authorization", ownerToken))
                .andExpect(status().isNoContent());

        assertFalse(projectRepository.findById(projectId).isPresent());
        assertFalse(taskRepository.findById(taskId).isPresent());
        assertTrue(commentRepository.findByTaskIdOrderByCreatedAtAscIdAsc(taskId).isEmpty());
        assertFalse(projectMemberRepository.existsByProjectIdAndUserId(projectId, memberId));
    }
}


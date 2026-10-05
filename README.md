# TaskFlow 📋

A **learning project** that evolves from plain Java classes to a full-stack Spring Boot 3 REST API.
Built to teach OOP, Java fundamentals, design patterns, and enterprise backend development.

---

## 🚀 Quick Start

### Run without PostgreSQL (H2 in-memory database)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
Then open: **http://localhost:8080/swagger-ui.html**

### Run with PostgreSQL (via Docker Compose)
```bash
docker-compose up -d
```

### Run tests
```bash
mvn test
```

### Run the plain Java learning demos (no Spring)
```bash
mvn compile exec:java -Dexec.mainClass="com.taskflow.learning.LearningMain"
```

---

## 📁 Project Structure & Concept Map

Every file is heavily commented to explain the concept it demonstrates.

### Phase 1: Plain Java Classes (`com.taskflow.model`)

| File | Concepts |
|------|----------|
| [`Task.java`](src/main/java/com/taskflow/model/Task.java) | Encapsulation, constructors, default state, behavior methods |
| [`User.java`](src/main/java/com/taskflow/model/User.java) | Encapsulation, getters/setters, toString() override |
| [`Project.java`](src/main/java/com/taskflow/model/Project.java) | Encapsulation, constructor, toString() override |
| [`TaskStatus.java`](src/main/java/com/taskflow/model/TaskStatus.java) | Enums (type-safe constants) |
| [`Priority.java`](src/main/java/com/taskflow/model/Priority.java) | Enums |
| [`Main.java`](src/main/java/com/taskflow/Main.java) | Object instantiation, calling methods, encapsulation in action |

### Phase 2: Java Learning Code (`com.taskflow.learning`)

| File | Concepts |
|------|----------|
| [`LearningMain.java`](src/main/java/com/taskflow/learning/LearningMain.java) | **All 4 OOP pillars** (Encapsulation, Inheritance, Polymorphism, Abstraction), Overloading vs Overriding, Interfaces vs Abstract Classes, Default methods, Composition ("HAS-A"), Custom exceptions, Collections (ArrayList, LinkedList, HashSet, HashMap, TreeMap, Iterator), Generics (type-safe `Result<T>`), Streams (filter, map, collect, reduce, groupingBy), Lambdas & method references, Optional, Design pattern demos |

### Phase 3: Spring Boot REST API

#### Entry Point
| File | Concepts |
|------|----------|
| [`TaskFlowApplication.java`](src/main/java/com/taskflow/TaskFlowApplication.java) | @SpringBootApplication, auto-configuration, component scanning, dependency injection |

#### Entity Layer (`com.taskflow.entity`)
| File | Concepts |
|------|----------|
| [`BaseEntity.java`](src/main/java/com/taskflow/entity/BaseEntity.java) | @MappedSuperclass, inheritance (DRY), JPA lifecycle callbacks (@PrePersist, @PreUpdate), equals/hashCode for entities |
| [`User.java`](src/main/java/com/taskflow/entity/User.java) | @Entity, @Table, BCrypt password hashing, @Enumerated(STRING), JPA no-arg constructor |
| [`Project.java`](src/main/java/com/taskflow/entity/Project.java) | @ManyToOne, @OneToMany, LAZY fetching, cascade/orphanRemoval, **Composition**, bidirectional relationships |
| [`Task.java`](src/main/java/com/taskflow/entity/Task.java) | @Entity, foreign keys, **Builder Pattern** (creational) |
| [`Role.java`](src/main/java/com/taskflow/entity/Role.java) | Enum for security roles |

#### DTO Layer (`com.taskflow.dto`)
| File | Concepts |
|------|----------|
| [`RegisterRequest.java`](src/main/java/com/taskflow/dto/RegisterRequest.java) | Java Records, Bean Validation (@NotBlank, @Email, @Size) |
| [`LoginRequest.java`](src/main/java/com/taskflow/dto/LoginRequest.java) | Records, validation |
| [`AuthResponse.java`](src/main/java/com/taskflow/dto/AuthResponse.java) | Records, compact constructor |
| [`UserResponse.java`](src/main/java/com/taskflow/dto/UserResponse.java) | Records (excludes password - security) |
| [`ProjectRequest.java`](src/main/java/com/taskflow/dto/ProjectRequest.java) | Records, validation |
| [`ProjectResponse.java`](src/main/java/com/taskflow/dto/ProjectResponse.java) | Records, denormalized data |
| [`TaskRequest.java`](src/main/java/com/taskflow/dto/TaskRequest.java) | Records, optional fields |
| [`TaskResponse.java`](src/main/java/com/taskflow/dto/TaskResponse.java) | Records |
| [`PagedResponse.java`](src/main/java/com/taskflow/dto/PagedResponse.java) | **Generics** (`PagedResponse<T>`) |
| [`DtoMapper.java`](src/main/java/com/taskflow/dto/DtoMapper.java) | **Adapter Pattern** (structural): Entity ↔ DTO conversion |

#### Repository Layer (`com.taskflow.repository`)
| File | Concepts |
|------|----------|
| [`UserRepository.java`](src/main/java/com/taskflow/repository/UserRepository.java) | Spring Data JPA, derived queries, **Optional**, interface-based repositories |
| [`ProjectRepository.java`](src/main/java/com/taskflow/repository/ProjectRepository.java) | JPQL custom queries, Pageable |
| [`TaskRepository.java`](src/main/java/com/taskflow/repository/TaskRepository.java) | JpaSpecificationExecutor for dynamic queries |

#### Service Layer (`com.taskflow.service`)
| File | Concepts |
|------|----------|
| [`ProjectService.java`](src/main/java/com/taskflow/service/ProjectService.java) | @Service, @Transactional, layered architecture, ownership checks, pagination |
| [`TaskService.java`](src/main/java/com/taskflow/service/TaskService.java) | Builder Pattern usage, **Observer Pattern** (event publishing), **Strategy Pattern** (specifications) |
| [`UserService.java`](src/main/java/com/taskflow/service/UserService.java) | Streams, method references, Single Responsibility |

#### Specification (`com.taskflow.specification`)
| File | Concepts |
|------|----------|
| [`TaskSpecifications.java`](src/main/java/com/taskflow/specification/TaskSpecifications.java) | **Strategy Pattern** (behavioral): composable JPA Criteria queries for dynamic filtering |

#### Controller Layer (`com.taskflow.controller`)
| File | Concepts |
|------|----------|
| [`AuthController.java`](src/main/java/com/taskflow/controller/AuthController.java) | @RestController, @Valid, HTTP status codes, Swagger annotations |
| [`UserController.java`](src/main/java/com/taskflow/controller/UserController.java) | @AuthenticationPrincipal, @PreAuthorize (method-level security) |
| [`ProjectController.java`](src/main/java/com/taskflow/controller/ProjectController.java) | CRUD endpoints, @PageableDefault, pagination |
| [`TaskController.java`](src/main/java/com/taskflow/controller/TaskController.java) | Filtering, sorting, pagination via query parameters |

#### Exception Handling (`com.taskflow.exception`)
| File | Concepts |
|------|----------|
| [`ResourceNotFoundException.java`](src/main/java/com/taskflow/exception/ResourceNotFoundException.java) | Custom unchecked exceptions |
| [`DuplicateResourceException.java`](src/main/java/com/taskflow/exception/DuplicateResourceException.java) | Custom exceptions |
| [`AccessDeniedException.java`](src/main/java/com/taskflow/exception/AccessDeniedException.java) | Custom exceptions |
| [`GlobalExceptionHandler.java`](src/main/java/com/taskflow/exception/GlobalExceptionHandler.java) | @RestControllerAdvice, **Chain of Responsibility Pattern**, centralized error handling |

#### Security (`com.taskflow.security`)
| File | Concepts |
|------|----------|
| [`SecurityConfig.java`](src/main/java/com/taskflow/security/SecurityConfig.java) | @Configuration, @Bean, CSRF, CORS, stateless sessions, URL security rules, BCrypt |
| [`JwtTokenProvider.java`](src/main/java/com/taskflow/security/JwtTokenProvider.java) | JWT creation/validation, **Facade Pattern** (hides JJWT complexity) |
| [`JwtAuthenticationFilter.java`](src/main/java/com/taskflow/security/JwtAuthenticationFilter.java) | **Chain of Responsibility** (filter chain), **Template Method** (OncePerRequestFilter) |
| [`SecurityUser.java`](src/main/java/com/taskflow/security/SecurityUser.java) | **Adapter Pattern**: User entity → Spring Security's UserDetails |
| [`CustomUserDetailsService.java`](src/main/java/com/taskflow/security/CustomUserDetailsService.java) | UserDetailsService implementation |
| [`AuthService.java`](src/main/java/com/taskflow/security/AuthService.java) | **Facade Pattern**: coordinates registration/login |
| [`JwtProperties.java`](src/main/java/com/taskflow/security/JwtProperties.java) | @ConfigurationProperties (type-safe config binding) |

#### Observer Pattern (`com.taskflow.event`)
| File | Concepts |
|------|----------|
| [`TaskEvent.java`](src/main/java/com/taskflow/event/TaskEvent.java) | **Observer Pattern** event record |
| [`TaskEventListener.java`](src/main/java/com/taskflow/event/TaskEventListener.java) | **Observer Pattern** listener (@EventListener) |

### Phase 4: Standalone Design Pattern Demos (`com.taskflow.pattern`)

| File | Pattern | Type |
|------|---------|------|
| [`SortingStrategyDemo.java`](src/main/java/com/taskflow/pattern/SortingStrategyDemo.java) | **Strategy** | Behavioral |
| [`NotificationFactory.java`](src/main/java/com/taskflow/pattern/NotificationFactory.java) | **Factory Method** | Creational |
| [`TextFormatterDemo.java`](src/main/java/com/taskflow/pattern/TextFormatterDemo.java) | **Decorator** | Structural |
| [`CommandPatternDemo.java`](src/main/java/com/taskflow/pattern/CommandPatternDemo.java) | **Command** (with undo) | Behavioral |
| [`ReportGenerator.java`](src/main/java/com/taskflow/pattern/ReportGenerator.java) | **Template Method** | Behavioral |

### Phase 5: Database (`src/main/resources`)

| File | Concepts |
|------|----------|
| [`application.properties`](src/main/resources/application.properties) | PostgreSQL config, JPA, Flyway, JWT, Swagger |
| [`application-h2.properties`](src/main/resources/application-h2.properties) | Spring Profiles, H2 in-memory database |
| [`V1__create_core_tables.sql`](src/main/resources/db/migration/V1__create_core_tables.sql) | Flyway migration, DDL, foreign keys, constraints |
| [`V2__add_indexes.sql`](src/main/resources/db/migration/V2__add_indexes.sql) | Flyway schema evolution, database indexes |

### Phase 6: Tests (`src/test`)

| File | Concepts |
|------|----------|
| [`ProjectServiceTest.java`](src/test/java/com/taskflow/service/ProjectServiceTest.java) | JUnit 5, Mockito (@Mock, @InjectMocks, when/verify), nested tests |
| [`JwtTokenProviderTest.java`](src/test/java/com/taskflow/security/JwtTokenProviderTest.java) | Unit testing JWT generation/validation |
| [`PatternTests.java`](src/test/java/com/taskflow/pattern/PatternTests.java) | Testing design pattern implementations |
| [`ApiIntegrationTest.java`](src/test/java/com/taskflow/integration/ApiIntegrationTest.java) | @SpringBootTest, MockMvc, full request flow, H2 |

### Phase 7: DevOps

| File | Concepts |
|------|----------|
| [`Dockerfile`](Dockerfile) | Multi-stage build, non-root user, JVM container flags |
| [`docker-compose.yml`](docker-compose.yml) | Container orchestration, health checks, named volumes |

---

## 🎨 Design Patterns Summary (10 Patterns)

| # | Pattern | Type | Where Used |
|---|---------|------|------------|
| 1 | **Strategy** | Behavioral | `TaskSpecifications` (composable query filters), `SortingStrategyDemo` |
| 2 | **Factory Method** | Creational | `NotificationFactory` |
| 3 | **Observer** | Behavioral | `TaskEvent` + `TaskEventListener` (Spring events) |
| 4 | **Builder** | Creational | `Task.Builder` |
| 5 | **Adapter** | Structural | `SecurityUser` (User → UserDetails), `DtoMapper` (Entity → DTO) |
| 6 | **Decorator** | Structural | `TextFormatterDemo` (stackable formatters) |
| 7 | **Facade** | Structural | `JwtTokenProvider`, `AuthService` |
| 8 | **Template Method** | Behavioral | `JwtAuthenticationFilter` (extends OncePerRequestFilter), `ReportGenerator` |
| 9 | **Chain of Responsibility** | Behavioral | `GlobalExceptionHandler`, Servlet filter chain |
| 10 | **Command** | Behavioral | `CommandPatternDemo` (execute/undo) |

---

## 📚 API Endpoints

| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| POST | `/api/auth/register` | ❌ | Register a new user |
| POST | `/api/auth/login` | ❌ | Login and get JWT |
| GET | `/api/users/me` | ✅ | Get current user profile |
| GET | `/api/users/{id}` | ✅ | Get user by ID |
| GET | `/api/users` | 🔑 Admin | List all users |
| POST | `/api/projects` | ✅ | Create project |
| GET | `/api/projects` | ✅ | List my projects (paginated) |
| GET | `/api/projects/{id}` | ✅ | Get project by ID |
| PUT | `/api/projects/{id}` | ✅ Owner/Admin | Update project |
| DELETE | `/api/projects/{id}` | ✅ Owner/Admin | Delete project |
| POST | `/api/tasks` | ✅ | Create task |
| GET | `/api/tasks` | ✅ | List tasks (filtered/sorted/paginated) |
| GET | `/api/tasks/{id}` | ✅ | Get task by ID |
| PUT | `/api/tasks/{id}` | ✅ Owner/Assignee/Admin | Update task |
| DELETE | `/api/tasks/{id}` | ✅ Owner/Assignee/Admin | Delete task |

### Task Filtering Parameters
```
GET /api/tasks?projectId=1&status=TODO&priority=HIGH&assigneeId=2&search=login&page=0&size=10&sort=dueDate,asc
```

---

## 🛠 Tech Stack

- **Java 17** (LTS)
- **Spring Boot 3.3** (Web, Data JPA, Security, Validation)
- **PostgreSQL 16** (production) / **H2** (development/testing)
- **Flyway** (database migrations)
- **Spring Security + JWT** (stateless authentication)
- **BCrypt** (password hashing)
- **SpringDoc OpenAPI** (Swagger UI)
- **JUnit 5 + Mockito** (testing)
- **Docker + Docker Compose** (containerization)

package com.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ENTRY POINT OF THE SPRING BOOT APPLICATION.
 *
 * <p>{@code @SpringBootApplication} is a shortcut for three annotations:
 * <ul>
 *   <li>{@code @Configuration} - this class may declare {@code @Bean} methods.</li>
 *   <li>{@code @EnableAutoConfiguration} - Spring Boot looks at the jars on the classpath
 *       (web, JPA, security, H2/PostgreSQL ...) and configures them with sensible defaults.</li>
 *   <li>{@code @ComponentScan} - Spring scans THIS package ({@code com.taskflow}) and all sub-packages
 *       for classes annotated with {@code @Component}, {@code @Service}, {@code @Repository},
 *       {@code @RestController}, {@code @Configuration}... and creates ONE object ("bean") of each.</li>
 * </ul>
 *
 * <p>DEPENDENCY INJECTION (DI) / INVERSION OF CONTROL (IoC): instead of our classes calling
 * {@code new TaskService(new TaskRepository(...))} themselves, they simply declare what they need in
 * their constructor, and the Spring "container" builds and wires every object for us.
 * This makes classes loosely coupled and easy to test (we can pass mocks in tests).
 *
 * <p>Run it:
 * <pre>
 *   mvn spring-boot:run -Dspring-boot.run.profiles=h2     (no database installation needed)
 *   mvn spring-boot:run                                    (expects PostgreSQL on localhost:5432)
 * </pre>
 * Then open http://localhost:8080/swagger-ui.html
 *
 * <p>Note: {@link Main} (Phase 1) and {@code com.taskflow.learning.LearningMain} are separate,
 * plain-Java programs. They still work and can be run on their own from the IDE.
 */
@SpringBootApplication
public class TaskFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskFlowApplication.class, args);
    }
}

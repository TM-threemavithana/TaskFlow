package com.taskflow.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * SWAGGER / OPENAPI configuration.
 *
 * <p>Once the app is running, visit http://localhost:8080/swagger-ui.html to see auto-generated
 * API documentation with a "Try it out" button for each endpoint.
 *
 * <p>{@code @SecurityScheme}: tells Swagger that endpoints require a "Bearer" JWT token.
 * When you click "Authorize" in the Swagger UI and paste your token, it automatically adds
 * the {@code Authorization: Bearer <token>} header to every request.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "TaskFlow API",
        version = "1.0",
        description = "A task management REST API - built for learning Spring Boot 3, JPA, JWT security, and design patterns."
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // No methods needed: the annotations above do all the work.
}

package me.amjath.expense_tracker_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 configuration.
 * Adds JWT Bearer auth scheme to all secured endpoints.
 * Accessible at: /swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Expense Tracker API",
                version = "v1.0",
                description = "Production-grade REST API for tracking personal income, expenses, budgets, and financial reports.",
                contact = @Contact(name = "Amjath", email = "hey.amjath@gmail.com"),
                license = @License(name = "MIT")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide a valid JWT access token in the Authorization header: Bearer <token>"
)
public class OpenApiConfig {
    // Bean configuration handled via annotations above
}

package io.techyowls.openapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * OpenAPI configuration with annotations.
 *
 * Swagger UI: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 * OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Product API",
        version = "1.0.0",
        description = "REST API for product management",
        contact = @Contact(
            name = "TechyOwls",
            url = "https://techyowls.io",
            email = "contact@techyowls.io"
        ),
        license = @License(
            name = "MIT",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local development"),
        @Server(url = "https://api.techyowls.io", description = "Production")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT token authentication"
)
public class OpenApiConfig {

    /**
     * Programmatic configuration for reusable components.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addResponses("BadRequest", createBadRequestResponse())
                .addResponses("NotFound", createNotFoundResponse())
                .addResponses("Unauthorized", createUnauthorizedResponse())
            );
    }

    private ApiResponse createBadRequestResponse() {
        return new ApiResponse()
            .description("Invalid request parameters")
            .content(new Content().addMediaType("application/problem+json",
                new MediaType().examples(Map.of(
                    "validation-error", new Example()
                        .summary("Validation error")
                        .value("""
                            {
                              "type": "about:blank",
                              "title": "Bad Request",
                              "status": 400,
                              "detail": "Validation failed",
                              "instance": "/api/products"
                            }
                            """)
                ))));
    }

    private ApiResponse createNotFoundResponse() {
        return new ApiResponse()
            .description("Resource not found")
            .content(new Content().addMediaType("application/problem+json",
                new MediaType().example("""
                    {
                      "type": "about:blank",
                      "title": "Not Found",
                      "status": 404,
                      "detail": "Product not found: 999",
                      "instance": "/api/products/999"
                    }
                    """)));
    }

    private ApiResponse createUnauthorizedResponse() {
        return new ApiResponse()
            .description("Authentication required")
            .content(new Content().addMediaType("application/problem+json",
                new MediaType().example("""
                    {
                      "type": "about:blank",
                      "title": "Unauthorized",
                      "status": 401,
                      "detail": "Invalid or missing authentication token"
                    }
                    """)));
    }
}

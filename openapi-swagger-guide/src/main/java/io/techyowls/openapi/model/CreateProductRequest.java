package io.techyowls.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating a product.
 * Separate from Product entity to control what clients can set.
 */
@Schema(description = "Request body for creating a new product")
public record CreateProductRequest(

    @Schema(description = "Product name", example = "Wireless Mouse", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be 100 characters or less")
    String name,

    @Schema(description = "Product description", example = "Ergonomic wireless mouse")
    @Size(max = 1000)
    String description,

    @Schema(description = "Price in USD", example = "29.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    BigDecimal price,

    @Schema(description = "Category", example = "Electronics", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category is required")
    String category,

    @Schema(description = "Initial stock", example = "100", defaultValue = "0")
    @Min(value = 0, message = "Stock cannot be negative")
    int stockQuantity

) {}

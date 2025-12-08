package io.techyowls.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Product model with OpenAPI annotations.
 *
 * These annotations generate accurate documentation AND examples.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product information")
public class Product {

    @Schema(
        description = "Unique product identifier",
        example = "12345",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
        description = "Product name",
        example = "Wireless Mouse",
        minLength = 1,
        maxLength = 100
    )
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be 100 characters or less")
    private String name;

    @Schema(
        description = "Product description",
        example = "Ergonomic wireless mouse with 6 buttons",
        maxLength = 1000
    )
    @Size(max = 1000, message = "Description must be 1000 characters or less")
    private String description;

    @Schema(
        description = "Product price in USD",
        example = "29.99",
        minimum = "0.01"
    )
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    @Schema(
        description = "Product category",
        example = "Electronics",
        allowableValues = {"Electronics", "Clothing", "Books", "Home", "Sports"}
    )
    @NotBlank(message = "Category is required")
    private String category;

    @Schema(
        description = "Available stock quantity",
        example = "150",
        minimum = "0"
    )
    @Min(value = 0, message = "Stock cannot be negative")
    private int stockQuantity;

    @Schema(
        description = "Whether the product is active",
        example = "true",
        defaultValue = "true"
    )
    @Builder.Default
    private boolean active = true;

    @Schema(
        description = "Product creation timestamp",
        example = "2024-01-15T10:30:00Z",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Instant createdAt;

    @Schema(
        description = "Last update timestamp",
        example = "2024-01-20T14:45:00Z",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Instant updatedAt;
}

package io.techyowls.openapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.techyowls.openapi.exception.ProductNotFoundException;
import io.techyowls.openapi.model.CreateProductRequest;
import io.techyowls.openapi.model.PageResponse;
import io.techyowls.openapi.model.Product;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Product API with comprehensive OpenAPI documentation.
 *
 * Demonstrates:
 * - @Operation for endpoint description
 * - @ApiResponses for response documentation
 * - @Parameter for query/path parameter docs
 * - @SecurityRequirement for protected endpoints
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management operations")
public class ProductController {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Operation(
        summary = "List all products",
        description = "Retrieves a paginated list of products with optional filtering"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully"
        )
    })
    @GetMapping
    public PageResponse<Product> listProducts(
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size,

        @Parameter(description = "Filter by category")
        @RequestParam(required = false) String category,

        @Parameter(description = "Minimum price filter", example = "10.00")
        @RequestParam(required = false) BigDecimal minPrice,

        @Parameter(description = "Maximum price filter", example = "100.00")
        @RequestParam(required = false) BigDecimal maxPrice
    ) {
        List<Product> filtered = products.values().stream()
            .filter(p -> category == null || p.getCategory().equals(category))
            .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
            .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
            .toList();

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        List<Product> pageContent = start < filtered.size()
            ? filtered.subList(start, end)
            : List.of();

        return PageResponse.of(pageContent, page, size, filtered.size());
    }

    @Operation(
        summary = "Get product by ID",
        description = "Retrieves a single product by its unique identifier"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(schema = @Schema(implementation = Product.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(mediaType = "application/problem+json")
        )
    })
    @GetMapping("/{id}")
    public Product getProduct(
        @Parameter(description = "Product ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        Product product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    @Operation(
        summary = "Create a new product",
        description = "Creates a new product and returns the created resource"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body"
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Product to create",
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = CreateProductRequest.class),
            examples = {
                @ExampleObject(
                    name = "Electronics",
                    summary = "Electronics product",
                    value = """
                        {
                          "name": "Wireless Mouse",
                          "description": "Ergonomic wireless mouse with 6 buttons",
                          "price": 29.99,
                          "category": "Electronics",
                          "stockQuantity": 100
                        }
                        """
                ),
                @ExampleObject(
                    name = "Book",
                    summary = "Book product",
                    value = """
                        {
                          "name": "Clean Code",
                          "description": "A Handbook of Agile Software Craftsmanship",
                          "price": 45.00,
                          "category": "Books",
                          "stockQuantity": 50
                        }
                        """
                )
            }
        )
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = Product.builder()
            .id(idGenerator.getAndIncrement())
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .category(request.category())
            .stockQuantity(request.stockQuantity())
            .active(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        products.put(product.getId(), product);
        return product;
    }

    @Operation(
        summary = "Update a product",
        description = "Updates an existing product. Requires authentication.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PutMapping("/{id}")
    public Product updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody CreateProductRequest request
    ) {
        Product existing = products.get(id);
        if (existing == null) {
            throw new ProductNotFoundException(id);
        }

        Product updated = Product.builder()
            .id(id)
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .category(request.category())
            .stockQuantity(request.stockQuantity())
            .active(existing.isActive())
            .createdAt(existing.getCreatedAt())
            .updatedAt(Instant.now())
            .build();

        products.put(id, updated);
        return updated;
    }

    @Operation(
        summary = "Delete a product",
        description = "Deletes a product permanently. Requires authentication.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        if (!products.containsKey(id)) {
            throw new ProductNotFoundException(id);
        }
        products.remove(id);
    }

    @Operation(
        summary = "Update product stock",
        description = "Updates the stock quantity for a product"
    )
    @PatchMapping("/{id}/stock")
    public Product updateStock(
        @PathVariable Long id,
        @Parameter(description = "Stock adjustment (positive to add, negative to subtract)")
        @RequestParam int adjustment
    ) {
        Product product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }

        int newStock = product.getStockQuantity() + adjustment;
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        product.setStockQuantity(newStock);
        product.setUpdatedAt(Instant.now());
        return product;
    }
}

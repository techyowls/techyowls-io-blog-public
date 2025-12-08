package io.techyowls.webflux.controller;

import io.techyowls.webflux.model.Product;
import io.techyowls.webflux.service.ProductService;
import io.techyowls.webflux.service.ProductService.ProductWithStock;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Annotated controller approach (familiar to Spring MVC developers).
 * See RouterConfig for functional endpoint alternative.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> create(@Valid @RequestBody CreateProductRequest request) {
        Product product = Product.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .category(request.category())
            .stockQuantity(request.stockQuantity())
            .build();
        return productService.createProduct(product);
    }

    @GetMapping("/{id}")
    public Mono<Product> getById(@PathVariable String id) {
        return productService.findById(id);
    }

    @GetMapping
    public Flux<Product> getAll() {
        return productService.findAll();
    }

    @PutMapping("/{id}")
    public Mono<Product> update(@PathVariable String id,
                                 @Valid @RequestBody CreateProductRequest request) {
        Product updates = Product.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .category(request.category())
            .stockQuantity(request.stockQuantity())
            .build();
        return productService.updateProduct(id, updates);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return productService.deleteProduct(id);
    }

    /**
     * Server-Sent Events endpoint.
     * Client receives products as a stream in real-time.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> streamProducts() {
        return productService.streamProductUpdates();
    }

    /**
     * Combines multiple async operations.
     */
    @GetMapping("/{id}/with-stock")
    public Mono<ProductWithStock> getWithStock(@PathVariable String id) {
        return productService.getProductWithStockStatus(id);
    }

    // ========== DTOs ==========

    public record CreateProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @Positive BigDecimal price,
        String category,
        @Positive int stockQuantity
    ) {}
}

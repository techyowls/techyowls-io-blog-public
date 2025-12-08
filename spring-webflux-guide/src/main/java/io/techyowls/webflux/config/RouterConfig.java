package io.techyowls.webflux.config;

import io.techyowls.webflux.model.Product;
import io.techyowls.webflux.service.ProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

/**
 * Functional endpoints - alternative to @RestController.
 *
 * Advantages:
 * - More testable (pure functions)
 * - Explicit routing composition
 * - Better for complex routing logic
 *
 * Available at /fn/products/* (separate from annotated controllers)
 */
@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductService productService) {
        return route()
            .path("/fn/products", builder -> builder
                .GET("", request ->
                    ok().contentType(MediaType.APPLICATION_JSON)
                        .body(productService.findAll(), Product.class))

                .GET("/{id}", request -> {
                    String id = request.pathVariable("id");
                    return productService.findById(id)
                        .flatMap(product -> ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(product))
                        .switchIfEmpty(notFound().build());
                })

                .POST("", request ->
                    request.bodyToMono(Product.class)
                        .flatMap(productService::createProduct)
                        .flatMap(created -> created(java.net.URI.create("/fn/products/" + created.getId()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(created)))

                // SSE streaming
                .GET("/stream", accept(MediaType.TEXT_EVENT_STREAM),
                    request -> ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(productService.streamProductUpdates(), Product.class))
            )
            .build();
    }
}

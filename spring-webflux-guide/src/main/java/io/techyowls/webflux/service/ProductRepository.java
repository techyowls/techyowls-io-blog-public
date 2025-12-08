package io.techyowls.webflux.service;

import io.techyowls.webflux.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {

    Flux<Product> findByCategory(String category);

    Flux<Product> findByPriceLessThan(BigDecimal maxPrice);

    Flux<Product> findByNameContainingIgnoreCase(String name);
}

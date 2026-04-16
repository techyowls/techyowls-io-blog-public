package io.techyowls.testcontainers.repository;

import io.techyowls.testcontainers.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // PostgreSQL-specific JSONB query - would fail with H2!
    @Query(value = """
        SELECT * FROM products
        WHERE metadata->>'color' = :color
        """, nativeQuery = true)
    List<Product> findByColor(@Param("color") String color);

    // Query JSONB array contains
    @Query(value = """
        SELECT * FROM products
        WHERE metadata->'tags' ? :tag
        """, nativeQuery = true)
    List<Product> findByTag(@Param("tag") String tag);
}

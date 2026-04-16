package io.techyowls.auditing.repository;

import io.techyowls.auditing.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

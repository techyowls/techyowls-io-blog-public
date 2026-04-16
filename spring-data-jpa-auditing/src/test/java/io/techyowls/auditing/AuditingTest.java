package io.techyowls.auditing;

import io.techyowls.auditing.model.Product;
import io.techyowls.auditing.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(io.techyowls.auditing.config.JpaConfig.class)
class AuditingTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @MockitoBean
    private AuditorAware<String> auditorAware;

    @Test
    void shouldSetCreatedAtOnInsert() {
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));

        Instant before = Instant.now();

        Product product = new Product("Widget", new BigDecimal("99.99"));
        productRepository.save(product);
        entityManager.flush();

        Instant after = Instant.now();

        assertThat(product.getCreatedAt())
            .isAfterOrEqualTo(before)
            .isBeforeOrEqualTo(after);
        assertThat(product.getCreatedBy()).isEqualTo("test-user");
    }

    @Test
    void shouldUpdateLastModifiedOnUpdate() throws InterruptedException {
        when(auditorAware.getCurrentAuditor())
            .thenReturn(Optional.of("creator"))
            .thenReturn(Optional.of("modifier"));

        Product product = productRepository.save(new Product("Widget", new BigDecimal("99.99")));
        entityManager.flush();
        entityManager.clear();

        Instant createdAt = product.getCreatedAt();

        // Wait to ensure different timestamp
        Thread.sleep(10);

        Product loaded = productRepository.findById(product.getId()).orElseThrow();
        loaded.setPrice(new BigDecimal("149.99"));
        productRepository.save(loaded);
        entityManager.flush();

        assertThat(loaded.getCreatedAt()).isEqualTo(createdAt);  // Unchanged
        assertThat(loaded.getUpdatedAt()).isAfter(createdAt);
        assertThat(loaded.getCreatedBy()).isEqualTo("creator");  // Original
        assertThat(loaded.getUpdatedBy()).isEqualTo("modifier"); // New
    }
}

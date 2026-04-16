package io.techyowls.testcontainers;

import io.techyowls.testcontainers.model.User;
import io.techyowls.testcontainers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test using Testcontainers with real PostgreSQL.
 * These tests catch issues that H2 would miss.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    @ServiceConnection  // Auto-configures datasource from container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindUserByCaseInsensitiveSearch() {
        // Given - names with different cases
        userRepository.saveAll(List.of(
            new User("John Doe", "john@example.com"),
            new User("Jane Johnson", "jane@example.com"),
            new User("Bob Smith", "bob@example.com")
        ));
        entityManager.flush();
        entityManager.clear();

        // When - search with lowercase
        List<User> results = userRepository.findByNameContainingIgnoreCase("john");

        // Then - should find both John and Johnson
        assertThat(results)
            .hasSize(2)
            .extracting(User::getName)
            .containsExactlyInAnyOrder("John Doe", "Jane Johnson");
    }

    @Test
    void shouldFindUsersByEmailDomain() {
        // Given
        userRepository.saveAll(List.of(
            new User("Alice", "alice@company.com"),
            new User("Bob", "bob@company.com"),
            new User("Charlie", "charlie@gmail.com")
        ));
        entityManager.flush();
        entityManager.clear();

        // When
        List<User> companyUsers = userRepository.findByEmailDomain("company.com");

        // Then
        assertThat(companyUsers)
            .hasSize(2)
            .extracting(User::getName)
            .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    void shouldHandleUnicodeCharacters() {
        // Given - PostgreSQL handles Unicode properly
        User user = new User("José García", "jose@example.com");
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // When
        User found = userRepository.findByEmail("jose@example.com").orElseThrow();

        // Then
        assertThat(found.getName()).isEqualTo("José García");
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {
        // Given
        userRepository.save(new User("First User", "duplicate@example.com"));
        entityManager.flush();

        // When/Then - PostgreSQL enforces unique constraint
        User duplicate = new User("Second User", "duplicate@example.com");

        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> {
                userRepository.save(duplicate);
                entityManager.flush();
            }
        );
    }
}

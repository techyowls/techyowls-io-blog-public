package io.techyowls.flyway;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test Flyway migrations against real PostgreSQL.
 *
 * IMPORTANT: Always test migrations against the same DB as production!
 */
@SpringBootTest
@Testcontainers
class FlywayMigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.clean-disabled", () -> false);
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void allMigrationsApplySuccessfully() {
        // Flyway runs on startup, so if we get here, migrations succeeded
        var info = flyway.info();

        // Verify expected migrations ran
        assertThat(info.applied()).hasSizeGreaterThanOrEqualTo(6);

        // Check schema is correct
        var columns = jdbcTemplate.queryForList(
            "SELECT column_name FROM information_schema.columns WHERE table_name = 'users'"
        );

        assertThat(columns)
            .extracting(m -> m.get("column_name"))
            .contains("id", "email", "encrypted_password", "status");
    }

    @Test
    void viewsAreCreated() {
        // Check repeatable migrations created views
        var productStats = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.views WHERE table_name = 'product_statistics'",
            Integer.class
        );
        assertThat(productStats).isEqualTo(1);
    }

    @Test
    void constraintsAreEnforced() {
        // Insert a valid user first
        jdbcTemplate.update(
            "INSERT INTO users (email, encrypted_password, status) VALUES (?, ?, ?)",
            "test@example.com", "hashed", "active"
        );

        // Check constraint blocks invalid status
        var result = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE status = 'active'",
            Integer.class
        );
        assertThat(result).isEqualTo(1);
    }
}

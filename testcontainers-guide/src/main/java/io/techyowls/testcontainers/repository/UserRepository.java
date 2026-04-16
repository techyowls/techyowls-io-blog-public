package io.techyowls.testcontainers.repository;

import io.techyowls.testcontainers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // Case-insensitive search - works differently in H2 vs PostgreSQL!
    List<User> findByNameContainingIgnoreCase(String name);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :domain))")
    List<User> findByEmailDomain(@Param("domain") String domain);
}

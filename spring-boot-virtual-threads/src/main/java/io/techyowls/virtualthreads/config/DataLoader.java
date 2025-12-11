package io.techyowls.virtualthreads.config;

import io.techyowls.virtualthreads.model.Customer;
import io.techyowls.virtualthreads.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Loads sample data on application startup.
 */
@Configuration
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner initDatabase(CustomerRepository customerRepository) {
        return args -> {
            log.info("Loading sample customers...");

            Customer c1 = new Customer();
            c1.setName("John Doe");
            c1.setEmail("john@example.com");
            c1.setPhone("+1-555-0101");
            customerRepository.save(c1);

            Customer c2 = new Customer();
            c2.setName("Jane Smith");
            c2.setEmail("jane@example.com");
            c2.setPhone("+1-555-0102");
            customerRepository.save(c2);

            Customer c3 = new Customer();
            c3.setName("Bob Wilson");
            c3.setEmail("bob@example.com");
            c3.setPhone("+1-555-0103");
            customerRepository.save(c3);

            log.info("Sample data loaded: {} customers", customerRepository.count());
        };
    }
}

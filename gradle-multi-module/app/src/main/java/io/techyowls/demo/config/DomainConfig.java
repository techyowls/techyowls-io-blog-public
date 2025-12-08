package io.techyowls.demo.config;

import io.techyowls.demo.domain.repository.OrderRepository;
import io.techyowls.demo.domain.service.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wire domain services with Spring.
 *
 * The domain module doesn't know about Spring.
 * This config class creates Spring beans from domain classes.
 */
@Configuration
public class DomainConfig {

    @Bean
    public OrderService orderService(OrderRepository orderRepository) {
        return new OrderService(orderRepository);
    }
}

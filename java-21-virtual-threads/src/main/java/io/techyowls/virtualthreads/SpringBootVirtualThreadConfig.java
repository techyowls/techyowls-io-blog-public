package io.techyowls.virtualthreads;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

/**
 * Spring Boot 3.2+ configuration for virtual threads.
 *
 * Alternative: Just add to application.yaml:
 * spring:
 *   threads:
 *     virtual:
 *       enabled: true
 */
// @Configuration  // Uncomment when using Spring Boot
public class SpringBootVirtualThreadConfig {

    /**
     * Configure @Async methods to use virtual threads.
     */
    // @Bean
    AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(
            Executors.newVirtualThreadPerTaskExecutor()
        );
    }

    /**
     * Configure Tomcat to use virtual threads for HTTP requests.
     */
    // @Bean
    TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(
                Executors.newVirtualThreadPerTaskExecutor()
            );
        };
    }
}

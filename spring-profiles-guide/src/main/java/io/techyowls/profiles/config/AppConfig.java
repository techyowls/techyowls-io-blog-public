package io.techyowls.profiles.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe configuration with @ConfigurationProperties.
 * Values come from application.yml based on active profile.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfig {

    private String environment;
    private String apiUrl;
    private int connectionTimeout;
    private int readTimeout;
    private boolean debugMode;

    private Cache cache = new Cache();
    private Email email = new Email();

    @Data
    public static class Cache {
        private boolean enabled;
        private int ttlMinutes;
    }

    @Data
    public static class Email {
        private String from;
        private boolean sendEnabled;
    }
}

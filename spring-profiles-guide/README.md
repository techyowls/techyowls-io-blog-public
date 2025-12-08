# Spring Profiles & Configuration

Code samples for: [Spring Profiles: Environment-Specific Configuration Done Right](https://techyowls.io/blog/spring-boot-profiles-configuration-guide)

## Configuration Hierarchy

```
Precedence (highest to lowest):
┌─────────────────────────────────────────────────────┐
│ 1. Command line args (--app.debug-mode=true)        │
│ 2. Environment variables (APP_DEBUG_MODE=true)      │
│ 3. application-{profile}.yml                        │
│ 4. application.yml                                  │
│ 5. @ConfigurationProperties defaults                │
└─────────────────────────────────────────────────────┘
```

## Project Structure

```
src/main/
├── java/io/techyowls/profiles/
│   ├── config/
│   │   └── AppConfig.java           # @ConfigurationProperties
│   └── service/
│       ├── NotificationService.java
│       ├── EmailNotificationService.java  # @Profile("prod")
│       └── MockNotificationService.java   # @Profile({"dev", "test"})
└── resources/
    ├── application.yml              # Base config
    ├── application-dev.yml          # Dev overrides
    ├── application-prod.yml         # Prod overrides
    └── application-test.yml         # Test overrides
```

## Run with Different Profiles

```bash
# Development (default)
./mvnw spring-boot:run

# With specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Via environment variable
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run

# Multiple profiles
SPRING_PROFILES_ACTIVE=prod,metrics ./mvnw spring-boot:run
```

## Key Patterns

### Type-Safe Configuration

```java
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String environment;
    private boolean debugMode;
    private Cache cache = new Cache();
    // Getters/setters...
}
```

### Profile-Specific Beans

```java
@Service
@Profile("prod")
public class EmailNotificationService implements NotificationService { }

@Service
@Profile({"dev", "test"})
public class MockNotificationService implements NotificationService { }
```

### Environment Variables

```yaml
app:
  api-url: ${API_URL:http://localhost:8080}  # With default
  secret: ${APP_SECRET}                       # Required (fails if missing)
```

## 12-Factor App Compliance

| Factor | Implementation |
|--------|---------------|
| III. Config | Environment variables for secrets |
| X. Dev/Prod Parity | Same code, different configs |
| XI. Logs | Profile-specific log levels |

## License

MIT

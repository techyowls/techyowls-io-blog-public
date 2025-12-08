rootProject.name = "gradle-multi-module-demo"

// Version Catalog (Gradle 8.x)
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Spring Boot
            version("spring-boot", "3.2.0")
            plugin("spring-boot", "org.springframework.boot").versionRef("spring-boot")
            plugin("spring-dependency-management", "io.spring.dependency-management").version("1.1.4")

            // Libraries
            library("spring-boot-starter-web", "org.springframework.boot", "spring-boot-starter-web").versionRef("spring-boot")
            library("spring-boot-starter-data-jpa", "org.springframework.boot", "spring-boot-starter-data-jpa").versionRef("spring-boot")
            library("spring-boot-starter-validation", "org.springframework.boot", "spring-boot-starter-validation").versionRef("spring-boot")
            library("spring-boot-starter-test", "org.springframework.boot", "spring-boot-starter-test").versionRef("spring-boot")

            // Database
            library("h2", "com.h2database", "h2").version("2.2.224")
            library("postgresql", "org.postgresql", "postgresql").version("42.7.1")

            // Utilities
            library("lombok", "org.projectlombok", "lombok").version("1.18.30")
        }
    }
}

// Modules following Clean Architecture
include(
    "domain",        // Core business logic - NO dependencies on other modules
    "infrastructure", // Database, external APIs - depends on domain
    "app"            // Web layer, configuration - depends on all
)

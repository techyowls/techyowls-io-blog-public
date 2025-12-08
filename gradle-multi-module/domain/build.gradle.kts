/**
 * Domain Module - The Heart of Your Application
 *
 * This module contains:
 * - Entities (pure business objects)
 * - Repository interfaces (ports)
 * - Domain services (business logic)
 * - Value objects
 *
 * CRITICAL: This module has ZERO framework dependencies.
 * It should compile and run without Spring, Hibernate, or any infrastructure.
 *
 * The Dependency Rule:
 *   app -> infrastructure -> domain
 *         (Nothing points outward)
 */
plugins {
    `java-library`
}

dependencies {
    // Only standard Java + utilities
    // NO Spring, NO JPA annotations, NO framework dependencies

    // Optional: Add lombok for boilerplate reduction
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

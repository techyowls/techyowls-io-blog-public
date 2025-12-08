/**
 * Infrastructure Module - Database & External Systems
 *
 * This module contains:
 * - JPA entities (mapped from domain objects)
 * - Repository implementations (adapters)
 * - External API clients
 * - Messaging adapters
 *
 * The infrastructure module:
 * - DEPENDS ON domain (implements its ports)
 * - KNOWS ABOUT Spring, JPA, external libraries
 * - Converts between domain objects and infrastructure objects
 */
plugins {
    `java-library`
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Domain module - we implement its interfaces
    api(project(":domain"))

    // Spring Data JPA
    implementation(libs.spring.boot.starter.data.jpa)

    // Database drivers
    runtimeOnly(libs.h2)
    runtimeOnly(libs.postgresql)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
}

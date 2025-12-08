/**
 * App Module - Web Layer & Configuration
 *
 * This module contains:
 * - REST controllers
 * - Spring Boot configuration
 * - Main application class
 * - DTOs for API
 *
 * The app module:
 * - DEPENDS ON both domain and infrastructure
 * - Wires everything together
 * - Handles HTTP concerns
 */
plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Internal modules
    implementation(project(":domain"))
    implementation(project(":infrastructure"))

    // Web
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
}

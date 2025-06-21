// farm/database/build.gradle.kts

plugins {
    kotlin("jvm") // Applied explicitly
    alias(libs.plugins.kotlin.plugin.serialization) // Reference Kotlinx Serialization plugin from TOML
}

group = "com.farm"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21) // Use Java 21 for JVM compilation, consistent with Dockerfile
}

dependencies {
    // Exposed Framework - Updated to 1.0.0-beta-2
    // You should also update your libs.versions.toml file accordingly:
    // [versions]
    // exposed = "1.0.0-beta-2"
    implementation(libs.exposed.core) // Reference from TOML
    implementation(libs.exposed.dao) // Reference from TOML
    implementation(libs.exposed.jdbc) // Reference from TOML
    implementation(libs.exposed.kotlin.datetime) // Reference from TOML

    // Database Driver (MySQL)
    implementation(libs.mysql.connector.java) // Reference from TOML

    // Connection Pool (HikariCP recommended)
    implementation(libs.hikari.cp) // Reference from TOML

    // Kotlinx Coroutines for asynchronous database access
    implementation(libs.kotlinx.coroutines.core) // Reference from TOML

    // Project Dependency
    implementation(project(":common")) // For accessing common data models

    // Logging
    implementation(libs.slf4j.simple) // Reference from TOML

    // Testing
    testImplementation(libs.kotlin.test.junit) // Reference from TOML
    testImplementation(libs.h2.database) // Reference from TOML
}
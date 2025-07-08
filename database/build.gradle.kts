// farm/database/build.gradle.kts

plugins {
    kotlin("jvm") // Applied explicitly
    alias(libs.plugins.kotlin.plugin.serialization) // Reference Kotlinx Serialization plugin from TOML
}

group = "com.farm"
version = "0.0.1"

kotlin {
    jvmToolchain(21) // Use Java 21 for JVM compilation, consistent with Dockerfile
}

dependencies {
    // Exposed Framework - Updated to 1.0.0-beta-2
    // You should also update your libs.versions.toml file accordingly:
    // [versions]
    // exposed = "1.0.0-beta-2"
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    // implementation(libs.exposed.r2dbc)
    implementation(libs.exposed.kotlin.datetime)

    // Database Driver (MySQL)
    implementation(libs.mysql.connector.java)

    // Connection Pool (HikariCP recommended)
    implementation(libs.hikari.cp)

    // Kotlinx Coroutines for asynchronous database access
    implementation(libs.kotlinx.coroutines.core)

    // Project Dependency
    implementation(project(":common")) // For accessing common data models

    // Logging
    implementation(libs.slf4j.simple)

    // Testing
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.h2.database)
}

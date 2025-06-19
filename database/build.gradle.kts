// farm/database/build.gradle.kts

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0-RC3" // For data classes if serialized
}

group = "com.farm"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21) // Use Java 21 for JVM compilation
}

dependencies {
    // Exposed Framework
    implementation("org.jetbrains.exposed:exposed-core:0.49.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.49.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.49.0") // For JDBC connections
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.49.0") // If using Kotlinx DateTime

    // Database Driver (MySQL)
    implementation("mysql:mysql-connector-java:8.0.33") // MySQL Connector

    // Connection Pool (HikariCP recommended)
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Kotlinx Coroutines for asynchronous database access
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Project Dependency
    implementation(project(":common")) // For accessing common data models

    // Logging
    implementation("org.slf4j:slf4j-simple:1.7.36") // For database connection logging

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.23")
    testImplementation("com.h2database:h2:2.2.224") // H2 for in-memory database testing
}

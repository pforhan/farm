// farm/common/build.gradle.kts

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
    // Kotlinx Serialization runtime
    implementation(libs.kotlinx.serialization.json) // Reference from TOML
}
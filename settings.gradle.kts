// farm/settings.gradle.kts

rootProject.name = "farm"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    // Apply the Kotlin JVM plugin to the root project to enable Kotlin DSL in settings.gradle.kts
    id("org.jetbrains.kotlin.jvm") version "1.9.23" apply false
    // Apply the Compose Multiplatform plugin to the root project
    id("org.jetbrains.compose") version "1.6.10" apply false
}

// Include the individual modules for backend, database, common, and frontend
include(
    "backend",
    "common",
    "database",
    "frontend"
)

// Define project directories for each module
project(":backend").projectDir = file("backend")
project(":common").projectDir = file("common")
project(":database").projectDir = file("database")
project(":frontend").projectDir = file("frontend")
// farm/settings.gradle.kts

rootProject.name = "farm"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap") // Keep Ktor EAP here for plugin resolution
    }
    // Use version catalogs for plugin versions where appropriate (e.g., in build.gradle.kts files)
    // For pluginManagement in settings.gradle.kts, direct version strings or resolution from repositories.
    plugins {
        id("org.jetbrains.kotlin.jvm") version "1.9.23" // Direct version, matching libs.versions.toml
        id("org.jetbrains.compose") version "1.6.10" // Direct version, matching libs.versions.toml
    }
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
// farm/build.gradle.kts (Root project build file)

plugins {
    // These are applied to all subprojects
    kotlin("jvm") version "1.9.23" apply false // Apply false here, each module applies it explicitly
    id("org.jetbrains.compose") version "1.6.10" apply false // Apply false here, frontend applies explicitly
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        // Add any other repositories needed by subprojects
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

// Configuration common to subprojects, or tasks for the root project.
// No specific tasks defined here for now, subprojects will define their own.
tasks.wrapper {
    gradleVersion = "8.6" // Recommended Gradle version
}

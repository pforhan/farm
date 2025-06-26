// farm/build.gradle.kts (Root project build file)

plugins {
    // These are applied to all subprojects
    alias(libs.plugins.kotlin.jvm) apply false // Apply false here, each module applies it explicitly
    alias(libs.plugins.jetbrains.compose) apply false // Apply false here, frontend applies explicitly
    alias(libs.plugins.compose.compiler) apply false // Apply false here, frontend applies explicitly
}

// Ensure all subprojects have access to necessary repositories
allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

// Configuration common to subprojects, or tasks for the root project.
// No specific tasks defined here for now, subprojects will define their own.
tasks.wrapper {
    gradleVersion = "8.6" // Recommended Gradle version
}

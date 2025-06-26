// farm/backend/build.gradle.kts

plugins {
    kotlin("jvm") // Applied explicitly
    alias(libs.plugins.ktor) // Reference Ktor plugin from TOML
    alias(libs.plugins.kotlin.plugin.serialization) // Reference Kotlinx Serialization plugin from TOML
}

group = "com.farm"
version = "0.0.1"

application {
    mainClass.set("com.farm.ApplicationKt") // Ktor application entry point
}

kotlin {
    jvmToolchain(21) // Use Java 21 for JVM compilation, consistent with Dockerfile
}

dependencies {
    // Project Dependencies
    implementation(project(":common")) // Backend depends on common for shared models
    implementation(project(":database")) // Backend depends on database for data access

    // Ktor Core
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Ktor Features (example, add as needed)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.sessions) // If user sessions are needed (TODO for auth)
    implementation(libs.ktor.server.auth) // For user authentication (TODO for auth)
    implementation(libs.ktor.server.call.logging) // For request logging

    // Ktor Static Content (to serve frontend files)
    implementation(libs.ktor.server.host.common)

    // Image Processing for thumbnails
    // Java AWT ImageIO is usually available with JVM, but explicitly mentioning its use
    // No direct external dependency typically, it's part of Java standard library
    // If a more robust image library is needed, consider:
    // implementation("com.sksamuel.scrimage:scrimage-core:4.0.0") // Example, would add more dependencies

    // Logging (Slf4j simple implementation)
    implementation(libs.slf4j.simple)

    // For file system operations
    implementation(libs.kotlinx.coroutines.core)

    // Testing
    testImplementation(libs.ktor.server.tests.jvm)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockito.kotlin)
}

// Task to create necessary directories for uploads and previews on build
val createDirs by tasks.creating(Copy::class) {
    doLast {
        // Define paths relative to the project root for Docker volumes
        val uploadDir = project.rootDir.resolve("public/uploads")
        val previewDir = project.rootDir.resolve("public/previews")
        val logsDir = project.rootDir.resolve("var/logs") // Assuming var/logs for backend logs
        val cacheDir = project.rootDir.resolve("var/cache") // Assuming var/cache

        println("Ensuring directories exist: $uploadDir, $previewDir, $logsDir, $cacheDir")
        uploadDir.mkdirs()
        previewDir.mkdirs()
        logsDir.mkdirs()
        cacheDir.mkdirs()

        // Set permissions (Docker handles this more robustly, but good for local setup)
        uploadDir.setReadable(true, false)
        uploadDir.setWritable(true, false)
        uploadDir.setExecutable(true, false)

        previewDir.setReadable(true, false)
        previewDir.setWritable(true, false)
        previewDir.setExecutable(true, false)

        logsDir.setReadable(true, false)
        logsDir.setWritable(true, false)
        logsDir.setExecutable(true, false)

        cacheDir.setReadable(true, false)
        cacheDir.setWritable(true, false)
        cacheDir.setExecutable(true, false)
    }
}
tasks.named("processResources") {
    dependsOn(createDirs)
}

// farm/backend/build.gradle.kts

plugins {
    kotlin("jvm")
    id("io.ktor.jvm") version "2.3.9" // Ktor plugin
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" // Kotlinx Serialization plugin
}

group = "com.farm"
version = "0.0.1"

application {
    mainClass.set("com.farm.ApplicationKt") // Ktor application entry point
}

repositories {
    mavenCentral()
}

dependencies {
    // Project Dependencies
    implementation(project(":common")) // Backend depends on common for shared models
    implementation(project(":database")) // Backend depends on database for data access

    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm:2.3.9")
    implementation("io.ktor:ktor-server-netty:2.3.9") // For Netty server engine
    implementation("io.ktor:ktor-server-content-negotiation:2.3.9") // For JSON handling
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.9") // For kotlinx.serialization JSON

    // Ktor Features (example, add as needed)
    implementation("io.ktor:ktor-server-status-pages:2.3.9") // For error handling
    implementation("io.ktor:ktor-server-sessions:2.3.9") // If user sessions are needed (TODO for auth)
    implementation("io.ktor:ktor-server-auth:2.3.9") // For user authentication (TODO for auth)
    implementation("io.ktor:ktor-server-call-logging:2.3.9") // For request logging

    // Ktor Static Content (to serve frontend files)
    implementation("io.ktor:ktor-server-host-common:2.3.9")
    implementation("io.ktor:ktor-server-http-netty:2.3.9")

    // Image Processing for thumbnails
    // Java AWT ImageIO is usually available with JVM, but explicitly mentioning its use
    // No direct external dependency typically, it's part of Java standard library
    // If a more robust image library is needed, consider:
    // implementation("com.sksamuel.scrimage:scrimage-core:4.0.0") // Example, would add more dependencies

    // Logging (Slf4j simple implementation)
    implementation("org.slf4j:slf4j-simple:1.7.36")

    // For file system operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.9")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.23")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1") // For mocking in tests
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
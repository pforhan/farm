// farm/frontend/build.gradle.kts

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0-RC3" // For kotlinx.serialization
}

group = "com.farm"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm() // For desktop (if needed later)
    iosX64() // For iOS simulator (if needed later)
    iosArm64() // For iOS device (if needed later)
    iosSimulatorArm64() // For iOS simulator (if needed later)

    js(IR) {
        browser()
        binaries.executable()
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.Experimental"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material) // Or compose.material3
                implementation(compose.ui)
                implementation(compose.components.resources)

                // Kotlinx Serialization runtime
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

                // Ktor Client for API calls
                implementation("io.ktor:ktor-client-core:2.3.9")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.9")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.9")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                // Project Dependencies
                implementation(project(":common")) // Frontend depends on common for shared models
            }
        }
        val jvmMain by getting {
            dependencies {
                // For desktop app
                implementation(compose.desktop.currentOs)
            }
        }
        val jsMain by getting {
            dependencies {
                // For web app
                implementation(compose.html.core)
                implementation("io.ktor:ktor-client-js:2.3.9") // Ktor client engine for JS
            }
        }
        val iosMain by getting {
            dependencies {
                // For iOS app
                implementation("io.ktor:ktor-client-darwin:2.3.9") // Ktor client engine for iOS
            }
        }
    }
}

compose.experimental {
    web.target = org.jetbrains.compose.web.targets.WebTarget.BROWSER // For Compose Web
    // If you want to enable WebAssembly (Wasm) target for Compose Multiplatform Web:
    // web.target = org.jetbrains.compose.web.targets.WebTarget.WASM
    // This requires Kotlin 1.9.20+ and still experimental.
}

// Configure JS assets output path for Ktor to serve
tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsProcessResources") {
    outputFileName = "main.js" // Renames the output JS bundle
    outputDir = file("${project.rootDir}/backend/src/main/resources/static/js") // Output to backend resources
}

tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserDevelopmentWebpack") {
    outputFileName = "main.js"
    outputDir = file("${project.rootDir}/backend/src/main/resources/static/js")
}

tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack") {
    outputFileName = "main.js"
    outputDir = file("${project.rootDir}/backend/src/main/resources/static/js")
}

// Copy index.html and styles.css to backend resources as well
tasks.named<Copy>("jsProcessResources") {
    val frontendResourcesDir = project.rootDir.resolve("backend/src/main/resources/static")
    from(sourceSets.jsMain.get().resources) {
        include("index.html")
        include("styles.css")
    }
    into(frontendResourcesDir)
}

// Task to assemble the frontend for the backend to serve
val assembleFrontend by tasks.registering(Copy::class) {
    dependsOn(tasks.named("jsBrowserProductionWebpack")) // Ensure JS bundle is built
    val frontendBuildDir = project.buildDir.resolve("distributions")
    val backendStaticDir = project(":backend").projectDir.resolve("src/main/resources/static")

    from(frontendBuildDir) {
        include("*.js") // The main JS bundle
    }
    from(sourceSets.jsMain.get().resources) {
        include("*.html") // index.html
        include("*.css") // styles.css
    }
    into(backendStaticDir)
}
tasks.named("jar", type = Jar::class) {
    dependsOn(assembleFrontend) // Ensure frontend is assembled before backend JAR
}

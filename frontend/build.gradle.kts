// farm/frontend/build.gradle.kts

plugins {
    kotlin("multiplatform") // Applied explicitly
    alias(libs.plugins.jetbrains.compose) // Reference Compose plugin from TOML
    alias(libs.plugins.compose.compiler) // Reference Compose plugin from TOML
    alias(libs.plugins.kotlin.plugin.serialization) // Reference Kotlinx Serialization plugin from TOML
}

group = "com.farm"
version = "0.0.1"

kotlin {
    jvm() // For desktop (if needed later)
    // Removed iOS targets entirely from here, user wants them commented out.
    // iosX64() // For iOS simulator (if needed later)
    // iosArm64() // For iOS device (if needed later)
    // iosSimulatorArm64() // For iOS simulator (if needed later)

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
                // Compose Multiplatform dependencies
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)

                // Kotlinx Serialization runtime
                implementation(libs.kotlinx.serialization.json) // Reference from TOML

                // Ktor Client for API calls
                implementation(libs.ktor.client.core) // Reference from TOML
                implementation(libs.ktor.client.content.negotiation) // Reference from TOML
                implementation(libs.ktor.serialization.kotlinx.json) // Reference from TOML

                // Coroutines
                implementation(libs.kotlinx.coroutines.core) // Reference from TOML

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
                implementation(libs.ktor.client.js) // Reference from TOML
            }
        }
//        val iosMain by getting { // This block was commented out by user, keeping it that way.
//            dependencies {
//                // For iOS app
//                implementation(libs.ktor.client.darwin) // Reference from TOML
//            }
//        }
    }
}

compose.experimental {
    // Removed: web.target = org.jetbrains.compose.web.targets.WebTarget.BROWSER (handled by js(IR) { browser() })
    // If you want to enable WebAssembly (Wasm) target for Compose Multiplatform Web:
    // web.target = org.jetbrains.compose.web.targets.WASM
    // This requires Kotlin 1.9.20+ and still experimental.
}

// Task to copy frontend web assets to the backend's static resources directory
val copyWebAssetsToBackend by tasks.registering(Copy::class) {
    // Ensure the production webpack bundle is built
    dependsOn(tasks.named("jsBrowserProductionWebpack"))

    // Define the source directory for compiled JS and resource files
    val jsDistributionDir = layout.buildDirectory.dir("distributions/frontend").get().asFile
    val jsMainResourcesDir = kotlin.sourceSets.getByName("jsMain").resources.sourceDirectories.singleFile

    // Define the destination directory in the backend's static resources
    val backendStaticDir = project(":backend").projectDir.resolve("src/main/resources/static")

    // Copy JS files (main.js, skiko.js) from the distribution directory
    from(jsDistributionDir) {
        include("*.js") // This should include main.js and skiko.js
    }
    into(backendStaticDir.resolve("js")) // Copy JS files into a 'js' subdirectory

    // Copy HTML and CSS files from jsMain resources
    from(jsMainResourcesDir) {
        include("index.html")
        include("styles.css")
    }
    into(backendStaticDir) // Copy HTML and CSS directly into the static root
}

// Removed the explicit dependency of backend's jar task on copyWebAssetsToBackend.
// The Dockerfile build process orchestrates these tasks, ensuring correct order.

// Optional: To also configure development webpack output for local testing outside Docker
/*
tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserDevelopmentWebpack") {
    // You can set an output directory here if you want it to differ from default
    // For example:
    // destinationDirectory.set(project.buildDir.resolve("development-frontend-dist"))
}
*/

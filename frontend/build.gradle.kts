// farm/frontend/build.gradle.kts

plugins {
    kotlin("multiplatform") // Applied explicitly
    alias(libs.plugins.jetbrains.compose) // Reference Compose plugin from TOML
    alias(libs.plugins.kotlin.plugin.serialization) // Reference Kotlinx Serialization plugin from TOML
}

group = "com.farm"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // Keep specific compose repo
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
        val iosMain by getting {
            dependencies {
                // For iOS app
                implementation(libs.ktor.client.darwin) // Reference from TOML
            }
        }
    }
}

compose.experimental {
    // Removed: web.target = org.jetbrains.compose.web.targets.WebTarget.BROWSER (handled by js(IR) { browser() })
    // If you want to enable WebAssembly (Wasm) target for Compose Multiplatform Web:
    // web.target = org.jetbrains.compose.web.targets.WebTarget.WASM
    // This requires Kotlin 1.9.20+ and still experimental.
}

// Configure JS assets output path for Ktor to serve
tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsProcessResources") {
    outputFileName = "main.js" // Renames the output JS bundle
    webpackTask {
        output.path = layout.projectDirectory.dir("backend/src/main/resources/static/js").get().asFile.absolutePath
    }
}

tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserDevelopmentWebpack") {
    outputFileName = "main.js"
    webpackTask {
        output.path = layout.projectDirectory.dir("backend/src/main/resources/static/js").get().asFile.absolutePath
    }
}

tasks.named<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack") {
    outputFileName = "main.js"
    webpackTask {
        output.path = layout.projectDirectory.dir("backend/src/main/resources/static/js").get().asFile.absolutePath
    }
}

// Copy index.html and styles.css to backend resources as well
tasks.named<Copy>("jsProcessResources") {
    val frontendResourcesDir = project.rootDir.resolve("backend/src/main/resources/static")
    from(kotlin.sourceSets.getByName("jsMain").resources) { // Corrected access to jsMain
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
    from(kotlin.sourceSets.getByName("jsMain").resources) { // Corrected access to jsMain
        include("*.html") // index.html
        include("*.css") // styles.css
    }
    into(backendStaticDir)
}
tasks.named("jar", type = Jar::class) {
    dependsOn(assembleFrontend) // Ensure frontend is assembled before backend JAR
}
// farm/common/build.gradle.kts

plugins {
    kotlin("multiplatform") // Changed to multiplatform
    alias(libs.plugins.kotlin.plugin.serialization) // Reference Kotlinx Serialization plugin from TOML
}

group = "com.farm"
version = "0.0.1"

kotlin {
    jvm() // Define JVM target for backend
    js() { // Define JS target for frontend
        browser()
    }
    // If you need more targets (e.g., iOS), add them here:
    // iosX64()
    // iosArm64()
    // iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val jvmMain by getting {
            // JVM-specific dependencies for common module, if any
            // For now, it might be empty as models are generally platform-agnostic
        }
        val jsMain by getting {
            // JS-specific dependencies for common module, if any
        }
    }
}

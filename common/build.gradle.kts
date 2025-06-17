// farm/common/build.gradle.kts

plugins {
    kotlin("jvm") // Using JVM for common module as it's primarily shared with JVM backend
    id("org.jetbrains.kotlin.plugin.serialization") // For kotlinx.serialization
}

group = "com.farm"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17) // Use Java 17 for JVM compilation
}

dependencies {
    // Kotlinx Serialization runtime
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Multiplatform compatibility if this was truly shared with non-JVM platforms
    // For now, it's JVM-only as frontend is also Kotlin/JS for web.
    // If Android/iOS apps were built from here, would change to:
    // val commonMain by getting {
    //     dependencies {
    //         implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    //     }
    // }
    // val jvmMain by getting {
    //     dependencies { }
    // }
    // val jsMain by getting {
    //     dependencies { }
    // }
}
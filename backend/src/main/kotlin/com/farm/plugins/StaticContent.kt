// farm/backend/src/main/kotlin/com/farm/plugins/StaticContent.kt
package com.farm.plugins

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.routing

fun Application.configureStaticContent() {
    routing {
        // Serve static files from the 'static' directory within resources
        // This is where Compose Multiplatform will output its web assets (JS, HTML, CSS)
        staticResources("/", "static") {
            default("index.html") // Serve index.html for root requests
        }
    }
}

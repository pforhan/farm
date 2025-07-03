// farm/backend/src/main/kotlin/com/farm/plugins/StaticContent.kt
package com.farm.plugins

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

class StaticContent

fun Application.configureStaticContent() {
    routing {
        // Serve static files from the 'static' directory within resources
        // This is where Compose Multiplatform will output its web assets (JS, HTML, CSS)
        staticResources("/", "static") {
            default("index.html") // Serve index.html for root requests
        }

        // Catch-all for undefined routes, redirect to index.html for React Router
        // This ensures client-side routing works even for direct URL access
        get("{...}") {
            // Correctly serve index.html from resources
            val indexStream = StaticContent::class.java.getResourceAsStream("/static/index.html")!!
            call.respondOutputStream(contentType = ContentType.Text.Html) {
                indexStream.copyTo(this)
            }
        }
    }
}

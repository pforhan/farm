// farm/backend/src/main/kotlin/com/farm/plugins/StatusPages.kt
package com.farm.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception: ${cause.message}", cause)
            call.respondText(text = "500: ${cause.message ?: "Internal Server Error"}", status = HttpStatusCode.InternalServerError)
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText(text = "${status.value}: ${status.description}", status = status)
        }
        status(HttpStatusCode.BadRequest) { call, status ->
            call.respondText(text = "${status.value}: ${status.description}", status = status)
        }
    }
}
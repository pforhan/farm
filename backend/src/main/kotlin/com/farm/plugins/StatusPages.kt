// farm/backend/src/main/kotlin/com/farm/plugins/StatusPages.kt
package com.farm.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception: ${cause.message}", cause)
            cause.printStackTrace(System.out)
            val stacktrace = cause.stackTraceToString()
            call.respondText(text = "500: ${cause.message ?: "Internal Server Error"}" +
              "\n\n$stacktrace", status = HttpStatusCode.InternalServerError)
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText(text = "${status.value}: ${status.description}", status = status)
        }
        status(HttpStatusCode.BadRequest) { call, status ->
            call.respondText(text = "${status.value}: ${status.description}", status = status)
        }
    }
}

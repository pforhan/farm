// farm/backend/src/main/kotlin/com/farm/Application.kt
package com.farm

import com.farm.database.DatabaseFactory
import com.farm.database.Dao
import com.farm.routes.assetRoutes
import com.farm.plugins.configureSerialization
import com.farm.plugins.configureStatusPages
import com.farm.plugins.configureStaticContent
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.* // Required for session management (future auth)
import io.ktor.server.auth.* // Required for authentication (future auth)
import io.ktor.server.plugins.callloging.* // For request logging
import io.ktor.server.plugins.compression.* // For response compression
import org.slf4j.event.Level
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Configure Serialization (JSON)
    configureSerialization()

    // Configure Status Pages (Error Handling)
    configureStatusPages()

    // Configure Call Logging
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    // Configure Compression for responses
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumLength(1024) // Compress only responses larger than 1KB
        }
    }

    // Initialize Database
    DatabaseFactory.init()

    // Configure Static Content serving for the frontend
    configureStaticContent()

    routing {
        get("/api/health") {
            call.respondText("Backend is healthy!")
        }

        // Apply asset routes
        assetRoutes(Dao)

        // TODO: Implement user authentication and authorization here
        // install(Sessions) {
        //     cookie<UserSession>("user_session") {
        //         cookie.path = "/"
        //         cookie.maxAgeInSeconds = 60 * 60 * 24 // 24 hours
        //     }
        // }
        // install(Authentication) {
        //     form("auth-form") {
        //         userParamName = "username"
        //         passwordParamName = "password"
        //         validate { credentials ->
        //             // TODO: Replace with actual user validation from database
        //             if (credentials.name == "admin" && credentials.password == "password") {
        //                 UserIdPrincipal(credentials.name)
        //             } else {
        //                 null
        //             }
        //         }
        //         challenge("/login") // Redirect to login page on failed authentication
        //     }
        // }
        // install(Authentication) {
        //     session<UserSession>("auth-session") {
        //         // TODO: Configure session-based authentication for authenticated routes
        //         validate { session ->
        //             if (session.name.isNotEmpty()) session else null
        //         }
        //         challenge { call.respondRedirect("/login") }
        //     }
        // }

        // Route for handling file downloads
        static("/uploads") {
            staticRootFolder = File(System.getProperty("user.dir"), "public/uploads")
            files(".")
        }

        static("/previews") {
            staticRootFolder = File(System.getProperty("user.dir"), "public/previews")
            files(".")
        }
    }
}

// Data class for user session (TODO: for authentication)
// data class UserSession(val name: String) : Principal
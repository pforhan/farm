// farm/backend/src/main/kotlin/com/farm/routes/AssetRoutes.kt
package com.farm.routes

import com.farm.common.UpdateAssetRequest
import com.farm.database.Dao
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.File
import java.nio.file.Paths
import java.util.UUID

// Constants for file storage
val UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "public", "uploads").toFile()
val PREVIEW_DIR = Paths.get(System.getProperty("user.dir"), "public", "previews").toFile()
const val MAX_FILE_SIZE = 20 * 1024 * 1024 // 20MB
val ALLOWED_EXTENSIONS = setOf("zip", "png", "jpg", "jpeg", "gif", "wav", "mp3", "ogg", "txt", "md", "html", "json", "xml")

fun Route.assetRoutes(dao: Dao) {
    // Ensure upload and preview directories exist
    UPLOAD_DIR.mkdirs()
    PREVIEW_DIR.mkdirs()

    // API routes
    route("/api/assets") {
        // Get all assets
        get {
            val assets = dao.allAssets()
            call.respond(assets)
        }

        // Get asset by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing or malformed asset ID")
                return@get
            }
            val asset = dao.asset(id)
            if (asset != null) {
                call.respond(asset)
            } else {
                call.respond(HttpStatusCode.NotFound, "Asset with ID $id not found")
            }
        }

        // Search assets
        get("/search") {
            val query = call.request.queryParameters["query"]
            if (query.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Missing search query parameter")
                return@get
            }
            val assets = dao.searchAssets(query)
            call.respond(assets)
        }

        // Upload new asset
        post("/upload") {
            val multipart = call.receiveMultipart()
            var assetName: String? = null
            var link: String? = null
            var storeName: String? = null
            var authorName: String? = null
            var licenseName: String? = null
            var tagsString: String? = null
            var projectsString: String? = null
            var tempUploadedFile: File? = null // To hold the temporarily streamed file
            var originalFileName: String? = null
            var fileContentType: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "asset_name" -> assetName = part.value
                            "link" -> link = part.value
                            "store_name" -> storeName = part.value
                            "author_name" -> authorName = part.value
                            "license_name" -> licenseName = part.value
                            "tags" -> tagsString = part.value
                            "projects" -> projectsString = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        originalFileName = part.originalFileName as String
                        fileContentType = part.contentType?.toString()

                        // Create a temporary file to stream the upload directly to disk
                        // Use a unique name to avoid conflicts, and keep original extension
                        val tempFileName = "${UUID.randomUUID()}-${originalFileName}"
                        tempUploadedFile = File("public/uploads/temp/$tempFileName") // Store in a temp sub-directory
                        tempUploadedFile.parentFile.mkdirs() // Ensure temp directory exists

                        part.provider().toInputStream().use { input ->
                            tempUploadedFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        println("File streamed to temporary path: ${tempUploadedFile.absolutePath}")
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (assetName == null || tempUploadedFile == null || originalFileName == null || fileContentType == null) {
                tempUploadedFile?.delete() // Clean up temp file if upload is incomplete
                call.respond(HttpStatusCode.BadRequest, "Missing required fields: asset_name, file")
                return@post
            }

            val newAsset = dao.addNewAsset(
              assetName = assetName,
              link = link,
              storeName = storeName,
              authorName = authorName,
              licenseName = licenseName,
              tagsString = tagsString,
              projectsString = projectsString
            )

            if (newAsset != null) {
                val assetDirectory = File("public/uploads/${newAsset.assetId}")
                assetDirectory.mkdirs() // Create directory for the asset's files

                val finalFilePath = "${assetDirectory.path}/$originalFileName"
                val finalFile = File(finalFilePath)

                // Move the temporary file to its final destination
                tempUploadedFile.copyTo(finalFile, overwrite = true)
                tempUploadedFile.delete() // Delete the temporary file
                println("Temporary file moved to final path: ${finalFile.absolutePath}")

                // Determine public path for the file
                val publicPath = "/uploads/${newAsset.assetId}/$originalFileName"

                // Generate preview if it's an image
                val previewPath: String? = if (fileContentType!!.startsWith("image/")) {
                    val previewDir = File("public/previews/${newAsset.assetId}")
                    previewDir.mkdirs()
                    val previewFileName = "thumbnail_${finalFile.nameWithoutExtension}.png"
                    val previewFilePath = "${previewDir.path}/$previewFileName"
                    // In a real application, you'd use an image processing library here
                    // For now, we'll just use the original image as its own preview
                    // or generate a placeholder.
                    finalFile.copyTo(File(previewFilePath), overwrite = true)
                    "/previews/${newAsset.assetId}/$previewFileName"
                } else {
                    null
                }

                dao.addFile(
                  assetId = newAsset.assetId,
                  fileName = originalFileName,
                  filePath = finalFilePath, // Use the final path
                  fileSize = finalFile.length(), // Get actual file size from the final file
                  fileType = fileContentType!!,
                  previewPath = previewPath
                )
                call.respond(HttpStatusCode.Created, "Asset uploaded successfully! Asset ID: ${newAsset.assetId}")
            } else {
                tempUploadedFile.delete() // Clean up temp file if asset creation failed
                call.respond(HttpStatusCode.InternalServerError, "Failed to create asset.")
            }
        }

        // Update asset
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing or malformed asset ID")
                return@put
            }
            val updateRequest = call.receive<UpdateAssetRequest>()

            val success = dao.editAsset(
              id = id,
              assetName = updateRequest.assetName,
              link = updateRequest.link,
              storeName = updateRequest.storeName,
              authorName = updateRequest.authorName,
              licenseName = updateRequest.licenseName,
              tagsString = updateRequest.tagsString,
              projectsString = updateRequest.projectsString
            )
            if (success) {
                call.respond(HttpStatusCode.OK, "Asset ID $id updated successfully.")
            } else {
                call.respond(HttpStatusCode.NotFound, "Asset with ID $id not found or failed to update.")
            }
        }

        // Delete asset
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing or malformed asset ID")
                return@delete
            }
            val success = dao.deleteAsset(id)
            if (success) {
                call.respond(HttpStatusCode.OK, "Asset ID $id deleted successfully.")
            } else {
                call.respond(HttpStatusCode.NotFound, "Asset with ID $id not found or failed to delete.")
            }
        }
    }
}

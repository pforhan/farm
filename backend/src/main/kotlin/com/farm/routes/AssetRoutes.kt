// farm/backend/src/main/kotlin/com/farm/routes/AssetRoutes.kt
package com.farm.routes

import com.farm.common.UpdateAssetRequest
import com.farm.database.Dao
import com.farm.util.generateFilenameTags
import com.farm.util.generateThumbnail
import com.farm.util.processZipFile
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Locale

// Constants for file storage
val UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "public", "uploads").toFile()
val PREVIEW_DIR = Paths.get(System.getProperty("user.dir"), "public", "previews").toFile()
const val MAX_FILE_SIZE = 20 * 1024 * 1024 // 20MB
val ALLOWED_EXTENSIONS = setOf("zip", "png", "jpg", "jpeg", "gif", "wav", "mp3", "ogg", "txt", "md", "html", "json", "xml")

fun Route.assetRoutes(dao: Dao) {
    // Ensure upload and preview directories exist
    UPLOAD_DIR.mkdirs()
    PREVIEW_DIR.mkdirs()

    route("/api/assets") {
        // GET all assets
        get {
            val assets = dao.getAllAssets()
            call.respond(assets)
        }

        // GET asset details by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid asset ID")
                return@get
            }
            val assetDetails = dao.getAssetDetails(id)
            if (assetDetails == null) {
                call.respond(HttpStatusCode.NotFound, "Asset not found")
            } else {
                call.respond(assetDetails)
            }
        }

        // POST new asset (upload file and metadata)
        post("/upload") {
            // TODO: This single-stage upload needs to be refactored into a two-stage process
            // 1. Pre-upload file(s) and get temporary ID + detected metadata
            // 2. User reviews/edits metadata, then finalizes upload with temporary ID
            val multipart = call.receiveMultipart()
            var assetName: String? = null
            var link: String? = null
            var storeName: String? = null
            var authorName: String? = null
            var licenseName: String? = null
            var tagsString: String? = null
            var projectsString: String? = null
            var fileItem: PartData.FileItem? = null

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
                        fileItem = part
                    }
                    else -> {} // Ignore other part types
                }
                part.dispose()
            }

            if (assetName == null || fileItem == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing asset name or file.")
                return@post
            }

            val fileBytes = fileItem!!.streamProvider().readBytes()
            val originalFileName = fileItem!!.originalFileName ?: "unknown_file"
            val fileExtension = originalFileName.substringAfterLast('.', "")
              .lowercase(Locale.getDefault())
            val fileSize = fileBytes.size

            if (fileSize > MAX_FILE_SIZE) {
                call.respond(HttpStatusCode.PayloadTooLarge, "File too large (max ${MAX_FILE_SIZE / (1024 * 1024)}MB)")
                return@post
            }
            if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
                call.respond(HttpStatusCode.BadRequest, "Invalid file type: .$fileExtension")
                return@post
            }

            // Create asset record in DB first
            val assetId = dao.createAsset(
                assetName = assetName!!,
                link = link,
                storeName = storeName,
                authorName = authorName,
                licenseName = licenseName,
                tagsString = tagsString,
                projectsString = projectsString
            )

            if (assetId == null) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to create asset record.")
                return@post
            }

            val assetUploadDir = File(UPLOAD_DIR, assetId.toString())
            assetUploadDir.mkdirs() // Create directory for this asset's files

            val targetFile = File(assetUploadDir, originalFileName)
            targetFile.writeBytes(fileBytes)

            val fileType = Files.probeContentType(targetFile.toPath()) ?: "application/octet-stream"
            var previewPath: String? = null

            // Generate other filename-based tags for the main uploaded file (not for base tags anymore)
            generateFilenameTags(assetId, originalFileName, dao)


            if (fileExtension == "zip") {
                // Process ZIP file
                // processZipFile will also add internal file entries to DB and generate tags
                val success = processZipFile(targetFile, assetId, dao, originalFileName) // Pass original ZIP filename
                if (success) {
                    call.respond(HttpStatusCode.Created, "ZIP file uploaded and processed. Asset ID: $assetId")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to process ZIP file. Asset ID: $assetId")
                }
            } else {
                // Handle single file upload
                if (fileType.startsWith("image/")) {
                    val thumbFileName = "${originalFileName.substringBeforeLast('.')}.jpg"
                    val thumbFile = File(PREVIEW_DIR, "${assetId}_$thumbFileName")
                    if (generateThumbnail(targetFile, thumbFile, 200, 200)) {
                        previewPath = "/previews/${assetId}_$thumbFileName"
                    }
                }

                dao.addFileToAsset(
                    assetId = assetId,
                    fileName = originalFileName.substringAfterLast('/'), // Only store filename, not full path in DB
                    filePath = targetFile.absolutePath,
                    fileSize = fileSize,
                    fileType = fileType,
                    previewPath = previewPath
                )
                call.respond(HttpStatusCode.Created, "File uploaded and details saved. Asset ID: $assetId")
            }
        }

        // TODO: Add a new endpoint for the first stage of a two-stage upload
        // For example:
        // post("/pre-upload") {
        //     val multipart = call.receiveMultipart()
        //     // Process file(s), save to a temp location, detect metadata
        //     // Return temporary ID and detected metadata (assetName, tags, etc.)
        //     // call.respond(HttpStatusCode.OK, DetectedMetadataResponse(tempAssetId, detectedName, detectedTags))
        // }

        // TODO: Refactor existing PUT endpoint or create a new one for finalizing
        // put("/finalize-upload/{tempAssetId}") {
        //     val tempAssetId = call.parameters["tempAssetId"]?.toIntOrNull()
        //     val metadata = call.receive<UpdateAssetRequest>()
        //     // Use tempAssetId to retrieve pre-uploaded files and metadata
        //     // Create final asset record based on metadata
        //     // call.respond(HttpStatusCode.OK, "Asset finalized and updated.")
        // }


        // PUT update asset metadata
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid asset ID")
                return@put
            }
            val updateRequest = call.receive<UpdateAssetRequest>()
            val message = dao.updateAssetDetails(id, updateRequest)
            call.respond(HttpStatusCode.OK, message)
        }

        // GET search assets
        get("/search") {
            val query = call.request.queryParameters["query"]
            if (query.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Search query cannot be empty.")
                return@get
            }
            val searchResults = dao.searchAssets(query)
            call.respond(searchResults)
        }
    }
}

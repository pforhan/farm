// farm/backend/src/main/kotlin/com/farm/util/FileProcessing.kt
package com.farm.util

import com.farm.database.Dao
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.zip.ZipFile
import javax.imageio.ImageIO
import java.awt.Image
import java.awt.RenderingHints
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

// Constants for file storage (re-declared here for clarity in this utility file)
val UPLOAD_DIR_UTIL = Paths.get(System.getProperty("user.dir"), "public", "uploads").toFile()
val PREVIEW_DIR_UTIL = Paths.get(System.getProperty("user.dir"), "public", "previews").toFile()
val ALLOWED_TYPES_UTIL = setOf("zip", "png", "jpg", "jpeg", "gif", "wav", "mp3", "ogg", "txt", "md", "html", "json", "xml")


/**
 * Generates a thumbnail for an image file.
 * @param sourceFile The original image file.
 * @param destinationFile The file path where the thumbnail will be saved.
 * @param width The desired width of the thumbnail.
 * @param height The desired height of the thumbnail.
 * @return True if thumbnail generation was successful, false otherwise.
 */
fun generateThumbnail(sourceFile: File, destinationFile: File, width: Int, height: Int): Boolean {
    return try {
        // Ensure destination directory exists
        destinationFile.parentFile.mkdirs()

        val originalImage: BufferedImage = ImageIO.read(sourceFile)
        val scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)

        val bufferedThumbnail = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = bufferedThumbnail.createGraphics()
        g2d.drawImage(scaledImage, 0, 0, null)
        g2d.dispose()

        // Preserve transparency for PNG and GIF (more robust way)
        val fileExtension = destinationFile.extension.toLowerCase()
        val imageType = originalImage.type
        val outputFormat: String = when (fileExtension) {
            "png" -> "png"
            "gif" -> "gif"
            "jpg", "jpeg" -> "jpeg" // Default to JPEG for thumbnails
            else -> "jpeg" // Fallback
        }

        // If the original image had transparency and the output format supports it, use ARGB
        val finalThumbnail = if (originalImage.alphaRaster != null && (outputFormat == "png" || outputFormat == "gif")) {
            BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).apply {
                val graphics = createGraphics()
                graphics.drawImage(scaledImage, 0, 0, null)
                graphics.dispose()
            }
        } else {
            bufferedThumbnail
        }

        ImageIO.write(finalThumbnail, outputFormat, destinationFile)
        true
    } catch (e: Exception) {
        println("Error generating thumbnail for ${sourceFile.name}: ${e.message}")
        false
    }
}

/**
 * Processes a ZIP file, extracts its contents, and adds them as files to an asset.
 * Also generates tags based on subdirectory structure and archive info.
 * @param zipFile The ZIP file to process.
 * @param assetId The ID of the asset to associate files with.
 * @param dao The DAO for database operations.
 * @param originalArchiveFileName The original filename of the uploaded ZIP archive.
 * @return True if ZIP processing was successful, false otherwise.
 */
fun processZipFile(zipFile: File, assetId: Int, dao: Dao, originalArchiveFileName: String): Boolean {
    return try {
        ZipFile(zipFile).use { zip ->
            val entries = zip.entries()
            val assetUploadDir = File(UPLOAD_DIR_UTIL, assetId.toString())
            assetUploadDir.mkdirs()

            // Add the "archive:archive-name" tag to the main asset
            dao.associateTagsWithAsset(assetId, "archive:${originalArchiveFileName.substringBeforeLast('.')}") // Tag without extension

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.isDirectory) {
                    continue
                }

                val entryName = entry.name // This is the path inside the archive
                val targetFile = File(assetUploadDir, entryName)
                val targetDir = targetFile.parentFile
                targetDir.mkdirs()

                zip.getInputStream(entry).use { inputStream ->
                    Files.copy(inputStream, targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                }

                val fileType = Files.probeContentType(targetFile.toPath()) ?: "application/octet-stream"
                var previewPath: String? = null

                // Handle image previews for extracted files
                if (fileType.startsWith("image/")) {
                    val thumbFileName = "${entryName.substringBeforeLast('.')}.jpg"
                    val thumbFile = File(PREVIEW_DIR_UTIL, "${assetId}_${UUID.randomUUID().toString().substring(0, 8)}_$thumbFileName") // Add UUID for uniqueness
                    if (generateThumbnail(targetFile, thumbFile, 200, 200)) {
                        previewPath = "/previews/${thumbFile.name}"
                    }
                }

                dao.addFileToAsset(
                    assetId = assetId,
                    fileName = entryName.substringAfterLast('/'), // Only store filename, not full path in DB
                    filePath = targetFile.absolutePath,
                    fileSize = entry.size.toInt(),
                    fileType = fileType,
                    previewPath = previewPath
                )

                // Add the "archive-path:path-inside-archive" tag for each extracted file
                dao.associateTagsWithAsset(assetId, "archive-path:$entryName")

                // Generate other tags from filename patterns for the extracted file
                // These tags are still associated with the main asset
                generateFilenameTags(assetId, entryName, dao)
            }
        }
        zipFile.delete() // Delete the original zip file after extraction
        true
    } catch (e: Exception) {
        println("Error processing zip file ${zipFile.name}: ${e.message}")
        e.printStackTrace()
        false
    }
}

/**
 * Generates tags from filename patterns and associates them with an asset.
 * @param assetId The ID of the asset.
 * @param fullFilePath The full path or name of the file (can be a filename or path within zip).
 * @param dao The DAO for database operations.
 */
fun generateFilenameTags(assetId: Int, fullFilePath: String, dao: Dao) {
    val fileName = fullFilePath.substringAfterLast(File.separatorChar) // Get just the filename
    val nameWithoutExt = fileName.substringBeforeLast('.', "")

    // 1. Always add the full filename (without extension) as a tag
    if (nameWithoutExt.isNotBlank()) {
        dao.associateTagsWithAsset(assetId, nameWithoutExt)
    }

    // 2. Split by common delimiters and add as tags (filtered)
    val parts = nameWithoutExt.split('_', '-').filter { it.isNotBlank() }.map { it.trim() }
    for (part in parts) {
        // Skip purely numeric parts (unless they are part of a specific pattern later)
        if (part.matches(Regex("\\d+")) && part.length < 3) {
            continue
        }
        // Skip common generic words
        if (part.toLowerCase() in listOf("a", "the", "and", "or", "to", "of", "for")) {
            continue
        }
        dao.associateTagsWithAsset(assetId, part)
    }

    // 3. Detect and add dimension tags (e.g., 20x20, 1280x720)
    val dimensionRegex = Regex("\\b(\\d{2,4}x\\d{2,4})\\b", RegexOption.IGNORE_CASE)
    // Search in filename and also in path (for zip internal paths)
    val textToSearch = "$nameWithoutExt $fullFilePath"
    dimensionRegex.findAll(textToSearch).forEach { matchResult ->
        dao.associateTagsWithAsset(assetId, matchResult.groupValues[1])
    }
}
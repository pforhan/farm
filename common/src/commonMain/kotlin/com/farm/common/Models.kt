// farm/common/src/commonMain/kotlin/com/farm/common/Models.kt
package com.farm.common

import kotlinx.serialization.Serializable

@Serializable
data class Asset(
    val assetId: Int,
    val assetName: String,
    val link: String?,
    val storeName: String?,
    val authorName: String?,
    val licenseName: String?,
    val tags: List<String> = emptyList(),
    val projects: List<String> = emptyList(),
    val files: List<FileDetail> = emptyList(),
    val previewThumbnail: String? // Path to the main thumbnail for browse/search view
)

@Serializable
data class FileDetail(
    val fileId: Int,
    val assetId: Int,
    val fileName: String,
    val filePath: String, // Full server-side path, primarily for backend use
    val publicPath: String, // Public URL path for download
    val fileSize: Int,
    val fileType: String,
    val previewPath: String? // Public URL path for file-specific thumbnail
)

@Serializable
data class UpdateAssetRequest(
    val assetName: String,
    val link: String?,
    val storeName: String?,
    val authorName: String?,
    val licenseName: String?,
    val tagsString: String?,
    val projectsString: String?
)

@Serializable
data class UploadAssetRequest(
    val assetName: String,
    val link: String?,
    val storeName: String?,
    val authorName: String?,
    val licenseName: String?,
    val tags: String?,
    val projects: String?
)
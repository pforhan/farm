// farm/database/src/main/kotlin/com/farm/database/Dao.kt
package com.farm.database

import com.farm.common.Asset
import com.farm.common.FileDetail
import com.farm.common.UpdateAssetRequest
import com.farm.database.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.nio.file.Paths

// Constants for public paths (consistent with Ktor's static serving)
private const val UPLOADS_PUBLIC_URL = "/uploads/"
private const val PREVIEWS_PUBLIC_URL = "/previews/"

object Dao {

    /**
     * Converts an Exposed AssetEntity and its related entities into a common.Asset data class.
     * @param assetEntity The Exposed AssetEntity.
     * @return A common.Asset data class.
     */
    private fun toCommonAsset(assetEntity: AssetEntity): Asset {
        val files = FileEntity.find { Files.asset eq assetEntity.id }.map { fileEntity ->
            FileDetail(
                fileId = fileEntity.id.value,
                assetId = assetEntity.id.value,
                fileName = fileEntity.fileName,
                filePath = fileEntity.filePath,
                publicPath = "$UPLOADS_PUBLIC_URL${fileEntity.asset.id.value}/${fileEntity.fileName}",
                fileSize = fileEntity.fileSize,
                fileType = fileEntity.fileType,
                previewPath = fileEntity.previewPath
            )
        }

        val tags = assetEntity.tags.map { it.tagName }
        val projects = assetEntity.projects.map { it.projectName }

        // Find a main preview thumbnail for the asset (e.g., the first image preview available)
        val mainPreviewThumbnail = files.firstOrNull { it.previewPath != null }?.previewPath

        return Asset(
            assetId = assetEntity.id.value,
            assetName = assetEntity.assetName,
            link = assetEntity.link,
            storeName = assetEntity.store?.storeName,
            authorName = assetEntity.author?.authorName,
            licenseName = assetEntity.license?.licenseName,
            tags = tags,
            projects = projects,
            files = files,
            previewThumbnail = mainPreviewThumbnail
        )
    }

    /**
     * Retrieves all assets from the database, ordered by ID descending.
     * @return A list of common.Asset data classes.
     */
    suspend fun getAllAssets(): List<Asset> = dbQuery {
        AssetEntity.all()
            .orderBy(Assets.id to SortOrder.DESC)
            .limit(20) // Limit to 20 for browsing
            .map(::toCommonAsset)
    }

    /**
     * Retrieves detailed information for a specific asset by its ID.
     * @param assetId The ID of the asset.
     * @return A common.Asset data class if found, null otherwise.
     */
    suspend fun getAssetDetails(assetId: Int): Asset? = dbQuery {
        AssetEntity.findById(assetId)?.let(::toCommonAsset)
    }

    /**
     * Searches for assets based on a query string across multiple fields.
     * @param query The search query string.
     * @return A list of common.Asset data classes matching the query.
     */
    suspend fun searchAssets(query: String): List<Asset> = dbQuery {
        val searchQuery = "%$query%"

        (Assets innerJoin Tags.optionalJoin(AssetTags, { Tags.id }, { AssetTags.tag }, { AssetTags.asset eq Assets.id }))
            .select(Assets.columns) // Select all columns from Assets table
            .where {
                (Assets.assetName like searchQuery) or
                        (Tags.tagName like searchQuery) or
                        (Assets.store.inSubQuery(Stores.select(Stores.id).where { Stores.storeName like searchQuery })) or
                        (Assets.author.inSubQuery(Authors.select(Authors.id).where { Authors.authorName like searchQuery })) or
                        (Assets.license.inSubQuery(Licenses.select(Licenses.id).where { Licenses.licenseName like searchQuery }))
            }
            .orderBy(Assets.id to SortOrder.DESC)
            .distinct() // Ensure unique assets
            .map {
                val assetEntity = AssetEntity.wrapRow(it) // Wrap the row back into an Entity
                toCommonAsset(assetEntity)
            }
    }


    /**
     * Helper function to get or create an ID for a related entity (Store, Author, License, Tag, Project).
     * @param table The Exposed table object (e.g., Stores, Authors, Tags).
     * @param nameColumn The column in the table containing the name (e.g., Stores.storeName).
     * @param nameValue The name value to find or create.
     * @return The ID of the existing or newly created entity, or null if nameValue is blank.
     */
    private suspend fun <T : IntIdTable> getOrCreateEntityId(
        table: T,
        nameColumn: Column<String>,
        nameValue: String?
    ): EntityID<Int>? = dbQuery {
        if (nameValue.isNullOrBlank()) {
            return@dbQuery null
        }
        table.select { nameColumn eq nameValue }.singleOrNull()?.get(table.id) ?:
        table.insert { it[nameColumn] = nameValue }.resultedValues?.first()?.get(table.id)
    }

    /**
     * Creates a new asset record in the database.
     * @return The ID of the newly created asset, or null if creation failed.
     */
    suspend fun createAsset(
        assetName: String,
        link: String?,
        storeName: String?,
        authorName: String?,
        licenseName: String?,
        tagsString: String?,
        projectsString: String?
    ): Int? = dbQuery {
        val storeId = getOrCreateEntityId(Stores, Stores.storeName, storeName)
        val authorId = getOrCreateEntityId(Authors, Authors.authorName, authorName)
        val licenseId = getOrCreateEntityId(Licenses, Licenses.licenseName, licenseName)

        val newAssetId = Assets.insert {
            it[Assets.assetName] = assetName
            it[Assets.link] = link
            it[Assets.store] = storeId
            it[Assets.author] = authorId
            it[Assets.license] = licenseId
        }[Assets.id].value

        // Associate tags and projects
        tagsString?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.forEach { tagName ->
            associateTagsWithAsset(newAssetId, tagName)
        }
        projectsString?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.forEach { projectName ->
            associateProjectsWithAsset(newAssetId, projectName)
        }

        newAssetId
    }

    /**
     * Updates an existing asset's metadata.
     */
    suspend fun updateAssetDetails(assetId: Int, updateRequest: UpdateAssetRequest): String = dbQuery {
        val assetEntity = AssetEntity.findById(assetId) ?: return@dbQuery "Asset with ID $assetId not found."

        val storeId = getOrCreateEntityId(Stores, Stores.storeName, updateRequest.storeName)
        val authorId = getOrCreateEntityId(Authors, Authors.authorName, updateRequest.authorName)
        val licenseId = getOrCreateEntityId(Licenses, Licenses.licenseName, updateRequest.licenseName)

        assetEntity.apply {
            assetName = updateRequest.assetName
            link = updateRequest.link
            store = storeId?.let { Store[it] }
            author = authorId?.let { Author[it] }
            license = licenseId?.let { License[it] }
        }

        // Update asset_tags: Delete existing and re-add
        AssetTags.deleteWhere { AssetTags.asset eq assetId }
        updateRequest.tagsString?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.forEach { tagName ->
            associateTagsWithAsset(assetId, tagName)
        }

        // Update asset_projects: Delete existing and re-add
        AssetProjects.deleteWhere { AssetProjects.asset eq assetId }
        updateRequest.projectsString?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.forEach { projectName ->
            associateProjectsWithAsset(assetId, projectName)
        }

        "Asset ID $assetId updated successfully."
    }

    /**
     * Adds a file record to an existing asset.
     */
    suspend fun addFileToAsset(
        assetId: Int,
        fileName: String,
        filePath: String,
        fileSize: Int,
        fileType: String,
        previewPath: String?
    ): Int = dbQuery {
        Files.insert {
            it[asset] = AssetEntity[assetId].id
            it[Files.fileName] = fileName
            it[Files.filePath] = filePath
            it[Files.fileSize] = fileSize
            it[Files.fileType] = fileType
            it[Files.previewPath] = previewPath
        }[Files.id].value
    }

    /**
     * Associates a tag with an asset. Creates the tag if it doesn't exist.
     */
    suspend fun associateTagsWithAsset(assetId: Int, tagName: String) = dbQuery {
        if (tagName.isBlank()) return@dbQuery

        val tagId = getOrCreateEntityId(Tags, Tags.tagName, tagName)
        if (tagId != null) {
            val exists = AssetTags.select { (AssetTags.asset eq assetId) and (AssetTags.tag eq tagId) }.count() > 0
            if (!exists) {
                AssetTags.insert {
                    it[asset] = AssetEntity[assetId].id
                    it[tag] = Tag[tagId].id
                }
            }
        }
    }

    /**
     * Associates a project with an asset. Creates the project if it doesn't exist.
     */
    suspend fun associateProjectsWithAsset(assetId: Int, projectName: String) = dbQuery {
        if (projectName.isBlank()) return@dbQuery

        val projectId = getOrCreateEntityId(Projects, Projects.projectName, projectName)
        if (projectId != null) {
            val exists = AssetProjects.select { (AssetProjects.asset eq assetId) and (AssetProjects.project eq projectId) }.count() > 0
            if (!exists) {
                AssetProjects.insert {
                    it[asset] = AssetEntity[assetId].id
                    it[project] = Project[projectId].id
                }
            }
        }
    }

    // TODO: Functions for user authentication, roles, permissions (future)
    // suspend fun createUser(...)
    // suspend fun getUserByUsername(...)
    // suspend fun assignRoleToUser(...)
    // suspend fun getPermissionsForRole(...)
}
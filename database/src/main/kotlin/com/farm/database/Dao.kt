package com.farm.database

import com.farm.common.Asset
import com.farm.common.FileDetail
import com.farm.common.UpdateAssetRequest
import com.farm.database.DatabaseFactory.dbQuery
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

// Constants for public paths (consistent with Ktor's static serving)
private const val UPLOADS_PUBLIC_URL = "/uploads/"
private const val PREVIEWS_PUBLIC_URL = "/previews/"

object Dao {

  /**
   * Helper to get or create an ID for related tables (Stores, Authors, Licenses, Tags, Projects).
   * This function calls `dbQuery`, so it must be a `suspend` function.
   */
  private suspend fun getOrCreateId(table: IntIdTable, nameColumn: Column<String>, name: String?): Int? = dbQuery {
    if (name.isNullOrBlank()) return@dbQuery null

    table.select(table.columns)
      .where { nameColumn eq name }
      .map { it[table.id].value }
      .singleOrNull() ?: table.insert { it[nameColumn] = name }[table.id].value
  }

  /**
   * Helper to add tags/projects to an asset.
   * This function calls `dbQuery`, so it must be a `suspend` function.
   */
  private suspend fun addAssociation(assetId: Int, itemId: Int?, associationTable: Table, assetIdColumn: Column<EntityID<Int>>, itemIdColumn: Column<EntityID<Int>>) {
    if (itemId != null) {
      dbQuery {
        // Check if association already exists to prevent duplicates
        val existing = associationTable.select(associationTable.columns)
          .where { (assetIdColumn eq assetId) and (itemIdColumn eq itemId) }
          .count()
        if (existing == 0L) {
          associationTable.insert {
            it[assetIdColumn] = EntityID(assetId, Assets) // Correctly wrap Int in EntityID
            it[itemIdColumn] = EntityID(itemId, Tags) // Or Projects, depending on context
          }
        }
      }
    }
  }

  /**
   * Maps a Exposed ResultRow to an Asset data class.
   * This function does not perform any suspending operations (it doesn't call dbQuery),
   * so it's a regular `fun`.
   */
  private fun resultRowToAsset(row: ResultRow): Asset {
    val assetId = row[Assets.id].value // Access ID via .id.value for IntIdTable
    // These queries are now part of the outer dbQuery transaction
    val tags = (Tags innerJoin AssetTags)
      .selectAll()
      .where { AssetTags.asset eq assetId } // Use `asset` reference
      .map { it[Tags.tagName] }

    val projects = (Projects innerJoin AssetProjects)
      .selectAll()
      .where { AssetProjects.asset eq assetId } // Use `asset` reference
      .map { it[Projects.projectName] }

    val files = Files.selectAll()
      .where { Files.asset eq assetId } // Use `asset` reference
      .map { fileRow ->
        FileDetail(
          fileId = fileRow[Files.id].value, // Access ID via .id.value
          assetId = fileRow[Files.asset].value,
          fileName = fileRow[Files.fileName],
          filePath = fileRow[Files.filePath],
          publicPath = "/uploads/${fileRow[Files.asset].value}/${fileRow[Files.fileName]}", // Construct public path
          fileSize = fileRow[Files.fileSize],
          fileType = fileRow[Files.fileType],
          previewPath = fileRow[Files.previewPath] // Already contains public path
        )
      }

    // Determine the main preview thumbnail for the asset
    val mainPreviewThumbnail = files.firstOrNull { it.previewPath != null }?.previewPath

    return Asset(
      assetId = assetId,
      assetName = row[Assets.assetName],
      link = row[Assets.link],
      storeName = row.getOrNull(Stores.storeName),
      authorName = row.getOrNull(Authors.authorName),
      licenseName = row.getOrNull(Licenses.licenseName),
      tags = tags,
      projects = projects,
      files = files,
      previewThumbnail = mainPreviewThumbnail
    )
  }

  /**
   * Adds tags and projects to a given asset.
   * This function calls other `suspend` helper functions, so it must be `suspend`.
   */
  private suspend fun addTagsAndProjects(assetId: Int, tagsString: String?, projectsString: String?) = dbQuery {
    tagsString?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.forEach { tagName ->
      val tagId = getOrCreateId(Tags, Tags.tagName, tagName)
      addAssociation(assetId, tagId, AssetTags, AssetTags.asset, AssetTags.tag) // Pass correct columns
    }
    projectsString?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }?.forEach { projectName ->
      val projectId = getOrCreateId(Projects, Projects.projectName, projectName)
      addAssociation(assetId, projectId, AssetProjects, AssetProjects.asset, AssetProjects.project) // Pass correct columns
    }
  }

  suspend fun allAssets(): List<Asset> = dbQuery {
    Assets.selectAll().map(::resultRowToAsset)
  }

  suspend fun asset(id: Int): Asset? = dbQuery {
    Assets.selectAll()
      .where { Assets.id eq id } // Use .id for IntIdTable
      .map(::resultRowToAsset)
      .singleOrNull()
  }

  suspend fun addNewAsset(
    assetName: String,
    link: String?,
    storeName: String?,
    authorName: String?,
    licenseName: String?,
    tagsString: String?,
    projectsString: String?
  ): Asset? {
    return dbQuery {
      val storeId = getOrCreateId(Stores, Stores.storeName, storeName)
      val authorId = getOrCreateId(Authors, Authors.authorName, authorName)
      val licenseId = getOrCreateId(Licenses, Licenses.licenseName, licenseName)

      val newAssetEntity = AssetEntity.new {
        this.assetName = assetName
        this.link = link
        this.store = storeId?.let { StoreEntity[it] } // Link by ID
        this.author = authorId?.let { AuthorEntity[it] } // Link by ID
        this.license = licenseId?.let { LicenseEntity[it] } // Link by ID
      }
      val newAssetId = newAssetEntity.id.value

      // Add tags and projects
      addTagsAndProjects(newAssetId, tagsString, projectsString)

      asset(newAssetId) // Fetch the newly created asset with all its details
    }
  }

  suspend fun editAsset(
    id: Int,
    updateAssetRequest: UpdateAssetRequest
  ) = editAsset(
    id = id,
    assetName = updateAssetRequest.assetName,
    link = updateAssetRequest.link,
    storeName = updateAssetRequest.storeName,
    authorName = updateAssetRequest.authorName,
    licenseName = updateAssetRequest.licenseName,
    tagsString = updateAssetRequest.tagsString,
    projectsString = updateAssetRequest.projectsString
  )

  suspend fun editAsset(
    id: Int,
    assetName: String,
    link: String?,
    storeName: String?,
    authorName: String?,
    licenseName: String?,
    tagsString: String?,
    projectsString: String?
  ): Boolean {
    return dbQuery {
      val assetEntity = AssetEntity.findById(id) ?: return@dbQuery false // Find by ID

      val storeId = getOrCreateId(Stores, Stores.storeName, storeName)
      val authorId = getOrCreateId(Authors, Authors.authorName, authorName)
      val licenseId = getOrCreateId(Licenses, Licenses.licenseName, licenseName)

      assetEntity.apply {
        this.assetName = assetName
        this.link = link
        this.store = storeId?.let { StoreEntity[it] }
        this.author = authorId?.let { AuthorEntity[it] }
        this.license = licenseId?.let { LicenseEntity[it] }
      }

      // Update tags - remove all existing and add new ones
      AssetTags.deleteWhere { AssetTags.asset eq id }
      addTagsAndProjects(id, tagsString, projectsString)

      // Update projects - remove all existing and add new ones
      AssetProjects.deleteWhere { AssetProjects.asset eq id }
      // Note: addTagsAndProjects handles both, so we call it with null for tags if only projects are updated.
      addTagsAndProjects(id, null, projectsString)

      true // If we reached here, update was successful
    }
  }

  suspend fun deleteAsset(id: Int): Boolean = dbQuery {
    // Delete associated files first
    Files.deleteWhere { Files.asset eq id }
    AssetTags.deleteWhere { AssetTags.asset eq id }
    AssetProjects.deleteWhere { AssetProjects.asset eq id }
    AssetEntity.findById(id)?.let {
      it.delete()
      true
    } ?: false
  }

  suspend fun addFile(
    assetId: Int,
    fileName: String,
    filePath: String,
    fileSize: Long,
    fileType: String,
    previewPath: String?
  ): FileDetail? {
    return dbQuery {
      val newFileEntity = FileEntity.new {
        this.asset = AssetEntity[assetId] // Link to existing AssetEntity
        this.fileName = fileName
        this.filePath = filePath
        this.fileSize = fileSize
        this.fileType = fileType
        this.previewPath = previewPath
      }
      FileDetail(
        fileId = newFileEntity.id.value,
        assetId = newFileEntity.asset.id.value,
        fileName = newFileEntity.fileName,
        filePath = newFileEntity.filePath,
        publicPath = "/uploads/${newFileEntity.asset.id.value}/${newFileEntity.fileName}",
        fileSize = newFileEntity.fileSize,
        fileType = newFileEntity.fileType,
        previewPath = newFileEntity.previewPath
      )
    }
  }

  suspend fun searchAssets(query: String): List<Asset> = dbQuery {
    val likeQuery = "%$query%"

    // Base query for assets, joined with other tables for search criteria
    val queryResult = (Assets
      .leftJoin(Stores)
      .leftJoin(Authors)
      .leftJoin(Licenses)
      .leftJoin(AssetTags)
      .leftJoin(Tags)
      .leftJoin(Files))
      .select(Assets.columns) // Select all columns from Assets table
      .where {
        // Search by asset name, store, author, license, tag name, file type
        (Assets.assetName like likeQuery) or
          (Stores.storeName.isNotNull() and (Stores.storeName like likeQuery)) or
          (Authors.authorName.isNotNull() and (Authors.authorName like likeQuery)) or
          (Licenses.licenseName.isNotNull() and (Licenses.licenseName like likeQuery)) or
          (Tags.tagName.isNotNull() and (Tags.tagName like likeQuery)) or
          (Files.fileType like likeQuery) or
          // Search for dimension tags like "20x20"
          (Tags.tagName like "%x%" and (Tags.tagName like likeQuery))
      }
      .orderBy(Assets.id, SortOrder.DESC) // Order by ID descending for most recent first
      .distinct() // Ensure unique assets in results

    queryResult.map(::resultRowToAsset)
  }

  suspend fun addTagToAsset(assetId: Int, tagName: String): Int? {
    return dbQuery {
      val tagId = getOrCreateId(Tags, Tags.tagName, tagName)
      if (tagId != null) {
        addAssociation(assetId, tagId, AssetTags, AssetTags.asset, AssetTags.tag)
      }
      tagId
    }
  }

  suspend fun addProjectToAsset(assetId: Int, projectName: String): Int? {
    return dbQuery {
      val projectId = getOrCreateId(Projects, Projects.projectName, projectName)
      if (projectId != null) {
        addAssociation(assetId, projectId, AssetProjects, AssetProjects.asset, AssetProjects.project)
      }
      projectId
    }
  }
}

// farm/database/src/main/kotlin/com/farm/database/Tables.kt
package com.farm.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

object Assets : IntIdTable("assets") {
    val assetName = varchar("asset_name", 255)
    val link = varchar("link", 255).nullable()
    val store = reference("store_id", Stores).nullable() // Reference to Stores table
    val author = reference("author_id", Authors).nullable() // Reference to Authors table
    val license = reference("license_id", Licenses).nullable() // Reference to Licenses table
}

object Files : IntIdTable("files") {
    val asset = reference("asset_id", Assets) // Reference to Assets table
    val fileName = varchar("file_name", 255)
    val filePath = varchar("file_path", 255)
    val fileSize = long("file_size")
    val fileType = varchar("file_type", 255)
    val previewPath = varchar("preview_path", 255).nullable()
}

object Stores : IntIdTable("stores") {
    val storeName = varchar("store_name", 255).uniqueIndex()
}

object Authors : IntIdTable("authors") {
    val authorName = varchar("author_name", 255).uniqueIndex()
}

object Licenses : IntIdTable("licenses") {
    val licenseName = varchar("license_name", 255).uniqueIndex()
}

object Tags : IntIdTable("tags") {
    val tagName = varchar("tag_name", 255).uniqueIndex()
}

object AssetTags : Table("asset_tags") {
    val asset = reference("asset_id", Assets)
    val tag = reference("tag_id", Tags)

    override val primaryKey = PrimaryKey(asset, tag)
}

object Projects : IntIdTable("projects") {
    val projectName = varchar("project_name", 255).uniqueIndex()
}

object AssetProjects : Table("asset_projects") {
    val asset = reference("asset_id", Assets)
    val project = reference("project_id", Projects)

    override val primaryKey = PrimaryKey(asset, project)
}

// Exposed DAO entities for easier data manipulation
class AssetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AssetEntity>(Assets)
    var assetName by Assets.assetName
    var link by Assets.link
    var store by StoreEntity optionalReferencedOn Assets.store
    var author by AuthorEntity optionalReferencedOn Assets.author
    var license by LicenseEntity optionalReferencedOn Assets.license
}

class FileEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FileEntity>(Files)
    var asset by AssetEntity referencedOn Files.asset
    var fileName by Files.fileName
    var filePath by Files.filePath
    var fileSize by Files.fileSize
    var fileType by Files.fileType
    var previewPath by Files.previewPath
}

class StoreEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<StoreEntity>(Stores)
    var storeName by Stores.storeName
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(Authors)
    var authorName by Authors.authorName
}

class LicenseEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<LicenseEntity>(Licenses)
    var licenseName by Licenses.licenseName
}

class TagEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TagEntity>(Tags)
    var tagName by Tags.tagName
}

class ProjectEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProjectEntity>(Projects)
    var projectName by Projects.projectName
}

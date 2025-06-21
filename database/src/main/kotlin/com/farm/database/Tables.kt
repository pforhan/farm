// farm/database/src/main/kotlin/com/farm/database/Tables.kt
package com.farm.database

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

// Tables for relational entities
object Stores : IntIdTable("stores") {
    val storeName: Column<String> = varchar("store_name", 255).uniqueIndex()
}

class Store(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Store>(Stores)
    var storeName by Stores.storeName
}

object Licenses : IntIdTable("licenses") {
    val licenseName: Column<String> = varchar("license_name", 255).uniqueIndex()
}

class License(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<License>(Licenses)
    var licenseName by Licenses.licenseName
}

object Authors : IntIdTable("authors") {
    val authorName: Column<String> = varchar("author_name", 255).uniqueIndex()
}

class Author(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Author>(Authors)
    var authorName by Authors.authorName
}

object Tags : IntIdTable("tags") {
    val tagName: Column<String> = varchar("tag_name", 255).uniqueIndex()
}

class Tag(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Tag>(Tags)
    var tagName by Tags.tagName
}

object Projects : IntIdTable("projects") {
    val projectName: Column<String> = varchar("project_name", 255).uniqueIndex()
}

class Project(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Project>(Projects)
    var projectName by Projects.projectName
}


// Main Asset Table
object Assets : IntIdTable("assets") {
    val store = reference("store_id", Stores).nullable()
    val link = varchar("link", 255).nullable()
    val author = reference("author_id", Authors).nullable()
    val license = reference("license_id", Licenses).nullable()
    val assetName = varchar("asset_name", 255)
}

class AssetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AssetEntity>(Assets)
    var store by Store optionalReferencedOn Assets.store
    var link by Assets.link
    var author by Author optionalReferencedOn Assets.author
    var license by License optionalReferencedOn Assets.license
    var assetName by Assets.assetName
    val tags by Tag via AssetTags
    val projects by Project via AssetProjects
    val files by FileEntity referrersOn Files.asset
}

// Files Table
object Files : IntIdTable("files") {
    val asset = reference("asset_id", Assets)
    val fileName = varchar("file_name", 255)
    val filePath = varchar("file_path", 255) // Server-side absolute path
    val fileSize = integer("file_size")
    val fileType = varchar("file_type", 255)
    val previewPath = varchar("preview_path", 255).nullable() // Public URL path for thumbnail
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

// Junction Tables for Many-to-Many Relationships
object AssetTags : IntIdTable("asset_tags") {
    val asset = reference("asset_id", Assets)
    val tag = reference("tag_id", Tags)
}

object AssetProjects : IntIdTable("asset_projects") {
    val asset = reference("asset_id", Assets)
    val project = reference("project_id", Projects)
}

// TODO: User, Role, Permission tables for future authentication
// object Users : IntIdTable("users") {
//     val username = varchar("username", 255).uniqueIndex()
//     val passwordHash = varchar("password_hash", 255)
//     val email = varchar("email", 255).uniqueIndex()
// }

// object Roles : IntIdTable("roles") {
//     val roleName = varchar("role_name", 50).uniqueIndex()
// }

// object UserRoles : IntIdTable("user_roles") {
//     val user = reference("user_id", Users)
//     val role = reference("role_id", Roles)
//     override val primaryKey = PrimaryKey(user, role)
// }

// object Permissions : IntIdTable("permissions") {
//    val permissionName = varchar("permission_name", 100).uniqueIndex()
// }

// object RolePermissions : IntIdTable("role_permissions") {
//    val role = reference("role_id", Roles)
//    val permission = reference("permission_id", Permissions)
//    override val primaryKey = PrimaryKey(role, permission)
// }

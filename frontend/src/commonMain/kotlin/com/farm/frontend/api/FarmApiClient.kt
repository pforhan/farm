// farm/frontend/src/commonMain/kotlin/com/farm/frontend/api/FarmApiClient.kt
package com.farm.frontend.api

import com.farm.common.Asset
import com.farm.common.FileDetail
import com.farm.common.UpdateAssetRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object FarmApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    private const val BASE_URL = "/api" // Ktor backend routes start with /api

    suspend fun getAssets(): List<Asset> {
        return client.get("$BASE_URL/assets").body()
    }

    suspend fun getAssetDetails(assetId: Int): Asset? {
        return client.get("$BASE_URL/assets/$assetId").body()
    }

    suspend fun searchAssets(query: String): List<Asset> {
        return client.get("$BASE_URL/assets/search") {
            parameter("query", query)
        }.body()
    }

    suspend fun uploadAsset(
        assetName: String,
        link: String?,
        storeName: String?,
        authorName: String?,
        licenseName: String?,
        tags: String?,
        projects: String?,
        fileBytes: ByteArray,
        fileName: String,
        fileType: String
    ): String {
        return client.post("$BASE_URL/assets/upload") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("asset_name", assetName)
                    link?.let { append("link", it) }
                    storeName?.let { append("store_name", it) }
                    authorName?.let { append("author_name", it) }
                    licenseName?.let { append("license_name", it) }
                    tags?.let { append("tags", it) }
                    projects?.let { append("projects", it) }
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentType, fileType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ))
        }.bodyAsText()
    }

    suspend fun updateAsset(assetId: Int, request: UpdateAssetRequest): String {
        return client.put("$BASE_URL/assets/$assetId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.bodyAsText()
    }
}
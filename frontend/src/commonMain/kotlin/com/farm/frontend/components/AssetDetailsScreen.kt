// farm/frontend/src/commonMain/kotlin/com/farm/frontend/components/AssetDetailsScreen.kt
package com.farm.frontend.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.farm.common.Asset
import com.farm.common.FileDetail
import com.farm.frontend.api.FarmApiClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Audio
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P

// For Image loading in Compose Multiplatform (Web/JVM/Desktop/Mobile)
// This is a placeholder for a multiplatform image loader.
// For Web, you'd typically use <img> tag directly with the public URL.
// For native (Android/iOS/Desktop), you'd need a multiplatform image loading library.
// For simplicity in this Web-only Compose Multiplatform context, we'll use <Img> tag directly.
@Composable
fun AsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    // In Compose Web, for a simple image, just use the <img> tag
    // For a more robust solution that loads images asynchronously for all platforms,
    // you'd typically use a library like Coil (for Android/Desktop) or Glide.
    // For web, direct <Img> is often fine as the browser handles loading.
    Img(src = imageUrl, alt = contentDescription, attrs = {
        style {
            maxWidth(200.px) // Example styling
            height(200.px)
            borderRadius(8.px)
            property("object-fit", "cover")
        }
    })
}


@Composable
fun AssetDetailsScreen(
    assetId: Int,
    onBackClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onMessage: (String) -> Unit
) {
    var asset by remember { mutableStateOf<Asset?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(assetId) {
        isLoading = true
        coroutineScope.launch {
            try {
                asset = FarmApiClient.getAssetDetails(assetId)
            } catch (e: Exception) {
                onMessage("Failed to load asset details: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (asset == null) {
            Text("Asset not found.", modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBackClick) { Text("Back to Browse") }
        } else {
            val currentAsset = asset!!
            Text("Asset: ${currentAsset.assetName} (ID: ${currentAsset.assetId})", style = MaterialTheme.typography.h5)
            Spacer(Modifier.height(16.dp))

            currentAsset.previewThumbnail?.let {
                AsyncImage(
                    imageUrl = it,
                    contentDescription = "Asset Preview",
                    modifier = Modifier.fillMaxWidth().height(200.dp).align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(16.dp))
            }

            Text("Store: ${currentAsset.storeName ?: "N/A"}")
            Text("Author: ${currentAsset.authorName ?: "N/A"}")
            Text("License: ${currentAsset.licenseName ?: "N/A"}")
            Text("Link: ")
            if (!currentAsset.link.isNullOrBlank()) {
                A(href = currentAsset.link, attrs = { target("_blank") }) {
                    Text(currentAsset.link)
                }
            } else {
                Text("N/A")
            }
            Text("Tags: ${currentAsset.tags.joinToString(", ").ifEmpty { "None" }}")
            Text("Projects: ${currentAsset.projects.joinToString(", ").ifEmpty { "None" }}")
            Spacer(Modifier.height(24.dp))

            Text("Files:", style = MaterialTheme.typography.h6)
            if (currentAsset.files.isEmpty()) {
                Text("No files associated with this asset.")
            } else {
                currentAsset.files.forEach { file ->
                    FileDetailCard(file = file)
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onBackClick) { Text("Back to Browse") }
                Button(onClick = { onEditClick(currentAsset.assetId) }) { Text("Edit Asset") }
            }
        }
    }
}

@Composable
fun FileDetailCard(file: FileDetail) {
    Div(attrs = {
        style {
            padding(16.px)
            borderRadius(8.px)
            property("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
            property("background", "#fff")
        }
    }) {
        Text("Name: ${file.fileName}")
        Text("Type: ${file.fileType}")
        Text("Size: ${"%.2f".format(file.fileSize / (1024.0 * 1024.0))} MB")

        file.previewPath?.let {
            if (file.fileType.startsWith("image/")) {
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    imageUrl = it,
                    contentDescription = "File Preview",
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (file.fileType.startsWith("audio/")) {
            Spacer(Modifier.height(8.dp))
            Audio(src = file.publicPath) // Using Compose Web's Audio DOM element
        }

        Spacer(Modifier.height(8.dp))
        A(href = file.publicPath, attrs = {
            attr("download", file.fileName)
            style {
                color(Color.Blue)
                textDecoration("underline")
            }
        }) {
            Text("Download File")
        }
    }
}
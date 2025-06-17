// farm/frontend/src/commonMain/kotlin/com/farm/frontend/components/BrowseAssets.kt
package com.farm.frontend.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.farm.common.Asset
import com.farm.frontend.api.FarmApiClient
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.Img // Use Img for web
import org.jetbrains.compose.web.dom.A // Use A for web links

@Composable
fun BrowseAssets(
    onAssetClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onMessage: (String) -> Unit
) {
    var assets by remember { mutableStateOf<List<Asset>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        coroutineScope.launch {
            try {
                assets = FarmApiClient.getAssets()
            } catch (e: Exception) {
                onMessage("Failed to load assets: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Browse Assets", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (assets.isEmpty()) {
            Text("No assets found. Start by uploading one!", modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ID", Modifier.weight(0.1f), style = MaterialTheme.typography.subtitle1)
                        Text("Preview", Modifier.weight(0.15f), style = MaterialTheme.typography.subtitle1)
                        Text("Name", Modifier.weight(0.4f), style = MaterialTheme.typography.subtitle1)
                        Text("Link", Modifier.weight(0.2f), style = MaterialTheme.typography.subtitle1)
                        Text("Actions", Modifier.weight(0.15f), style = MaterialTheme.typography.subtitle1)
                    }
                }
                items(assets) { asset ->
                    AssetRow(asset, onAssetClick, onEditClick)
                }
            }
        }
    }
}

@Composable
fun AssetRow(
    asset: Asset,
    onAssetClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onAssetClick(asset.assetId) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(asset.assetId.toString(), Modifier.weight(0.1f))
        Box(modifier = Modifier.weight(0.15f).size(50.dp)) {
            if (!asset.previewThumbnail.isNullOrBlank()) {
                Img(
                    src = asset.previewThumbnail,
                    alt = "Preview",
                    attrs = {
                        style {
                            width(50.px)
                            height(50.px)
                            property("object-fit", "cover")
                            borderRadius(4.px)
                        }
                    }
                )
            } else {
                Text("N/A", style = MaterialTheme.typography.caption, color = Color.Gray)
            }
        }
        Text(asset.assetName, Modifier.weight(0.4f))
        Box(modifier = Modifier.weight(0.2f)) {
            if (!asset.link.isNullOrBlank()) {
                A(href = asset.link, attrs = { target("_blank") }) {
                    Text("View Link")
                }
            } else {
                Text("N/A", style = MaterialTheme.typography.caption, color = Color.Gray)
            }
        }
        Row(modifier = Modifier.weight(0.15f)) {
            Button(onClick = { onAssetClick(asset.assetId) }) { Text("Details") }
            Spacer(Modifier.width(4.dp))
            Button(onClick = { onEditClick(asset.assetId) }) { Text("Edit") }
        }
    }
}
// farm/frontend/src/commonMain/kotlin/com/farm/frontend/App.kt
package com.farm.frontend

import androidx.compose.runtime.*
import com.farm.common.Asset
import com.farm.common.FileDetail
import com.farm.common.UpdateAssetRequest
import com.farm.frontend.api.FarmApiClient
import com.farm.frontend.components.* // Import all components
import io.ktor.http.*
import kotlinx.coroutines.launch

// Define possible application views/pages
sealed class Screen {
    object Upload : Screen()
    object Browse : Screen()
    data class AssetDetails(val assetId: Int) : Screen()
    data class EditAsset(val assetId: Int) : Screen()
    data class SearchResults(val query: String) : Screen()
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Upload) }
    var message by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Global message display
    LaunchedEffect(message) {
        if (message != null) {
            // Simple delay to show message then clear
            kotlinx.coroutines.delay(3000)
            message = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { currentScreen = Screen.Upload }) { Text("Upload New Asset") }
            Button(onClick = { currentScreen = Screen.Browse }) { Text("Browse Assets") }
            // Simple inline search for demonstration; dedicated search page below
            var searchQuery by remember { mutableStateOf("") }
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Button(onClick = {
                if (searchQuery.isNotBlank()) {
                    currentScreen = Screen.SearchResults(searchQuery)
                } else {
                    message = "Please enter a search query."
                }
            }) { Text("Search Assets") }
        }

        // Main content area based on currentScreen
        when (currentScreen) {
            is Screen.Upload -> {
                UploadForm(
                    onUploadSuccess = { assetId, msg ->
                        message = msg
                        currentScreen = Screen.AssetDetails(assetId) // Navigate to details after upload
                    },
                    onUploadError = { errorMsg ->
                        message = "Upload Error: $errorMsg"
                    }
                )
            }
            is Screen.Browse -> {
                BrowseAssets(
                    onAssetClick = { assetId -> currentScreen = Screen.AssetDetails(assetId) },
                    onEditClick = { assetId -> currentScreen = Screen.EditAsset(assetId) },
                    onMessage = { msg -> message = msg }
                )
            }
            is Screen.AssetDetails -> {
                val assetId = (currentScreen as Screen.AssetDetails).assetId
                AssetDetailsScreen(
                    assetId = assetId,
                    onBackClick = { currentScreen = Screen.Browse },
                    onEditClick = { id -> currentScreen = Screen.EditAsset(id) },
                    onMessage = { msg -> message = msg }
                )
            }
            is Screen.EditAsset -> {
                val assetId = (currentScreen as Screen.EditAsset).assetId
                EditAssetScreen(
                    assetId = assetId,
                    onUpdateSuccess = { updatedAssetId, msg ->
                        message = msg
                        currentScreen = Screen.AssetDetails(updatedAssetId) // Go back to details after edit
                    },
                    onCancel = { currentScreen = Screen.AssetDetails(assetId) },
                    onMessage = { msg -> message = msg }
                )
            }
            is Screen.SearchResults -> {
                val query = (currentScreen as Screen.SearchResults).query
                SearchResultsScreen(
                    query = query,
                    onAssetClick = { assetId -> currentScreen = Screen.AssetDetails(assetId) },
                    onEditClick = { assetId -> currentScreen = Screen.EditAsset(assetId) },
                    onMessage = { msg -> message = msg }
                )
            }
        }

        // Global message display at the bottom
        message?.let {
            Text(
                text = it,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                color = Color.Green, // Style success messages
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// Re-using common Compose Multiplatform components for web
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
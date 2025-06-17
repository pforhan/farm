// farm/frontend/src/commonMain/kotlin/com/farm/frontend/components/EditAssetScreen.kt
package com.farm.frontend.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farm.common.UpdateAssetRequest
import com.farm.frontend.api.FarmApiClient
import kotlinx.coroutines.launch

@Composable
fun EditAssetScreen(
    assetId: Int,
    onUpdateSuccess: (Int, String) -> Unit,
    onCancel: () -> Unit,
    onMessage: (String) -> Unit
) {
    var assetName by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("") }
    var licenseName by remember { mutableStateOf("") }
    var tagsString by remember { mutableStateOf("") }
    var projectsString by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Load existing asset details
    LaunchedEffect(assetId) {
        isLoading = true
        coroutineScope.launch {
            try {
                val asset = FarmApiClient.getAssetDetails(assetId)
                if (asset != null) {
                    assetName = asset.assetName
                    link = asset.link ?: ""
                    storeName = asset.storeName ?: ""
                    authorName = asset.authorName ?: ""
                    licenseName = asset.licenseName ?: ""
                    tagsString = asset.tags.joinToString(", ")
                    projectsString = asset.projects.joinToString(", ")
                } else {
                    onMessage("Asset not found for editing.")
                    onCancel() // Go back if asset not found
                }
            } catch (e: Exception) {
                onMessage("Failed to load asset for editing: ${e.message}")
                onCancel()
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
        Text("Edit Asset (ID: $assetId)", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            OutlinedTextField(
                value = assetName,
                onValueChange = { assetName = it },
                label = { Text("Asset Name") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text("Source URL (Link)") },
                placeholder = { Text("http://example.com/source") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Store") },
                placeholder = { Text("e.g., Unity Asset Store") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = authorName,
                onValueChange = { authorName = it },
                label = { Text("Author") },
                placeholder = { Text("e.g., Jane Doe") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = licenseName,
                onValueChange = { licenseName = it },
                label = { Text("License") },
                placeholder = { Text("e.g., MIT, Royalty-Free") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = tagsString,
                onValueChange = { tagsString = it },
                label = { Text("Tags (comma-separated)") },
                placeholder = { Text("e.g., 2D, character, pixelart") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            OutlinedTextField(
                value = projectsString,
                onValueChange = { projectsString = it },
                label = { Text("Projects (comma-separated)") },
                placeholder = { Text("e.g., MyGameTitle, RPG") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onCancel) {
                    Text("Cancel")
                }
                Button(onClick = {
                    coroutineScope.launch {
                        try {
                            val request = UpdateAssetRequest(
                                assetName = assetName,
                                link = link.takeIf { it.isNotBlank() },
                                storeName = storeName.takeIf { it.isNotBlank() },
                                authorName = authorName.takeIf { it.isNotBlank() },
                                licenseName = licenseName.takeIf { it.isNotBlank() },
                                tagsString = tagsString.takeIf { it.isNotBlank() },
                                projectsString = projectsString.takeIf { it.isNotBlank() }
                            )
                            val responseMessage = FarmApiClient.updateAsset(assetId, request)
                            onUpdateSuccess(assetId, responseMessage)
                        } catch (e: Exception) {
                            onMessage("Update failed: ${e.message}")
                        }
                    }
                }) {
                    Text("Update Asset")
                }
            }
        }
    }
}
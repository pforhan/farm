// farm/frontend/src/commonMain/kotlin/com/farm/frontend/components/SearchResultsScreen.kt
package com.farm.frontend.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farm.common.Asset
import com.farm.frontend.api.FarmApiClient
import kotlinx.coroutines.launch

@Composable
fun SearchResultsScreen(
    query: String, // Initial query
    onAssetClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onMessage: (String) -> Unit
) {
    var currentQuery by remember { mutableStateOf(query) }
    var searchResults by remember { mutableStateOf<List<Asset>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    val performSearch: (String) -> Unit = { q ->
        coroutineScope.launch {
            isLoading = true
            try {
                searchResults = FarmApiClient.searchAssets(q)
            } catch (e: Exception) {
                onMessage("Search failed: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Perform initial search when the screen is first composed or query changes
    LaunchedEffect(currentQuery) {
        if (currentQuery.isNotBlank()) {
            performSearch(currentQuery)
        } else {
            searchResults = emptyList()
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Search Results", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        // Search input field
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentQuery,
                onValueChange = { currentQuery = it },
                label = { Text("Search Query") },
                placeholder = { Text("Enter name, tag, type, or size") },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Button(onClick = {
                if (currentQuery.isNotBlank()) {
                    performSearch(currentQuery)
                } else {
                    onMessage("Please enter a search query.")
                }
            }) {
                Text("Search")
            }
        }

        Text("Results for: \"$currentQuery\"")
        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (searchResults.isEmpty()) {
            Text("No assets found matching your search query.", modifier = Modifier.align(Alignment.CenterHorizontally))
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
                items(searchResults) { asset ->
                    AssetRow(asset, onAssetClick, onEditClick)
                }
            }
        }
    }
}
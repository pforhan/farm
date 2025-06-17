// farm/frontend/src/commonMain/kotlin/com/farm/frontend/components/UploadForm.kt
package com.farm.frontend.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farm.frontend.api.FarmApiClient
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.dom.url.URLSearchParams
import kotlinx.browser.window
import org.w3c.files.Blob
import org.khronos.webgl.Uint8Array


@Composable
fun UploadForm(
    onUploadSuccess: (Int, String) -> Unit,
    onUploadError: (String) -> Unit
) {
    var selectedFile: File? by remember { mutableStateOf(null) }
    var assetName by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("") }
    var licenseName by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var projects by remember { mutableStateOf("") }

    var isUploading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Upload New Asset", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))

        // File Input
        // Using a custom composable for file input due to Compose Web limitations with standard components
        FileInput(
            onFileSelected = { file ->
                selectedFile = file
                // Auto-fill asset name from file name
                assetName = file.name.substringBeforeLast('.', "")
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        selectedFile?.let {
            Text("Selected file: ${it.name}", style = MaterialTheme.typography.body2)
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = assetName,
            onValueChange = { assetName = it },
            label = { Text("Asset Name") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            readOnly = selectedFile != null // Read-only if file selected for auto-fill
        )
        OutlinedTextField(
            value = link,
            onValueChange = { link = it },
            label = { Text("Source URL (Link)") },
            placeholder = { Text("http://example.com/source") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = storeName,
            onValueChange = { storeName = it },
            label = { Text("Store") },
            placeholder = { Text("e.g., Unity Asset Store") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = authorName,
            onValueChange = { authorName = it },
            label = { Text("Author") },
            placeholder = { Text("e.g., Jane Doe") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = licenseName,
            onValueChange = { licenseName = it },
            label = { Text("License") },
            placeholder = { Text("e.g., MIT, Royalty-Free") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Initial Tags (comma-separated)") },
            placeholder = { Text("e.g., 2D, character, pixelart") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = projects,
            onValueChange = { projects = it },
            label = { Text("Projects (comma-separated)") },
            placeholder = { Text("e.g., MyGameTitle, RPG") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedFile == null) {
                    onUploadError("Please select a file to upload.")
                    return@Button
                }
                if (assetName.isBlank()) {
                    onUploadError("Asset name cannot be empty.")
                    return@Button
                }

                isUploading = true
                coroutineScope.launch {
                    try {
                        val fileReader = FileReader()
                        fileReader.onload = { event ->
                            val arrayBuffer = fileReader.result as? kotlinx.js.ArrayBuffer
                            if (arrayBuffer != null) {
                                val fileBytes = Uint8Array(arrayBuffer).unsafeCast<ByteArray>()
                                launch {
                                    val response = FarmApiClient.uploadAsset(
                                        assetName = assetName,
                                        link = link.takeIf { it.isNotBlank() },
                                        storeName = storeName.takeIf { it.isNotBlank() },
                                        authorName = authorName.takeIf { it.isNotBlank() },
                                        licenseName = licenseName.takeIf { it.isNotBlank() },
                                        tags = tags.takeIf { it.isNotBlank() },
                                        projects = projects.takeIf { it.isNotBlank() },
                                        fileBytes = fileBytes,
                                        fileName = selectedFile!!.name,
                                        fileType = selectedFile!!.type
                                    )
                                    // Parse response for asset ID (assuming success message contains it)
                                    val assetIdMatch = Regex("Asset ID: (\\d+)").find(response)
                                    val uploadedAssetId = assetIdMatch?.groupValues?.get(1)?.toIntOrNull()

                                    if (uploadedAssetId != null) {
                                        onUploadSuccess(uploadedAssetId, response)
                                    } else {
                                        onUploadError(response) // Or parse actual error message
                                    }
                                    isUploading = false
                                }
                            } else {
                                onUploadError("Failed to read file as ArrayBuffer.")
                                isUploading = false
                            }
                        }
                        fileReader.onerror = {
                            onUploadError("Error reading file: ${fileReader.error?.message}")
                            isUploading = false
                        }
                        fileReader.readAsArrayBuffer(selectedFile!!)
                    } catch (e: Exception) {
                        onUploadError("File upload process failed: ${e.message}")
                        isUploading = false
                    }
                }
            },
            enabled = !isUploading && selectedFile != null && assetName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Uploading...")
            } else {
                Text("Upload Asset")
            }
        }
    }
}

@Composable
fun FileInput(onFileSelected: (File) -> Unit, modifier: Modifier = Modifier) {
    // This uses a raw HTML input element because Compose Web's TextField doesn't natively support file selection.
    // This approach is typical for handling file inputs in Compose Web.
    DisposableEffect(Unit) {
        val inputElement = org.w3c.dom.document.createElement("input") as HTMLInputElement
        inputElement.type = "file"
        inputElement.style.padding = "10px"
        inputElement.style.margin = "5px 0"
        inputElement.style.borderRadius = "5px"
        inputElement.style.border = "1px solid #ddd"
        inputElement.style.width = "calc(100% - 22px)"
        inputElement.style.boxSizing = "border-box"

        val onChange: (Event) -> Unit = {
            val files = inputElement.files
            if (files != null && files.length > 0) {
                onFileSelected(files[0]!!)
            }
        }

        inputElement.addEventListener("change", onChange)
        org.w3c.dom.document.body!!.appendChild(inputElement) // Temporarily add to body for interaction

        onDispose {
            inputElement.removeEventListener("change", onChange)
            org.w3c.dom.document.body!!.removeChild(inputElement)
        }
    }
    Text("Select file to upload:") // Label for the hidden file input
}
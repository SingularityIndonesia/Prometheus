package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.utils.LocalWindowController
import com.singularityuniverse.prometheus.utils.runInMainThread
import com.singularityuniverse.prometheus.utils.to
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import prometheus.composeapp.generated.resources.Res
import prometheus.composeapp.generated.resources.ic_back
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.ByteBuffer
import kotlin.random.Random

class CreateModelFormState {
    val modelName = mutableStateOf("")
    val neuronPerLayer = mutableStateOf("1000")
    val layerCount = mutableStateOf("1000")
    val initialBiasMode = mutableStateOf("Random")
    val determinedBias = mutableStateOf<Double?>(null)
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val totalParameter
        get() = run {
            val neuronsPerLayer = neuronPerLayer.value.toIntOrNull() ?: 0
            val layerCount = layerCount.value.toIntOrNull() ?: 0
            val totalParams = neuronsPerLayer * layerCount

            // Format number with spaces as thousand separators
            totalParams.toString()
                .reversed()
                .chunked(3)
                .joinToString(" ")
                .reversed()
        }

    suspend fun createModel(): Result<URI> {
        isLoading.value = true
        errorMessage.value = null

        return runCatching {
            withContext(Dispatchers.IO) {
                // Calculate total parameters
                val neuronsPerLayer = neuronPerLayer.value.toIntOrNull() ?: 0
                val layers = layerCount.value.toIntOrNull() ?: 0

                // Create directory structure
                val homeDir = System.getProperty("user.home")
                val prometheusDir = File(homeDir, "Prometheus")
                val projectDir = File(prometheusDir, modelName.value)

                // Check if project already exists
                if (projectDir.exists()) {
                    throw IllegalStateException("Project with name '${modelName.value}' already exists")
                }

                // Create directories
                if (!projectDir.mkdirs()) {
                    throw IOException("Failed to create project directory")
                }

                // Write bias values to binary file
                val biasFile = File(projectDir, "bias")
                biasFile.outputStream()
                    .buffered()
                    .use { outputStream ->
                        // Create buffer for one layer worth of floats (4 bytes per float)
                        val layerBuffer = ByteBuffer.allocate(neuronsPerLayer * 4)

                        repeat(layers) { layerIndex ->
                            layerBuffer.clear() // Reset buffer position for reuse

                            // Fill buffer with bias values for this layer
                            repeat(neuronsPerLayer) {
                                val biasValue = when (initialBiasMode.value) {
                                    "Determined" -> (determinedBias.value ?: 0.0).toFloat()
                                    else -> Random.nextDouble(-1.0, 1.0).toFloat()
                                }
                                layerBuffer.putFloat(biasValue)
                            }

                            // Write entire layer buffer to file at once
                            outputStream.write(
                                layerBuffer.array(),
                                0,
                                layerBuffer.position()
                            )
                        }
                    }

                // Write weight values to binary file
                val weightsFile = File(projectDir, "weights")
                weightsFile.outputStream()
                    .buffered()
                    .use { outputStream ->
                        // For each layer (except the last one), create connections to the next layer
                        repeat(layers - 1) { layerIndex ->
                            val currentLayerSize = neuronsPerLayer
                            val nextLayerSize = neuronsPerLayer

                            // Create buffer for weights between current and next layer
                            val weightBuffer = ByteBuffer.allocate(currentLayerSize * nextLayerSize * 4)

                            // Fill buffer with weight values
                            repeat(currentLayerSize * nextLayerSize) {
                                val weightValue = Random.nextDouble(-1.0, 1.0).toFloat()
                                weightBuffer.putFloat(weightValue)
                            }

                            // Write weights to file
                            outputStream.write(weightBuffer.array(), 0, weightBuffer.position())
                        }
                    }

                // Create metadata file with additional information
                val metadataFile = File(projectDir, "metadata.txt")
                metadataFile.bufferedWriter().use { writer ->
                    writer.appendLine("createdAt = ${System.currentTimeMillis()}")
                    writer.appendLine("version = 1.0")
                    writer.appendLine("modelName = ${modelName.value}")
                    writer.appendLine("nodesPerLayer = $neuronsPerLayer")
                    writer.appendLine("layerCount = $layers")
                    writer.appendLine("totalParameters = ${neuronsPerLayer * layers}")
                    writer.appendLine()
                    writer.appendLine("biasMode = ${initialBiasMode.value}")
                    if (initialBiasMode.value == "Determined") {
                        writer.appendLine("biasValue = ${determinedBias.value}")
                    }
                    writer.appendLine()
                }

                // Create Uri for the model file
                metadataFile.toURI()
            }
        }
            .onFailure { e ->
                errorMessage.value = e.message
                isLoading.value = false
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModelForm(
    state: CreateModelFormState,
    modifier: Modifier = Modifier,
    onReturn: (fileUri: URI?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val windowController = LocalWindowController.current

    // Check and adjust window size if needed
    LaunchedEffect(Unit) {
        windowController.setMinimumSize(500.dp to 600.dp)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Create Model")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onReturn.invoke(null)
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_back),
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
        ) {

            // Error message display
            state.errorMessage.value?.let { message ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { state.errorMessage.value = null }
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
                Spacer(Modifier.size(16.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Model Name")
                        },
                        value = state.modelName.value,
                        onValueChange = { value ->
                            state.modelName.value = value
                        },
                        isError = state.modelName.value.isBlank(),
                        supportingText = if (state.modelName.value.isBlank()) {
                            { Text("Model name is required") }
                        } else null
                    )
                }

                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Neurons per Layer")
                        },
                        value = state.neuronPerLayer.value,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { value ->
                            // Only allow numeric input
                            if (value.isEmpty() || value.all { it.isDigit() }) {
                                state.neuronPerLayer.value = value
                            }
                        },
                        isError = state.neuronPerLayer.value.toIntOrNull() == null || state.neuronPerLayer.value.toInt() <= 0,
                        supportingText = {
                            val neurons = state.neuronPerLayer.value.toIntOrNull()
                            when {
                                neurons == null -> Text("Please enter a valid number")
                                neurons <= 0 -> Text("Number must be greater than 0")
                                else -> Text("Valid")
                            }
                        }
                    )
                }

                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Layer Count")
                        },
                        value = state.layerCount.value,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { value ->
                            // Only allow numeric input
                            if (value.isEmpty() || value.all { it.isDigit() }) {
                                state.layerCount.value = value
                            }
                        },
                        isError = state.layerCount.value.toIntOrNull() == null || state.layerCount.value.toInt() <= 1,
                        supportingText = {
                            val layers = state.layerCount.value.toIntOrNull()
                            when {
                                layers == null -> Text("Please enter a valid number")
                                layers <= 1 -> Text("Must have at least 2 layers")
                                else -> Text("Total Parameters: ${state.totalParameter}")
                            }
                        }
                    )
                }

                item {
                    var expanded by remember { mutableStateOf(false) }
                    val biasOptions = listOf("Random", "Determined")

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            readOnly = true,
                            value = state.initialBiasMode.value,
                            onValueChange = {},
                            label = { Text("Initial Bias") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            supportingText = {
                                Text(
                                    when (state.initialBiasMode.value) {
                                        "Random" -> "Bias values will be randomized between -1 and 1"
                                        "Determined" -> "All bias values will be set to the specified value"
                                        else -> ""
                                    }
                                )
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            biasOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        state.initialBiasMode.value = option
                                        expanded = false
                                        // Reset determined bias when switching modes
                                        if (option == "Random") {
                                            state.determinedBias.value = null
                                        }
                                        if (option == "Determined") {
                                            state.determinedBias.value = 0.0
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (state.initialBiasMode.value == "Determined") {
                    item {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Bias Value") },
                            value = state.determinedBias.value?.toString() ?: "",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            onValueChange = { input ->
                                state.determinedBias.value = input.toDoubleOrNull()
                            },
                            isError = state.determinedBias.value == null && state.initialBiasMode.value == "Determined",
                            supportingText = {
                                if (state.determinedBias.value == null && state.initialBiasMode.value == "Determined") {
                                    Text("Please enter a valid decimal number")
                                } else {
                                    Text("Recommended range: -1.0 to 1.0")
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.size(16.dp))

            Button(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                enabled = !state.isLoading.value &&
                        state.modelName.value.isNotBlank() &&
                        state.neuronPerLayer.value.toIntOrNull() != null &&
                        state.neuronPerLayer.value.toInt() > 0 &&
                        state.layerCount.value.toIntOrNull() != null &&
                        state.layerCount.value.toInt() > 1 &&
                        (state.initialBiasMode.value != "Determined" || state.determinedBias.value != null),
                onClick = {
                    scope.launch {
                        state.createModel()
                            .onSuccess { fileUri ->
                                runInMainThread { onReturn(fileUri) }
                            }
                    }
                }
            ) {
                if (state.isLoading.value) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Creating Model...")
                    }
                } else {
                    Text("Create Model")
                }
            }

            Spacer(Modifier.size(16.dp))
        }
    }
}
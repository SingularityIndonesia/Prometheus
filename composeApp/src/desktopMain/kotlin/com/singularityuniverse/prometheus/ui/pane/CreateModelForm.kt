package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.ui.component.CreateModelButton
import com.singularityuniverse.prometheus.ui.component.CreateModelFormFields
import com.singularityuniverse.prometheus.ui.component.ErrorMessageCard
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
                ErrorMessageCard(
                    message = message,
                    onDismiss = { state.errorMessage.value = null },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.size(16.dp))
            }

            CreateModelFormFields(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = state
            )

            Spacer(Modifier.size(16.dp))

            CreateModelButton(
                isLoading = state.isLoading.value,
                isEnabled = !state.isLoading.value &&
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
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.size(16.dp))
        }
    }
}

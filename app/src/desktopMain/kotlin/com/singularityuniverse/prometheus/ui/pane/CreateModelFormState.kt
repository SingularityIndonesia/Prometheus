package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.ByteBuffer
import kotlin.random.Random

class CreateModelFormState {
    var modelName by mutableStateOf("")
    var neuronsPerLayer by mutableStateOf("1000")
    var layerCount by mutableStateOf("1000")
    var initialBiasMode by mutableStateOf("Random")
    var determinedBias by mutableStateOf<Float?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    val totalParameter
        get() = run {
            val neuronsPerLayer = neuronsPerLayer.toIntOrNull() ?: 0
            val layerCount = layerCount.toIntOrNull() ?: 0
            val totalParams = neuronsPerLayer * layerCount

            // Format number with spaces as thousand separators
            totalParams.toString()
                .reversed()
                .chunked(3)
                .joinToString(" ")
                .reversed()
        }

    val formIsValid
        get() = !isLoading &&
                modelName.isNotBlank() &&
                neuronsPerLayer.toIntOrNull() != null &&
                neuronsPerLayer.toInt() > 0 &&
                layerCount.toIntOrNull() != null &&
                layerCount.toInt() > 1 &&
                (initialBiasMode != "Determined" || determinedBias != null)

    suspend fun createModel(): Result<URI> {
        isLoading = true
        errorMessage = null

        return runCatching {
            withContext(Dispatchers.IO) {
                // Calculate total parameters
                val neuronsPerLayer = neuronsPerLayer.toIntOrNull() ?: 0
                val layers = layerCount.toIntOrNull() ?: 0

                // Create directory structure
                val homeDir = System.getProperty("user.home")
                val prometheusDir = File(homeDir, "Prometheus")
                val projectDir = File(prometheusDir, modelName)

                // Check if project already exists
                if (projectDir.exists()) {
                    throw IllegalStateException("Project with name '${modelName}' already exists")
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
                                val biasValue = when (initialBiasMode) {
                                    "Determined" -> (determinedBias ?: 0.0).toFloat()
                                    else -> Random.Default.nextFloat()
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
                                val weightValue = Random.Default.nextFloat()
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
                    writer.appendLine("modelName = $modelName")
                    writer.appendLine("neuronsPerLayer = $neuronsPerLayer")
                    writer.appendLine("layerCount = $layers")
                    writer.appendLine("totalParameters = ${neuronsPerLayer * layers}")
                    writer.appendLine("biasMode = $initialBiasMode")
                    if (initialBiasMode == "Determined") {
                        writer.appendLine("biasValue = $determinedBias")
                    }
                    writer.appendLine()
                }

                // Create Uri for the model file
                metadataFile.toURI()
            }
        }
            .onFailure { e ->
                errorMessage = e.message
                isLoading = false
            }
    }
}
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
import kotlin.math.pow
import kotlin.random.Random.Default.nextDouble

class CreateModelFormState {
    var modelName by mutableStateOf("")
    var neuronsPerLayer by mutableStateOf("1000")
    var layerCount by mutableStateOf("10")
    var initialBiasMode by mutableStateOf("Random")
    var determinedBias by mutableStateOf<Float?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    val totalParameter
        get() = run {
            val neuronsPerLayer = neuronsPerLayer.toIntOrNull() ?: 0
            val layerCount = layerCount.toIntOrNull() ?: 0
            val layerSizes = List(layerCount) { neuronsPerLayer }
            val totalParams = calculateTotalParameter(layerSizes)
            totalParams.toString()
        }

    fun calculateTotalParameter(layerSizes: List<Int>): Long {
        var totalParameter = 0L

        for (i in 0 until layerSizes.size - 1) {
            val from = layerSizes[i]
            val to = layerSizes[i + 1]
            val weights = from.toLong() * to
            val biases = to.toLong()
            totalParameter += weights + biases
        }

        return totalParameter
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
                    .use { outputStream ->
                        // Create buffer for one layer worth of floats (4 bytes per float)
                        val layerBuffer = ByteBuffer.allocate(neuronsPerLayer * 4)

                        repeat(layers) { layerIndex ->
                            layerBuffer.clear() // Reset buffer position for reuse

                            // Fill buffer with bias values for this layer
                            repeat(neuronsPerLayer) {
                                val biasValue = when (initialBiasMode) {
                                    "Determined" -> determinedBias ?: 0f
                                    else -> nextDouble(-1.0, 1.0).toFloat()
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
                    .use { outputStream ->
                        // For each layer (except the last one), create connections to the next layer
                        repeat(layers - 1) { layerIndex ->
                            val currentLayerSize = neuronsPerLayer
                            val nextLayerSize = neuronsPerLayer

                            // Create buffer for weights between current and next layer
                            val weightBuffer = ByteBuffer.allocate(currentLayerSize * nextLayerSize * 4)

                            // Fill buffer with weight values
                            repeat(currentLayerSize * nextLayerSize) {
                                val weightValue = nextDouble(-1.0, 1.0).toFloat()
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
                    writer.appendLine("totalParameters = $totalParameter")
                    writer.appendLine("biasMode = $initialBiasMode")
                    if (initialBiasMode == "Determined") {
                        writer.appendLine("biasValue = $determinedBias")
                    }
                    writer.appendLine()
                }

                // Create Uri for the model file
                metadataFile.toURI()
            }
        }.onFailure { e ->
            errorMessage = e.message ?: e.cause?.message ?: e::class.qualifiedName
            isLoading = false
        }
    }
}
package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.entity.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun Landscape(project: Project) {
    val metadata = project.metadata
    val neuronsPerLayer = metadata["neuronsPerLayer"]?.toInt() ?: 0
    val layerCount = metadata["layerCount"]?.toInt() ?: 0
    
    var biasData by remember { mutableStateOf<List<Float>?>(null) }
    var weightData by remember { mutableStateOf<List<Float>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(project) {
        withContext(Dispatchers.IO) {
            try {
                // Load bias data
                val biasArray = mutableListOf<Float>()
                project.biasFile.inputStream().buffered().use { inputStream ->
                    val buffer = ByteArray(neuronsPerLayer * layerCount * 4)
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead)
                        repeat(bytesRead / 4) {
                            biasArray.add(byteBuffer.getFloat())
                        }
                    }
                }
                
                // Load weight data (sample for visualization)
                val weightArray = mutableListOf<Float>()
                if (project.weightFile.exists()) {
                    project.weightFile.inputStream().buffered().use { inputStream ->
                        val buffer = ByteArray(min(1024 * 1024, inputStream.available())) // Limit to 1MB for performance
                        val bytesRead = inputStream.read(buffer)
                        if (bytesRead > 0) {
                            val byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead)
                            repeat(bytesRead / 4) {
                                weightArray.add(byteBuffer.getFloat())
                            }
                        }
                    }
                }
                
                biasData = biasArray
                weightData = weightArray
                isLoading = false
            } catch (e: Exception) {
                error = "Failed to load model data: ${e.message}"
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Neural Network Landscape",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Model: ${metadata["modelName"] ?: "Unknown"}")
                Text("Layers: $layerCount")
                Text("Neurons per Layer: $neuronsPerLayer")
                Text("Total Parameters: ${metadata["totalParameters"] ?: "Unknown"}")
                Text("Bias Mode: ${metadata["biasMode"] ?: "Unknown"}")
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading model data...")
                }
            }
            
            error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            biasData != null -> {
                // Network Architecture Visualization
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Network Architecture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        NetworkArchitectureCanvas(
                            layerCount = layerCount,
                            neuronsPerLayer = neuronsPerLayer,
                            biasData = biasData!!,
                            modifier = Modifier.fillMaxWidth().height(400.dp)
                        )
                    }
                }

                // Bias Distribution Visualization
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Bias Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        BiasDistributionCanvas(
                            biasData = biasData!!,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }
                }

                // Statistics
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        ModelStatistics(
                            biasData = biasData!!,
                            weightData = weightData
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkArchitectureCanvas(
    layerCount: Int,
    neuronsPerLayer: Int,
    biasData: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        if (layerCount == 0 || neuronsPerLayer == 0) return@Canvas
        
        val layerSpacing = canvasWidth / (layerCount + 1)
        val neuronSpacing = canvasHeight / (neuronsPerLayer + 1)
        val nodeRadius = min(layerSpacing, neuronSpacing) * 0.1f
        
        // Find bias range for color scaling
        val maxAbsBias = biasData.maxOfOrNull { abs(it) } ?: 1f
        
        // Draw connections between layers
        for (layer in 0 until layerCount - 1) {
            val x1 = layerSpacing * (layer + 1)
            val x2 = layerSpacing * (layer + 2)
            
            for (neuron in 0 until neuronsPerLayer) {
                val y = neuronSpacing * (neuron + 1)
                
                // Draw connections to next layer (sample a few)
                val connectionsToShow = min(neuronsPerLayer, 10)
                for (nextNeuron in 0 until connectionsToShow) {
                    val nextY = neuronSpacing * (nextNeuron + 1)
                    
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(x1 + nodeRadius, y),
                        end = Offset(x2 - nodeRadius, nextY),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }
        
        // Draw neurons with bias coloring
        for (layer in 0 until layerCount) {
            val x = layerSpacing * (layer + 1)
            
            for (neuron in 0 until neuronsPerLayer) {
                val y = neuronSpacing * (neuron + 1)
                val biasIndex = layer * neuronsPerLayer + neuron
                
                if (biasIndex < biasData.size) {
                    val bias = biasData[biasIndex]
                    val normalizedBias = bias / maxAbsBias
                    val color = when {
                        normalizedBias > 0 -> Color.Red.copy(alpha = normalizedBias.coerceIn(0.3f, 1f))
                        normalizedBias < 0 -> Color.Blue.copy(alpha = (-normalizedBias).coerceIn(0.3f, 1f))
                        else -> Color.Gray
                    }
                    
                    drawCircle(
                        color = color,
                        radius = nodeRadius,
                        center = Offset(x, y)
                    )
                    
                    // Draw outline
                    drawCircle(
                        color = Color.Black,
                        radius = nodeRadius,
                        center = Offset(x, y),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun BiasDistributionCanvas(
    biasData: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        if (biasData.isEmpty()) return@Canvas
        
        // Create histogram
        val binCount = 50
        val minBias = biasData.minOrNull() ?: 0f
        val maxBias = biasData.maxOrNull() ?: 0f
        val range = maxBias - minBias
        
        if (range == 0f) return@Canvas
        
        val bins = IntArray(binCount)
        biasData.forEach { bias ->
            val binIndex = ((bias - minBias) / range * (binCount - 1)).toInt().coerceIn(0, binCount - 1)
            bins[binIndex]++
        }
        
        val maxCount = bins.maxOrNull() ?: 1
        val binWidth = canvasWidth / binCount
        
        // Draw histogram bars
        bins.forEachIndexed { index, count ->
            val barHeight = (count.toFloat() / maxCount) * canvasHeight * 0.8f
            val x = index * binWidth
            val y = canvasHeight - barHeight
            
            drawRect(
                color = Color.Blue.copy(alpha = 0.7f),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(binWidth * 0.8f, barHeight)
            )
        }
    }
}

@Composable
private fun ModelStatistics(
    biasData: List<Float>,
    weightData: List<Float>?
) {
    Column {
        Text("Bias Statistics:")
        if (biasData.isNotEmpty()) {
            val mean = biasData.average()
            val variance = biasData.map { (it - mean) * (it - mean) }.average()
            val stdDev = sqrt(variance)
            
            Text("  Mean: ${"%.6f".format(mean)}")
            Text("  Std Dev: ${"%.6f".format(stdDev)}")
            Text("  Min: ${"%.6f".format(biasData.minOrNull() ?: 0f)}")
            Text("  Max: ${"%.6f".format(biasData.maxOrNull() ?: 0f)}")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Weight Statistics:")
        if (weightData != null && weightData.isNotEmpty()) {
            val mean = weightData.average()
            val variance = weightData.map { (it - mean) * (it - mean) }.average()
            val stdDev = sqrt(variance)
            
            Text("  Mean: ${"%.6f".format(mean)}")
            Text("  Std Dev: ${"%.6f".format(stdDev)}")
            Text("  Min: ${"%.6f".format(weightData.minOrNull() ?: 0f)}")
            Text("  Max: ${"%.6f".format(weightData.maxOrNull() ?: 0f)}")
            Text("  Sample Size: ${weightData.size} (limited for performance)")
        } else {
            Text("  No weight data available")
        }
    }
}

package com.singularityuniverse.prometheus.utils

import com.singularityuniverse.prometheus.entity.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.sqrt

object GnuplotGenerator {

    suspend fun generateGnuplotScript(project: Project): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val metadata = project.metadata
                val neuronsPerLayer = metadata["neuronsPerLayer"]?.toInt() ?: 0
                val layerCount = metadata["layerCount"]?.toInt() ?: 0

                // Generate data files
                val projectPath = File(project.uri)
                val gnuPlotDir = File(projectPath, "gnuplot/")
                if (!gnuPlotDir.exists() || !gnuPlotDir.isDirectory) {
                    gnuPlotDir.mkdir()
                }

                // Read bias data from file and create data files
                val biasStats = biasStats(
                    biasFile = project.biasFile,
                )

                // Fix palette range to ensure monotonic gradient
                val paletteMin = biasStats.min.toDouble()
                val paletteMax = biasStats.max.toDouble()
                val paletteRange = paletteMax - paletteMin

                // Ensure we have a valid range for the palette
                val (effectiveMin, effectiveMax) = if (paletteRange < 1e-10) {
                    // If range is too small, create a small artificial range
                    val center = (paletteMin + paletteMax) / 2
                    Pair(center - 0.1, center + 0.1)
                } else {
                    Pair(paletteMin, paletteMax)
                }

                // Generate gnuplot script
                val gnuplotScript = """
                    #!/usr/bin/env gnuplot
                    
                    # Neural Network Landscape Visualization Script
                    # Generated for model: ${metadata["modelName"] ?: "Unknown"}
                    # Layers: $layerCount, Neurons per Layer: $neuronsPerLayer
                    
                    layers=$layerCount
                    neurons=$neuronsPerLayer
                    
                    # Set terminal and output for heatmap
                    set terminal pngcairo enhanced font "Arial,12" size 1200,800
                    set output 'bias_heatmap.png'
                    
                    # Bias Heatmap
                    set title "Neural Network Bias Heatmap"
                    set xlabel "Neuron Index"
                    set ylabel "Layer"
                    set cbrange [${"%.6f".format(effectiveMin)}:${"%.6f".format(effectiveMax)}]
                    set palette defined (${"%.6f".format(effectiveMin)} "blue", ${"%.6f".format((effectiveMin + effectiveMax) / 2)} "white", ${
                    "%.6f".format(
                        effectiveMax
                    )
                } "red")
                    set view map
                    set pm3d interpolate 0,0
                    unset grid
                    
                    splot '../bias' binary array=(neurons,layers) format='%float32' endian=big with pm3d title "Bias Values" 
                    
                    # Reset all settings for surface plot
                    reset
                    
                    # Generate detailed bias surface plot
                    set terminal pngcairo enhanced font "Arial,12" size 1200,800
                    set output 'bias_surface.png'
                    set title "Neural Network Bias Surface"
                    set xlabel "Neuron Index"
                    set ylabel "Layer"
                    set zlabel "Bias Value"
                    set cbrange [${"%.6f".format(effectiveMin)}:${"%.6f".format(effectiveMax)}]
                    set palette defined (${"%.6f".format(effectiveMin)} "blue", ${"%.6f".format((effectiveMin + effectiveMax) / 2)} "white", ${
                    "%.6f".format(
                        effectiveMax
                    )
                } "red")
                    set pm3d
                    set view 60,30
                    set grid
                    
                    splot '../bias' binary array=(neurons,layers) format='%float32' endian=big with pm3d title "Bias Surface"
                    
                    print "Neural network visualization complete!"
                    print "Generated files:"
                    print "  - bias_heatmap.png (2D bias heatmap)"
                    print "  - bias_surface.png (3D bias surface)"
                    print "Data files:"
                    print "  - bias_data.txt (bias values)"
                    print "  - bias_matrix.txt (bias matrix for heatmap)"
                """.trimIndent()

                // Write gnuplot script
                val scriptFile = File(gnuPlotDir, "gnuplot-script")
                scriptFile.writeText(gnuplotScript)
                scriptFile.setExecutable(true)

                true

            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private data class BiasStats(
        val mean: Double,
        val stdDev: Double,
        val min: Float,
        val max: Float,
        val count: Int
    )

    private fun biasStats(
        biasFile: File,
    ): BiasStats {
        // Stream process bias file to avoid loading all data into memory
        // TODO: do not buffer, calculate everything directly
        val biasData = mutableListOf<Float>()
        var biasSum = 0.0
        var biasMin = Float.MAX_VALUE
        var biasMax = Float.MIN_VALUE

        biasFile.inputStream().buffered().use { inputStream ->
            val buffer = ByteArray(8192) // 8KB buffer
            val byteBuffer = ByteBuffer.allocate(8192)

            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                byteBuffer.clear()
                byteBuffer.put(buffer, 0, bytesRead)
                byteBuffer.flip()

                while (byteBuffer.remaining() >= 4) {
                    val value = byteBuffer.getFloat()
                    biasData.add(value)
                    biasSum += value
                    if (value < biasMin) biasMin = value
                    if (value > biasMax) biasMax = value
                }
            }
        }

        val biasMean = if (biasData.isNotEmpty()) biasSum / biasData.size else 0.0
        val biasVariance = if (biasData.isNotEmpty()) {
            biasData.map { (it - biasMean) * (it - biasMean) }.average()
        } else 0.0
        val biasStdDev = sqrt(biasVariance)

        val stats = BiasStats(
            mean = biasMean,
            stdDev = biasStdDev,
            min = if (biasData.isNotEmpty()) biasMin else 0f,
            max = if (biasData.isNotEmpty()) biasMax else 0f,
            count = biasData.size
        )

        return stats
    }
}

package com.singularityuniverse.prometheus.utils

import com.singularityuniverse.prometheus.entity.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.min
import kotlin.math.sqrt

object GnuplotGenerator {

    suspend fun generateGnuplotScript(project: Project): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val metadata = project.metadata
                val neuronsPerLayer = metadata["neuronsPerLayer"]?.toInt() ?: 0
                val layerCount = metadata["layerCount"]?.toInt() ?: 0

                // Generate data files
                val projectPath = File(project.path)
                val gnuPlotDir = File(projectPath, "gnuplot/")
                if (!gnuPlotDir.exists() || !gnuPlotDir.isDirectory) {
                    gnuPlotDir.mkdir()
                }

                // Read bias data from file and create data files
                val (biasDataFile, biasStats) = createBiasDataFiles(
                    project.biasFile,
                    gnuPlotDir,
                    neuronsPerLayer,
                    layerCount
                )

                // Read weight data from file if available
                val weightStats = if (project.weightFile.exists()) {
                    createWeightDataFile(project.weightFile, gnuPlotDir)
                } else null

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
                    
                    splot 'bias_matrix.txt' matrix with pm3d title "Bias Values"
                    
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
                    
                    splot 'bias_matrix.txt' matrix with pm3d title "Bias Surface"
                    
                    print "Neural network visualization complete!"
                    print "Generated files:"
                    print "  - bias_heatmap.png (2D bias heatmap)"
                    print "  - bias_surface.png (3D bias surface)"
                    print "Data files:"
                    print "  - bias_data.txt (bias values)"
                    print "  - bias_matrix.txt (bias matrix for heatmap)"
                    ${if (weightStats != null) "print \"  - weight_data.txt (weight values)\"" else ""}
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

    private data class WeightStats(
        val mean: Double,
        val stdDev: Double,
        val min: Float,
        val max: Float,
        val sampleSize: Int
    )

    private fun createBiasDataFiles(
        biasFile: File,
        outputDir: File,
        neuronsPerLayer: Int,
        layerCount: Int
    ): Pair<File, BiasStats> {
        val biasDataFile = File(outputDir, "bias_data.txt")
        val biasMatrixFile = File(outputDir, "bias_matrix.txt")

        // Stream process bias file to avoid loading all data into memory
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

        // Write bias data file
        biasDataFile.writeText(biasData.joinToString("\n"))

        // Create bias matrix file for heatmap
        val biasMatrix = StringBuilder()
        for (layer in 0 until layerCount) {
            val rowData = mutableListOf<String>()
            for (neuron in 0 until neuronsPerLayer) {
                val biasIndex = layer * neuronsPerLayer + neuron
                if (biasIndex < biasData.size) {
                    rowData.add(biasData[biasIndex].toString())
                } else {
                    rowData.add("0.0")
                }
            }
            biasMatrix.appendLine(rowData.joinToString(" "))
        }
        biasMatrixFile.writeText(biasMatrix.toString())

        val stats = BiasStats(
            mean = biasMean,
            stdDev = biasStdDev,
            min = if (biasData.isNotEmpty()) biasMin else 0f,
            max = if (biasData.isNotEmpty()) biasMax else 0f,
            count = biasData.size
        )

        return Pair(biasDataFile, stats)
    }

    private fun createWeightDataFile(weightFile: File, outputDir: File): WeightStats? {
        try {
            val weightDataFile = File(outputDir, "weight_data.txt")
            val maxSampleSize = 100000 // Limit sample size for performance
            val weightSample = mutableListOf<Float>()

            weightFile.inputStream().buffered().use { inputStream ->
                val maxBytes = min(maxSampleSize * 4, inputStream.available())
                val buffer = ByteArray(maxBytes)
                val bytesRead = inputStream.read(buffer)

                if (bytesRead > 0) {
                    val byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead)
                    while (byteBuffer.remaining() >= 4 && weightSample.size < maxSampleSize) {
                        weightSample.add(byteBuffer.getFloat())
                    }
                }
            }

            if (weightSample.isNotEmpty()) {
                // Write sampled weight data
                weightDataFile.writeText(weightSample.joinToString("\n"))

                // Calculate statistics
                val weightMean = weightSample.average()
                val weightVariance = weightSample.map { (it - weightMean) * (it - weightMean) }.average()
                val weightStdDev = sqrt(weightVariance)
                val weightMin = weightSample.minOrNull() ?: 0f
                val weightMax = weightSample.maxOrNull() ?: 0f

                return WeightStats(
                    mean = weightMean,
                    stdDev = weightStdDev,
                    min = weightMin,
                    max = weightMax,
                    sampleSize = weightSample.size
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}

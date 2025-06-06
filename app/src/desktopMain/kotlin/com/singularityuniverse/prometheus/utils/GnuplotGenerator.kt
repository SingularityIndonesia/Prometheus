package com.singularityuniverse.prometheus.utils

import com.singularityuniverse.prometheus.entity.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sqrt

object GnuplotGenerator {

    suspend fun generateGnuplotScript(
        project: Project,
        biasData: List<Float>,
        weightData: List<Float>?
    ): Boolean {
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

                // Create bias data file
                val biasDataFile = File(gnuPlotDir, "bias_data.txt")
                biasDataFile.writeText(biasData.joinToString("\n"))

                // Create bias matrix file for heatmap
                val biasMatrixFile = File(gnuPlotDir, "bias_matrix.txt")
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

                // Create weight data file if available
                if (weightData != null && weightData.isNotEmpty()) {
                    val weightDataFile = File(gnuPlotDir, "weight_data.txt")
                    weightDataFile.writeText(weightData.joinToString("\n"))
                }

                // Calculate statistics
                val biasMean = biasData.average()
                val biasVariance = biasData.map { (it - biasMean) * (it - biasMean) }.average()
                val biasStdDev = sqrt(biasVariance)
                val biasMin = biasData.minOrNull() ?: 0f
                val biasMax = biasData.maxOrNull() ?: 0f

                // Fix palette range to ensure monotonic gradient
                val paletteMin = biasMin.toDouble()
                val paletteMax = biasMax.toDouble()
                val paletteRange = paletteMax - paletteMin

                // Ensure we have a valid range for the palette
                val (effectiveMin, effectiveMax) = if (paletteRange < 1e-10) {
                    // If range is too small, create a small artificial range
                    val center = (paletteMin + paletteMax) / 2
                    Pair(center - 0.1, center + 0.1)
                } else {
                    Pair(paletteMin, paletteMax)
                }

                val weightStats = if (weightData != null && weightData.isNotEmpty()) {
                    val weightMean = weightData.average()
                    val weightVariance = weightData.map { (it - weightMean) * (it - weightMean) }.average()
                    val weightStdDev = sqrt(weightVariance)
                    val weightMin = weightData.minOrNull() ?: 0f
                    val weightMax = weightData.maxOrNull() ?: 0f
                    mapOf(
                        "mean" to weightMean,
                        "stddev" to weightStdDev,
                        "min" to weightMin,
                        "max" to weightMax
                    )
                } else null

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
}
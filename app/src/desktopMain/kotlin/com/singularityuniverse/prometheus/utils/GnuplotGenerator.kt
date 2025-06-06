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
                    
                    # Set terminal and output
                    set terminal pngcairo enhanced font "Arial,12" size 1600,1200
                    set output 'neural_network_landscape.png'
                    
                    # Set up multiplot layout
                    set multiplot layout 2,2 title "Neural Network Landscape - ${metadata["modelName"] ?: "Unknown"}" font ",16"
                    
                    # Plot 1: Bias Distribution Histogram
                    set title "Bias Distribution"
                    set xlabel "Bias Value"
                    set ylabel "Frequency"
                    set style fill solid 0.5
                    set boxwidth 0.02 relative
                    set grid
                    set auto x
                    set auto y
                    
                    plot 'bias_data.txt' using 1:(1) smooth freq with boxes lc rgb "red" title "Bias Distribution"
                    
                    # Plot 2: Bias Heatmap
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
                    
                    # Plot 3: Weight Distribution (if available)
                    set title "Weight Distribution"
                    set xlabel "Weight Value"
                    set ylabel "Frequency"
                    set style fill solid 0.3
                    set cbrange [*:*]
                    unset view
                    unset pm3d
                    set grid
                    set auto x
                    set auto y
                    
                    ${
                    if (weightStats != null) {
                        "plot 'weight_data.txt' using 1:(1) smooth freq with boxes lc rgb \"green\" title \"Weight Distribution\""
                    } else {
                        "set label \"No Weight Data Available\" at graph 0.5,0.5 center font \",16\"\nplot NaN notitle"
                    }
                }
                    
                    # Plot 4: Model Statistics Summary
                    set title "Model Statistics"
                    unset xlabel
                    unset ylabel
                    unset border
                    unset tics
                    unset grid
                    unset key
                    unset cbrange
                    unset palette
                    unset pm3d
                    unset view
                    clear
                    
                    # Statistics labels
                    set label 1 "Model: ${metadata["modelName"] ?: "Unknown"}" at 0.05, 0.95 font ",14"
                    set label 2 "Layers: $layerCount" at 0.05, 0.90 font ",12"
                    set label 3 "Neurons per Layer: $neuronsPerLayer" at 0.05, 0.85 font ",12"
                    set label 4 "Total Parameters: ${metadata["totalParameters"] ?: "Unknown"}" at 0.05, 0.80 font ",12"
                    
                    set label 5 "Bias Statistics:" at 0.05, 0.70 font ",14"
                    set label 6 "  Mean: ${"%.6f".format(biasMean)}" at 0.05, 0.65 font ",12"
                    set label 7 "  Std Dev: ${"%.6f".format(biasStdDev)}" at 0.05, 0.60 font ",12"
                    set label 8 "  Min: ${"%.6f".format(biasMin)}" at 0.05, 0.55 font ",12"
                    set label 9 "  Max: ${"%.6f".format(biasMax)}" at 0.05, 0.50 font ",12"
                    
                    ${
                    if (weightStats != null) {
                        """set label 10 "Weight Statistics:" at 0.05, 0.40 font ",14"
                    set label 11 "  Mean: ${"%.6f".format(weightStats["mean"])}" at 0.05, 0.35 font ",12"
                    set label 12 "  Std Dev: ${"%.6f".format(weightStats["stddev"])}" at 0.05, 0.30 font ",12"
                    set label 13 "  Min: ${"%.6f".format(weightStats["min"])}" at 0.05, 0.25 font ",12"
                    set label 14 "  Max: ${"%.6f".format(weightStats["max"])}" at 0.05, 0.20 font ",12"
                    set label 15 "  Sample Size: ${weightData?.size ?: 0}" at 0.05, 0.15 font ",12" """
                    } else {
                        """set label 10 "Weight Statistics:" at 0.05, 0.40 font ",14"
                    set label 11 "  No weight data available" at 0.05, 0.35 font ",12" """
                    }
                }
                    
                    plot [-1:1] [-1:1] NaN notitle
                    
                    unset multiplot
                    
                    # Reset all settings for next plot
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
                    print "  - neural_network_landscape.png (main overview)"
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
package com.singularityuniverse.prometheus.ui.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.singularityuniverse.prometheus.entity.Project
import com.singularityuniverse.prometheus.utils.GnuplotGenerator
import kotlinx.coroutines.*
import org.jetbrains.skia.Image
import java.io.File
import java.util.concurrent.TimeUnit

class LandscapeState(val project: Project) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val surfaceImageFile = File(File(project.uri), "gnuplot/bias_surface.png")
    private val heatmapImageFile = File(File(project.uri), "gnuplot/bias_heatmap.png")

    var isLoading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var heatmapImage by mutableStateOf<ImageBitmap?>(null)
    var surfaceImage by mutableStateOf<ImageBitmap?>(null)

    init {
        scope.launch {
            reload()
        }
    }

    suspend fun generatePlotScript(): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val success = GnuplotGenerator.generateGnuplotScript(project)
            if (!success) {
                error = "Failed to generate gnuplot script"
            }
        }.onFailure { e ->
            error = "Error generating script: ${e.message}"
        }
    }

    suspend fun executePlotScript(): Unit = withContext(Dispatchers.IO) {
        runCatching {
            val gnuplotDir = File(File(project.uri), "gnuplot")
            val scriptFile = File(gnuplotDir, "gnuplot-script")
            if (!scriptFile.exists()) {
                error = "Gnuplot script not found at: ${scriptFile.absolutePath}"
                return@runCatching
            }

            // Make the script executable on Unix-like systems
            if (!System.getProperty("os.name").lowercase().contains("windows")) {
                ProcessBuilder("chmod", "+x", scriptFile.absolutePath)
                    .start()
                    .waitFor()
            }

            // Execute the gnuplot script
            val processBuilder = if (System.getProperty("os.name").lowercase().contains("windows")) {
                ProcessBuilder("cmd", "/c", scriptFile.absolutePath)
            } else {
                ProcessBuilder("bash", "-c", "cd \"${gnuplotDir.absolutePath}\" && ./gnuplot-script")
            }

            processBuilder.directory()
            val process = processBuilder.start()

            // Wait for completion with timeout
            val completed = process.waitFor(1, TimeUnit.MINUTES)

            if (!completed) {
                process.destroyForcibly()
                error = "Gnuplot execution timed out after 1 minute"
            } else if (process.exitValue() != 0) {
                val errorOutput = process.errorStream.bufferedReader().readText()
                error = "Gnuplot execution failed with exit code ${process.exitValue()}: $errorOutput"
            } else {
                // Success - check if output files were generated in the gnuplot directory
                val outputFiles = gnuplotDir.listFiles { file ->
                    file.extension.lowercase() in listOf("png", "jpg", "jpeg", "svg", "pdf", "eps")
                }

                if (outputFiles?.isNotEmpty() == true) {
                    error = null // Clear any previous errors
                    println("Gnuplot execution completed successfully. Generated files: ${outputFiles.map { it.name }}")
                } else {
                    error = "Gnuplot completed but no output files were generated"
                }
            }
        }.onFailure { e ->
            error = "Failed to execute gnuplot script: ${e.message}"
        }
    }

    suspend fun reloadImages() = withContext(Dispatchers.IO) {
        runCatching {
            check(heatmapImageFile.exists() && surfaceImageFile.exists())

            // Load heatmap image if it exists
            val job1 = async {
                while (heatmapImageFile.length() == 0L) {
                    delay(100)
                }
                val heatmapBytes = heatmapImageFile.readBytes()
                val heatmapSkiaImage = Image.makeFromEncoded(heatmapBytes)
                heatmapImage = heatmapSkiaImage.toComposeImageBitmap()
            }

            // Load surface image if it exists
            val job2 = async {
                while (surfaceImageFile.length() == 0L) {
                    delay(100)
                }
                val surfaceBytes = surfaceImageFile.readBytes()
                val surfaceSkiaImage = Image.makeFromEncoded(surfaceBytes)
                surfaceImage = surfaceSkiaImage.toComposeImageBitmap()
            }

            awaitAll(job1, job2)
        }.onFailure { e ->
            error = "Failed to load images: ${e.message}"
        }
    }

    suspend fun reload(hard: Boolean = false) = withContext(Dispatchers.IO) {
        isLoading = true
        // Validate that required files exist
        if (!project.biasFile.exists()) {
            error = "Bias file not found: ${project.biasFile.absolutePath}"
            isLoading = false

            return@withContext
        }

        runCatching {
            if (!surfaceImageFile.exists() || !heatmapImageFile.exists() || hard) {
                generatePlotScript()
                executePlotScript()
            }
            reloadImages()
        }.onFailure {
            error = "Fail to generate images $it"
            isLoading = false
            return@withContext
        }

        isLoading = false
    }
}
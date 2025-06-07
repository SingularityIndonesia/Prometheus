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
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

class LandscapeState(val project: Project) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val surfaceImageFile = File(File(project.uri), "gnuplot/bias_surface.png")
    private val heatmapImageFile = File(File(project.uri), "gnuplot/bias_heatmap.png")

    var isLoading by mutableStateOf(true)
    var loadingState by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var heatmapImage by mutableStateOf<ImageBitmap?>(null)
    var surfaceImage by mutableStateOf<ImageBitmap?>(null)

    init {
        scope.launch {
            withContext(Dispatchers.IO) {
                reload()
            }
        }
    }

    suspend fun generatePlotScript(): Unit = withContext(Dispatchers.IO) {
        loadingState += "\nGenerate plot script"
        runCatching {
            val success = GnuplotGenerator.generateGnuplotScript(project)
            if (!success) {
                error = "Failed to generate gnuplot script"
                loadingState += "\nFail to generate plot script"
            }
        }.onFailure { e ->
            error = "Error generating script: ${e.message}"
            loadingState += "\nFail to generate plot script ${e.message}"
        }
    }

    suspend fun executePlotScript(): Unit = withContext(Dispatchers.IO) {
        runCatching {
            loadingState += "\nExecuting plot script"
            val gnuplotDir = File(File(project.uri), "gnuplot")
            val scriptFile = File(gnuplotDir, "gnuplot-script")
            if (!scriptFile.exists()) {
                error = "Gnuplot script not found at: ${scriptFile.absolutePath}"
                loadingState += "\nFail to execute plot script: File not found"
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
            loadingState += "\nWaiting for plot script execution"
            val completed = process.waitFor(1, TimeUnit.MINUTES)

            if (!completed) {
                process.destroyForcibly()
                error = "Gnuplot execution timed out after 1 minute"
                loadingState += "\nGnuplot execution timed out after 1 minute"
            } else if (process.exitValue() != 0) {
                val errorOutput = process.errorStream.bufferedReader().readText()
                error = "Gnuplot execution failed with exit code ${process.exitValue()}: $errorOutput"
                loadingState += "\nGnuplot execution failed: ${process.exitValue()}: $errorOutput"
            } else {
                // Success - check if output files were generated in the gnuplot directory
                val outputFiles = gnuplotDir.listFiles { file ->
                    file.extension.lowercase() in listOf("png", "jpg", "jpeg", "svg", "pdf", "eps")
                }

                if (outputFiles?.isNotEmpty() == true) {
                    error = null // Clear any previous errors
                    println("Gnuplot execution completed successfully. Generated files: ${outputFiles.map { it.name }}")
                    loadingState += "\nGnuplot execution success. Generated files: ${outputFiles.map { it.name }}"
                } else {
                    error = "Gnuplot completed but no output files were generated"
                    loadingState += "\nGnuplot completed but no output files were generated"
                }
            }
        }.onFailure { e ->
            error = "Failed to execute gnuplot script: ${e.message}"
            loadingState += "\nFailed to execute gnuplot script: ${e.message}"
        }
    }

    suspend fun reloadImages() = withContext(Dispatchers.IO) {
        runCatching {
            loadingState += "\nReload images"
            check(heatmapImageFile.exists() && surfaceImageFile.exists()) {
                loadingState += "\nFail to reload images: heatmap file or source image file not found"
                throw FileNotFoundException("Heatmap file or source image file not found")
            }

            // Load heatmap image if it exists
            val job1 = async {
                loadingState += "\nLoad heatmap image"
                while (heatmapImageFile.length() == 0L) {
                    delay(100)
                }
                val heatmapBytes = heatmapImageFile.readBytes()
                val heatmapSkiaImage = Image.makeFromEncoded(heatmapBytes)
                heatmapImage = heatmapSkiaImage.toComposeImageBitmap()
            }

            job1.invokeOnCompletion {
                loadingState += if (it == null)
                    "\nLoad heatmap image success"
                else
                    "\nFail to load heatmap image: ${it.message}"
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

            job2.invokeOnCompletion {
                loadingState += if (it == null)
                    "\nLoad surface image success"
                else
                    "\nFail to load surface image: ${it.message}"
            }

            awaitAll(job1, job2)
        }.onFailure { e ->
            error = "Failed to load images: ${e.message}"
            loadingState += "\nFail to reload images: ${e.message}"
        }
    }

    suspend fun reload(hard: Boolean = false) = withContext(Dispatchers.IO) {
        isLoading = true
        loadingState = "Reloading.."
        // Validate that required files exist
        if (!project.biasFile.exists()) {
            error = "Bias file not found: ${project.biasFile.absolutePath}"
            isLoading = false
            loadingState += "\nFail to reload: Bias file not found: ${project.biasFile.absolutePath}"
            return@withContext
        }

        runCatching {
            if (!surfaceImageFile.exists() || !heatmapImageFile.exists() || hard) {
                generatePlotScript()
                executePlotScript()
            }
            reloadImages()
        }.onFailure {
            error = "Fail to reload images $it"
            loadingState += "\nFail to reload images $it"
            isLoading = false
            return@withContext
        }

        isLoading = false
    }
}
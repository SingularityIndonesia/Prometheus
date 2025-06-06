package com.singularityuniverse.prometheus.ui.component

import androidx.compose.runtime.Composable
import com.singularityuniverse.prometheus.entity.Project
import java.nio.ByteBuffer

@Composable
fun Landscape(project: Project) {
    val metadata = project.metadata
    val neuronsPerLayer = metadata["neuronsPerLayer"]?.toInt() ?: 0
    val layerCount = metadata["layerCount"]?.toInt() ?: 0

    project.biasFile.inputStream()
        .buffered()
        .use { inputStream ->
            val buffer = ByteArray(neuronsPerLayer * layerCount * 4)
            inputStream.read(buffer)

            val biasArray = mutableListOf<Float>()
            val byteBuffer = ByteBuffer.wrap(buffer)

            repeat(neuronsPerLayer * layerCount) {
                biasArray.add(byteBuffer.getFloat())
            }

            println("Bias array: ${biasArray.toTypedArray().contentToString()}")
        }
}
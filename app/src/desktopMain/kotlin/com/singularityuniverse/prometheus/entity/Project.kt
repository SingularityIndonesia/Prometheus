package com.singularityuniverse.prometheus.entity

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

class Project(
    val name: String,
    val path: URI,
    val modelFileSize: Long,
    val lastModified: Long
) {

    /**
     * metadata content:
     * ```
     * createdAt = 1749191842997
     * version = 1.0
     * modelName = Lilith
     * nodesPerLayer = 100
     * layerCount = 100
     * totalParameters = 10000
     * biasMode = Random
     * ```
     */
    val metadata: Map<String, String> by lazy {
        runCatching {
            val metadataFile = File(File(path), "metadata.txt")

            check(metadataFile.exists() && metadataFile.isFile) {
                return@lazy mapOf()
            }

            metadataFile.readLines()
                .mapNotNull { line ->
                    val trimmedLine = line.trim()
                    check(trimmedLine.isNotEmpty() && trimmedLine.contains("=")) {
                        return@mapNotNull null
                    }

                    val parts = trimmedLine.split("=", limit = 2)

                    check(parts.size == 2) {
                        return@mapNotNull null
                    }

                    parts[0].trim() to parts[1].trim()
                }.toMap()
        }.getOrElse {
            emptyMap()
        }
    }
    val biasIs: InputStream get() = FileInputStream(File(File(path), "bias"))
    val weightsIs: InputStream get() = FileInputStream(File(File(path), "weights"))
}

/**
 * Scans for Prometheus project directories that contain model.csv files
 * Returns a list of Project objects for each valid project found
 */
fun scanForProjects(): List<Project> {
    return runCatching {
        val homeDir = System.getProperty("user.home")
        val prometheusDir = File(homeDir, "Prometheus")

        // Check if Prometheus directory exists
        if (!prometheusDir.exists() || !prometheusDir.isDirectory) {
            return emptyList()
        }

        // Scan all subdirectories for model.csv files
        prometheusDir.listFiles { file -> file.isDirectory }
            ?.mapNotNull { projectDir ->
                val metadata = File(projectDir, "metadata.txt")
                val bias = File(projectDir, "bias")
                val weight = File(projectDir, "weights")
                val modelSize = bias.length() + weight.length()
                if (metadata.exists() && metadata.isFile) {
                    Project(
                        name = projectDir.name,
                        path = projectDir.toURI(),
                        modelFileSize = modelSize,
                        lastModified = weight.lastModified()
                    )
                } else {
                    null
                }
            }
            ?.sortedByDescending { it.lastModified } // Sort by most recently modified first
            ?: emptyList()
    }.getOrElse {
        println("Error: $it")
        emptyList()
    }
}

fun getProjectByName(name: String): Result<Project> {
    return runCatching {
        val homeDir = System.getProperty("user.home")
        val prometheusDir = File(homeDir, "Prometheus")
        val targetProjectDir = File(prometheusDir, name)

        // Check if Prometheus directory exists
        if (!prometheusDir.exists() || !prometheusDir.isDirectory || !targetProjectDir.isDirectory) {
            throw NullPointerException("project $name not found")
        }

        val metadata = File(targetProjectDir, "metadata.txt")
        val bias = File(targetProjectDir, "bias")
        val weight = File(targetProjectDir, "weights")
        val modelSize = bias.length() + weight.length()
        if (metadata.exists() && metadata.isFile) {
            Project(
                name = targetProjectDir.name,
                path = targetProjectDir.toURI(),
                modelFileSize = modelSize,
                lastModified = weight.lastModified()
            )
        } else {
            throw IllegalStateException("Project is corrupted")
        }
    }
}
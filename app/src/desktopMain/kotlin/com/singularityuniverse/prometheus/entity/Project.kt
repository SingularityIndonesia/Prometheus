package com.singularityuniverse.prometheus.entity

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URI

class Project(
    val name: String,
    val path: URI,
    val modelFileSize: Long,
    val lastModified: Long
) {
    val metadata: Map<String, String> by lazy {
        runCatching {
            val metadataFile = File(File(path), "metadata.txt")
            if (metadataFile.exists() && metadataFile.isFile) {
                metadataFile.readLines()
                    .mapNotNull { line ->
                        val trimmedLine = line.trim()
                        if (trimmedLine.isNotEmpty() && trimmedLine.contains("=")) {
                            val parts = trimmedLine.split("=", limit = 2)
                            if (parts.size == 2) {
                                parts[0].trim() to parts[1].trim()
                            } else null
                        } else null
                    }.toMap()
            } else {
                emptyMap()
            }
        }.getOrElse { 
            emptyMap() 
        }
    }
    val biasOs: OutputStream get() = FileOutputStream(File(File(path), "bias"))
    val weightsOs: OutputStream get() = FileOutputStream(File(File(path), "weights"))
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
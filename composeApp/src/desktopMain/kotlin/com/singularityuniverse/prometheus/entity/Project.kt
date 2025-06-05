package com.singularityuniverse.prometheus.entity

import java.io.File
import java.net.URI

class Project(
    val name: String,
    val path: URI,
    val modelFileSize: Long,
    val lastModified: Long
)

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
                val modelFile = File(projectDir, "model.txt")
                if (modelFile.exists() && modelFile.isFile) {
                    Project(
                        name = projectDir.name,
                        path = projectDir.toURI(),
                        modelFileSize = modelFile.length(),
                        lastModified = modelFile.lastModified()
                    )
                } else {
                    null
                }
            }
            ?.sortedByDescending { it.lastModified } // Sort by most recently modified first
            ?: emptyList()
    }.getOrNull() ?: emptyList()
}
package com.singularityuniverse.prometheus.utils

import com.singularityuniverse.prometheus.entity.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

suspend fun openProjectFolder(project: Project) {
    withContext(Dispatchers.IO) {
        try {
            val projectDir = File(project.uri)
            if (projectDir.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(projectDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
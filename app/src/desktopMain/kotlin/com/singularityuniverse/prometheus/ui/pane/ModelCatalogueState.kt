package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.singularityuniverse.prometheus.entity.Project
import com.singularityuniverse.prometheus.entity.scanForProjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

class ModelCatalogueState {
    var projects by mutableStateOf<List<Project>>(emptyList())
    var isLoading by mutableStateOf(true)
    var showDeleteDialog by mutableStateOf<Project?>(null)
    var expandedProject by mutableStateOf<Project?>(null)

    // Function to refresh projects list
    suspend fun refreshProjects() {
        isLoading = true
        projects = withContext(Dispatchers.IO) {
            scanForProjects()
        }
        isLoading = false

    }

    // Function to delete a project
    suspend fun deleteProject(project: Project) {
        withContext(Dispatchers.IO) {
            try {
                val projectDir = File(project.path)
                if (projectDir.exists() && projectDir.isDirectory) {
                    projectDir.deleteRecursively()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        refreshProjects()
    }

    // Function to open project folder
    suspend fun openProjectFolder(project: Project) {
        withContext(Dispatchers.IO) {
            try {
                val projectDir = File(project.path)
                if (projectDir.exists() && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(projectDir)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
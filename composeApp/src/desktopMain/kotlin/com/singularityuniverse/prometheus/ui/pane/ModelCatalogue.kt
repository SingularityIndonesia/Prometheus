package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.entity.Project
import com.singularityuniverse.prometheus.entity.scanForProjects
import com.singularityuniverse.prometheus.ui.component.CommonTopAppBar
import com.singularityuniverse.prometheus.ui.component.DeleteProjectDialog
import com.singularityuniverse.prometheus.ui.component.ModelCatalogueBottomBar
import com.singularityuniverse.prometheus.ui.component.ProjectsList
import com.singularityuniverse.prometheus.utils.LocalWindowController
import com.singularityuniverse.prometheus.utils.to
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelCatalogue(
    modifier: Modifier,
    onCreateNewModel: () -> Unit,
) {
    val windowController = LocalWindowController.current
    val coroutineScope = rememberCoroutineScope()

    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<Project?>(null) }
    var expandedProject by remember { mutableStateOf<Project?>(null) }

    // Check and adjust window size if needed
    LaunchedEffect(Unit) {
        windowController.setMinimumSize(400.dp to 600.dp)
    }

    // Scan for projects on first composition
    LaunchedEffect(Unit) {
        isLoading = true
        projects = withContext(Dispatchers.IO) {
            scanForProjects()
        }
        isLoading = false
    }

    // Function to refresh projects list
    fun refreshProjects() {
        coroutineScope.launch {
            isLoading = true
            projects = withContext(Dispatchers.IO) {
                scanForProjects()
            }
            isLoading = false
        }
    }

    // Function to delete a project
    fun deleteProject(project: Project) {
        coroutineScope.launch {
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
    }

    // Function to open project folder
    fun openProjectFolder(project: Project) {
        coroutineScope.launch {
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

    // Delete confirmation dialog
    DeleteProjectDialog(
        project = showDeleteDialog,
        onDismiss = { showDeleteDialog = null },
        onConfirm = { project -> deleteProject(project) }
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            CommonTopAppBar(titleText = "Projects")
        },
        bottomBar = {
            ModelCatalogueBottomBar(
                onRefresh = { refreshProjects() },
                onCreateNewModel = onCreateNewModel
            )
        }
    ) {
        ProjectsList(
            projects = projects,
            isLoading = isLoading,
            expandedProject = expandedProject,
            onToggleExpanded = { project ->
                expandedProject = if (expandedProject == project) null else project
            },
            onDeleteClick = { project ->
                showDeleteDialog = project
                expandedProject = null
            },
            onOpenFolder = { project -> openProjectFolder(project) },
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
            modifier = Modifier.padding(it)
        )
    }
}

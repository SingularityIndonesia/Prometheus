package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.io.File
import com.singularityuniverse.prometheus.entity.Project
import com.singularityuniverse.prometheus.entity.scanForProjects
import com.singularityuniverse.prometheus.utils.LocalWindowController
import com.singularityuniverse.prometheus.utils.formatDate
import com.singularityuniverse.prometheus.utils.formatFileSize
import com.singularityuniverse.prometheus.utils.to
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import prometheus.composeapp.generated.resources.Res
import prometheus.composeapp.generated.resources.ic_delete
import prometheus.composeapp.generated.resources.ic_folder
import prometheus.composeapp.generated.resources.ic_more_vert

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
    showDeleteDialog?.let { project ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Project") },
            text = {
                Text("Are you sure you want to delete the project \"${project.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteProject(project)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Projects")
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { refreshProjects() }
                ) {
                    Text("Refresh")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onCreateNewModel
                ) {
                    Text("New Model")
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            text = "Loading projects..."
                        )
                    }
                }
            } else if (projects.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text("No projects found")
                            Text(
                                text = "Create your first model to get started!",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                items(projects) { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .requiredHeight(120.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Size: ${formatFileSize(project.modelFileSize)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Last modified: ${formatDate(project.lastModified)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // More options menu
                            Box(
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                IconButton(
                                    onClick = {
                                        expandedProject = if (expandedProject == project) null else project
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_more_vert),
                                        contentDescription = "More options"
                                    )
                                }

                                DropdownMenu(
                                    expanded = expandedProject == project,
                                    onDismissRequest = { expandedProject = null }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            showDeleteDialog = project
                                            expandedProject = null
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(Res.drawable.ic_delete),
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }

                            // Quick action buttons
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd),
                            ) {
                                IconButton(
                                    onClick = { openProjectFolder(project) }
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_folder),
                                        contentDescription = "Open folder"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

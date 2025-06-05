package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.entity.Project
import com.singularityuniverse.prometheus.entity.scanForProjects
import com.singularityuniverse.prometheus.utils.LocalWindowController
import com.singularityuniverse.prometheus.utils.formatDate
import com.singularityuniverse.prometheus.utils.formatFileSize
import com.singularityuniverse.prometheus.utils.to
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            projects = withContext(Dispatchers.IO) {
                                scanForProjects()
                            }
                            isLoading = false
                        }
                    }
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
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
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
                    }
                }
            }
        }
    }
}

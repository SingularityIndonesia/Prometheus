package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.entity.Project
import com.singularityuniverse.prometheus.entity.scanForProjects
import com.singularityuniverse.prometheus.utils.formatDate
import com.singularityuniverse.prometheus.utils.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ModelCatalogue(
    modifier: Modifier,
    onCreateNewModel: () -> Unit,
) {
    var projects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Function to refresh project list
    val refreshProjects = remember {
        {
            coroutineScope.launch {
                isLoading = true
                projects = withContext(Dispatchers.IO) {
                    scanForProjects()
                }
                isLoading = false
            }
            Unit
        }
    }

    // Scan for projects on first composition
    LaunchedEffect(Unit) {
        isLoading = true
        projects = withContext(Dispatchers.IO) {
            scanForProjects()
        }
        isLoading = false
    }

    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
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
                                style = MaterialTheme.typography.headlineSmall
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

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = refreshProjects
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
}

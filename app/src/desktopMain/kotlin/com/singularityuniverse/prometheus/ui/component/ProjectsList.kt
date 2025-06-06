package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.entity.Project

@Composable
fun ProjectsList(
    projects: List<Project>,
    isLoading: Boolean,
    expandedProject: Project?,
    onToggleExpanded: (Project) -> Unit,
    onDeleteClick: (Project) -> Unit,
    onOpenFolder: (Project) -> Unit,
    goToWorkSpace: (Project) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
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
                ProjectCard(
                    project = project,
                    isExpanded = expandedProject == project,
                    onToggleExpanded = { onToggleExpanded(project) },
                    onDeleteClick = { onDeleteClick(project) },
                    onOpenFolder = { onOpenFolder(project) },
                    goToWorkSpace = goToWorkSpace
                )
            }
        }
    }
}

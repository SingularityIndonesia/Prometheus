package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.entity.Project
import com.singularityuniverse.prometheus.entity.scanForProjects
import com.singularityuniverse.prometheus.ui.component.CommonTopAppBar
import com.singularityuniverse.prometheus.ui.component.DeleteProjectDialog
import com.singularityuniverse.prometheus.ui.component.LightSeparator
import com.singularityuniverse.prometheus.ui.component.ProjectsList
import com.singularityuniverse.prometheus.ui.scaffold.Info
import com.singularityuniverse.prometheus.ui.scaffold.Navigator
import com.singularityuniverse.prometheus.ui.scaffold.Status
import com.singularityuniverse.prometheus.utils.LocalWindowController
import com.singularityuniverse.prometheus.utils.openProjectFolder
import com.singularityuniverse.prometheus.utils.to
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelCatalogue(
    state: ModelCatalogueState,
    onCreateNewModel: () -> Unit,
    goToWorkSpace: (Project) -> Unit
) {
    val windowController = LocalWindowController.current
    val scope = rememberCoroutineScope()

    // Check and adjust window size if needed
    LaunchedEffect(Unit) {
        windowController.setMinimumSize(400.dp to 600.dp)
        windowController.requestFullScreen(false)
    }

    // Scan for projects on first composition
    LaunchedEffect(Unit) {
        state.isLoading = true
        state.projects = withContext(Dispatchers.IO) {
            scanForProjects()
        }
        state.isLoading = false
    }

    // Delete confirmation dialog
    DeleteProjectDialog(
        project = state.showDeleteDialog,
        onDismiss = { state.showDeleteDialog = null },
        onConfirm = { project ->
            scope.launch {
                state.deleteProject(project)
            }
        }
    )

    Status {
        Column {
            CommonTopAppBar(
                titleText = "Projects",
                onRefresh = {
                    scope.launch {
                        state.refreshProjects()
                    }
                }
            )
            LightSeparator()
        }
    }

    Navigator { }

    Info { }

    Column {
        ProjectsList(
            modifier = Modifier.weight(1f),
            projects = state.projects,
            isLoading = state.isLoading,
            expandedProject = state.expandedProject,
            onToggleExpanded = { project ->
                state.expandedProject = if (state.expandedProject == project) null else project
            },
            onDeleteClick = { project ->
                state.showDeleteDialog = project
                state.expandedProject = null
            },
            onOpenFolder = { project ->
                scope.launch {
                    openProjectFolder(project)
                }
            },
            contentPadding = PaddingValues(16.dp),
            goToWorkSpace = goToWorkSpace,
        )
        Button(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onClick = onCreateNewModel
        ) {
            Text("New Model")
        }
    }
}

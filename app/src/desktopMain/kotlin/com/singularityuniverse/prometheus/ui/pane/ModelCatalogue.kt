package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelCatalogue(
    state: ModelCatalogueState,
    modifier: Modifier = Modifier,
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

    Scaffold(
        modifier = modifier,
        topBar = {
            CommonTopAppBar(titleText = "Projects")
        },
        bottomBar = {
            ModelCatalogueBottomBar(
                onRefresh = {
                    scope.launch {
                        state.refreshProjects()
                    }
                },
                onCreateNewModel = onCreateNewModel
            )
        }
    ) {
        ProjectsList(
            modifier = Modifier.padding(it),
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
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
            goToWorkSpace = goToWorkSpace,
        )
    }
}

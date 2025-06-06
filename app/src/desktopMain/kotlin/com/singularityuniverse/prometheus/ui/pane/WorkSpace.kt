package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.entity.Project
import com.singularityuniverse.prometheus.entity.getProjectByName
import com.singularityuniverse.prometheus.ui.component.CommonTopAppBar
import com.singularityuniverse.prometheus.ui.component.Landscape
import com.singularityuniverse.prometheus.utils.LocalWindowController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun WorkSpace(projectName: String, onNavigateBack: () -> Unit) {
    val windowController = LocalWindowController.current

    val error = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val project = remember { mutableStateOf<Project?>(null) }

    LaunchedEffect(Unit) {
        windowController.requestFullScreen(true)

        isLoading.value = true
        withContext(Dispatchers.IO) {
            project.value = getProjectByName(projectName)
                .getOrElse {
                    error.value = it.message ?: "Unknown Error"
                    return@withContext
                }
        }
        isLoading.value = false
    }

    Scaffold(
        topBar = {
            val title = if (project.value == null) "Loading.." else project.value?.name
            CommonTopAppBar(
                titleText = title,
                onNavigateBack = onNavigateBack
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
        ) {
            if (isLoading.value)
                item {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

            // no need to display anything
            if (project.value == null) return@LazyColumn

            item {
                Landscape(project.value!!)
            }
        }
    }
}
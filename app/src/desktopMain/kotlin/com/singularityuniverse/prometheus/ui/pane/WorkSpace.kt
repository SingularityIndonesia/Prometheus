package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.entity.getProjectByName
import com.singularityuniverse.prometheus.ui.component.CommonTopAppBar
import com.singularityuniverse.prometheus.ui.component.Landscape
import com.singularityuniverse.prometheus.ui.component.LandscapeState
import com.singularityuniverse.prometheus.utils.LocalWindowController
import kotlinx.coroutines.launch

@Composable
fun WorkSpace(projectName: String, onNavigateBack: () -> Unit) {
    val windowController = LocalWindowController.current

    val scope = rememberCoroutineScope()
    val error = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val project = remember {
        getProjectByName(projectName)
            .getOrElse {
                error.value = it.message ?: "Unknown Error"
                null
            }
    }

    LaunchedEffect(Unit) {
        windowController.requestFullScreen(true)
    }

    Scaffold(
        topBar = {
            val title = project?.name ?: "Loading.."
            CommonTopAppBar(
                titleText = title,
                onNavigateBack = onNavigateBack,
                openDir = {
                    scope.launch {
                        openProjectFolder(project!!)
                    }
                }
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
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
            if (project == null) return@LazyColumn

            item {
                val metadata = project.metadata
                Text(
                    "Metadata",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.size(8.dp))
                val modelName = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold),
                    ) {
                        append("Model Name: ")
                    }
                    append(metadata["modelName"])
                    append(", ")

                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold),
                    ) {
                        append("Version: ")
                    }
                    append(metadata["version"])
                    append(", ")

                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold),
                    ) {
                        append("Neurons PerLayer: ")
                    }
                    append(metadata["neuronsPerLayer"])
                    append(", ")

                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold),
                    ) {
                        append("Total Layers: ")
                    }
                    append(metadata["layerCount"])
                    append(", ")

                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold),
                    ) {
                        append("Total Parameters: ")
                    }
                    append(metadata["totalParameters"])
                    append(", ")

                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold),
                    ) {
                        append("Initial Bias: ")
                    }
                    append(metadata["biasMode"])
                }

                Text(
                    text = modelName,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            item {
                val state = remember(project) {
                    LandscapeState(project)
                }
                Landscape(
                    modifier = Modifier.height(300.dp),
                    state = state
                )
            }
        }
    }
}
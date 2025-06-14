package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularityuniverse.prometheus.entity.getProjectByName
import com.singularityuniverse.prometheus.ui.component.CommonTopAppBar
import com.singularityuniverse.prometheus.ui.component.Landscape
import com.singularityuniverse.prometheus.ui.component.LandscapeState
import com.singularityuniverse.prometheus.ui.component.LightSeparator
import com.singularityuniverse.prometheus.ui.scaffold.Info
import com.singularityuniverse.prometheus.ui.scaffold.Navigator
import com.singularityuniverse.prometheus.ui.scaffold.Status
import com.singularityuniverse.prometheus.utils.LocalWindowController
import com.singularityuniverse.prometheus.utils.openProjectFolder
import com.singularityuniverse.prometheus.utils.to
import kotlinx.coroutines.launch

class WorkSpaceState(projectName: String) {
    var error by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    val project = getProjectByName(projectName)
        .getOrElse {
            error = it.message ?: "Unknown Error"
            null
        }
}

@Composable
fun WorkSpace(state: WorkSpaceState, onNavigateBack: () -> Unit) {
    val windowController = LocalWindowController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        windowController.setMinimumSize(900.dp to 700.dp)
        windowController.requestFullScreen(true)
    }

    Navigator {
        Column(
            modifier = Modifier
                .width(72.dp)
                .padding(16.dp)
        ) {
            Text("V1.0")
        }
    }

    Status {
        val title = state.project?.name ?: "Loading.."
        Column {
            CommonTopAppBar(
                titleText = title,
                onNavigateBack = onNavigateBack,
                onOpenFolder = {
                    scope.launch {
                        openProjectFolder(state.project!!)
                    }
                }
            )
            LightSeparator()
        }
    }

    Info {
        InfoContent(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight(),
            state = state,
        )
    }

    ProjectSate(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxHeight(),
        state = state
    )

}

@Composable
fun InfoContent(
    state: WorkSpaceState,
    modifier: Modifier = Modifier
) {
    val metadata = state.project?.metadata ?: return
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
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
                append("\n")

                withStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                ) {
                    append("Version: ")
                }
                append(metadata["version"])
                append("\n")

                withStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                ) {
                    append("Neurons PerLayer: ")
                }
                append(metadata["neuronsPerLayer"])
                append("\n")

                withStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                ) {
                    append("Total Layers: ")
                }
                append(metadata["layerCount"])
                append("\n")

                withStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                ) {
                    append("Total Parameters: ")
                }
                append(metadata["totalParameters"])
                append("\n")

                withStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                ) {
                    append("Initial Bias: ")
                }
                append(metadata["biasMode"])
            }

            Text(
                text = modelName,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 20.sp,
                )
            )
        }
    }
}

@Composable
fun ProjectSate(
    modifier: Modifier = Modifier,
    state: WorkSpaceState,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.isLoading)
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
        if (state.project == null) return@LazyColumn

        item {
            val state = remember(state.project) {
                LandscapeState(state.project)
            }

            Landscape(
                state = state
            )
        }
    }
}
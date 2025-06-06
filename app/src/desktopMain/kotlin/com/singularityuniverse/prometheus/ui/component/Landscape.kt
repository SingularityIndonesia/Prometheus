package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import prometheus.app.generated.resources.Res
import prometheus.app.generated.resources.ic_delete

@Composable
fun Landscape(
    modifier: Modifier = Modifier,
    state: LandscapeState
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.surfaceImage != null && state.heatmapImage != null)
            Row(
                modifier = Modifier
                    .height(300.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxHeight(),
                    bitmap = state.surfaceImage ?: return@Row,
                    contentScale = ContentScale.FillHeight,
                    contentDescription = null
                )
                Image(
                    modifier = Modifier
                        .fillMaxHeight(),
                    bitmap = state.heatmapImage ?: return@Row,
                    contentScale = ContentScale.FillHeight,
                    contentDescription = null
                )
            }

        if (state.error != null)
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            state.error = null
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete),
                            contentDescription = null
                        )
                    }
                }
            }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier.width(160.dp),
                onClick = {
                    if (!state.isLoading)
                        scope.launch {
                            state.reload()
                        }
                }
            ) {
                if (state.isLoading)
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                else
                    Text("Reload Image")
            }

            Button(
                modifier = Modifier.width(160.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                onClick = {
                    if (!state.isLoading)
                        scope.launch {
                            state.reload(true)
                        }
                }
            ) {
                if (state.isLoading)
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onError
                    )
                else
                    Text("Regenerate")
            }

            if (state.isLoading)
                Text("Loading model data...")
        }
    }
}

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import prometheus.app.generated.resources.Res
import prometheus.app.generated.resources.ic_delete

@Composable
fun Landscape(modifier: Modifier = Modifier, state: LandscapeState) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.surfaceImage != null && state.heatmapImage != null)
            Row(
                modifier = Modifier
                    .height(400.dp)
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

        if (state.isLoading)
            Text("Loading model data...")

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

        Button(
            enabled = !state.isLoading,
            onClick = {
                scope.launch {
                    state.reload()
                }
            }
        ) {
            if (state.isLoading)
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else
                Text("Reload")
        }
    }
}

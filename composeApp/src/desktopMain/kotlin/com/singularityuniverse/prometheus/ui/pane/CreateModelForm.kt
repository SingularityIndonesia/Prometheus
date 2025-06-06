package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.ui.component.CreateModelButton
import com.singularityuniverse.prometheus.ui.component.CreateModelFormFields
import com.singularityuniverse.prometheus.ui.component.ErrorMessageCard
import com.singularityuniverse.prometheus.utils.LocalWindowController
import com.singularityuniverse.prometheus.utils.runInMainThread
import com.singularityuniverse.prometheus.utils.to
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import prometheus.composeapp.generated.resources.Res
import prometheus.composeapp.generated.resources.ic_back
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModelForm(
    state: CreateModelFormState,
    modifier: Modifier = Modifier,
    onReturn: (fileUri: URI?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val windowController = LocalWindowController.current

    // Check and adjust window size if needed
    LaunchedEffect(Unit) {
        windowController.setMinimumSize(500.dp to 600.dp)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Create Model")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onReturn.invoke(null)
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_back),
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
        ) {

            // Error message display
            state.errorMessage.value?.let { message ->
                ErrorMessageCard(
                    message = message,
                    onDismiss = { state.errorMessage.value = null },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.size(16.dp))
            }

            CreateModelFormFields(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = state
            )

            Spacer(Modifier.size(16.dp))

            CreateModelButton(
                isLoading = state.isLoading.value,
                isEnabled = !state.isLoading.value &&
                        state.modelName.value.isNotBlank() &&
                        state.neuronPerLayer.value.toIntOrNull() != null &&
                        state.neuronPerLayer.value.toInt() > 0 &&
                        state.layerCount.value.toIntOrNull() != null &&
                        state.layerCount.value.toInt() > 1 &&
                        (state.initialBiasMode.value != "Determined" || state.determinedBias.value != null),
                onClick = {
                    scope.launch {
                        state.createModel()
                            .onSuccess { fileUri ->
                                runInMainThread { onReturn(fileUri) }
                            }
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.size(16.dp))
        }
    }
}

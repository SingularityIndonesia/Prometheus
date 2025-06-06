package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.ui.pane.CreateModelFormState

@Composable
fun CreateModelFormFields(
    state: CreateModelFormState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModelNameField(
                value = state.modelName,
                onValueChange = { value ->
                    state.modelName = value
                }
            )
        }

        item {
            NeuronsPerLayerField(
                value = state.neuronsPerLayer,
                onValueChange = { value ->
                    state.neuronsPerLayer = value
                }
            )
        }

        item {
            LayerCountField(
                value = state.layerCount,
                totalParameter = state.totalParameter,
                onValueChange = { value ->
                    state.layerCount = value
                }
            )
        }

        item {
            BiasConfigurationField(
                biasMode = state.initialBiasMode,
                onBiasModeChange = { mode ->
                    state.initialBiasMode = mode
                },
                onDeterminedBiasChange = { bias ->
                    state.determinedBias = bias
                }
            )
        }

        if (state.initialBiasMode == "Determined") {
            item {
                DeterminedBiasValueField(
                    value = state.determinedBias,
                    onValueChange = { bias ->
                        state.determinedBias = bias
                    }
                )
            }
        }
    }
}

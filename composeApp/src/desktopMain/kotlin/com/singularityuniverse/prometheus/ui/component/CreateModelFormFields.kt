package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
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
                value = state.modelName.value,
                onValueChange = { value ->
                    state.modelName.value = value
                }
            )
        }

        item {
            NeuronsPerLayerField(
                value = state.neuronPerLayer.value,
                onValueChange = { value ->
                    state.neuronPerLayer.value = value
                }
            )
        }

        item {
            LayerCountField(
                value = state.layerCount.value,
                totalParameter = state.totalParameter,
                onValueChange = { value ->
                    state.layerCount.value = value
                }
            )
        }

        item {
            BiasConfigurationField(
                biasMode = state.initialBiasMode.value,
                determinedBias = state.determinedBias.value,
                onBiasModeChange = { mode ->
                    state.initialBiasMode.value = mode
                },
                onDeterminedBiasChange = { bias ->
                    state.determinedBias.value = bias
                }
            )
        }

        if (state.initialBiasMode.value == "Determined") {
            item {
                DeterminedBiasValueField(
                    value = state.determinedBias.value,
                    onValueChange = { bias ->
                        state.determinedBias.value = bias
                    }
                )
            }
        }
    }
}

package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModelCatalogueBottomBar(
    onRefresh: () -> Unit,
    onCreateNewModel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onRefresh
        ) {
            Text("Refresh")
        }

        Button(
            modifier = Modifier.weight(1f),
            onClick = onCreateNewModel
        ) {
            Text("New Model")
        }
    }
}

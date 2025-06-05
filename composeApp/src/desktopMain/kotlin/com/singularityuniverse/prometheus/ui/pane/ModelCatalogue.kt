package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModelCatalogue(
    modifier: Modifier,
    onCreateNewModel: () -> Unit,
) {
    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

        }

        Button(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onClick = onCreateNewModel
        ) {
            Text("New Model")
        }
    }
}

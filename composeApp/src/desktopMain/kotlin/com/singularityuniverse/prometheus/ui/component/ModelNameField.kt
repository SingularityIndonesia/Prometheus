package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ModelNameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        label = {
            Text("Model Name")
        },
        value = value,
        onValueChange = onValueChange,
        isError = value.isBlank(),
        supportingText = if (value.isBlank()) {
            { Text("Model name is required") }
        } else null
    )
}

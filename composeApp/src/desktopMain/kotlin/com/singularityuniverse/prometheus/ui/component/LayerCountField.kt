package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun LayerCountField(
    value: String,
    totalParameter: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        label = {
            Text("Layer Count")
        },
        value = value,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        onValueChange = { newValue ->
            // Only allow numeric input
            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        isError = value.toIntOrNull() == null || value.toInt() <= 1,
        supportingText = {
            val layers = value.toIntOrNull()
            when {
                layers == null -> Text("Please enter a valid number")
                layers <= 1 -> Text("Must have at least 2 layers")
                else -> Text("Total Parameters: $totalParameter")
            }
        }
    )
}

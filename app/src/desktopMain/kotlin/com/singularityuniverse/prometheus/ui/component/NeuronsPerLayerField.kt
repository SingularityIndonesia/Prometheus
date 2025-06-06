package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun NeuronsPerLayerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        label = {
            Text("Neurons per Layer")
        },
        value = value,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        onValueChange = { newValue ->
            // Only allow numeric input
            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        isError = value.toIntOrNull() == null || value.toInt() <= 0,
        supportingText = {
            val neurons = value.toIntOrNull()
            when {
                neurons == null -> Text("Please enter a valid number")
                neurons <= 0 -> Text("Number must be greater than 0")
                else -> Text("Valid")
            }
        }
    )
}

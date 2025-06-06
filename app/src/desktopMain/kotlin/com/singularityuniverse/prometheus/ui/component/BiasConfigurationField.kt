package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiasConfigurationField(
    biasMode: String,
    onBiasModeChange: (String) -> Unit,
    onDeterminedBiasChange: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val biasOptions = listOf("Random", "Determined")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            value = biasMode,
            onValueChange = {},
            label = { Text("Initial Bias") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            supportingText = {
                Text(
                    when (biasMode) {
                        "Random" -> "Bias values will be randomized between -1 and 1"
                        "Determined" -> "All bias values will be set to the specified value"
                        else -> ""
                    }
                )
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            biasOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onBiasModeChange(option)
                        expanded = false
                        // Reset determined bias when switching modes
                        if (option == "Random") {
                            onDeterminedBiasChange(null)
                        }
                        if (option == "Determined") {
                            onDeterminedBiasChange(0f)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DeterminedBiasValueField(
    value: Float?,
    onValueChange: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        label = { Text("Bias Value") },
        value = value?.toString() ?: "",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        onValueChange = { input ->
            input.toFloatOrNull()?.let { onValueChange(it) }
        },
        isError = value == null,
        supportingText = {
            if (value == null) {
                Text("Please enter a valid decimal number")
            } else {
                Text("Recommended range: -1.0 to 1.0")
            }
        }
    )
}

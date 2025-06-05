package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.utils.LocalWindowController
import com.singularityuniverse.prometheus.utils.runInMainThread
import com.singularityuniverse.prometheus.utils.to
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import prometheus.composeapp.generated.resources.Res
import prometheus.composeapp.generated.resources.ic_back
import java.io.File
import java.io.IOException
import java.net.URI
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModelForm(
    modifier: Modifier = Modifier,
    onReturn: (fileUri: URI?) -> Unit
) {
    val windowController = LocalWindowController.current

    var modelName by remember { mutableStateOf("") }
    var neuronPerLayer by remember { mutableStateOf("1000") }
    var layerCount by remember { mutableStateOf("1000") }
    var initialBiasMode by remember { mutableStateOf("Random") }
    var determinedBias by remember { mutableStateOf<Double?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val totalParameter = run {
        val neuronsPerLayer = neuronPerLayer.toIntOrNull() ?: 0
        val layerCount = layerCount.toIntOrNull() ?: 0
        val totalParams = neuronsPerLayer * layerCount

        // Format number with spaces as thousand separators
        totalParams.toString()
            .reversed()
            .chunked(3)
            .joinToString(" ")
            .reversed()
    }

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
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(it),
        ) {

            // Error message display
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { errorMessage = null }
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
                Spacer(Modifier.size(16.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Model Name")
                        },
                        value = modelName,
                        onValueChange = {
                            modelName = it
                        }
                    )
                }
                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Neurons per Layer")
                        },
                        value = neuronPerLayer,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            neuronPerLayer = it
                        }
                    )
                }

                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Layer Count")
                        },
                        value = layerCount,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            layerCount = it
                        },
                        supportingText = {
                            Text("Total Parameter: $totalParameter")
                        }
                    )
                }

                item {
                    var expanded by remember { mutableStateOf(false) }
                    val biasOptions = listOf("Random", "Determined")

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            value = initialBiasMode,
                            onValueChange = {},
                            label = { Text("Initial Bias") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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
                                        initialBiasMode = option
                                        expanded = false
                                        // Reset determined bias when switching modes
                                        if (option == "Random") {
                                            determinedBias = null
                                        }
                                        if (option == "Determined") {
                                            determinedBias = 1.0
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (initialBiasMode == "Determined") {
                    item {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Bias Value") },
                            value = determinedBias?.toString() ?: "",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            onValueChange = { input ->
                                determinedBias = input.toDoubleOrNull()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.size(16.dp))

            Button(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                enabled = !isLoading && modelName.isNotBlank() &&
                        neuronPerLayer.toIntOrNull() != null &&
                        layerCount.toIntOrNull() != null &&
                        (initialBiasMode != "Determined" || determinedBias != null),
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null

                        runCatching {
                            withContext(Dispatchers.IO) {
                                // Calculate total parameters
                                val neuronsPerLayer = neuronPerLayer.toIntOrNull() ?: 0
                                val layers = layerCount.toIntOrNull() ?: 0
                                val totalParams = neuronsPerLayer * layers

                                // Create the array with initial bias values
                                val modelData = Array(totalParams) {
                                    when (initialBiasMode) {
                                        "Determined" -> determinedBias ?: 0.0
                                        else -> Random.nextDouble(-1.0, 1.0) // Random bias between -1 and 1
                                    }
                                }

                                // Create directory structure
                                val homeDir = System.getProperty("user.home")
                                val prometheusDir = File(homeDir, "Prometheus")
                                val projectDir = File(prometheusDir, modelName)

                                // Check if project already exists
                                if (projectDir.exists()) {
                                    throw IllegalStateException("Project with name '$modelName' already exists")
                                }

                                // Create directories
                                if (!projectDir.mkdirs()) {
                                    throw IOException("Failed to create project directory")
                                }

                                // Save model data to CSV
                                val modelFile = File(projectDir, "model.csv")
                                modelFile.writeText(
                                    buildString {
                                        // Write header
                                        appendLine("index,bias_value")
                                        // Write data
                                        modelData.forEachIndexed { index, value ->
                                            appendLine("$index,$value")
                                        }
                                    }
                                )

                                // Create Uri for the file
                                modelFile.toURI()
                            }
                        }.onSuccess {
                            runInMainThread { onReturn(it) }
                        }.onFailure { e ->
                            runInMainThread {
                                errorMessage = e.message
                                isLoading = false
                            }
                        }
                    }
                }
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Creating...")
                    }
                } else {
                    Text("Create")
                }
            }

            Spacer(Modifier.size(16.dp))
        }
    }
}
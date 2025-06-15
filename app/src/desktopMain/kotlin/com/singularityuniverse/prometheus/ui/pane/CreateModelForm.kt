package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.ui.component.CommonTopAppBar
import com.singularityuniverse.prometheus.ui.component.CreateModelButton
import com.singularityuniverse.prometheus.ui.component.CreateModelFormFields
import com.singularityuniverse.prometheus.ui.component.ErrorMessageCard
import com.singularityuniverse.prometheus.ui.scaffold.Status
import com.singularityuniverse.prometheus.utils.LocalWindowController
import com.singularityuniverse.prometheus.utils.runInMainThread
import com.singularityuniverse.prometheus.utils.to
import kotlinx.coroutines.launch
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModelForm(
    state: CreateModelFormState,
    onReturn: (fileUri: URI?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val windowController = LocalWindowController.current

    // Check and adjust window size if needed
    LaunchedEffect(Unit) {
        windowController.setMinimumSize(500.dp to 600.dp)
    }

    Status {
        CommonTopAppBar(
            titleText = "Create Model",
            onNavigateBack = {
                onReturn.invoke(null)
            }
        )
    }

    Column {

        // Error message display
        state.errorMessage?.let { message ->
            ErrorMessageCard(
                message = message,
                onDismiss = { state.errorMessage = null },
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
            isLoading = state.isLoading,
            isEnabled = state.formIsValid,
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

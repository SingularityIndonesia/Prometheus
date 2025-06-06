package com.singularityuniverse.prometheus.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.painterResource
import prometheus.app.generated.resources.Res
import prometheus.app.generated.resources.ic_back

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(titleText: String? = null, onNavigateBack: (() -> Unit)? = null) {
    TopAppBar(
        title = {
            titleText?.let { Text(it) }
        },
        navigationIcon = {
            onNavigateBack?.let {
                IconButton(
                    onClick = {
                        it.invoke()
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_back),
                        contentDescription = "Go back"
                    )
                }
            }
        }
    )
}
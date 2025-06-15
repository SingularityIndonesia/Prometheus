package com.singularityuniverse.prometheus.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.painterResource
import prometheus.app.generated.resources.Res
import prometheus.app.generated.resources.ic_back
import prometheus.app.generated.resources.ic_folder
import prometheus.app.generated.resources.ic_refresh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    titleText: String? = null,
    onOpenFolder: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
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
        },
        actions = {
            onOpenFolder?.let {
                IconButton(
                    onClick = {
                        it.invoke()
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_folder),
                        contentDescription = "Open dir"
                    )
                }
            }

            onRefresh?.let {
                IconButton(
                    onClick = {
                        it.invoke()
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_refresh),
                        contentDescription = "Refresh"
                    )
                }
            }
        }
    )
}
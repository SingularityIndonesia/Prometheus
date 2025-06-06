package com.singularityuniverse.prometheus.ui.pane

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.singularityuniverse.prometheus.ui.component.CommonTopAppBar
import com.singularityuniverse.prometheus.utils.LocalWindowController

@Composable
fun WorkSpace(projectName: String, onNavigateBack: () -> Unit) {
    val windowController = LocalWindowController.current
    LaunchedEffect(Unit) {
        windowController.requestFullScreen()
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                titleText = projectName,
                onNavigateBack = onNavigateBack
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

        }
    }
}
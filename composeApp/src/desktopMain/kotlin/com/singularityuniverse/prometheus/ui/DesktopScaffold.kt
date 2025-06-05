package com.singularityuniverse.prometheus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DesktopScaffold(
    modifier: Modifier = Modifier,
    navigator: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val contentPadding = PaddingValues(0.dp)
    Box(modifier = modifier) {
        content.invoke(contentPadding)
    }
}
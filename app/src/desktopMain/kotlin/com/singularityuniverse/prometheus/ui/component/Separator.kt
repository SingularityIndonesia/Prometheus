package com.singularityuniverse.prometheus.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.ui.LocalDesignSystem

@Composable
fun ColumnScope.LightSeparator() {
    val attr = LocalDesignSystem.current
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(1.dp)
            .background(attr.darkStroke)
    )
}
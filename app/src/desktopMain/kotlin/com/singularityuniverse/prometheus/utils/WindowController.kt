package com.singularityuniverse.prometheus.utils

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.DpSize

interface WindowController {
    fun setMinimumSize(size: DpSize)
    fun requestFullScreen()
}

val LocalWindowController = staticCompositionLocalOf<WindowController> { error("Window Controller not provided") }
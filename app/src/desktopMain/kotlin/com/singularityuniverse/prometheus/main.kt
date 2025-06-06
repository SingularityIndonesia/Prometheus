package com.singularityuniverse.prometheus

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.singularityuniverse.prometheus.utils.LocalWindowController
import com.singularityuniverse.prometheus.utils.WindowController
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Prometheus",
        state = WindowState(
            size = DpSize(400.dp, 400.dp),
            position = WindowPosition.Aligned(Alignment.Center)
        ),
        resizable = true,
        onPreviewKeyEvent = { false },
        onKeyEvent = { false }
    ) {
        val windowController = remember {
            object : WindowController {
                override fun setMinimumSize(size: DpSize) {
                    window.minimumSize = Dimension(
                        size.width.value.toInt(),
                        size.height.value.toInt()
                    )
                }

                override fun requestFullScreen() {
                    window.extendedState = java.awt.Frame.MAXIMIZED_BOTH
                }
            }
        }

        // Ensure minimum window size
        window.minimumSize = Dimension(400, 600)
        CompositionLocalProvider(LocalWindowController provides windowController) {
            App()
        }
    }
}

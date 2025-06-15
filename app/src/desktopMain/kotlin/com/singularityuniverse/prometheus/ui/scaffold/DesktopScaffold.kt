package com.singularityuniverse.prometheus.ui.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.singularityuniverse.prometheus.ui.LocalDesignSystem


val LocalScaffoldComponent = compositionLocalOf { ScaffoldComponent() }

class ScaffoldComponent {
    var navigator by mutableStateOf(@Composable {})
    var status by mutableStateOf(@Composable {})
    var info by mutableStateOf(@Composable {})
}

@Composable
fun Navigator(content: @Composable () -> Unit) {
    val component = LocalScaffoldComponent.current
    val attr = LocalDesignSystem.current

    LaunchedEffect(Unit) {
        component.navigator = content
    }
}

@Composable
fun Info(content: @Composable () -> Unit) {
    val component = LocalScaffoldComponent.current
    val attr = LocalDesignSystem.current

    LaunchedEffect(Unit) {
        component.info = content
    }
}

@Composable
fun Status(content: @Composable () -> Unit) {
    val component = LocalScaffoldComponent.current

    LaunchedEffect(Unit) {
        component.status = content
    }
}

@Composable
fun DesktopScaffold(
    status: @Composable () -> Unit,
    navigator: @Composable () -> Unit,
    info: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val navigatorSize = remember { mutableStateOf(IntSize.Zero) }
    val infoSize = remember { mutableStateOf(IntSize.Zero) }
    val navigatorPadding by rememberUpdatedState(
        PaddingValues(start = (navigatorSize.value.width / density.density).dp)
    )
    val infoPadding by rememberUpdatedState(
        PaddingValues(end = (infoSize.value.width / density.density).dp)
    )

    Box {
        Scaffold(
            modifier = Modifier
                .padding(navigatorPadding),
            topBar = {
                Box(
                    modifier = Modifier
                        .background(colorScheme.surface)
                        .fillMaxWidth()
                ) {
                    status.invoke()
                }
            },
        ) {
            Box(
                Modifier
                    .padding(it)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(infoPadding),
                ) {
                    content.invoke()
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(colorScheme.surface)
                        .onSizeChanged {
                            infoSize.value = it
                        }
                        .fillMaxHeight(),
                ) {
                    CompositionLocalProvider(LocalContentColor provides colorScheme.onSurface) {
                        info.invoke()
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .background(colorScheme.surface)
                .onSizeChanged {
                    navigatorSize.value = it
                }
                .fillMaxHeight(),
            contentAlignment = Alignment.TopStart
        ) {
            CompositionLocalProvider(LocalContentColor provides colorScheme.onSurface) {
                navigator.invoke()
            }
        }
    }
}
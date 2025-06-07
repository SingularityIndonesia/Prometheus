package com.singularityuniverse.prometheus.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class DesignSystem(
    val background: Color = Color(0xff2b2d30),
    val onBackground: Color = Color(0xffdfe1e5),
    val borderColor: Color = Color(0xff1e1f22),
    val surface: Color = Color(0xff1e1f22),
    val onSurface: Color = Color(0xffdfe1e5),
    val darkStroke: Color = Color(0xff1e1f22),
    val lightStroke: Color = Color(0xff2b2d30),

    val primary: Color = Color(0xff3474f0),
    val onPrimary: Color = Color(0xffffffff)
)

private val darkTheme = DesignSystem()

val DarkTheme = darkColorScheme(
    background = darkTheme.background,
    onBackground = darkTheme.onBackground,
    surface = darkTheme.surface,
    onSurface = darkTheme.onSurface,
    primary = darkTheme.primary,
    onPrimary = darkTheme.onPrimary
)

val LocalDesignSystem = staticCompositionLocalOf { DesignSystem() }
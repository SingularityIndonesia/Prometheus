package com.singularityuniverse.prometheus

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.singularityuniverse.prometheus.ui.DarkTheme
import com.singularityuniverse.prometheus.ui.DesktopScaffold
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val navController = rememberNavController()

    MaterialTheme(
        colorScheme = DarkTheme
    ) {
        DesktopScaffold {
            MainNavigation(navController)
        }
    }
}

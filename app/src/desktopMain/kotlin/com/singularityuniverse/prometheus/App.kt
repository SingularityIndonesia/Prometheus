package com.singularityuniverse.prometheus

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.singularityuniverse.prometheus.ui.DarkTheme
import com.singularityuniverse.prometheus.ui.scaffold.DesktopScaffold
import com.singularityuniverse.prometheus.ui.scaffold.LocalScaffoldComponent
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    val scaffoldComponent = LocalScaffoldComponent.current

    MaterialTheme(
        colorScheme = DarkTheme
    ) {
        DesktopScaffold(
            status = scaffoldComponent.status,
            navigator = scaffoldComponent.navigator,
            info = scaffoldComponent.info,
        ) {
            MainNavigation(
                navController = navController
            )
        }
    }
}

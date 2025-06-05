package com.singularityuniverse.prometheus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.singularityuniverse.prometheus.ui.DesktopScaffold
import com.singularityuniverse.prometheus.ui.pane.CreateModelForm
import com.singularityuniverse.prometheus.ui.pane.ModelCatalogue
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    MaterialTheme {
        DesktopScaffold {
            NavHost(navController = navController, startDestination = "create") {
                composable(
                    route = "catalogue"
                ) {
                    ModelCatalogue(
                        modifier = Modifier.fillMaxSize(),
                        onCreateNewModel = {
                            navController.navigate("create")
                        }
                    )
                }

                composable(
                    route = "create"
                ) {
                    CreateModelForm(
                        modifier = Modifier.fillMaxSize(),
                        onReturn = {

                        }
                    )
                }
            }
        }
    }
}

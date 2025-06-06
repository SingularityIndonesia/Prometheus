package com.singularityuniverse.prometheus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.singularityuniverse.prometheus.ui.pane.CreateModelForm
import com.singularityuniverse.prometheus.ui.pane.CreateModelFormState
import com.singularityuniverse.prometheus.ui.pane.ModelCatalogue
import com.singularityuniverse.prometheus.ui.pane.ModelCatalogueState
import com.singularityuniverse.prometheus.ui.pane.WorkSpace

@Composable
fun MainNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "catalogue") {
        composable(
            route = "catalogue"
        ) {
            ModelCatalogue(
                modifier = Modifier.fillMaxSize(),
                state = remember { ModelCatalogueState() },
                onCreateNewModel = {
                    navController.navigate("create")
                },
                goToWorkSpace = {
                    navController.navigate("workspace/${it.name}")
                }
            )
        }

        composable(
            route = "create"
        ) {
            CreateModelForm(
                modifier = Modifier.fillMaxSize(),
                state = remember { CreateModelFormState() },
                onReturn = {
                    navController.popBackStack()
                }
            )
        }

        // Parse projectName argument from navigation
        composable(
            route = "workspace/{projectName}",
            arguments = listOf(
                navArgument("projectName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectName = backStackEntry.savedStateHandle.get<String>("projectName")
            WorkSpace(
                projectName = projectName ?: return@composable,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
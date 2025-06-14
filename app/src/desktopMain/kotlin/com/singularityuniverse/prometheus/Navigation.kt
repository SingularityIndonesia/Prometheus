package com.singularityuniverse.prometheus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.singularityuniverse.prometheus.ui.pane.*

@Composable
fun MainNavigation(modifier: Modifier = Modifier, navController: NavHostController) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "catalogue"
    ) {
        composable(
            route = "catalogue"
        ) {
            ModelCatalogue(
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
                state = remember { WorkSpaceState(projectName!!) },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
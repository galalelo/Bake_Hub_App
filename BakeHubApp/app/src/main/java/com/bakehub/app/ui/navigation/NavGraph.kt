package com.bakehub.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.runtime.getValue
import com.bakehub.app.data.MockData
import com.bakehub.app.ui.components.BakeHubBottomBar
import com.bakehub.app.ui.screens.BakeModeScreen
import com.bakehub.app.ui.screens.CreateRecipeScreen
import com.bakehub.app.ui.screens.GroceryListScreen
import com.bakehub.app.ui.screens.HomeScreen
import com.bakehub.app.ui.screens.LoginScreen
import com.bakehub.app.ui.screens.OnboardingScreen
import com.bakehub.app.ui.screens.ProfileScreen
import com.bakehub.app.ui.screens.RecipeBoxScreen
import com.bakehub.app.ui.screens.RecipeDetailScreen
import com.bakehub.app.ui.screens.SearchScreen
import com.bakehub.app.ui.screens.SettingsScreen

/** Routes shown with the persistent bottom tab bar (design document, Section 3.11). */
private val bottomBarRoutes = BottomTab.values().map { it.screen.route }.toSet()

@Composable
fun BakeHubNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BakeHubBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Onboarding.route,
            modifier = Modifier.padding(
                bottom = if (currentRoute in bottomBarRoutes) innerPadding.calculateBottomPadding() else 0.dp
            ),
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onGetStarted = { navController.navigate(Screen.Login.route) },
                    onLogin = { navController.navigate(Screen.Login.route) },
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onOpenSearch = { navController.navigate(Screen.Search.route) },
                    onOpenRecipe = { recipe -> navController.navigate(Screen.RecipeDetail.createRoute(recipe.id)) },
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onOpenRecipe = { recipe -> navController.navigate(Screen.RecipeDetail.createRoute(recipe.id)) },
                )
            }

            composable(Screen.RecipeBox.route) {
                RecipeBoxScreen(
                    onOpenRecipe = { recipe -> navController.navigate(Screen.RecipeDetail.createRoute(recipe.id)) },
                    onCreateRecipe = { navController.navigate(Screen.CreateRecipe.route) },
                )
            }

            composable(Screen.Grocery.route) {
                GroceryListScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                )
            }

            composable(Screen.CreateRecipe.route) {
                CreateRecipeScreen(
                    onPublished = { navController.popBackStack() },
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogOut = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0)
                        }
                    },
                )
            }

            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType }),
            ) { entry ->
                val recipeId = entry.arguments?.getString("recipeId")
                val recipe = MockData.recipeById(recipeId ?: "") ?: MockData.recipes.first()
                RecipeDetailScreen(
                    recipe = recipe,
                    onBack = { navController.popBackStack() },
                    onStartBakeMode = { navController.navigate(Screen.BakeMode.createRoute(recipe.id)) },
                )
            }

            composable(
                route = Screen.BakeMode.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType }),
            ) { entry ->
                val recipeId = entry.arguments?.getString("recipeId")
                val recipe = MockData.recipeById(recipeId ?: "") ?: MockData.recipes.first()
                BakeModeScreen(
                    recipe = recipe,
                    onExit = { navController.popBackStack() },
                    onCompleted = {
                        // Section 2.3.1 — completing Bake Mode awards XP and updates the streak.
                        // Wiring this to real state is left for the full build; the prototype
                        // simply returns the user to Home to reflect a completed session.
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

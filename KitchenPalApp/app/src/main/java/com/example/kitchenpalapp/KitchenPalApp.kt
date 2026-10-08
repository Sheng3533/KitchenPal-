package com.example.kitchenpalapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kitchenpalapp.ui.components.BottomNavBar
import com.example.kitchenpalapp.ui.navigation.Screen
import com.example.kitchenpalapp.ui.screens.AboutScreen
import com.example.kitchenpalapp.ui.screens.AssistantScreen
import com.example.kitchenpalapp.ui.screens.ChefDetailScreen
import com.example.kitchenpalapp.ui.screens.ChefListScreen
import com.example.kitchenpalapp.ui.screens.CookingCompleteScreen
import com.example.kitchenpalapp.ui.screens.CookingHistoryScreen
import com.example.kitchenpalapp.ui.screens.CookingMonitorScreen
import com.example.kitchenpalapp.ui.screens.DietReportScreen
import com.example.kitchenpalapp.ui.screens.DiscoverMode
import com.example.kitchenpalapp.ui.screens.DiscoverScreen
import com.example.kitchenpalapp.ui.screens.FavoritesListScreen
import com.example.kitchenpalapp.ui.screens.HelpFeedbackScreen
import com.example.kitchenpalapp.ui.screens.HomeScreen
import com.example.kitchenpalapp.ui.screens.IngredientDetailScreen
import com.example.kitchenpalapp.ui.screens.LoginScreen
import com.example.kitchenpalapp.ui.screens.NotificationsScreen
import com.example.kitchenpalapp.ui.screens.OnboardingScreen
import com.example.kitchenpalapp.ui.screens.PrivacyScreen
import com.example.kitchenpalapp.ui.screens.ProfileEditScreen
import com.example.kitchenpalapp.ui.screens.ProfileScreen
import com.example.kitchenpalapp.ui.screens.RecipeDetailScreen
import com.example.kitchenpalapp.ui.screens.ScanScreen
import com.example.kitchenpalapp.ui.screens.SearchScreen
import com.example.kitchenpalapp.ui.screens.SettingsScreen
import com.example.kitchenpalapp.ui.screens.FridgeScreen
import com.example.kitchenpalapp.ui.screens.PublishRecipeScreen
import com.example.kitchenpalapp.ui.screens.ShoppingListScreen
import com.example.kitchenpalapp.ui.screens.SplashScreen
import com.example.kitchenpalapp.ui.screens.TastePreferenceEditScreen
import com.example.kitchenpalapp.ui.screens.TastePreferencesScreen
import com.example.kitchenpalapp.ui.screens.TermsScreen
import com.example.kitchenpalapp.ui.theme.KitchenPalAppTheme
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel

@Composable
fun KitchenPalApp() {
    val viewModel: MainViewModel = viewModel()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    KitchenPalAppTheme(darkTheme = darkMode) {
        val navController = rememberNavController()
        var showSplash by remember { mutableStateOf(true) }
        var appStarted by remember { mutableStateOf(false) }

        val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
        val autoLoginChecked by viewModel.autoLoginChecked.collectAsStateWithLifecycle()

        if (showSplash) {
            SplashScreen(onTimeout = {
                showSplash = false
                appStarted = true
            })
        } else if (!appStarted || !autoLoginChecked) {
        } else {
            val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Onboarding.route
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            val showBottomBar = currentRoute in listOf(
                Screen.Home.route,
                Screen.Discover.route,
                Screen.Assistant.route,
                Screen.ChefList.route,
                Screen.Profile.route
            )

            // 全局错误提示：任何网络失败不再静默
            val snackbarHostState = remember { SnackbarHostState() }
            val globalErrorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
            LaunchedEffect(globalErrorMessage) {
                globalErrorMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
                    viewModel.clearErrorMessage()
                }
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = { if (showBottomBar) BottomNavBar(navController = navController) }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(onTimeout = {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        })
                    }

                    composable(Screen.Onboarding.route) {
                        OnboardingScreen(
                            onStart = {
                                navController.navigate(Screen.Login.route)
                            },
                            onSkip = {
                                navController.navigate(Screen.Login.route)
                            }
                        )
                    }

                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onTermsClick = { navController.navigate(Screen.Terms.route) },
                            onPrivacyClick = { navController.navigate(Screen.Privacy.route) },
                            // 复用 App 级共享 ViewModel，否则登录只写入登录页自己的实例，
                            // App 级 currentUser 仍为 null → 首页问候语用默认名、历史页空白
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Home.route) {
                        HomeScreen(
                            onRecipeClick = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                            },
                            onScanClick = { navController.navigate(Screen.Scan.route) },
                            onDiscoverClick = { category ->
                                viewModel.selectCategory(category)
                                navController.navigate(Screen.Discover.route)
                            },
                            onSeeAll = { navController.navigate(Screen.Discover.route) },
                            onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                            onHistoryClick = { navController.navigate(Screen.History.route) },
                            onShoppingListClick = { navController.navigate(Screen.ShoppingList.route) },
                            onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Search.route) {
                        SearchScreen(
                            onBack = { navController.popBackStack() },
                            onRecipeClick = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                            },
                            onChannelClick = { channel ->
                                viewModel.selectCategory(channel)
                                navController.navigate(Screen.Discover.route)
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Discover.route) {
                        DiscoverScreen(
                            onRecipeClick = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                            },
                            mode = DiscoverMode.All,
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Favorites.route) {
                        FavoritesListScreen(
                            onBack = { navController.popBackStack() },
                            onRecipeClick = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                            },
                            onFavoriteToggle = { viewModel.toggleFavorite(it.toLong()) },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.History.route) {
                        CookingHistoryScreen(
                            onBack = { navController.popBackStack() },
                            onRecipeClick = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.ShoppingList.route) {
                        ShoppingListScreen(
                            onBack = { navController.popBackStack() },
                            onFridgeClick = { navController.navigate(Screen.Fridge.route) },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Fridge.route) {
                        FridgeScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.PublishRecipe.route) {
                        PublishRecipeScreen(
                            onBack = { navController.popBackStack() },
                            onPublished = { navController.popBackStack() },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Scan.route) {
                        ScanScreen(
                            onClose = {
                                // 离开拍照流程时清空结果态，避免下次进入直接闪现旧结果
                                viewModel.resetScan()
                                navController.popBackStack()
                            },
                            onRecipeClick = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(
                        route = Screen.RecipeDetail.route,
                        arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                        RecipeDetailScreen(
                            recipeId = recipeId,
                            onBack = { navController.popBackStack() },
                            onStartCooking = { id ->
                                navController.navigate(Screen.CookingMonitor.createRoute(id))
                            },
                            onIngredientClick = { name ->
                                navController.navigate(Screen.IngredientDetail.createRoute(name))
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(
                        route = Screen.CookingMonitor.route,
                        arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                        CookingMonitorScreen(
                            recipeId = recipeId,
                            onBack = { navController.popBackStack() },
                            onComplete = { elapsedSeconds ->
                                navController.navigate(
                                    Screen.CookingComplete.createRoute(recipeId, elapsedSeconds)
                                ) {
                                    popUpTo(Screen.CookingMonitor.route) { inclusive = true }
                                }
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(
                        route = Screen.CookingComplete.route,
                        arguments = listOf(
                            navArgument("recipeId") { type = NavType.StringType },
                            navArgument("elapsedSeconds") {
                                type = NavType.IntType
                                defaultValue = 0
                            }
                        )
                    ) { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                        val elapsedSeconds = backStackEntry.arguments?.getInt("elapsedSeconds") ?: 0
                        CookingCompleteScreen(
                            recipeId = recipeId,
                            elapsedSeconds = elapsedSeconds,
                            onHome = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            },
                            onMoreRecipes = {
                                navController.navigate(Screen.Discover.route)
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(
                        route = Screen.IngredientDetail.route,
                        arguments = listOf(navArgument("ingredientName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val name = backStackEntry.arguments?.getString("ingredientName") ?: ""
                        IngredientDetailScreen(
                            ingredientName = name,
                            onBack = { navController.popBackStack() },
                            onRecipeClick = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.DietReport.route) {
                        DietReportScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Notifications.route) {
                        NotificationsScreen(
                            onBack = { navController.popBackStack() },
                            onRecipeClick = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId.toString()))
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.HelpFeedback.route) {
                        HelpFeedbackScreen(
                            onBack = { navController.popBackStack() },
                            onTermsClick = { navController.navigate(Screen.Terms.route) },
                            onPrivacyClick = { navController.navigate(Screen.Privacy.route) },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                            onHistoryClick = { navController.navigate(Screen.History.route) },
                            onDietReportClick = { navController.navigate(Screen.DietReport.route) },
                            onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                            onHelpFeedbackClick = { navController.navigate(Screen.HelpFeedback.route) },
                            onFridgeClick = { navController.navigate(Screen.Fridge.route) },
                            onPublishRecipeClick = { navController.navigate(Screen.PublishRecipe.route) },
                            onRecipeClick = { recipeId ->
                                navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                            },
                            onSettingsClick = { navController.navigate(Screen.Settings.route) },
                            onTastePreferenceEdit = {
                                navController.navigate(Screen.TastePreferenceEdit.route)
                            },
                            onTastePreferencesClick = {
                                navController.navigate(Screen.TastePreferences.route)
                            },
                            onLogout = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.ProfileEdit.route) {
                        ProfileEditScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onHelpFeedback = { navController.navigate(Screen.HelpFeedback.route) },
                            onAboutClick = { navController.navigate(Screen.About.route) },
                            onPrivacyClick = { navController.navigate(Screen.Privacy.route) }
                        )
                    }

                    composable(Screen.About.route) {
                        AboutScreen(
                            onBack = { navController.popBackStack() },
                            onTermsClick = { navController.navigate(Screen.Terms.route) },
                            onPrivacyClick = { navController.navigate(Screen.Privacy.route) },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.TastePreferenceEdit.route) {
                        TastePreferenceEditScreen(
                            onBack = { navController.popBackStack() },
                            currentPreferences = viewModel.tastePreferences.value,
                            onSave = { viewModel.updateTastePreferences(it) }
                        )
                    }

                    composable(Screen.Privacy.route) {
                        PrivacyScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Terms.route) {
                        TermsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.TastePreferences.route) {
                        TastePreferencesScreen(
                            onBack = { navController.popBackStack() },
                            currentPreferences = viewModel.tastePreferences.value,
                            currentAllergens = emptyList(),
                            onSave = { tastes, allergens ->
                                viewModel.updateTastePreferences(tastes)
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Screen.ChefList.route) {
                        ChefListScreen(
                            onChefClick = { chefId ->
                                navController.navigate(Screen.ChefDetail.createRoute(chefId))
                            },
                            viewModel = viewModel
                        )
                    }

                    composable(
                        route = Screen.ChefDetail.route,
                        arguments = listOf(navArgument("chefId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val chefId = backStackEntry.arguments?.getLong("chefId") ?: 0
                        ChefDetailScreen(
                            chefId = chefId,
                            onBack = { navController.popBackStack() },
                            viewModel = viewModel
                        )
                    }

                    composable(Screen.Assistant.route) {
                        AssistantScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

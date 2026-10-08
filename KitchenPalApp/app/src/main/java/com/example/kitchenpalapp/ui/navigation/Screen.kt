package com.example.kitchenpalapp.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Home : Screen("home")
    object Discover : Screen("discover")
    object Favorites : Screen("favorites")
    object History : Screen("history")
    object ShoppingList : Screen("shopping_list")
    object Fridge : Screen("fridge")
    object PublishRecipe : Screen("publish_recipe")
    object Search : Screen("search")
    object Scan : Screen("scan")
    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }
    object CookingMonitor : Screen("cooking_monitor/{recipeId}") {
        fun createRoute(recipeId: String) = "cooking_monitor/$recipeId"
    }
    object CookingComplete : Screen("cooking_complete/{recipeId}?elapsedSeconds={elapsedSeconds}") {
        fun createRoute(recipeId: String, elapsedSeconds: Int = 0) =
            "cooking_complete/$recipeId?elapsedSeconds=$elapsedSeconds"
    }
    object IngredientDetail : Screen("ingredient_detail/{ingredientName}") {
        fun createRoute(name: String) = "ingredient_detail/$name"
    }
    object DietReport : Screen("diet_report")
    object Notifications : Screen("notifications")
    object HelpFeedback : Screen("help_feedback")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object About : Screen("about")
    object Privacy : Screen("privacy")
    object Terms : Screen("terms")
    object TastePreferences : Screen("taste_preferences")
    object ProfileEdit : Screen("profile_edit")
    object TastePreferenceEdit : Screen("taste_preference_edit")
    object ChefList : Screen("chef_list")
    object ChefDetail : Screen("chef_detail/{chefId}") {
        fun createRoute(chefId: Long) = "chef_detail/$chefId"
    }
    object Assistant : Screen("assistant")
}

sealed class BottomNavItem(
    val route: String,
    val label: String
) {
    object Home : BottomNavItem("home", "首页")
    object Discover : BottomNavItem("discover", "发现")
    object Scan : BottomNavItem("scan", "拍照")
    object Assistant : BottomNavItem("assistant", "助手")
    object Chef : BottomNavItem("chef_list", "大厨")
    object Profile : BottomNavItem("profile", "我的")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Discover,
    BottomNavItem.Assistant,
    BottomNavItem.Scan,
    BottomNavItem.Profile
)

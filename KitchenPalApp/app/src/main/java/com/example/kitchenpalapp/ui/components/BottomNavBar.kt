package com.example.kitchenpalapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.navigation.BottomNavItem
import com.example.kitchenpalapp.ui.navigation.Screen
import com.example.kitchenpalapp.ui.navigation.bottomNavItems
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.WarmGray

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            val (iconRes, isScan) = when (item) {
                BottomNavItem.Home -> R.drawable.home to false
                BottomNavItem.Discover -> R.drawable.compass to false
                BottomNavItem.Chef -> R.drawable.chef_hat to false
                BottomNavItem.Assistant -> R.drawable.sparkles to false
                BottomNavItem.Scan -> R.drawable.camera to true
                BottomNavItem.Profile -> R.drawable.user to false
            }

            if (isScan) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(item.route) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .offset(y = (-4).dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(painter = painterResource(id = iconRes), contentDescription = item.label, colorFilter = ColorFilter.tint(androidx.compose.ui.graphics.Color.White), modifier = Modifier.size(24.dp))
                        }
                    },
                    label = { Text(item.label, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        unselectedIconColor = WarmGray,
                        unselectedTextColor = WarmGray,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            } else {
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.id) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Image(painter = painterResource(id = iconRes), contentDescription = item.label, colorFilter = ColorFilter.tint(if (selected) Primary else WarmGray), modifier = Modifier.size(24.dp))
                    },
                    label = { Text(item.label, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        unselectedIconColor = WarmGray,
                        unselectedTextColor = WarmGray,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        }
    }
}

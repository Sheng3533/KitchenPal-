package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.components.RecipeCard
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onFavoriteToggle: (Long) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val recipes by viewModel.recipes.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    var selectedFilter by remember { mutableStateOf("全部") }
    val filters = listOf("全部", "今天", "昨天", "7天内", "30天内")

    val historyRecipes = remember(recipes) {
        if (recipes.isNotEmpty()) {
            com.example.kitchenpalapp.data.RecipeConverter.toRecipes(recipes).take(10)
        } else {
            com.example.kitchenpalapp.data.SampleData.recipes.take(8)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        HistoryTopBar(
            onBack = onBack,
            count = historyRecipes.size
        )

        if (historyRecipes.isEmpty()) {
            EmptyHistoryState()
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                FilterTabs(
                    filters = filters,
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyRecipes) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            isFavorite = favoriteIds.contains(recipe.id.toLongOrNull() ?: 0L),
                            onClick = { onRecipeClick(recipe.id) },
                            onFavorite = { onFavoriteToggle(recipe.id.toLongOrNull() ?: 0L) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTopBar(
    onBack: () -> Unit,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceColor)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Background)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = "返回",
                colorFilter = ColorFilter.tint(TextPrimary),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "浏览历史",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$count 条记录",
                color = TextTertiary,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.clock),
                contentDescription = null,
                colorFilter = ColorFilter.tint(TextTertiary),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "还没有浏览记录",
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "快去发现美食吧",
                color = TextTertiary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun FilterTabs(
    filters: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selected == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (isSelected) Primary else SurfaceColor)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    color = if (isSelected) Color.White else TextTertiary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

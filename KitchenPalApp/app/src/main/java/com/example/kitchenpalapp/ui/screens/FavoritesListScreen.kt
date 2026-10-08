package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.Recipe
import com.example.kitchenpalapp.data.RecipeConverter
import com.example.kitchenpalapp.ui.components.rememberRecipePainter
import com.example.kitchenpalapp.ui.theme.softTint
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Border
import com.example.kitchenpalapp.ui.theme.BorderLight
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.SurfaceOverlay
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary

@Composable
fun FavoritesListScreen(
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onManageClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    // 必须订阅 StateFlow 才能触发重组：直接调 viewModel.getFavoriteRecipeList()
    // 读的是 MutableStateFlow.value（非 Compose 可观测状态），取消收藏后界面不会刷新，
    // 表现为「点击爱心没有响应」。
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val allRecipes by viewModel.recipes.collectAsState()
    val favoriteRecipes = remember(allRecipes, favoriteIds) {
        RecipeConverter.toRecipes(allRecipes.filter { favoriteIds.contains(it.id) })
    }
    var selectedTab by remember { mutableStateOf(0) }
    var isManageMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }
    val tabs = listOf("全部", "家常菜", "快手菜", "汤煲")

    // 取消收藏后清理已失效的勾选，避免「删除 (N)」计数与列表不符
    LaunchedEffect(favoriteRecipes) {
        val validIds = favoriteRecipes.mapTo(mutableSetOf()) { it.id }
        selectedIds.removeAll { it !in validIds }
        if (favoriteRecipes.isEmpty()) isManageMode = false
    }

    val filteredRecipes = remember(favoriteRecipes, selectedTab) {
        if (selectedTab == 0) {
            favoriteRecipes
        } else {
            favoriteRecipes.filter { it.category.contains(tabs[selectedTab]) || it.tags.any { tag -> tag.contains(tabs[selectedTab]) } }
        }
    }

    val doManage = {
        isManageMode = !isManageMode
        if (!isManageMode) {
            selectedIds.clear()
        }
    }

    val doFilter = {
    }

    val toggleSelectAll: () -> Unit = {
        if (selectedIds.size == filteredRecipes.size) {
            selectedIds.clear()
        } else {
            selectedIds.addAll(filteredRecipes.map { it.id })
        }
    }

    val deleteSelected = {
        selectedIds.forEach { onFavoriteToggle(it) }
        selectedIds.clear()
        isManageMode = false
    }

    val toggleSelect = { id: String ->
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        FavoritesTopBar(onBack = onBack, onManageClick = doManage)

        if (favoriteRecipes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(TextTertiary),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "还没有收藏食谱",
                        color = TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "快去发现喜欢的美食吧",
                        color = TextTertiary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                FilterRow(count = filteredRecipes.size, onFilterClick = doFilter)

                TabFilterRow(
                    tabs = tabs,
                    selected = selectedTab,
                    onSelect = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isManageMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(2.dp, if (selectedIds.size == filteredRecipes.size && filteredRecipes.isNotEmpty()) Primary else BorderLight)
                                    .background(if (selectedIds.size == filteredRecipes.size && filteredRecipes.isNotEmpty()) Primary else SurfaceColor)
                                    .clickable(onClick = toggleSelectAll),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedIds.size == filteredRecipes.size && filteredRecipes.isNotEmpty()) {
                                    Image(
                                        painter = painterResource(id = R.drawable.check),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.White),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "全选",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFF4D4F))
                                .clickable(onClick = deleteSelected)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "删除 (${selectedIds.size})",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (filteredRecipes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "该分类暂无收藏",
                            color = TextTertiary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        filteredRecipes.forEachIndexed { index, recipe ->
                            FavoriteRecipeCard(
                                recipe = recipe,
                                favTime = when (index % 4) {
                                    0 -> "收藏于 3天前"
                                    1 -> "收藏于 1周前"
                                    2 -> "收藏于 2周前"
                                    else -> "收藏于 1月前"
                                },
                                onClick = { onRecipeClick(recipe.id) },
                                onFavoriteToggle = { onFavoriteToggle(recipe.id) },
                                isManageMode = isManageMode,
                                isSelected = selectedIds.contains(recipe.id),
                                onSelect = { toggleSelect(recipe.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.move_horizontal),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(TextTertiary),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isManageMode) "点击选择要删除的食谱" else "点击爱心可取消收藏",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesTopBar(onBack: () -> Unit, onManageClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceOverlay)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = "返回",
                colorFilter = ColorFilter.tint(TextTertiary),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "我的收藏",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "管理",
            color = Primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onManageClick)
        )
    }
}

@Composable
private fun FilterRow(count: Int, onFilterClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "共 $count 个食谱",
            color = TextTertiary,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onFilterClick)
        ) {
            Text(
                text = "最近收藏",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(id = R.drawable.chevron_down),
                contentDescription = null,
                colorFilter = ColorFilter.tint(TextTertiary),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun TabFilterRow(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs.size) { index ->
            val isSelected = selected == index
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (isSelected) Primary else SurfaceOverlay)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabs[index],
                    color = if (isSelected) Color.White else TextTertiary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun FavoriteRecipeCard(
    recipe: Recipe,
    favTime: String,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    isManageMode: Boolean = false,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .clickable(onClick = if (isManageMode) onSelect else onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isManageMode) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(2.dp, if (isSelected) Primary else BorderLight)
                    .background(if (isSelected) Primary else SurfaceColor)
                    .clickable(onClick = onSelect),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Image(
                        painter = painterResource(id = R.drawable.check),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
        Image(
            painter = rememberRecipePainter(recipe.imageRes, recipe.image),
            contentDescription = recipe.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = recipe.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.clock),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(TextTertiary),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${recipe.cookTimeMin}分钟",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "·",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = recipe.difficulty,
                    color = TextTertiary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "·",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.star_filled),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xFFE8A33D)),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "%.1f".format(recipe.rating),
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = favTime,
                color = TextTertiary,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(softTint(Color(0xFFFDE8E8)))
                .clickable(onClick = onFavoriteToggle),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.heart_filled),
                contentDescription = "取消收藏",
                colorFilter = ColorFilter.tint(Color(0xFFD94B4B)),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

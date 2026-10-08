package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.Recipe
import com.example.kitchenpalapp.data.RecipeConverter
import com.example.kitchenpalapp.ui.components.RecipeCard
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.CreamWhite
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.WarmGray
import com.example.kitchenpalapp.ui.theme.WarmOrangeLighter
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel

enum class DiscoverMode { All, Favorites, History }

/** 发现页筛选面板的排序方式（客户端排序）。
 *  desc 用于弹窗副标题说明；badge 用于卡片的指标徽章，让用户能直观看出排序依据。 */
enum class DiscoverSort(val label: String, val desc: String) {
    DEFAULT("默认排序", "按推荐顺序"),
    RATING("评分最高", "高评分优先"),
    TIME("用时最短", "快手菜优先"),
    CALORIES("卡路里最低", "低热量优先");

    /** 从持久化字符串还原，非法值回退 DEFAULT。 */
    companion object {
        fun fromName(name: String?): DiscoverSort =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

/** 按当前排序维度给出卡片上要高亮的指标文案；默认排序不显示徽章。 */
private fun DiscoverSort.badgeFor(recipe: Recipe): String? = when (this) {
    DiscoverSort.DEFAULT -> null
    DiscoverSort.RATING -> "${recipe.rating}分"
    DiscoverSort.TIME -> "${recipe.cookTimeMin}分钟"
    DiscoverSort.CALORIES -> "${recipe.calories}卡"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onRecipeClick: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    mode: DiscoverMode = DiscoverMode.All,
    initialCategory: String = "全部",
    viewModel: MainViewModel = viewModel()
) {
    val query by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 订阅 StateFlow，避免读到 MutableStateFlow.value 的旧快照导致列表不刷新
    // 全量列表（收藏/历史按 id 反查用）与当前筛选结果分开，避免筛选污染全局数据
    val recipeData by viewModel.recipes.collectAsState()
    val filteredData by viewModel.filteredRecipes.collectAsState()
    val allRecipes = remember(recipeData) { RecipeConverter.toRecipes(recipeData) }
    val filteredRecipes = remember(filteredData) { RecipeConverter.toRecipes(filteredData) }
    val displayRecipes = when (mode) {
        DiscoverMode.All -> filteredRecipes
        DiscoverMode.Favorites -> {
            val ids = favoriteIds
            allRecipes.filter { ids.contains(it.id.toLongOrNull()) }
        }
        DiscoverMode.History -> allRecipes.take(3)
    }

    val title = when (mode) {
        DiscoverMode.All -> "发现食谱"
        DiscoverMode.Favorites -> "我的收藏"
        DiscoverMode.History -> "浏览历史"
    }

    var searchText by remember { mutableStateOf(query) }
    var showFilterSheet by remember { mutableStateOf(false) }
    // 排序方式持久化在 ViewModel（本地偏好），切换页面/重启 App 后仍保留
    val sortName by viewModel.discoverSort.collectAsState()
    val sortOption = DiscoverSort.fromName(sortName)

    // 输入即时本地过滤（回车仍走后端精确搜索），避免后端被打爆
    val searchedRecipes = remember(displayRecipes, query) {
        if (query.isBlank()) displayRecipes
        else displayRecipes.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    // 排序在客户端完成：评分为 Float，用时/卡路里为 Int，字段由后端保证非空默认值
    val sortedRecipes = remember(searchedRecipes, sortOption) {
        when (sortOption) {
            DiscoverSort.DEFAULT -> searchedRecipes
            DiscoverSort.RATING -> searchedRecipes.sortedByDescending { it.rating }
            DiscoverSort.TIME -> searchedRecipes.sortedBy { it.cookTimeMin }
            DiscoverSort.CALORIES -> searchedRecipes.sortedBy { it.calories }
        }
    }

    LaunchedEffect(initialCategory) {
        if (mode == DiscoverMode.All && initialCategory != "全部" && initialCategory != selectedCategory) {
            viewModel.selectCategory(initialCategory)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamWhite)
    ) {
        DiscoverHeader(
            title = title,
            showBack = onBack != null,
            onBack = onBack ?: {},
            filterActive = sortOption != DiscoverSort.DEFAULT,
            onFilterClick = { showFilterSheet = true }
        )
        SearchAndFilter(
            query = searchText,
            onQueryChange = {
                searchText = it
                viewModel.onSearchQueryChange(it)
            },
            onSearch = {
                viewModel.onSearchQueryChange(searchText)
                viewModel.performSearch()
            }
        )

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                FilterSheetContent(
                    mode = mode,
                    sortOption = sortOption,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSortSelected = { viewModel.saveSortOption(it.name) },
                    onCategorySelected = viewModel::selectCategory,
                    onApply = { showFilterSheet = false }
                )
            }
        }

        if (mode == DiscoverMode.All) {
            CategoryFilter(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = viewModel::selectCategory
            )
        }
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (searchedRecipes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "暂无相关食谱", color = WarmGray, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "共找到 ${sortedRecipes.size} 道食谱",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(sortedRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        isFavorite = favoriteIds.contains(recipe.id.toLongOrNull()),
                        onClick = { onRecipeClick(recipe.id.toString()) },
                        onFavorite = { recipe.id.toLongOrNull()?.let { viewModel.toggleFavorite(it) } },
                        isHorizontal = true,
                        sortBadge = sortOption.badgeFor(recipe)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoverHeader(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    filterActive: Boolean,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = "返回",
                        colorFilter = ColorFilter.tint(WarmGray),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        IconButton(onClick = onFilterClick) {
            Image(
                painter = painterResource(id = R.drawable.filter),
                contentDescription = "筛选",
                colorFilter = ColorFilter.tint(if (filterActive) Primary else WarmGray),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 筛选底部弹层：排序方式（客户端排序）+ 分类（仅「发现食谱」全量模式提供，
 * 收藏/历史模式分类已被数据源限定，再选分类没有意义）。
 * 选择即时生效，弹层不阻塞列表预览。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSheetContent(
    mode: DiscoverMode,
    sortOption: DiscoverSort,
    categories: List<String>,
    selectedCategory: String,
    onSortSelected: (DiscoverSort) -> Unit,
    onCategorySelected: (String) -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "筛选与排序",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "重置",
                color = Primary,
                fontSize = 13.sp,
                modifier = Modifier.clickable {
                    onSortSelected(DiscoverSort.DEFAULT)
                    onCategorySelected("全部")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "排序方式", fontSize = 13.sp, color = WarmGray)
        Spacer(modifier = Modifier.height(10.dp))
        // 两列等宽网格：每个选项带标签 + 副标题说明，比单行 FlowRow 更易读且不会挤成两行
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DiscoverSort.entries.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowItems.forEach { option ->
                        SortOptionCard(
                            option = option,
                            selected = option == sortOption,
                            onClick = { onSortSelected(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // 奇数个选项时补一个空占位，保持两列等宽对齐
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (mode == DiscoverMode.All && categories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(text = "分类", fontSize = 13.sp, color = WarmGray)
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        Button(
            onClick = onApply,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
            Text(text = "查看结果", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** 排序选项卡片：选中态用主色描边 + 浅色底，附一行说明文案。 */
@Composable
private fun SortOptionCard(
    option: DiscoverSort,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Primary else Surface)
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else WarmGray.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = option.label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = option.desc,
            fontSize = 11.sp,
            color = if (selected) Color.White.copy(alpha = 0.85f) else WarmGray
        )
    }
}

@Composable
private fun SearchAndFilter(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("搜索食谱、食材...", color = WarmGray, fontSize = 14.sp) },
            leadingIcon = {
                Image(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "搜索",
                    colorFilter = ColorFilter.tint(WarmGray),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onSearch() }
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = WarmOrangeLighter,
                focusedBorderColor = Primary,
                unfocusedContainerColor = Surface,
                focusedContainerColor = Surface
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CategoryFilter(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Primary else Surface)
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else WarmGray
                )
            }
        }
    }
}

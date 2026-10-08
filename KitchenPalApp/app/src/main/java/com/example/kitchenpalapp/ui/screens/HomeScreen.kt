package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.components.rememberRecipePainter
import com.example.kitchenpalapp.data.Recipe
import com.example.kitchenpalapp.data.RecipeConverter
import com.example.kitchenpalapp.ui.components.RecipeCard
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.CreamWhite
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.WarmBlack
import com.example.kitchenpalapp.ui.theme.WarmGray
import com.example.kitchenpalapp.ui.theme.WarmOrangeLighter
import com.example.kitchenpalapp.ui.theme.iconBackground
import com.example.kitchenpalapp.ui.theme.softTint
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import com.example.kitchenpalapp.network.RetrofitClient

@Composable
fun HomeScreen(
    onRecipeClick: (String) -> Unit,
    onScanClick: () -> Unit,
    onDiscoverClick: (String) -> Unit,
    onSeeAll: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onShoppingListClick: () -> Unit = {},
    onNotificationsClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    // 订阅 StateFlow 而非直接调用 viewModel.getRecipeList()，
    // 否则读到的只是 MutableStateFlow.value 快照，数据后到时界面不会刷新。
    val heroRecipeData by viewModel.heroRecipes.collectAsState()
    val recipeData by viewModel.recipes.collectAsState()
    val heroRecipe = remember(heroRecipeData) { RecipeConverter.toRecipes(heroRecipeData).firstOrNull() }
    val recipes = remember(recipeData) { RecipeConverter.toRecipes(recipeData) }
    val dailyImageUrl by viewModel.dailyImageUrl.collectAsState()
    val dailyImageTitle by viewModel.dailyImageTitle.collectAsState()
    val dailyImageRecipeId by viewModel.dailyImageRecipeId.collectAsState()
    // 每日推荐的真实菜谱：优先用 dailyImageRecipeId 在列表中找对应菜谱，否则用 hero 兜底
    val dailyRecipe = remember(dailyImageRecipeId, recipes) {
        dailyImageRecipeId?.let { id -> recipes.find { it.id.toLongOrNull() == id } }
    }
    val categories by viewModel.categories.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val cookingHistory by viewModel.cookingHistory.collectAsState()
    val streakDays = remember(cookingHistory) { MainViewModel.calculateStreakDays(cookingHistory) }
    val heroImageFull = dailyImageUrl?.let { RetrofitClient.BASE_URL.removeSuffix("/") + it }
    var showRandomPicker by remember { mutableStateOf(false) }

    if (showRandomPicker) {
        RandomRecipeDialog(
            recipes = recipes,
            onDismiss = { showRandomPicker = false },
            onRecipeClick = { recipe ->
                showRandomPicker = false
                onRecipeClick(recipe.id)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamWhite)
            .verticalScroll(rememberScrollState())
    ) {
        GreetingRow(
            userName = user?.nickname ?: "美食家",
            streakDays = streakDays,
            onNotificationsClick = onNotificationsClick
        )
        Spacer(modifier = Modifier.height(20.dp))
        QuickActionsRow(
            onScanClick = onScanClick,
            onRecommendClick = { showRandomPicker = true },
            onFavoritesClick = onFavoritesClick,
            onHistoryClick = onHistoryClick,
            onShoppingListClick = onShoppingListClick
        )
        Spacer(modifier = Modifier.height(20.dp))
        val heroSectionRecipe = dailyRecipe ?: heroRecipe
        if (heroSectionRecipe != null) {
            HeroSection(
                recipe = heroSectionRecipe,
                onClick = { onRecipeClick(dailyImageRecipeId?.toString() ?: heroSectionRecipe.id.toString()) },
                heroImageUrl = heroImageFull,
                heroTitle = dailyImageTitle
            )
        } else if (isLoading) {
            HeroSkeleton()
        }
        Spacer(modifier = Modifier.height(8.dp))
        CategorySection(
            categories = categories.filter { it != "全部" }.take(8),
            onCategoryClick = onDiscoverClick,
            onViewAll = { onDiscoverClick("全部") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        PopularRecipesSection(
            recipes = recipes.take(4),
            favoriteIds = favoriteIds,
            onRecipeClick = { onRecipeClick(it.id.toString()) },
            onFavoriteClick = { viewModel.toggleFavorite(it) },
            onSeeAll = onSeeAll,
            isLoading = isLoading
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun GreetingRow(userName: String, streakDays: Int, onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "你好，$userName 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "今天想做什么好吃的？",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(softTint(WarmOrangeLighter))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.flame),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Primary),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${streakDays}天",
                        fontSize = 13.sp,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onNotificationsClick) {
                Image(
                    painter = painterResource(id = R.drawable.bell),
                    contentDescription = "通知",
                    colorFilter = ColorFilter.tint(WarmBlack),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private data class QuickAction(
    val label: String,
    val iconRes: Int,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionsRow(
    onScanClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onShoppingListClick: () -> Unit = {}
) {
    val actions = listOf(
        QuickAction("扫食材", R.drawable.camera, Color(0xFFE8743B), onScanClick),
        QuickAction("吃什么", R.drawable.sparkles, Color(0xFFE8A33D), onRecommendClick),
        QuickAction("收藏", R.drawable.heart, Color(0xFFE8743B), onFavoritesClick),
        QuickAction("历史", R.drawable.clock, Color(0xFFE8743B), onHistoryClick),
        QuickAction("买菜", R.drawable.sprout, Color(0xFF4C9A6A), onShoppingListClick)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        actions.forEach { action ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = action.onClick)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(action.color.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = action.iconRes),
                        contentDescription = action.label,
                        colorFilter = ColorFilter.tint(action.color),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = action.label,
                    fontSize = 12.sp,
                    color = WarmBlack,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroSection(
    recipe: Recipe,
    onClick: () -> Unit,
    heroImageUrl: String? = null,
    heroTitle: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 0.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = rememberRecipePainter(recipe.imageRes, heroImageUrl ?: recipe.image),
            contentDescription = recipe.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                        startY = 150f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "今日推荐",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = heroTitle ?: recipe.title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = recipe.subtitle,
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.star_filled),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFFFFD700)),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "%.1f".format(recipe.rating),
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.clock),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.85f)),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${recipe.cookTimeMin}分钟",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.flame),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.85f)),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${recipe.calories}卡",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    categories: List<String>,
    onCategoryClick: (String) -> Unit,
    onViewAll: () -> Unit
) {
    if (categories.isEmpty()) return
    // 相邻两格保证不撞同一个图标
    val catData = ArrayList<Pair<String, CatStyle>>(categories.size)
    var prevIcon = 0
    categories.forEachIndexed { index, name ->
        val style = categoryStyle(name, index, prevIcon).also { prevIcon = it.icon }
        catData.add(name to style)
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "食材分类",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "查看全部",
                fontSize = 12.sp,
                color = Primary.copy(alpha = 0.7f),
                modifier = Modifier.clickable(onClick = onViewAll)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            val rows = catData.chunked(4)
            rows.forEachIndexed { rowIdx, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { item ->
                        CategoryItem(
                            name = item.first,
                            iconRes = item.second.icon,
                            bgColor = item.second.bg,
                            fgColor = item.second.fg,
                            onClick = { onCategoryClick(item.first) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // 末行不满 4 个：用等宽占位补齐，保证各行列宽一致（不再追加重复入口）
                    repeat(4 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** 分类格子的视觉：图标 + 柔和底色 + 同色系图标色 */
private data class CatStyle(val icon: Int, val bg: Color, val fg: Color)

/** 已知分类的专属图标与配色；未命中的走 CATEGORY_PALETTE 循环 */
private val CATEGORY_PRESETS: Map<String, CatStyle> = mapOf(
    "主食" to CatStyle(R.drawable.wheat, Color(0xFFFBF1D9), Color(0xFFC09A1C)),
    "汤煲" to CatStyle(R.drawable.soup, Color(0xFFFDF3E6), Color(0xFFCE8A38)),
    "家常菜" to CatStyle(R.drawable.soup, Color(0xFFFBF3E6), Color(0xFFCE8A38)),
    "家常热菜" to CatStyle(R.drawable.flame, Color(0xFFFDEBE5), Color(0xFFE5562C)),
    "川菜" to CatStyle(R.drawable.flame, Color(0xFFFDEBE5), Color(0xFFE5562C)),
    "湘菜" to CatStyle(R.drawable.flame, Color(0xFFFDECEA), Color(0xFFD4435C)),
    "凉菜" to CatStyle(R.drawable.salad, Color(0xFFE6F7ED), Color(0xFF2E9C6A)),
    "粤菜" to CatStyle(R.drawable.fish, Color(0xFFE5F1FB), Color(0xFF397FC0)),
    "海鲜" to CatStyle(R.drawable.fish, Color(0xFFE2F0FA), Color(0xFF2C74B8)),
    "浙菜" to CatStyle(R.drawable.fish, Color(0xFFE5F6F4), Color(0xFF2C8C86)),
    "鲁菜" to CatStyle(R.drawable.beef, Color(0xFFFBEDEA), Color(0xFFC25F48)),
    "苏菜" to CatStyle(R.drawable.beef, Color(0xFFF1EFE9), Color(0xFF8A7550)),
    "西餐" to CatStyle(R.drawable.beef, Color(0xFFEFECF9), Color(0xFF6A5CB8)),
    "早餐" to CatStyle(R.drawable.egg, Color(0xFFFFFBEF), Color(0xFFD9A327)),
    "烘焙" to CatStyle(R.drawable.cake_slice, Color(0xFFFDEFF0), Color(0xFFD2647F)),
    "甜品" to CatStyle(R.drawable.cake_slice, Color(0xFFFDEFF5), Color(0xFFC9558A)),
    "减脂餐" to CatStyle(R.drawable.sprout, Color(0xFFE8F7E9), Color(0xFF4E9B44)),
    "轻食" to CatStyle(R.drawable.sprout, Color(0xFFE7F6EA), Color(0xFF5CA84F)),
    "素食" to CatStyle(R.drawable.salad, Color(0xFFE8F7E8), Color(0xFF4E9B44)),
    "快手菜" to CatStyle(R.drawable.timer, Color(0xFFE9F2FB), Color(0xFF4A7FC4)),
    // 地区菜系：同图标、不同色，成组有节奏
    "东北菜" to CatStyle(R.drawable.leaf, Color(0xFFF7F1E7), Color(0xFFA8825A)),
    "东南亚" to CatStyle(R.drawable.leaf, Color(0xFFE8F2F8), Color(0xFF4F87A8)),
    "云南菜" to CatStyle(R.drawable.leaf, Color(0xFFF5EFF8), Color(0xFF9A5FA0)),
    "小吃" to CatStyle(R.drawable.flame, Color(0xFFFDF3E4), Color(0xFFD28B32))
)

/** 兜底调色板：色相均匀、明度一致，底色与图标同色系 */
private val CATEGORY_PALETTE = listOf(
    CatStyle(R.drawable.leaf, Color(0xFFFBEDE0), Color(0xFFE8743B)),
    CatStyle(R.drawable.flame, Color(0xFFFDF0EA), Color(0xFFD9542F)),
    CatStyle(R.drawable.wheat, Color(0xFFFBF3DB), Color(0xFFC19420)),
    CatStyle(R.drawable.salad, Color(0xFFE7F6EC), Color(0xFF3A9C68)),
    CatStyle(R.drawable.fish, Color(0xFFE6F0FA), Color(0xFF3A7BC4)),
    CatStyle(R.drawable.sprout, Color(0xFFF1EDFA), Color(0xFF6B5CC0))
)

/** 未知分类按位次取用调色板；与上一格图标相同时顺延，避免连着两个一样的图 */
private fun categoryStyle(name: String, index: Int, prevIcon: Int): CatStyle {
    CATEGORY_PRESETS[name]?.let { return it }
    var i = index
    var style = CATEGORY_PALETTE[i % CATEGORY_PALETTE.size]
    var guard = 0
    while (style.icon == prevIcon && guard++ < CATEGORY_PALETTE.size) {
        i++
        style = CATEGORY_PALETTE[i % CATEGORY_PALETTE.size]
    }
    return style
}

@Composable
private fun CategoryItem(
    name: String,
    iconRes: Int,
    bgColor: Color,
    fgColor: Color = Primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = name,
                colorFilter = ColorFilter.tint(fgColor),
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = name,
            fontSize = 12.sp,
            color = WarmBlack,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/** 骨架屏基础灰块：呼吸式透明度动画 */
@Composable
private fun SkeletonBlock(modifier: Modifier, cornerRadius: Int) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    Box(
        modifier = modifier
            .alpha(alpha)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(WarmGray.copy(alpha = 0.28f))
    )
}

/** 今日推荐骨架：220dp 大卡占位 */
@Composable
private fun HeroSkeleton() {
    SkeletonBlock(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(220.dp),
        cornerRadius = 20
    )
}

/** 热门食谱骨架：3 张竖版卡占位（比例对齐 RecipeCard） */
@Composable
private fun PopularSkeletonRow() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        items(3) {
            Column(modifier = Modifier.width(200.dp)) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    cornerRadius = 16
                )
                Spacer(modifier = Modifier.height(10.dp))
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                    cornerRadius = 6
                )
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBlock(
                    modifier = Modifier
                        .width(120.dp)
                        .height(14.dp),
                    cornerRadius = 6
                )
            }
        }
    }
}

@Composable
private fun PopularRecipesSection(
    recipes: List<Recipe>,
    favoriteIds: Set<Long>,
    onRecipeClick: (Recipe) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onSeeAll: () -> Unit,
    isLoading: Boolean
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "热门食谱",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onSeeAll) {
                Text(
                    text = "查看全部",
                    color = Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (isLoading) {
            PopularSkeletonRow()
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                content = {
                    items(recipes, key = { it.id }) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            isFavorite = favoriteIds.contains(recipe.id.toLongOrNull()),
                            onClick = { onRecipeClick(recipe) },
                            onFavorite = { recipe.id.toLongOrNull()?.let { onFavoriteClick(it) } },
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            )
        }
    }
}

/** "今天吃什么"随机推荐弹窗 */
@Composable
private fun RandomRecipeDialog(
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    var current by remember { mutableStateOf(recipes.randomOrNull()) }

    // 抽下一道：池中排除当前这道，避免「换一个」抽到同一道（用户会误以为按钮没反应）
    val pickNext: () -> Unit = {
        val pool = recipes.filter { it.id != current?.id }
        current = (pool.ifEmpty { recipes }).randomOrNull()
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "今天吃什么？",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (recipes.isEmpty()) "菜谱库是空的，先去逛逛吧"
                else "已从 ${recipes.size} 道菜谱中为你挑选",
                color = WarmGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            current?.let { recipe ->
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(178.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onRecipeClick(recipe) }
                ) {
                    Image(
                        painter = rememberRecipePainter(recipe.imageRes, recipe.image),
                        contentDescription = recipe.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                                    startY = 60f
                                )
                            )
                    )
                    // 右上角评分徽章
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.38f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.star_filled),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color(0xFFFFC24B)),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = String.format("%.1f", recipe.rating),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 3.dp)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = recipe.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.clock),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.85f)),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${recipe.cookTimeMin}分钟 · ${recipe.difficulty} · ${recipe.category}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            } ?: run {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(softTint(WarmOrangeLighter)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sparkles),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Primary),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = pickNext,
                    enabled = recipes.size > 1,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("换一个", fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { current?.let(onRecipeClick) },
                    enabled = current != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("就吃它", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }

            if (current != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("点击卡片查看详细做法", color = WarmGray, fontSize = 11.sp)
            }
        }
    }
}

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import com.example.kitchenpalapp.data.CookStep
import com.example.kitchenpalapp.data.Ingredient
import com.example.kitchenpalapp.data.Recipe
import com.example.kitchenpalapp.ui.components.StepTtsState
import com.example.kitchenpalapp.ui.components.rememberStepTts
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.SurfaceOverlay
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.theme.StateWarning
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBack: () -> Unit,
    onStartCooking: (String) -> Unit = {},
    onIngredientClick: (String) -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    // 详情解析：优先本地缓存（秒开）；未命中时回源后端 /api/recipe/{id} 拉取，
    // 保证「每日推荐」等按 id 进入的入口点击后一定能显示对应菜谱信息。
    var loadedRecipe by remember(recipeId) { mutableStateOf(viewModel.getRecipeByIdStr(recipeId)) }
    var detailLoading by remember(recipeId) { mutableStateOf(loadedRecipe == null) }
    androidx.compose.runtime.LaunchedEffect(recipeId) {
        if (loadedRecipe == null) {
            loadedRecipe = viewModel.fetchRecipeDetail(recipeId)
            detailLoading = false
        }
    }
    val recipe = loadedRecipe
    var selectedTab by remember { mutableStateOf(0) }
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isFavorite = recipe != null && favoriteIds.contains(recipe.id.toLongOrNull())
    val checkedStepsMap by viewModel.checkedSteps.collectAsState()
    val checkedSteps = checkedStepsMap[recipeId] ?: emptySet()
    val context = androidx.compose.ui.platform.LocalContext.current
    // 收藏是「无声操作」，统一用 Toast 给出明确反馈
    val showToast: (String) -> Unit = { msg ->
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    if (recipe == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center
        ) {
            if (detailLoading) {
                androidx.compose.material3.CircularProgressIndicator(color = Primary)
            } else {
                Text("食谱未找到", color = TextTertiary)
            }
        }
        return
    }

    val ingredients = recipe.ingredients
    val steps = recipe.steps

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                RecipeHero(
                    recipe = recipe,
                    onBack = onBack,
                    onFavorite = {
                        val id = recipe.id.toLongOrNull()
                        when {
                            id == null -> showToast("该食谱暂不支持收藏")
                            viewModel.currentUser.value == null -> showToast("请先登录后再收藏")
                            else -> {
                                val adding = !viewModel.isFavorite(id)
                                viewModel.toggleFavorite(id)
                                showToast(if (adding) "已加入收藏" else "已取消收藏")
                            }
                        }
                    },
                    onShare = {
                        com.example.kitchenpalapp.utils.ShareHelper.shareRecipe(
                            context,
                            recipe.title,
                            recipe.subtitle
                        )
                    },
                    isFavorite = isFavorite
                )
            }
            item { QuickStats(recipe = recipe) }
            item {
                TabSelector(
                    selectedTab = selectedTab,
                    onSelect = { selectedTab = it }
                )
            }
            item {
                when (selectedTab) {
                    0 -> IngredientsSection(
                        ingredients = ingredients,
                        onIngredientClick = onIngredientClick,
                        onAddToShoppingList = { viewModel.addToShoppingList(it) }
                    )
                    1 -> StepsSection(
                        steps = steps,
                        checkedSteps = checkedSteps,
                        onToggleStep = { index ->
                            viewModel.toggleStep(recipeId, index)
                        }
                    )
                }
            }
            item {
                val numericId = recipe.id.toLongOrNull()
                if (numericId != null) {
                    CommentsRatingSection(
                        recipeId = numericId,
                        currentUserId = viewModel.currentUser.value?.id,
                        showToast = showToast,
                        viewModel = viewModel
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        BottomCtaBar(
            checkedCount = checkedSteps.size,
            totalSteps = steps.size,
            onStartCooking = { onStartCooking(recipe.id.toString()) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun RecipeHero(
    recipe: Recipe,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    isFavorite: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
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
                        listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "返回",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(28.dp)
                )
            }
            Row {
                IconButton(onClick = onShare) {
                    Image(
                        painter = painterResource(id = R.drawable.share_2),
                        contentDescription = "分享",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onFavorite) {
                    Image(
                        painter = painterResource(id = if (isFavorite) R.drawable.heart_filled else R.drawable.heart),
                        contentDescription = "收藏",
                        colorFilter = ColorFilter.tint(if (isFavorite) Color(0xFFD94E4E) else Color.White),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
        ) {
            Row {
                recipe.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Primary.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Text(
                text = recipe.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = recipe.subtitle,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun QuickStats(recipe: Recipe) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .offset(y = (-24).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                iconRes = R.drawable.clock,
                value = "${recipe.cookTimeMin}分钟",
                label = "烹饪时长"
            )
            StatItem(
                iconRes = R.drawable.flame,
                value = "${recipe.calories}",
                label = "千卡"
            )
            StatItem(
                iconRes = R.drawable.star,
                value = "%.1f".format(recipe.rating),
                label = "评分"
            )
            StatItem(
                iconRes = R.drawable.chef_hat,
                value = recipe.difficulty,
                label = "难度"
            )
        }
    }
}

@Composable
private fun StatItem(iconRes: Int, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Primary),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextTertiary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun TabSelector(selectedTab: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf("食材", "步骤")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTab == index) Primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selectedTab == index) Color.White else TextTertiary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun IngredientsSection(
    ingredients: List<Ingredient>,
    onIngredientClick: (String) -> Unit,
    onAddToShoppingList: (List<Ingredient>) -> Unit = {}
) {
    var addedToShoppingList by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "所需食材",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${ingredients.size}种",
                color = TextTertiary,
                fontSize = 13.sp
            )
        }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ingredients.forEachIndexed { index, ingredient ->
                    IngredientRow(
                        ingredient = ingredient,
                        onClick = { onIngredientClick(ingredient.name) }
                    )
                    if (index < ingredients.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = SurfaceOverlay
                        )
                    }
                }
            }
        }

        // 一键加入购物清单
        Button(
            onClick = {
                onAddToShoppingList(ingredients)
                addedToShoppingList = true
            },
            enabled = ingredients.isNotEmpty() && !addedToShoppingList,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (addedToShoppingList) SurfaceOverlay else Primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(44.dp)
        ) {
            Image(
                painter = painterResource(id = if (addedToShoppingList) R.drawable.check else R.drawable.sprout),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (addedToShoppingList) "已加入购物清单" else "食材一键加入购物清单",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun IngredientRow(ingredient: Ingredient, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceOverlay),
            contentAlignment = Alignment.Center
        ) {
            val iconRes = when (ingredient.iconName) {
                "tomato" -> R.drawable.leaf
                "egg" -> R.drawable.egg
                "beef" -> R.drawable.beef
                "wheat" -> R.drawable.wheat
                "onion" -> R.drawable.leaf
                "garlic" -> R.drawable.leaf
                "flask" -> R.drawable.flask_round
                else -> R.drawable.leaf
            }
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Primary),
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                text = ingredient.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = ingredient.amount,
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            // 家常单位（勺/两/碗）自动折算成克/毫升，方便按需换算
            com.example.kitchenpalapp.util.UnitConverter.hintFromAmountText(ingredient.amount)?.let { hint ->
                Text(
                    text = hint,
                    fontSize = 11.sp,
                    color = Primary.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun StepsSection(
    steps: List<CookStep>,
    checkedSteps: Set<Int>,
    onToggleStep: (Int) -> Unit
) {
    val totalMinutes = steps.sumOf { it.durationSeconds } / 60
    val tts = rememberStepTts()
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Text(
                text = "烹饪步骤",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { if (tts.speaking) tts.stop() else tts.speakAll(steps) },
                modifier = Modifier.size(36.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_volume),
                    contentDescription = if (tts.speaking) "停止朗读" else "朗读步骤",
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "已完成 ${checkedSteps.size}/${steps.size}",
                color = TextTertiary,
                fontSize = 13.sp
            )
        }

        // 步骤概览：总步数 / 总时长 / 操作提示
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Primary.copy(alpha = 0.07f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.clock),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Primary),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = buildString {
                    append("共 ${steps.size} 步")
                    if (totalMinutes > 0) append(" · 约 $totalMinutes 分钟")
                    append(" · 建议按顺序操作")
                },
                color = Primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        steps.forEachIndexed { index, step ->
            StepCard(
                step = step,
                index = index,
                isDone = checkedSteps.contains(index),
                onToggle = { onToggleStep(index) },
                tts = tts
            )
        }
    }
}

@Composable
private fun StepCard(
    step: CookStep,
    index: Int,
    isDone: Boolean,
    onToggle: () -> Unit,
    tts: StepTtsState
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDone) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDone) Color(0xFF4CAF50) else Primary)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Image(
                        painter = painterResource(id = R.drawable.check),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = step.title.ifBlank { "步骤 ${index + 1}" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDone) TextTertiary else TextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (step.durationSeconds > 0) {
                        DurationChip(seconds = step.durationSeconds)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { tts.speakStep(step, index) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_volume),
                            contentDescription = "朗读这一步",
                            colorFilter = ColorFilter.tint(Primary),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (step.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = step.description,
                        fontSize = 14.sp,
                        color = if (isDone) TextTertiary else TextSecondary,
                        lineHeight = 21.sp
                    )
                }

                if (!step.heat.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HeatChip(heat = step.heat)
                }

                if (step.ingredients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    IngredientChips(items = step.ingredients)
                }

                if (!step.tip.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TipBox(tip = step.tip)
                }
            }
        }
    }
}

@Composable
private fun DurationChip(seconds: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(Primary.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.clock),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Primary),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formatStepDuration(seconds),
            color = Primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HeatChip(heat: String) {
    // "无需火候""常温"类步骤不再加「火候 ·」前缀，避免语义重复
    val label = if (heat.contains("无需") || heat.contains("常温")) heat else "火候 · $heat"
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StateWarning.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.flame),
            contentDescription = null,
            colorFilter = ColorFilter.tint(StateWarning),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = StateWarning,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun IngredientChips(items: List<String>) {
    Column {
        Text(
            text = "本步用料",
            color = TextTertiary,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { name ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Primary.copy(alpha = 0.06f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = name,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TipBox(tip: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceOverlay)
            .padding(10.dp)
    ) {
        Text(
            text = "小贴士",
            color = Primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tip,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

private fun formatStepDuration(seconds: Int): String {
    if (seconds <= 0) return ""
    val minutes = seconds / 60
    val secs = seconds % 60
    return when {
        minutes > 0 && secs > 0 -> "${minutes}分${secs}秒"
        minutes > 0 -> "${minutes}分钟"
        else -> "${secs}秒"
    }
}

@Composable
private fun BottomCtaBar(
    checkedCount: Int,
    totalSteps: Int,
    onStartCooking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Background.copy(alpha = 0.95f)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Button(
            onClick = onStartCooking,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.play_circle),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "开始烹饪",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$checkedCount/$totalSteps 准备完成",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ==================== 评论 & 评分 ====================

@Composable
private fun CommentsRatingSection(
    recipeId: Long,
    currentUserId: Long?,
    showToast: (String) -> Unit,
    viewModel: MainViewModel
) {
    val comments by viewModel.comments.collectAsState()
    val ratings by viewModel.recipeRatings.collectAsState()
    val submitting by viewModel.commentSubmitting.collectAsState()
    var input by remember { mutableStateOf("") }
    var myStars by remember { mutableStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(recipeId) {
        viewModel.loadComments(recipeId)
        viewModel.loadRatings(recipeId)
    }

    val avg = if (ratings.isEmpty()) 0.0 else ratings.map { it.rating }.average()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        HorizontalDivider(color = SurfaceOverlay)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "用户评价",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (ratings.isNotEmpty()) {
                Text(
                    text = String.format("%.1f", avg),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = " · ${ratings.size} 人评分",
                    fontSize = 13.sp,
                    color = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 我的评分
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "我的评分：", fontSize = 14.sp, color = TextSecondary)
            for (i in 1..5) {
                val filled = i <= myStars
                Image(
                    painter = painterResource(id = if (filled) R.drawable.star_filled else R.drawable.star),
                    contentDescription = "$i 星",
                    colorFilter = if (filled) ColorFilter.tint(Primary) else null,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(horizontal = 2.dp)
                        .clickable {
                            if (currentUserId == null) {
                                showToast("请先登录后再评分")
                            } else {
                                myStars = i
                                viewModel.submitRating(recipeId, i) { ok, msg ->
                                    showToast(msg)
                                    if (ok) {
                                        // 刷新平均分
                                        viewModel.loadRatings(recipeId)
                                    }
                                }
                            }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 发评论输入框
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("说点什么…", fontSize = 14.sp, color = TextTertiary) },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (currentUserId == null) {
                        showToast("请先登录后再评论")
                    } else if (input.isBlank()) {
                        showToast("评论内容不能为空")
                    } else {
                        viewModel.submitComment(recipeId, input.trim()) { ok, msg ->
                            showToast(msg)
                            if (ok) input = ""
                        }
                    }
                },
                enabled = !submitting,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (submitting) "发送中" else "发表", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (comments.isEmpty()) {
            Text(
                text = "还没有评论，来抢沙发～",
                fontSize = 14.sp,
                color = TextTertiary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            comments.forEach { c ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = c.userName.take(1).ifBlank { "用" },
                            color = Primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = c.userName.ifBlank { "匿名用户" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            if (currentUserId != null && c.userId == currentUserId) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "删除",
                                    fontSize = 12.sp,
                                    color = TextTertiary,
                                    modifier = Modifier.clickable {
                                        viewModel.removeComment(c.id) { ok, msg ->
                                            showToast(msg)
                                        }
                                    }
                                )
                            }
                        }
                        if (!c.createdAt.isNullOrBlank()) {
                            Text(
                                text = c.createdAt.replace("T", " ").take(16),
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        }
                        Text(
                            text = c.content,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

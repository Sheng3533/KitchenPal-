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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.components.rememberRecipePainter
import com.example.kitchenpalapp.data.IngredientDetailData
import com.example.kitchenpalapp.data.Recipe
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Border
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.PrimaryLightest
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.SurfaceOverlay
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary

@Composable
fun IngredientDetailScreen(
    ingredientName: String,
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    // 全部内容由真实菜谱数据推导：哪些菜用到它、和谁最常一起出现。
    // 之前这里无视 ingredientName、直接读 SampleData.ingredientInfo，
    // 导致点任何食材看到的都是同一份写死的信息。
    val allRecipes by viewModel.recipes.collectAsState()
    val detail = remember(ingredientName, allRecipes) {
        viewModel.buildIngredientDetail(ingredientName)
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        // Sticky top bar
        IngredientTopBar(onBack = onBack)

        if (detail.recipeCount == 0) {
            EmptyIngredientState(name = ingredientName)
            return@Column
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            IngredientHero(
                name = detail.name,
                subtitle = "在 ${detail.recipeCount} 道菜谱中出现",
                tag = detail.topCategory,
                coverImageUrl = detail.coverImageUrl
            )

            IngredientStatCard(
                recipeCount = detail.recipeCount,
                pairingCount = detail.pairings.size,
                avgCalories = detail.avgCalories
            )

            PairingSection(pairings = detail.pairings)

            RelatedRecipesSection(
                recipes = detail.recipes,
                onRecipeClick = onRecipeClick
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/** 数据库里没有这道食材时给明确提示，而不是拿别的食材的信息糊弄。 */
@Composable
private fun EmptyIngredientState(name: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.leaf),
            contentDescription = null,
            colorFilter = ColorFilter.tint(TextTertiary),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无「$name」的资料",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "现有菜谱里还没有用到这个食材",
            color = TextTertiary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun IngredientTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceOverlay)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = "返回",
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(TextPrimary)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "食材百科",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun IngredientHero(
    name: String,
    subtitle: String,
    tag: String,
    coverImageUrl: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(208.dp)
    ) {
        Image(
            // 封面用「第一道用到这个食材的菜谱图」，比通用占位图更贴合
            painter = rememberRecipePainter(imageRes = null, imageUrl = coverImageUrl),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        )
        // Dark gradient overlay at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                    )
                )
        )
        // Category tag pill at top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.3f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = tag,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // Ingredient name at bottom-left
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** 三项真实指标：用到它的菜谱数 / 常见搭配数 / 相关菜谱平均热量。 */
@Composable
private fun IngredientStatCard(
    recipeCount: Int,
    pairingCount: Int,
    avgCalories: Int
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-16).dp)
            .zIndex(1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickInfoColumn(
                label = "相关菜谱",
                value = "$recipeCount 道",
                modifier = Modifier.weight(1f)
            )
            VerticalDivider()
            QuickInfoColumn(
                label = "常见搭配",
                value = "$pairingCount 种",
                modifier = Modifier.weight(1f)
            )
            VerticalDivider()
            QuickInfoColumn(
                label = "平均热量",
                value = "$avgCalories 千卡",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickInfoColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextTertiary
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(Border)
    )
}

@Composable
private fun PairingSection(
    pairings: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "常见搭配",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "统计自现有菜谱，数字越靠前出现得越多",
            fontSize = 12.sp,
            color = TextTertiary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (pairings.isEmpty()) {
            Text(
                text = "暂无搭配数据",
                fontSize = 13.sp,
                color = TextTertiary
            )
        } else {
            // 每行 4 个，避免条目多时溢出屏幕
            pairings.chunked(4).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    rowItems.forEach { pairing ->
                        PairingPill(
                            text = pairing,
                            bgColor = SurfaceOverlay,
                            textColor = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PairingPill(
    text: String,
    bgColor: Color,
    textColor: Color,
    showWarningIcon: Boolean = false
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showWarningIcon) {
            Image(
                painter = painterResource(id = R.drawable.x),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(textColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            fontSize = 13.sp,
            color = textColor
        )
    }
}

@Composable
private fun RelatedRecipesSection(
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "相关食谱",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            // 原来是「查看全部」的纯文字（点了没反应），改成真实数量
            Text(
                text = "共 ${recipes.size} 道",
                fontSize = 13.sp,
                color = TextTertiary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { recipe ->
                RelatedRecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id) }
                )
            }
        }
    }
}

@Composable
private fun RelatedRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = rememberRecipePainter(recipe.imageRes, recipe.image),
                contentDescription = recipe.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = recipe.title,
            fontSize = 13.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(50))
                .background(PrimaryLightest)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "匹配 ${recipe.matchPercent}%",
                fontSize = 11.sp,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

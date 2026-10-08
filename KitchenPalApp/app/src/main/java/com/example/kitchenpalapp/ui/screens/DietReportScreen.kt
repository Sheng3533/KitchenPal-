package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.CategoryDist
import com.example.kitchenpalapp.data.CookingStat
import com.example.kitchenpalapp.data.DietReportData
import com.example.kitchenpalapp.data.NutritionIntake
import com.example.kitchenpalapp.data.SampleData
import com.example.kitchenpalapp.data.SuggestionItem
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.StateInfo
import com.example.kitchenpalapp.ui.theme.StateSuccess
import com.example.kitchenpalapp.ui.theme.StateWarning
import androidx.compose.material3.Surface
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.SurfaceOverlay
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary

@Composable
fun DietReportScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = viewModel(),
    userId: Long? = null
) {
    val userStats by viewModel.userStats.collectAsState()
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(userId) {
        viewModel.loadUserStats(userId ?: 1L)
    }

    val report = if (userStats.isEmpty()) {
        SampleData.dietReport
    } else {
        convertUserStatsToReport(userStats)
    }

    if (userStats.isEmpty() && viewModel.isLoading.collectAsState().value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            DietReportTopBar(onBack = onBack, context = context)
            PeriodSelector()
            ScoreCard(score = report.score, scoreDelta = report.scoreDelta)
            NutritionSection(items = report.nutrition)
            CookingStatsSection(stats = report.cookingStats)
            CategoryDistributionSection(items = report.categoryDistribution)
            SuggestionsSection(items = report.suggestions)
        }
    }
}

private fun convertUserStatsToReport(stats: Map<String, Any>): DietReportData {
    val totalCooking = (stats["totalCooking"] as? Number)?.toInt() ?: 0
    val totalFavorites = (stats["totalFavorites"] as? Number)?.toInt() ?: 0
    val totalCalories = (stats["totalCalories"] as? Number)?.toInt() ?: 0
    val streakDays = (stats["streakDays"] as? Number)?.toInt() ?: 0

    val weeklyCooking = (stats["weeklyCooking"] as? List<*>)?.filterIsInstance<Number>()?.map { it.toInt() } ?: listOf(0, 0, 0, 0, 0, 0, 0)

    val topCategoriesRaw = stats["topCategories"] as? List<*>
    val topCategories = topCategoriesRaw?.mapNotNull { item ->
        if (item is Map<*, *>) {
            val name = item["name"] as? String ?: "未知"
            val percent = (item["percent"] as? Number)?.toInt() ?: 0
            val color = (item["color"] as? Number)?.toLong() ?: 0xFFE8743B
            CategoryDist(name, percent, color)
        } else null
    } ?: listOf(
        CategoryDist("家常菜", 42, 0xFFE8743B),
        CategoryDist("快手菜", 28, 0xFF8B5E3C),
        CategoryDist("汤煲", 18, 0xFF4A9D5B),
        CategoryDist("烘焙", 12, 0xFFD97B93)
    )

    val nutritionTrend = stats["nutritionTrend"] as? Map<*, *>
    val proteinCurrent = (nutritionTrend?.get("proteinCurrent") as? Number)?.toInt() ?: 62
    val proteinTarget = (nutritionTrend?.get("proteinTarget") as? Number)?.toInt() ?: 80
    val carbsCurrent = (nutritionTrend?.get("carbsCurrent") as? Number)?.toInt() ?: 195
    val carbsTarget = (nutritionTrend?.get("carbsTarget") as? Number)?.toInt() ?: 300
    val fatCurrent = (nutritionTrend?.get("fatCurrent") as? Number)?.toInt() ?: 32
    val fatTarget = (nutritionTrend?.get("fatTarget") as? Number)?.toInt() ?: 70
    val fiberCurrent = (nutritionTrend?.get("fiberCurrent") as? Number)?.toInt() ?: 23
    val fiberTarget = (nutritionTrend?.get("fiberTarget") as? Number)?.toInt() ?: 25

    val score = (stats["score"] as? Number)?.toInt() ?: 85
    val scoreDelta = (stats["scoreDelta"] as? Number)?.toInt() ?: 3

    val avgCookTime = (stats["avgCookTime"] as? Number)?.toInt() ?: 35
    val perfectHeatRate = (stats["perfectHeatRate"] as? Number)?.toInt() ?: 91

    val cookingStats = listOf(
        CookingStat(totalCooking.toString(), "次", "烹饪", "chef_hat", 0xFFE8743B),
        CookingStat(totalFavorites.toString(), "道", "收藏菜谱", "sparkles", 0xFFE8A33D),
        CookingStat(avgCookTime.toString(), "min", "平均用时", "clock", 0xFF5B8EC9),
        CookingStat(perfectHeatRate.toString(), "%", "火候完美率", "flame", 0xFFD94B4B)
    )

    val nutrition = listOf(
        NutritionIntake("蛋白质", "${proteinCurrent}g", "${proteinTarget}g",
            (proteinCurrent * 100 / proteinTarget.coerceAtLeast(1)), "beef"),
        NutritionIntake("碳水化合物", "${carbsCurrent}g", "${carbsTarget}g",
            (carbsCurrent * 100 / carbsTarget.coerceAtLeast(1)), "wheat"),
        NutritionIntake("脂肪", "${fatCurrent}g", "${fatTarget}g",
            (fatCurrent * 100 / fatTarget.coerceAtLeast(1)), "droplets"),
        NutritionIntake("膳食纤维", "${fiberCurrent}g", "${fiberTarget}g",
            (fiberCurrent * 100 / fiberTarget.coerceAtLeast(1)), "salad",
            achieved = fiberCurrent >= fiberTarget)
    )

    val suggestions = listOf(
        SuggestionItem("增加蛋白质摄入", "建议在日常饮食中增加豆类、鸡蛋等高蛋白食材"),
        SuggestionItem("尝试更多汤煲", "推荐山药鸡汤，滋补养生，适合本周烹饪")
    )

    return DietReportData(
        score = score,
        scoreDelta = scoreDelta,
        nutrition = nutrition,
        cookingStats = cookingStats,
        categoryDistribution = topCategories,
        suggestions = suggestions
    )
}

@Composable
private fun DietReportTopBar(onBack: () -> Unit, context: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Image(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = "返回",
                colorFilter = ColorFilter.tint(TextSecondary),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "饮食报告",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {
            com.example.kitchenpalapp.utils.ShareHelper.shareDietReport(
                context,
                "饮食报告",
                "本周共摄入 15800 千卡，营养均衡度 85%"
            )
        }) {
            Image(
                painter = painterResource(id = R.drawable.share_2),
                contentDescription = "分享",
                colorFilter = ColorFilter.tint(TextSecondary),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PeriodSelector() {
    var selectedPeriod by remember { mutableStateOf(0) }
    val periods = listOf("本周", "本月", "本年")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceOverlay)
                .padding(4.dp)
        ) {
            periods.forEachIndexed { index, label ->
                val isSelected = selectedPeriod == index
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Primary else SurfaceColor)
                        .clickable { selectedPeriod = index }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreCard(score: Int, scoreDelta: Int) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScoreGauge(score = score)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "↑",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StateSuccess
                )
                Text(
                    text = "较上周提升 $scoreDelta 分",
                    fontSize = 13.sp,
                    color = StateSuccess,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ScoreGauge(score: Int) {
    // Canvas 的绘制 lambda 不是 @Composable，颜色需先在此取出
    val trackColor = SurfaceOverlay
    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 12.dp.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            val sweep = (score / 100f) * 360f
            drawArc(
                color = Primary,
                startAngle = 90f + (360f - sweep) / 2f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "健康评分",
                fontSize = 13.sp,
                color = TextTertiary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun NutritionSection(items: List<NutritionIntake>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SectionTitle(title = "营养摄入")
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                items.forEachIndexed { index, item ->
                    NutritionRow(item = item)
                    if (index < items.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionRow(item: NutritionIntake) {
    val iconColor = nutritionIconColor(item.icon)
    val iconRes = nutritionIconRes(item.icon)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = item.name,
                    colorFilter = ColorFilter.tint(iconColor),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${item.current}/${item.target}",
                fontSize = 13.sp,
                color = TextSecondary
            )
            if (item.achieved) {
                Image(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = "已达成",
                    colorFilter = ColorFilter.tint(StateSuccess),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ProgressBar(percent = item.percent, color = iconColor)
    }
}

@Composable
private fun CookingStatsSection(stats: List<CookingStat>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SectionTitle(title = "烹饪统计")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CookingStatCard(stat = stats[0], modifier = Modifier.weight(1f))
                CookingStatCard(stat = stats[1], modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CookingStatCard(stat = stats[2], modifier = Modifier.weight(1f))
                CookingStatCard(stat = stats[3], modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CookingStatCard(stat: CookingStat, modifier: Modifier = Modifier) {
    val statColor = Color(stat.color)
    val iconRes = cookingIconRes(stat.icon)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceOverlay)
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(statColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = stat.label,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stat.value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = stat.unit,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                )
            }
            Text(
                text = stat.label,
                fontSize = 12.sp,
                color = TextTertiary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun CategoryDistributionSection(items: List<CategoryDist>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SectionTitle(title = "本周菜谱分布")
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                items.forEachIndexed { index, item ->
                    CategoryDistRow(item = item)
                    if (index < items.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDistRow(item: CategoryDist) {
    val color = Color(item.color)
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${item.percent}%",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ProgressBar(percent = item.percent, color = color)
    }
}

@Composable
private fun SuggestionsSection(items: List<SuggestionItem>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SectionTitle(title = "改善建议")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { item ->
                SuggestionCard(item = item)
            }
        }
    }
}

@Composable
private fun SuggestionCard(item: SuggestionItem) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Primary)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(percent: Int, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(SurfaceOverlay)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color)
        )
    }
}

private fun nutritionIconRes(icon: String): Int = when (icon) {
    "beef" -> R.drawable.beef
    "wheat" -> R.drawable.wheat
    "droplets" -> R.drawable.droplets
    "salad" -> R.drawable.salad
    else -> R.drawable.salad
}

private fun nutritionIconColor(icon: String): Color = when (icon) {
    "beef" -> Primary
    "wheat" -> StateWarning
    "droplets" -> StateInfo
    "salad" -> StateSuccess
    else -> Primary
}

private fun cookingIconRes(icon: String): Int = when (icon) {
    "chef_hat" -> R.drawable.chef_hat
    "sparkles" -> R.drawable.sparkles
    "clock" -> R.drawable.clock
    "flame" -> R.drawable.flame
    else -> R.drawable.chef_hat
}

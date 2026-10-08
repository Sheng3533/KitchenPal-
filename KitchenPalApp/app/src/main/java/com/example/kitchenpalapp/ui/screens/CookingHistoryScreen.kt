package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.components.rememberRecipePainter
import com.example.kitchenpalapp.data.CookingHistoryData
import com.example.kitchenpalapp.data.CookingHistoryEntry
import com.example.kitchenpalapp.data.RecipeConverter
import com.example.kitchenpalapp.ui.theme.softTint
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.SurfaceOverlay
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.theme.StateSuccess
import com.example.kitchenpalapp.ui.theme.StateWarning
import com.example.kitchenpalapp.ui.theme.StateInfo
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel

@Composable
fun CookingHistoryScreen(
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onCalendarClick: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val rawHistory by viewModel.cookingHistory.collectAsState()
    val history = remember(rawHistory) { RecipeConverter.toCookingHistoryEntries(rawHistory) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("全部") }

    // 进页面刷新一次真实历史（登录态下 ViewModel 里可能还是空的）
    LaunchedEffect(Unit) { viewModel.loadHistory() }

    // 日期选项由真实数据生成（日期 → 当天烹饪次数），不再写死「7月11日/7月9日/7月7日」
    val dateOptions = remember(history) {
        val counts = history.groupingBy { it.date }.eachCount()
        listOf("全部" to history.size) + counts.map { (date, count) -> date to count }
    }

    val filteredHistory = if (selectedDate == "全部") {
        history
    } else {
        history.filter { it.date == selectedDate }
    }

    // 按真实日期分组（顺序 = 时间倒序，最新在最上）
    val groups = filteredHistory.groupBy { it.date }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        HistoryTopBar(onBack = onBack, onCalendarClick = { showCalendarDialog = true })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            StatsCard(data = rawHistory)

            Spacer(modifier = Modifier.height(24.dp))

            if (filteredHistory.isEmpty()) {
                EmptyHistoryState(hasAnyRecord = history.isNotEmpty(), onReset = { selectedDate = "全部" })
            } else {
                groups.forEach { (date, entries) ->
                    TimelineGroup(
                        date = date,
                        dayLabel = entries.firstOrNull()?.dayLabel.orEmpty(),
                        isToday = entries.any { it.dayLabel == "今天" },
                        entries = entries,
                        onRecipeClick = onRecipeClick
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    if (showCalendarDialog) {
        CalendarDialog(
            selectedDate = selectedDate,
            dates = dateOptions,
            onDateSelected = { selectedDate = it },
            onConfirm = { showCalendarDialog = false },
            onReset = { selectedDate = "全部" }
        )
    }
}

/** 空态：区分「一条记录都没有」和「当前筛选没结果」，避免用户以为功能坏了。 */
@Composable
private fun EmptyHistoryState(hasAnyRecord: Boolean, onReset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (hasAnyRecord) "该日期暂无烹饪记录" else "还没有烹饪记录",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (hasAnyRecord) "换个日期看看" else "完成一次烹饪后会自动记录在这里",
            color = TextTertiary,
            fontSize = 13.sp
        )
        if (hasAnyRecord) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onReset) {
                Text("查看全部记录", color = Primary)
            }
        }
    }
}

@Composable
private fun CalendarDialog(
    selectedDate: String,
    dates: List<Pair<String, Int>>,
    onDateSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onReset: () -> Unit
) {
    val dayCount = dates.count { it.first != "全部" }
    AlertDialog(
        onDismissRequest = onConfirm,
        title = {
            Column {
                Text(
                    text = "选择日期",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (dayCount > 0) "共 $dayCount 天有烹饪记录" else "暂无可筛选的记录",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            // 两列等宽网格：避免原先单列 chip 宽度参差、右侧大片留白
            Column(
                modifier = Modifier
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                dates.chunked(2).forEachIndexed { rowIndex, rowDates ->
                    if (rowIndex > 0) Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowDates.forEach { (date, count) ->
                            DateOptionChip(
                                label = date,
                                count = count,
                                selected = selectedDate == date,
                                onClick = { onDateSelected(date) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 奇数个时补空位，保证最后一行与上一行等宽对齐
                        if (rowDates.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text("确定", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text("重置", color = TextSecondary, fontSize = 14.sp)
            }
        },
        containerColor = SurfaceColor
    )
}

/** 日期选项胶囊：上行日期、下行当天烹饪次数；选中态填充主色。 */
@Composable
private fun DateOptionChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (selected) Primary else SurfaceOverlay
    val labelColor = if (selected) Color.White else TextPrimary
    val countColor = if (selected) Color.White.copy(alpha = 0.85f) else TextTertiary
    Column(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
        Text(
            text = "$count 次",
            color = countColor,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun HistoryTopBar(onBack: () -> Unit, onCalendarClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceColor)
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
        Text(
            text = "烹饪历史",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceColor)
                .clickable(onClick = onCalendarClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.calendar),
                contentDescription = "日历",
                colorFilter = ColorFilter.tint(TextPrimary),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * 烹饪统计：全部数字由真实烹饪历史算出（原来写死 28次/14h/5道/89%）。
 * 一次记录都没有时显示 0，不再编造数据。
 */
@Composable
private fun StatsCard(data: List<CookingHistoryData>) {
    val count = data.size
    val totalHours = data.sumOf { it.durationMin } / 60
    val dishCount = data.map { it.recipeId }.distinct().size
    val perfectRate = if (count == 0) 0 else data.count { it.score >= 90 } * 100 / count

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.calendar),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "烹饪统计",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBox(
                    iconRes = R.drawable.chef_hat,
                    value = count.toString(),
                    unit = "次",
                    label = "烹饪",
                    iconColor = Primary,
                    iconBgColor = Color(0xFFFFF6ED)
                )
                StatBox(
                    iconRes = R.drawable.clock,
                    value = totalHours.toString(),
                    unit = "h",
                    label = "总时长",
                    iconColor = StateInfo,
                    iconBgColor = Color(0xFFE8F0FA)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBox(
                    iconRes = R.drawable.sparkles,
                    value = dishCount.toString(),
                    unit = "道",
                    label = "做过菜品",
                    iconColor = StateWarning,
                    iconBgColor = Color(0xFFFFF4E0)
                )
                StatBox(
                    iconRes = R.drawable.trophy,
                    value = perfectRate.toString(),
                    unit = "%",
                    label = "90分以上",
                    iconColor = StateSuccess,
                    iconBgColor = Color(0xFFE8F5EA)
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    iconRes: Int,
    value: String,
    unit: String,
    label: String,
    iconColor: Color,
    iconBgColor: Color
) {
    Column(
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(iconColor),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                color = TextTertiary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
        Text(
            text = label,
            color = TextTertiary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun TimelineGroup(
    date: String,
    dayLabel: String,
    isToday: Boolean,
    entries: List<CookingHistoryEntry>,
    onRecipeClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(
                text = date,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(10.dp))
            if (isToday) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(softTint(Color(0xFFFFE8D6)))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayLabel,
                        color = Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = dayLabel,
                    color = TextTertiary,
                    fontSize = 13.sp
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            entries.forEachIndexed { index, entry ->
                TimelineEntry(
                    entry = entry,
                    isLast = index == entries.size - 1,
                    onClick = { onRecipeClick(entry.recipeId) }
                )
            }
        }
    }
}

@Composable
private fun TimelineEntry(
    entry: CookingHistoryEntry,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(48.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(140.dp)
                        .padding(top = 20.dp)
                        .background(softTint(Color(0xFFFFE4D0)))
                )
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(softTint(Color(0xFFFFE4D0)))
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Primary)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp, top = 0.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceColor)
                .clickable(onClick = onClick)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.recipeName,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = entry.time,
                            color = TextTertiary,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildString {
                            append(entry.duration)
                            if (entry.heatAdjustments > 0) {
                                append(" · 火候调整${entry.heatAdjustments}次")
                            }
                            append(" · 得分${entry.score}分")
                        },
                        color = TextTertiary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        repeat(5) { i ->
                            val earned = i < entry.stars
                            Image(
                                // 已得星用实心填充星，未得星用空心描边星（star 是描边，tint 也只是描边，会显得"全空"）
                                painter = painterResource(
                                    id = if (earned) R.drawable.star_filled else R.drawable.star
                                ),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(
                                    if (earned) Color(0xFFE8A33D) else Color(0xFFE8DDD0)
                                ),
                                modifier = Modifier.size(14.dp)
                            )
                            if (i < 4) {
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (entry.imageRes != null || !entry.imageUrl.isNullOrBlank()) {
                    Image(
                        painter = rememberRecipePainter(entry.imageRes, entry.imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(softTint(Color(0xFFF0E8DE))),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.image),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(TextTertiary),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
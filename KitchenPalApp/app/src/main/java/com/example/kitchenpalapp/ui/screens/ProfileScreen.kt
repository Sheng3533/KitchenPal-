package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.AppPreferences
import com.example.kitchenpalapp.ui.components.ServerConfigDialog
import com.example.kitchenpalapp.ui.theme.softTint
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.theme.StateSuccess
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    onFavoritesClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onDietReportClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onHelpFeedbackClick: () -> Unit = {},
    onFridgeClick: () -> Unit = {},
    onPublishRecipeClick: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTastePreferenceEdit: () -> Unit = {},
    onTastePreferencesClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val darkMode by viewModel.darkMode.collectAsState()
    var cookingNotify by remember { mutableStateOf(true) }
    // 火候灵敏度显示实际保存值（低/中等/高），不再写死「中等」
    // 进入「我的」页时加载已保存的服务器地址，供设置项与弹窗回显
    LaunchedEffect(Unit) { viewModel.loadServerUrl() }
    val currentUser by viewModel.currentUser.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val cookingHistory by viewModel.cookingHistory.collectAsState()
    val tastePreferences by viewModel.tastePreferences.collectAsState()
    val favoriteRecipes = recipes.filter { favoriteIds.contains(it.id) }
    val scrollState = rememberScrollState()

    // 个人页三项统计全部基于真实数据，不再使用 UserData 里可能写死的计数
    val realCookingCount = remember(cookingHistory) { cookingHistory.size }
    val realFavoriteCount = remember(favoriteIds) { favoriteIds.size }
    val realStreakDays = remember(cookingHistory) {
        MainViewModel.calculateStreakDays(cookingHistory)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        ProfileHeader(
            user = currentUser,
            cookingCount = realCookingCount,
            onSettingsClick = onSettingsClick
        )
        StatsRow(
            cookingCount = realCookingCount,
            favoriteCount = realFavoriteCount,
            streakDays = realStreakDays,
            onFavoritesClick = onFavoritesClick,
            onHistoryClick = onHistoryClick,
            onStreakClick = onHistoryClick
        )
        Spacer(modifier = Modifier.height(20.dp))
        TastePreferenceSection(
            preferences = tastePreferences,
            onEditClick = onTastePreferenceEdit
        )
        Spacer(modifier = Modifier.height(20.dp))
        FavoritesPreviewSection(
            favoriteRecipes = favoriteRecipes,
            onSeeAll = onFavoritesClick,
            onRecipeClick = onRecipeClick
        )
        Spacer(modifier = Modifier.height(20.dp))
        SettingsSection(
            darkMode = darkMode,
            onDarkModeChange = { viewModel.setDarkMode(it) },
            cookingNotify = cookingNotify,
            onCookingNotifyChange = { cookingNotify = it },
            onNotificationsClick = onNotificationsClick,
            onHelpFeedbackClick = onHelpFeedbackClick,
            onFridgeClick = onFridgeClick,
            onPublishRecipeClick = onPublishRecipeClick,
            onSettingsClick = onSettingsClick,
            onTastePreferencesClick = onTastePreferencesClick,
            onLogout = {
                viewModel.logout()
                onLogout()
            },
            viewModel = viewModel
        )
    }
}

@Composable
private fun ProfileHeader(
    user: com.example.kitchenpalapp.data.UserData?,
    cookingCount: Int,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.home_recipe_card),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Primary.copy(alpha = 0.85f),
                            Primary.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "头像",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.nickname?.takeIf { it.isNotBlank() } ?: "料理达人",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "已烹饪 $cookingCount 道菜",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "设置",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    cookingCount: Int,
    favoriteCount: Int,
    streakDays: Int,
    onFavoritesClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onStreakClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .offset(y = (-20).dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor)
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ProfileStat(
            iconRes = R.drawable.chef_hat,
            value = cookingCount.toString(),
            label = "美食",
            onClick = onHistoryClick
        )
        ProfileDivider()
        ProfileStat(
            iconRes = R.drawable.heart,
            value = favoriteCount.toString(),
            label = "收藏",
            onClick = onFavoritesClick
        )
        ProfileDivider()
        ProfileStat(
            iconRes = R.drawable.flame,
            value = streakDays.toString(),
            label = "天",
            subLabel = "连续",
            onClick = onStreakClick
        )
    }
}

@Composable
private fun ProfileDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(softTint(Color(0xFFE8DDD0)))
    )
}

@Composable
private fun ProfileStat(
    iconRes: Int,
    value: String,
    label: String,
    subLabel: String? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Primary),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (subLabel != null) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = subLabel,
                    fontSize = 12.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextTertiary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private val tastePreferenceIcons = mapOf(
    "清淡" to R.drawable.snowflake,
    "微辣" to R.drawable.flame,
    "香辣" to R.drawable.flame,
    "麻辣" to R.drawable.flame,
    "酸甜" to R.drawable.egg,
    "咸鲜" to R.drawable.wheat,
    "红烧" to R.drawable.flame,
    "清蒸" to R.drawable.droplets,
    "油炸" to R.drawable.flame,
    "炖煮" to R.drawable.soup,
    "烧烤" to R.drawable.flame,
    "凉拌" to R.drawable.salad,
    "素食" to R.drawable.leaf,
    "肉食" to R.drawable.beef,
    "海鲜" to R.drawable.fish,
    "甜点" to R.drawable.cake_slice,
    "面食" to R.drawable.wheat,
    "米饭" to R.drawable.soup
)

@Composable
private fun TastePreferenceSection(
    preferences: List<String>,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "我的口味偏好",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                    modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SurfaceColor)
                            .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pencil),
                        contentDescription = "编辑",
                        colorFilter = ColorFilter.tint(TextTertiary),
                        modifier = Modifier.size(14.dp)
                    )
                }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            preferences.forEach { preference ->
                val iconRes = tastePreferenceIcons[preference] ?: R.drawable.star
                TasteTag(
                    text = preference,
                    iconRes = iconRes,
                    bgColor = softTint(Color(0xFFFFE8D6)),
                    textColor = Primary
                )
            }
            if (preferences.isEmpty()) {
                Text(
                    text = "还没有口味偏好",
                    color = TextTertiary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9999.dp))
                .background(SurfaceColor)
                .clickable(onClick = onEditClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.plus),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(TextTertiary),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (preferences.isEmpty()) "添加" else "管理",
                    color = TextTertiary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun TasteTag(
    text: String,
    iconRes: Int,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(textColor),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FavoritesPreviewSection(
    favoriteRecipes: List<com.example.kitchenpalapp.data.RecipeData>,
    onSeeAll: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    val displayRecipes = if (favoriteRecipes.isNotEmpty()) {
        favoriteRecipes.take(2)
    } else {
        emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "我的收藏",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "查看全部",
                color = Primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onSeeAll)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            displayRecipes.forEach { recipe ->
                FavoritePreviewCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id.toString()) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FavoritePreviewCard(
    recipe: com.example.kitchenpalapp.data.RecipeData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceColor)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            AsyncImage(
                model = com.example.kitchenpalapp.data.RecipeConverter.fullImageUrl(recipe.imageUrl),
                contentDescription = recipe.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(id = R.drawable.image),
                error = painterResource(id = R.drawable.image)
            )
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = recipe.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
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
    }
}

@Composable
private fun SettingsSection(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    cookingNotify: Boolean,
    onCookingNotifyChange: (Boolean) -> Unit,
    onNotificationsClick: () -> Unit,
    onHelpFeedbackClick: () -> Unit,
    onFridgeClick: () -> Unit = {},
    onPublishRecipeClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    onTastePreferencesClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MainViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showServerDialog by remember { mutableStateOf(false) }
    // 火候灵敏度显示实际保存值（低/中等/高），不再写死「中等」
    val heatSensitivityText = remember(context) {
        arrayOf("低", "中等", "高")[AppPreferences(context).getHeatSensitivity()]
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "设置",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                ToggleSettingItem(
                    iconRes = R.drawable.bell,
                    title = "烹饪提醒",
                    checked = cookingNotify,
                    onCheckedChange = onCookingNotifyChange
                )
                MenuDivider()
                SettingItem(
                    iconRes = R.drawable.flame,
                    title = "火候灵敏度",
                    value = heatSensitivityText,
                    onClick = onSettingsClick
                )
                MenuDivider()
                SettingItem(
                    iconRes = R.drawable.sparkles,
                    title = "口味偏好",
                    onClick = onTastePreferencesClick
                )
                MenuDivider()
                ToggleSettingItem(
                    iconRes = R.drawable.moon,
                    title = "深色模式",
                    checked = darkMode,
                    onCheckedChange = onDarkModeChange
                )
                MenuDivider()
                SettingItem(
                    iconRes = R.drawable.sprout,
                    title = "我的冰箱",
                    onClick = onFridgeClick
                )
                MenuDivider()
                SettingItem(
                    iconRes = R.drawable.plus,
                    title = "发布我的菜谱",
                    onClick = onPublishRecipeClick
                )
                MenuDivider()
                SettingItem(
                    iconRes = R.drawable.share_2,
                    title = "服务器地址",
                    value = if (viewModel.serverUrl.collectAsState().value.isBlank()) "默认" else "自定义",
                    onClick = { showServerDialog = true }
                )
                MenuDivider()
                SettingItem(
                    iconRes = R.drawable.bell,
                    title = "通知设置",
                    onClick = onNotificationsClick
                )
                MenuDivider()
                SettingItem(
                    iconRes = R.drawable.message_square,
                    title = "帮助与反馈",
                    onClick = onHelpFeedbackClick
                )
                MenuDivider()
                SettingItem(
                    iconRes = R.drawable.info,
                    title = "关于我们",
                    onClick = {
                        android.widget.Toast.makeText(
                            context,
                            "KitchenPal v1.0.0 - 智能厨房助手",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceColor)
                .clickable(onClick = onLogout)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "退出登录",
                color = Color(0xFFFF4D4F),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showServerDialog) {
        ServerConfigDialog(
            viewModel = viewModel,
            onDismiss = { showServerDialog = false }
        )
    }
}

@Composable
private fun SettingItem(
    iconRes: Int,
    title: String,
    value: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            colorFilter = ColorFilter.tint(Primary),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                fontSize = 13.sp,
                color = TextTertiary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Image(
            painter = painterResource(id = R.drawable.chevron_right),
            contentDescription = null,
            colorFilter = ColorFilter.tint(TextTertiary),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ToggleSettingItem(
    iconRes: Int,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            colorFilter = ColorFilter.tint(Primary),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF4CAF50),
                checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun MenuDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(start = 48.dp),
        color = Color(0xFFF0E8DD),
        thickness = 0.5.dp
    )
}

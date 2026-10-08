package com.example.kitchenpalapp.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.AppPreferences
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.SurfaceOverlay
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onHelpFeedback: () -> Unit,
    onAboutClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        SettingsTopBar(onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        HeatSensitivitySection()
        Spacer(modifier = Modifier.height(20.dp))
        GeneralSettingsSection(
            onHelpFeedback = onHelpFeedback,
            onCheckUpdate = onAboutClick,
            onPrivacyPolicy = onPrivacyClick,
            onShareApp = {
                val shareText = "KitchenPal - 智能厨房助手，帮助您轻松烹饪美味佳肴！\n下载地址：https://play.google.com/store/apps/details?id=com.example.kitchenpalapp"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "KitchenPal")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, "分享应用"))
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        AboutSection()
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
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
            text = "设置",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun HeatSensitivitySection() {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    // 读取上次保存的选择（默认中等），点击即时落盘，重启后仍生效
    var sensitivity by remember { mutableIntStateOf(prefs.getHeatSensitivity()) }
    val levels = listOf("低", "中等", "高")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flame),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "火候灵敏度",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = levels[sensitivity],
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                levels.forEachIndexed { index, level ->
                    val isSelected = sensitivity == index
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) Primary else SurfaceOverlay)
                            .clickable {
                                sensitivity = index
                                prefs.setHeatSensitivity(index)
                            }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "灵敏度越高，火候提醒越频繁",
                fontSize = 12.sp,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun GeneralSettingsSection(
    onHelpFeedback: () -> Unit,
    onCheckUpdate: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onShareApp: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            SettingRow(
                iconRes = R.drawable.message_square,
                title = "帮助与反馈",
                onClick = onHelpFeedback
            )
            MenuDivider()
            SettingRow(
                iconRes = R.drawable.download,
                title = "检查更新",
                value = "已是最新版本",
                onClick = onCheckUpdate
            )
            MenuDivider()
            SettingRow(
                iconRes = R.drawable.lock,
                title = "隐私政策",
                onClick = onPrivacyPolicy
            )
            MenuDivider()
            SettingRow(
                iconRes = R.drawable.share_2,
                title = "分享应用",
                onClick = onShareApp
            )
        }
    }
}

@Composable
private fun SettingRow(
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
private fun MenuDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(start = 48.dp),
        color = SurfaceOverlay,
        thickness = 0.5.dp
    )
}

@Composable
private fun AboutSection() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chef_hat),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "KitchenPal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "智能厨房助手",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "版本 1.0.0",
                fontSize = 13.sp,
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "KitchenPal 是一款智能厨房助手应用，帮助您轻松烹饪美味佳肴。",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AI 火候提醒 · 海量菜谱 · 健康饮食管理",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}


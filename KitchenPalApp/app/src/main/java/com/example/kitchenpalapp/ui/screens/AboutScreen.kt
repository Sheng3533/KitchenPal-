package com.example.kitchenpalapp.ui.screens

import android.content.Intent
import android.net.Uri
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
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.SurfaceOverlay
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val appVersionInfo by viewModel.appVersionInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        AboutTopBar(onBack = onBack)
        Spacer(modifier = Modifier.height(24.dp))
        AppInfoSection()
        Spacer(modifier = Modifier.height(24.dp))
        AboutSettingsSection(
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
            onLicenseClick = { showLicenseDialog = true },
            onRateClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=com.example.kitchenpalapp")
                }
                context.startActivity(intent)
            },
            onCheckUpdate = {
                viewModel.checkAppVersion()
                showUpdateDialog = true
            }
        )
        Spacer(modifier = Modifier.height(32.dp))
        CopyrightSection()
    }

    if (showLicenseDialog) {
        LicenseDialog(onDismiss = { showLicenseDialog = false })
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Text(
                    text = "检查更新",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                val versionName = appVersionInfo?.get("versionName") as? String ?: "1.0.0"
                val isLatest = appVersionInfo?.get("isLatest") as? Boolean ?: true
                Text(
                    text = if (isLatest) "当前版本已是最新版本 v$versionName" else "发现新版本 v$versionName",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("确定", color = Primary)
                }
            },
            containerColor = SurfaceColor
        )
    }
}

@Composable
private fun AboutTopBar(onBack: () -> Unit) {
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
            text = "关于我们",
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
private fun AppInfoSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.chef_hat),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "KitchenPal",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "v1.0.0",
            fontSize = 14.sp,
            color = TextTertiary
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "KitchenPal 是一款智能厨房助手应用，\n帮助您轻松烹饪美味佳肴。",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun AboutSettingsSection(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLicenseClick: () -> Unit,
    onRateClick: () -> Unit,
    onCheckUpdate: () -> Unit
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
                iconRes = R.drawable.help_circle,
                title = "用户协议",
                onClick = onTermsClick
            )
            MenuDivider()
            SettingRow(
                iconRes = R.drawable.lock,
                title = "隐私政策",
                onClick = onPrivacyClick
            )
            MenuDivider()
            SettingRow(
                iconRes = R.drawable.flask_round,
                title = "开源许可",
                onClick = onLicenseClick
            )
            MenuDivider()
            SettingRow(
                iconRes = R.drawable.star,
                title = "给我们评分",
                onClick = onRateClick
            )
            MenuDivider()
            SettingRow(
                iconRes = R.drawable.download,
                title = "检查更新",
                value = "v1.0.0",
                onClick = onCheckUpdate
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
private fun CopyrightSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "© 2026 KitchenPal",
            fontSize = 12.sp,
            color = TextTertiary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "All Rights Reserved",
            fontSize = 12.sp,
            color = TextTertiary
        )
    }
}

@Composable
private fun LicenseDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "开源许可",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "本应用使用了以下开源库：\n\n- Jetpack Compose (Apache 2.0)\n- Material Design 3 (Apache 2.0)\n- Navigation Compose (Apache 2.0)\n- Coil (Apache 2.0)\n\n详细许可信息请参考各项目的 LICENSE 文件。",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定", color = Primary)
            }
        },
        containerColor = SurfaceColor
    )
}

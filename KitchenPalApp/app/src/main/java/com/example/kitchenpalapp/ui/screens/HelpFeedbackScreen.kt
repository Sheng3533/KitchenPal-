package com.example.kitchenpalapp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.FaqItem
import com.example.kitchenpalapp.data.QuickAction
import com.example.kitchenpalapp.data.SampleData
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.BorderLight
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.PrimaryLightest
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.SurfaceOverlay
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HelpFeedbackScreen(
    onBack: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    initialTab: Int = 0,
    viewModel: MainViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var searchText by remember { mutableStateOf("") }
    var expandedIndex by remember { mutableStateOf(-1) }
    var feedbackText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSubmitSuccess by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        HelpTopBar(onBack = onBack)

        HelpTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(bottom = 24.dp)
            ) {
                SearchBox(
                    text = searchText,
                    onTextChange = { searchText = it }
                )
                FaqSection(
                    expandedIndex = expandedIndex,
                    onToggle = { index ->
                        expandedIndex = if (expandedIndex == index) -1 else index
                    }
                )
                QuickActionsSection(
                    onServiceClick = { showServiceDialog = true },
                    onFeedbackClick = { selectedTab = 1 },
                    onTutorialClick = { showTutorialDialog = true }
                )
                AboutSection(
                    onTermsClick = onTermsClick,
                    onPrivacyClick = onPrivacyClick,
                    onLicenseClick = { showLicenseDialog = true }
                )
                ShareCard(context)
            }
        } else {
            FeedbackTabContent(
                feedbackText = feedbackText,
                onFeedbackChange = { feedbackText = it },
                onSubmit = {
                    if (feedbackText.isNotBlank()) {
                        isSubmitting = true
                        viewModel.submitFeedback("", feedbackText, "", "feedback")
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isSubmitting = false
                            feedbackText = ""
                            showSubmitSuccess = true
                        }, 500)
                    }
                },
                isSubmitting = isSubmitting
            )
        }
    }

    if (showSubmitSuccess) {
        AlertDialog(
            onDismissRequest = { showSubmitSuccess = false },
            title = {
                Text("提交成功", fontWeight = FontWeight.SemiBold)
            },
            text = {
                Text("感谢您的反馈，我们会尽快处理！")
            },
            confirmButton = {
                TextButton(onClick = { showSubmitSuccess = false }) {
                    Text("确定", color = Primary)
                }
            },
            containerColor = SurfaceColor
        )
    }

    if (showServiceDialog) {
        ServiceDialog(onDismiss = { showServiceDialog = false })
    }

    if (showTutorialDialog) {
        TutorialDialog(onDismiss = { showTutorialDialog = false })
    }

    if (showLicenseDialog) {
        LicenseDialog(onDismiss = { showLicenseDialog = false })
    }
}

@Composable
private fun HelpTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("帮助中心", "意见反馈")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            val bgColor = if (isSelected) Primary else SurfaceColor
            val textColor = if (isSelected) Color.White else TextTertiary
            val modifier = if (isSelected) {
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(bgColor)
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            } else {
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(bgColor)
                    .border(1.dp, BorderLight, RoundedCornerShape(50))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            }
            Text(
                text = label,
                fontSize = 13.sp,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun FeedbackTabContent(
    feedbackText: String,
    onFeedbackChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "我们重视您的反馈",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "您的建议将帮助我们改进产品体验",
            color = TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "反馈内容",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Background)
                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = feedbackText,
                        onValueChange = onFeedbackChange,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(Primary),
                        modifier = Modifier.fillMaxSize(),
                        decorationBox = { innerTextField ->
                            Box {
                                if (feedbackText.isEmpty()) {
                                    Text(
                                        text = "请详细描述您的问题或建议...",
                                        color = TextTertiary,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${feedbackText.length}/500",
                    color = TextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (feedbackText.isNotBlank()) Primary
                            else Primary.copy(alpha = 0.5f)
                        )
                        .clickable(enabled = feedbackText.isNotBlank()) {
                            if (feedbackText.isNotBlank()) onSubmit()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSubmitting) "提交中..." else "提交反馈",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryLightest),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "温馨提示",
                        color = Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "我们通常会在1-3个工作日内回复您的反馈。如有紧急问题，请联系在线客服。",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpTopBar(onBack: () -> Unit) {
    Surface(
        color = SurfaceColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceOverlay)
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
                text = "帮助与反馈",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun SearchBox(
    text: String,
    onTextChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(CircleShape)
            .background(SurfaceColor)
            .border(1.dp, BorderLight, CircleShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.search),
                contentDescription = null,
                colorFilter = ColorFilter.tint(TextTertiary),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (text.isEmpty()) {
                    Text(
                        text = "搜索问题...",
                        color = TextTertiary,
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(Primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FaqSection(
    expandedIndex: Int,
    onToggle: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "常见问题",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SampleData.faqItems.forEachIndexed { index, item ->
                    FaqItemRow(
                        item = item,
                        isExpanded = expandedIndex == index,
                        onClick = { onToggle(index) }
                    )
                    if (index < SampleData.faqItems.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(1.dp)
                                .background(BorderLight)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqItemRow(
    item: FaqItem,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "chevron_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
            .animateContentSize(animationSpec = tween(durationMillis = 300))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.question,
                color = TextPrimary,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.chevron_down),
                contentDescription = if (isExpanded) "收起" else "展开",
                colorFilter = ColorFilter.tint(TextTertiary),
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation)
            )
        }
        if (isExpanded) {
            Text(
                text = item.answer,
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onServiceClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onTutorialClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "快捷操作",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SampleData.helpQuickActions.forEach { action ->
                QuickActionCard(
                    action = action,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        when (action.title) {
                            "联系客服" -> onServiceClick()
                            "意见反馈" -> onFeedbackClick()
                            "使用教程" -> onTutorialClick()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(action.bgColor)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconResByName(action.icon)),
                    contentDescription = action.title,
                    colorFilter = ColorFilter.tint(Color(action.color)),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = action.subtitle,
                color = TextTertiary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun iconResByName(name: String): Int = when (name) {
    "message_square" -> R.drawable.message_square
    "headset" -> R.drawable.headset
    "play_circle" -> R.drawable.play_circle
    else -> R.drawable.message_square
}

@Composable
private fun AboutSection(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLicenseClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前版本",
                    color = TextPrimary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "v1.0.0",
                    color = TextTertiary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PrimaryLightest)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable {
                            android.widget.Toast.makeText(
                                context,
                                "已是最新版本 v1.0.0",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                ) {
                    Text(
                        text = "检查更新",
                        color = Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            AboutDivider()
            AboutLinkRow(title = "用户协议", onClick = onTermsClick)
            AboutDivider()
            AboutLinkRow(title = "隐私政策", onClick = onPrivacyClick)
            AboutDivider()
            AboutLinkRow(title = "开源许可", onClick = onLicenseClick)
        }
    }
}

@Composable
private fun AboutDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(BorderLight)
    )
}

@Composable
private fun AboutLinkRow(title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(id = R.drawable.chevron_right),
            contentDescription = null,
            colorFilter = ColorFilter.tint(TextTertiary),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ShareCard(context: android.content.Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.heart),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Primary),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "觉得好用？分享给朋友吧",
            color = TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .border(1.dp, Primary, CircleShape)
                .clickable {
                    com.example.kitchenpalapp.utils.ShareHelper.shareApp(context)
                }
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.share_2),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "分享给朋友",
                    color = Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ServiceDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "在线客服",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    text = "客服工作时间：\n周一至周日 9:00 - 22:00\n\n联系方式：\n客服电话：400-888-8888\n客服邮箱：support@kitchenpal.com\n\n您也可以在APP内直接与在线客服沟通，我们会尽快回复您的问题。",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了", color = Primary)
            }
        },
        containerColor = SurfaceColor
    )
}

@Composable
private fun TutorialDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "视频教程",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    text = "KitchenPal 提供丰富的视频教程，帮助您快速上手各项功能：\n\n• 食材识别使用教程\n• 火候监控功能介绍\n• 食谱收藏与分享\n• 烹饪记录查看方法\n\n视频教程入口即将上线，敬请期待！",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了", color = Primary)
            }
        },
        containerColor = SurfaceColor
    )
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

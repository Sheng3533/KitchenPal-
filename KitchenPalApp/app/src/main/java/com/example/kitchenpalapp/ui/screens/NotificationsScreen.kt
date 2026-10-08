package com.example.kitchenpalapp.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.NotificationItem
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.BorderLight
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.PrimaryLightest
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onRecipeClick: (Long) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val notifications = viewModel.getNotificationList()
    var selectedTab by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    var showSystemDialog by remember { mutableStateOf<NotificationItem?>(null) }
    var showPromoDialog by remember { mutableStateOf<NotificationItem?>(null) }

    // 分组日期跟随真实日期，不再写死「7月11日」
    val dateFormat = remember { SimpleDateFormat("M月d日", Locale.CHINA) }
    val todayLabel = remember { dateFormat.format(Date()) }
    val yesterdayLabel = remember {
        val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }
        dateFormat.format(c.time)
    }

    val cookingIcons = remember { setOf("flame", "chef_hat") }

    val filteredNotifications = when (selectedTab) {
        1 -> notifications.filter { it.icon in cookingIcons }
        2 -> notifications.filter { it.icon !in cookingIcons }
        else -> notifications
    }

    val todayItems = filteredNotifications.take(4)
    val yesterdayItems = filteredNotifications.drop(4)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        NotificationsTopBar(
            onBack = onBack,
            onMarkAllRead = { viewModel.markAllAsRead() },
            hasUnread = notifications.any { !it.isRead }
        )

        NotificationTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        if (filteredNotifications.isEmpty()) {
            EmptyNotificationState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                if (todayItems.isNotEmpty()) {
                    NotificationSection(
                        title = "今天",
                        date = todayLabel,
                        items = todayItems,
                        onMarkRead = { id -> id.toLongOrNull()?.let { viewModel.markAsRead(it) } },
                        onItemClick = { item ->
                            if (!item.isRead) {
                                item.id.toLongOrNull()?.let { viewModel.markAsRead(it) }
                            }
                            when (item.type) {
                                0 -> onRecipeClick(item.relatedId)
                                1 -> showSystemDialog = item
                                2 -> showPromoDialog = item
                            }
                        }
                    )
                }

                if (yesterdayItems.isNotEmpty()) {
                    NotificationSection(
                        title = "昨天",
                        date = yesterdayLabel,
                        items = yesterdayItems,
                        onMarkRead = { id -> id.toLongOrNull()?.let { viewModel.markAsRead(it) } },
                        onItemClick = { item ->
                            if (!item.isRead) {
                                item.id.toLongOrNull()?.let { viewModel.markAsRead(it) }
                            }
                            when (item.type) {
                                0 -> onRecipeClick(item.relatedId)
                                1 -> showSystemDialog = item
                                2 -> showPromoDialog = item
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showSystemDialog != null) {
        NotificationDetailDialog(
            title = showSystemDialog!!.title,
            content = showSystemDialog!!.description,
            onDismiss = { showSystemDialog = null }
        )
    }

    if (showPromoDialog != null) {
        NotificationDetailDialog(
            title = "活动详情",
            content = showPromoDialog!!.description,
            onDismiss = { showPromoDialog = null }
        )
    }
}

@Composable
private fun EmptyNotificationState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PrimaryLightest),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bell),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无通知",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "您的通知会显示在这里",
                fontSize = 14.sp,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun NotificationsTopBar(
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    hasUnread: Boolean
) {
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
                .background(BorderLight)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = "返回",
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = "通知消息",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Text(
            text = "全部已读",
            fontSize = 14.sp,
            color = if (hasUnread) Primary else TextTertiary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onMarkAllRead)
        )
    }
}

@Composable
private fun NotificationTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("全部", "烹饪提醒", "系统通知")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            val bgColor = if (isSelected) Primary else Surface
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
private fun NotificationSection(
    title: String,
    date: String,
    items: List<NotificationItem>,
    onMarkRead: (String) -> Unit,
    onItemClick: (NotificationItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = date,
                fontSize = 13.sp,
                color = TextTertiary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                NotificationCard(
                    item = item,
                    isRead = item.isRead,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    isRead: Boolean,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            NotificationIcon(iconName = item.icon ?: "bell", isRead = isRead)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = if (isRead) FontWeight.Medium else FontWeight.SemiBold,
                        color = if (isRead) TextTertiary else TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { showDeleteDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.x),
                            contentDescription = "删除",
                            colorFilter = ColorFilter.tint(TextTertiary),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    color = if (isRead) TextTertiary else TextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.time,
                    fontSize = 12.sp,
                    color = TextTertiary
                )
            }
        }
    }

    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("删除通知", fontWeight = FontWeight.SemiBold)
            },
            text = {
                Text("确定要删除这条通知吗？")
            },
            confirmButton = {
                Text(
                    text = "删除",
                    color = Color(0xFFD94B4B),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            showDeleteDialog = false
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "取消",
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { showDeleteDialog = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            },
            containerColor = Surface
        )
    }
}

@Composable
private fun NotificationIcon(iconName: String, isRead: Boolean) {
    val iconRes = iconResFor(iconName)
    val bgColor = if (isRead) BorderLight else PrimaryLightest
    val iconColor = if (isRead) TextTertiary else Primary

    Box(
        modifier = Modifier
            .size(40.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(iconColor),
                modifier = Modifier.size(20.dp)
            )
        }
        if (!isRead) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Primary)
            )
        }
    }
}

private fun iconResFor(iconName: String): Int = when (iconName) {
    "flame" -> R.drawable.flame
    "sparkles" -> R.drawable.sparkles
    "chef_hat" -> R.drawable.chef_hat
    "heart" -> R.drawable.heart
    "bell" -> R.drawable.bell
    else -> R.drawable.bell
}

@Composable
private fun NotificationDetailDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Text(content, color = TextSecondary)
        },
        confirmButton = {
            Text(
                text = "确定",
                color = Primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        containerColor = Surface
    )
}

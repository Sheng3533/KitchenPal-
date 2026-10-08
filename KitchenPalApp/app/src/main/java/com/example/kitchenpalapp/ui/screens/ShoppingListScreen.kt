package com.example.kitchenpalapp.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.WarmBlack
import com.example.kitchenpalapp.ui.theme.WarmGray
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel

@Composable
fun ShoppingListScreen(
    onBack: () -> Unit,
    onFridgeClick: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val items by viewModel.shoppingItems.collectAsState()
    val syncing by viewModel.syncing.collectAsState()
    val checkedCount = items.count { it.checked }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // 顶栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "返回",
                    colorFilter = ColorFilter.tint(WarmBlack),
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "购物清单",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "冰箱",
                color = Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(onClick = onFridgeClick)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
            if (checkedCount > 0) {
                Text(
                    text = "清空已完成",
                    color = Primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { viewModel.clearCheckedShoppingItems() }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (items.isEmpty()) "" else "共 ${items.size} 项 · 已买 $checkedCount 项",
                color = WarmGray,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            // 买菜清单上云 / 拉取
            Text(
                text = if (syncing) "同步中…" else "上云",
                color = Primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(enabled = !syncing) { viewModel.syncShoppingToCloud() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
            Text(
                text = "拉取",
                color = Primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(enabled = !syncing) { viewModel.pullShoppingFromCloud() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.sprout),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Primary),
                        modifier = Modifier.size(44.dp)
                    )
                }
                Text(
                    text = "购物清单还是空的",
                    style = MaterialTheme.typography.titleMedium,
                    color = WarmBlack,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "打开菜谱详情，一键把食材加进来",
                    color = WarmGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 20.dp, vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.name }) { item ->
                    ShoppingItemRow(
                        name = item.name,
                        amount = item.amount,
                        checked = item.checked,
                        onToggle = { viewModel.toggleShoppingItem(item.name) },
                        onRemove = { viewModel.removeShoppingItem(item.name) }
                    )
                }
                item {
                    Text(
                        text = "清空全部",
                        color = WarmGray,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.clearShoppingList() }
                            .padding(vertical = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(
    name: String,
    amount: String,
    checked: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 勾选圆圈
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (checked) Primary else Color.Transparent)
                .border(2.dp, if (checked) Primary else WarmGray.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (checked) WarmGray else WarmBlack,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            )
            if (amount.isNotBlank()) {
                Text(
                    text = amount,
                    fontSize = 12.sp,
                    color = WarmGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.x),
            contentDescription = "删除",
            colorFilter = ColorFilter.tint(WarmGray.copy(alpha = 0.6f)),
            modifier = Modifier
                .size(16.dp)
                .clickable(onClick = onRemove)
        )
    }
}

package com.example.kitchenpalapp.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.FridgeItemData
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.WarmBlack
import com.example.kitchenpalapp.ui.theme.WarmGray
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** 冰箱食材管理 + 临期提醒 */
@Composable
fun FridgeScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val items by viewModel.fridgeItems.collectAsState()
    val expiring by viewModel.expiringItems.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    val today = LocalDate.now()

    // 进入页面即拉取云端冰箱数据（否则永远是空态）
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadFridgeItems()
    }

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
                text = "我的冰箱",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "添加",
                color = Primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { showAdd = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        if (expiring.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF3E0))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "⚠️ 有 ${expiring.size} 种食材即将过期",
                        color = Color(0xFFE65100),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = expiring.joinToString("、") { it.name },
                        color = Color(0xFFBF360C),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
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
                    Text("🧊", fontSize = 40.sp)
                }
                Text(
                    text = "冰箱还是空的",
                    style = MaterialTheme.typography.titleMedium,
                    color = WarmBlack,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "把买回来的食材记下来，临期自动提醒你",
                    color = WarmGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    FridgeRow(item = item, today = today, onDelete = { viewModel.deleteFridgeItem(item.id) })
                }
            }
        }
    }

    if (showAdd) {
        AddFridgeDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, qty, unit, expire ->
                viewModel.addFridgeItem(
                    FridgeItemData(
                        name = name,
                        quantity = qty,
                        unit = unit,
                        expireDate = expire
                    )
                )
                showAdd = false
            }
        )
    }
}

@Composable
private fun FridgeRow(item: FridgeItemData, today: LocalDate, onDelete: () -> Unit) {
    val (label, color) = expiryLabel(item.expireDate, today)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = WarmBlack
            )
            val amount = listOfNotNull(item.quantity?.let { fmt(it) }, item.unit)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            if (amount.isNotBlank() || item.category != null) {
                Text(
                    text = listOfNotNull(amount.takeIf { it.isNotBlank() }, item.category).joinToString(" · "),
                    fontSize = 12.sp,
                    color = WarmGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (label != null) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.x),
            contentDescription = "删除",
            colorFilter = ColorFilter.tint(WarmGray.copy(alpha = 0.6f)),
            modifier = Modifier
                .size(16.dp)
                .clickable(onClick = onDelete)
        )
    }
}

@Composable
private fun expiryLabel(expireDate: String?, today: LocalDate): Pair<String?, Color> {
    if (expireDate.isNullOrBlank()) return null to WarmGray
    return try {
        val d = LocalDate.parse(expireDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val days = d.toEpochDay() - today.toEpochDay()
        when {
            days < 0 -> "已过期 ${-days} 天" to Color(0xFFD32F2F)
            days == 0L -> "今天到期" to Color(0xFFD32F2F)
            days <= 3 -> "剩 $days 天" to Color(0xFFE65100)
            else -> "剩 $days 天" to Color(0xFF388E3C)
        }
    } catch (e: Exception) {
        null to WarmGray
    }
}

private fun fmt(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()

@Composable
private fun AddFridgeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double?, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var expire by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加食材") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("食材名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = { Text("数量") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("单位") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = expire,
                    onValueChange = { expire = it },
                    label = { Text("保质期至 (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            qty.toDoubleOrNull(),
                            unit.trim().ifBlank { null },
                            expire.trim().ifBlank { null }
                        )
                    }
                }
            ) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

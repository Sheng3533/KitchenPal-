package com.example.kitchenpalapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel

/**
 * 服务器地址配置弹窗（真机联调用）。
 *
 * 登录页与「我的 → 服务器地址」共用：
 * 输入电脑局域网 IP（如 192.168.0.102:18080）→ 保存后立即生效（无需重启 App）→
 * 点「保存并测试」可当场验证能否连上后端。
 */
@Composable
fun ServerConfigDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val saved by viewModel.serverUrl.collectAsState()
    var input by remember { mutableStateOf("") }
    val testing by viewModel.connTesting.collectAsState()
    val testResult by viewModel.connTestResult.collectAsState()
    // 弹窗首次打开时用已保存值回显
    LaunchedEffect(saved) {
        if (input.isBlank()) input = saved
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("服务器地址") },
        text = {
            Column {
                Text(
                    text = "真机联调：填电脑的局域网 IP（手机与电脑连同一个 WiFi），例如 192.168.0.102:18080。\n" +
                            "模拟器保持默认（10.0.2.2）即可。\n" +
                            "登录 / 注册失败时，多半就是这里的地址不对。",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("地址（可省略 http://）") },
                    placeholder = { Text("192.168.0.102:18080") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "当前生效：${viewModel.effectiveServerUrl}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                if (saved.isNotBlank()) {
                    Text(
                        text = "自定义：$saved",
                        fontSize = 11.sp,
                        color = Primary
                    )
                }
                if (testResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (testResult == true) "✅ 连接正常" else "❌ 连接失败，请检查 IP / WiFi / 防火墙",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (testResult == true) Color(0xFF388E3C) else Color(0xFFD32F2F)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !testing,
                onClick = {
                    if (viewModel.saveServerUrl(input)) {
                        viewModel.testConnection()
                    }
                }
            ) {
                Text(if (testing) "测试中..." else "保存并测试")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        viewModel.resetServerUrl()
                        input = ""
                    }
                ) { Text("用默认") }
                TextButton(onClick = onDismiss) { Text("关闭") }
            }
        }
    )
}

package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.data.ChefData
import com.example.kitchenpalapp.data.KitchenPalRepository
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.CreamWhite
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.WarmGray
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel

@Composable
fun ChefListScreen(
    onChefClick: (Long) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    var chefs by remember { mutableStateOf<List<ChefData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        loadFailed = false
        val result = KitchenPalRepository.getChefList()
        isLoading = false
        if (result.isSuccess && result.data != null) {
            chefs = result.data
        } else {
            chefs = emptyList()
            loadFailed = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamWhite)
    ) {
        ChefListHeader()
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            loadFailed -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "😕", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "大厨列表加载失败",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "请确认后端服务已启动，然后重试",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmGray
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { reloadKey++ },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "重新加载", color = Color.White)
                    }
                }
            }
            chefs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "暂无大厨", color = WarmGray, style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chefs, key = { it.id }) { chef ->
                        ChefCard(chef = chef, onClick = { onChefClick(chef.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChefListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "大厨工作室",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
    Text(
        text = "与专业大厨一对一交流，获取专业烹饪指导",
        style = MaterialTheme.typography.bodyMedium,
        color = WarmGray,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
    )
}

@Composable
private fun ChefCard(chef: ChefData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chef.name.first().toString(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chef.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (chef.status == 1) Color(0xFF22C55E) else Color(0xFF9CA3AF))
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chef.title,
                style = MaterialTheme.typography.bodySmall,
                color = Primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "擅长：${chef.specialty}",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = chef.signatureDish,
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${chef.experienceYears}年经验",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray
            )
        }
    }
}

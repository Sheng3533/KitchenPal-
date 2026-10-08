package com.example.kitchenpalapp.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary

val allPreferences = listOf(
    "清淡", "微辣", "香辣", "麻辣", "酸甜", "咸鲜",
    "红烧", "清蒸", "油炸", "炖煮", "烧烤", "凉拌",
    "素食", "肉食", "海鲜", "甜点", "面食", "米饭"
)

@Composable
fun TastePreferenceEditScreen(
    onBack: () -> Unit,
    currentPreferences: List<String>,
    onSave: (List<String>) -> Unit
) {
    val selectedPreferences = remember { mutableStateListOf<String>().apply { addAll(currentPreferences) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TastePreferenceTopBar(onBack = onBack)
        Spacer(modifier = Modifier.height(20.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "选择您的口味偏好",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "选择您喜欢的口味，我们会为您推荐更合适的菜谱",
                fontSize = 13.sp,
                color = TextTertiary
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(allPreferences) { preference ->
                val isSelected = selectedPreferences.contains(preference)
                TastePreferenceChip(
                    text = preference,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedPreferences.remove(preference)
                        } else {
                            selectedPreferences.add(preference)
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 24.dp)) {
            Button(
                onClick = { onSave(selectedPreferences.toList()) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "保存偏好",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TastePreferenceTopBar(onBack: () -> Unit) {
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
            text = "口味偏好",
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
private fun TastePreferenceChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) Primary else SurfaceColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}
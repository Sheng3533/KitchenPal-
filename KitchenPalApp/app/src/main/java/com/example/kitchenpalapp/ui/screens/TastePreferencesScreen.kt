package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.BorderLight
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.PrimaryLightest
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary

private val tasteOptions = listOf(
    "麻辣", "香辣", "微辣", "清淡", "酸甜", "咸鲜",
    "酱香", "蒜蓉", "糖醋", "红烧", "清蒸", "油炸",
    "炖煮", "烧烤", "凉拌", "素食", "减脂", "高蛋白"
)

private val commonAllergens = listOf(
    "花生", "坚果", "牛奶", "鸡蛋", "海鲜", "虾",
    "蟹", "芒果", "菠萝", "猕猴桃", "小麦", "大豆"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TastePreferencesScreen(
    onBack: () -> Unit,
    currentPreferences: List<String>,
    currentAllergens: List<String>,
    onSave: (List<String>, List<String>) -> Unit
) {
    val context = LocalContext.current
    val selectedTastes = remember { mutableStateListOf<String>().apply { addAll(currentPreferences) } }
    val selectedAllergens = remember { mutableStateListOf<String>().apply { addAll(currentAllergens) } }
    var customAllergen by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TastePrefTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "口味偏好",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "选择您喜欢的口味，我们会为您推荐更合适的菜谱",
                    fontSize = 13.sp,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(320.dp)
            ) {
                items(tasteOptions) { taste ->
                    val isSelected = selectedTastes.contains(taste)
                    SelectChip(
                        text = taste,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) {
                                selectedTastes.remove(taste)
                            } else {
                                selectedTastes.add(taste)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "过敏食材",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "选择或输入您过敏的食材，我们会避免推荐相关菜谱",
                    fontSize = 13.sp,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(180.dp)
            ) {
                items(commonAllergens) { allergen ->
                    val isSelected = selectedAllergens.contains(allergen)
                    SelectChip(
                        text = allergen,
                        isSelected = isSelected,
                        small = true,
                        onClick = {
                            if (isSelected) {
                                selectedAllergens.remove(allergen)
                            } else {
                                selectedAllergens.add(allergen)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "自定义过敏食材",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceColor)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (customAllergen.isEmpty()) {
                            Text(
                                text = "输入其他过敏食材...",
                                color = TextTertiary,
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = customAllergen,
                            onValueChange = { customAllergen = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(Primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (customAllergen.isNotBlank()) Primary else Primary.copy(alpha = 0.5f)
                            )
                            .clickable(enabled = customAllergen.isNotBlank()) {
                                if (customAllergen.isNotBlank() && !selectedAllergens.contains(customAllergen)) {
                                    selectedAllergens.add(customAllergen)
                                    customAllergen = ""
                                }
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "添加",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (selectedAllergens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "已选过敏食材",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedAllergens.forEach { allergen ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryLightest)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = allergen,
                                        color = Primary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Image(
                                        painter = painterResource(id = R.drawable.x),
                                        contentDescription = "移除",
                                        colorFilter = ColorFilter.tint(Primary),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { selectedAllergens.remove(allergen) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 24.dp)
        ) {
            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        onSave(selectedTastes.toList(), selectedAllergens.toList())
                        android.widget.Toast.makeText(
                            context,
                            "保存成功",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        isSaving = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = if (isSaving) "保存中..." else "保存",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TastePrefTopBar(onBack: () -> Unit) {
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
private fun SelectChip(
    text: String,
    isSelected: Boolean,
    small: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) Primary else SurfaceColor)
            .clickable(onClick = onClick)
            .padding(vertical = if (small) 8.dp else 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (small) 13.sp else 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

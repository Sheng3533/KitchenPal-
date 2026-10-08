package com.example.kitchenpalapp.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.CookStepData
import com.example.kitchenpalapp.data.IngredientData
import com.example.kitchenpalapp.data.RecipeData
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import com.google.gson.Gson

/** 发布用户自制菜谱（UGC） */
@Composable
fun PublishRecipeScreen(
    onBack: () -> Unit,
    onPublished: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val gson = remember { Gson() }

    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("家常菜") }
    var difficulty by remember { mutableStateOf("简单") }
    var cookTime by remember { mutableStateOf("30") }
    var calories by remember { mutableStateOf("300") }
    val ingredients = remember { mutableStateListOf("", "") }
    val steps = remember { mutableStateListOf("", "") }
    var submitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
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
                    colorFilter = ColorFilter.tint(TextPrimary),
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "发布我的菜谱",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Field(label = "菜谱名称 *", value = title, onValueChange = { title = it })
            Field(label = "一句简介", value = subtitle, onValueChange = { subtitle = it })
            Row {
                Box(modifier = Modifier.weight(1f)) {
                    Field(label = "分类", value = category, onValueChange = { category = it })
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    Field(label = "难度(简单/中等/困难)", value = difficulty, onValueChange = { difficulty = it })
                }
            }
            Row {
                Box(modifier = Modifier.weight(1f)) {
                    Field(label = "耗时(分钟)", value = cookTime, onValueChange = { cookTime = it })
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    Field(label = "热量(kcal)", value = calories, onValueChange = { calories = it })
                }
            }

            SectionTitle("食材（名称 + 用量）")
            ingredients.forEachIndexed { idx, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { ingredients[idx] = it },
                            placeholder = { Text("如：番茄 2个", color = TextTertiary, fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    removeIcon { if (ingredients.size > 1) ingredients.removeAt(idx) }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            AddText("+ 添加食材") { ingredients.add("") }

            SectionTitle("步骤")
            steps.forEachIndexed { idx, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { steps[idx] = it },
                            placeholder = { Text("第 ${idx + 1} 步…", color = TextTertiary, fontSize = 14.sp) },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    removeIcon { if (steps.size > 1) steps.removeAt(idx) }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            AddText("+ 添加步骤") { steps.add("") }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        toast(context, "请填写菜谱名称")
                    } else if (viewModel.currentUser.value == null) {
                        toast(context, "请先登录后再发布")
                    } else {
                        submitting = true
                        val ingList = ingredients
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .map { parseIngredient(it) }
                        val stepList = steps
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .mapIndexed { i, s -> CookStepData(stepNumber = i + 1, title = s, description = "") }

                        val recipe = RecipeData(
                            title = title.trim(),
                            subtitle = subtitle.trim().ifBlank { "我家的私房菜" },
                            category = category.trim().ifBlank { "家常菜" },
                            difficulty = difficulty.trim().ifBlank { "简单" },
                            cookTimeMin = cookTime.toIntOrNull() ?: 30,
                            calories = calories.toIntOrNull() ?: 300,
                            ingredientsJson = gson.toJson(ingList),
                            stepsJson = gson.toJson(stepList),
                            isUserCreated = true
                        )
                        viewModel.publishRecipe(recipe) { ok, msg ->
                            submitting = false
                            toast(context, msg)
                            if (ok) onPublished()
                        }
                    }
                },
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (submitting) "发布中…" else "发布菜谱",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun parseIngredient(text: String): IngredientData {
    // 支持 "番茄 2个" / "番茄,2个" 两种写法
    val sep = text.indexOfFirst { it == ' ' || it == ',' || it == '，' }
    return if (sep > 0) {
        IngredientData(
            name = text.substring(0, sep).trim(),
            amount = text.substring(sep + 1).trim()
        )
    } else {
        IngredientData(name = text, amount = "")
    }
}

private fun toast(context: android.content.Context, msg: String) {
    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
}

@Composable
private fun Field(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
    )
}

@Composable
private fun AddText(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun removeIcon(onClick: () -> Unit) {
    androidx.compose.foundation.Image(
        painter = painterResource(id = R.drawable.x),
        contentDescription = "删除",
        colorFilter = ColorFilter.tint(TextTertiary),
        modifier = Modifier
            .size(18.dp)
            .clickable(onClick = onClick)
    )
}

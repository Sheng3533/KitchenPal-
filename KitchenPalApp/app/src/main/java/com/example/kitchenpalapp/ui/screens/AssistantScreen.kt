package com.example.kitchenpalapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.AssistantMessage
import com.example.kitchenpalapp.data.AssistantMessageType
import com.example.kitchenpalapp.data.AssistantRole
import com.example.kitchenpalapp.data.GeneratedIngredient
import com.example.kitchenpalapp.data.GeneratedRecipe
import com.example.kitchenpalapp.data.IngredientAnalysis
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.BackgroundWarm
import com.example.kitchenpalapp.ui.theme.Border
import com.example.kitchenpalapp.ui.theme.BorderLight
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.PrimaryLightest
import com.example.kitchenpalapp.ui.theme.StateErrorBg
import com.example.kitchenpalapp.ui.theme.StateInfo
import com.example.kitchenpalapp.ui.theme.StateInfoBg
import com.example.kitchenpalapp.ui.theme.StateSuccess
import com.example.kitchenpalapp.ui.theme.StateSuccessBg
import com.example.kitchenpalapp.ui.theme.StateWarning
import com.example.kitchenpalapp.ui.theme.StateWarningBg
import com.example.kitchenpalapp.ui.theme.Surface as SurfaceColor
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.theme.softTint
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private val tabs = listOf("智能问答" to R.drawable.message_square, "食谱生成" to R.drawable.sparkles, "食材分析" to R.drawable.search, "烹饪技巧" to R.drawable.info)

/** 「食谱生成」页的常用食材，点一下即加入输入框（再点一下移除）。 */
private val COMMON_INGREDIENTS = listOf(
    "番茄", "鸡蛋", "土豆", "青椒", "洋葱", "胡萝卜",
    "豆腐", "牛肉", "猪肉", "虾仁", "米饭", "面条"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    viewModel: MainViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val messages by viewModel.assistantMessages.collectAsState()
    val isLoading by viewModel.assistantLoading.collectAsState()
    val generatedRecipe by viewModel.generatedRecipe.collectAsState()
    val ingredientAnalysis by viewModel.ingredientAnalysis.collectAsState()
    val cookingTipsContent by viewModel.cookingTipsContent.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        ScreenHeader(title = "AI 厨房助手", subtitle = "智能食谱推荐、烹饪技巧问答、食材分析")

        AssistantTabBar(selected = selectedTab, onSelect = { selectedTab = it })

        when (selectedTab) {
            0 -> ChatTab(messages = messages, isLoading = isLoading, onSend = { viewModel.sendAssistantMessage(it) })
            1 -> GenerateRecipeTab(
                isLoading = isLoading,
                recipe = generatedRecipe,
                onGenerate = { ingredients, style, dietary ->
                    viewModel.generateRecipe(ingredients, style, dietary)
                },
                onClear = { viewModel.clearGeneratedRecipe() }
            )
            2 -> AnalyzeTab(
                isLoading = isLoading,
                analysis = ingredientAnalysis,
                onAnalyze = { viewModel.analyzeIngredients(it) },
                onClear = { viewModel.clearIngredientAnalysis() }
            )
            3 -> TipsTab(
                isLoading = isLoading,
                tips = cookingTipsContent,
                onGetTips = { viewModel.getCookingTips(it) },
                onClear = { viewModel.clearCookingTips() }
            )
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

/**
 * 四段等宽分段控件，替代原来的 ScrollableTabRow。
 *
 * 原实现 4 个「图标 + 4 个汉字」的 Tab 总宽已超过一屏（约 416dp > 411dp），
 * 第 4 个「烹饪技巧」被硬生生截掉半个字，观感像界面没做完。
 * 改成等宽分段：外层浅橙底槽 + 选中段填充 Primary，宽度自适应任何字数/字号，
 * 不会再有裁切，也不需要用户横向滑动才能看到完整标签。
 */
@Composable
private fun AssistantTabBar(selected: Int, onSelect: (Int) -> Unit) {
    val trackShape = RoundedCornerShape(15.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(trackShape)
            .background(softTint(PrimaryLightest, 0.22f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, (label, iconRes) ->
            val isSelected = selected == index
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isSelected) Primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(if (isSelected) Color.White else TextSecondary),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else TextSecondary
                )
            }
        }
    }
}

/**
 * 表单字段外壳：小图标 + 标签 + 可选副提示，下方挂真正的输入控件。
 *
 * 原来各 Tab 都是「一行裸标签 + 一个下划线输入框」，且输入框容器色用的是
 * BackgroundWarm（== 页面底色），整个输入框直接融进背景，屏幕上只剩一条
 * 孤零零的下划线，看着像没写完。统一套一层外壳后才像个正经表单。
 */
@Composable
private fun FormFieldCard(
    label: String,
    iconRes: Int,
    hint: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(softTint(PrimaryLightest, 0.30f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (!hint.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(7.dp))
                Text(text = hint, fontSize = 11.sp, color = TextTertiary)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

/**
 * 统一的圆角输入框：白底描边卡片，聚焦时描边转主色。
 * 去掉 Material 默认的底部下划线，避免页面里出现"孤立的橙色横线"。
 */
@Composable
private fun FormInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    singleLine: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 13.sp, color = TextTertiary) },
        trailingIcon = trailingIcon,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        minLines = minLines,
        singleLine = singleLine,
        textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceColor,
            unfocusedContainerColor = SurfaceColor,
            focusedBorderColor = Primary,
            unfocusedBorderColor = BorderLight,
            cursorColor = Primary,
            focusedPlaceholderColor = TextTertiary,
            unfocusedPlaceholderColor = TextTertiary
        )
    )
}

@Composable
private fun ChatTab(
    messages: List<AssistantMessage>,
    isLoading: Boolean,
    onSend: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val showQuickPrompts = messages.size <= 1

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (showQuickPrompts) {
            QuickPromptsPanel(
                onSelect = { prompt ->
                    onSend(prompt)
                    focusManager.clearFocus()
                }
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }
            if (isLoading) {
                item {
                    LoadingBubble()
                }
            }
        }

        ChatInputBar(
            value = input,
            onValueChange = { input = it },
            onSend = {
                if (input.isNotBlank()) {
                    onSend(input.trim())
                    input = ""
                    focusManager.clearFocus()
                }
            },
            placeholder = "例如：冰箱里剩鸡蛋和番茄，能做什么？"
        )
    }
}

private val quickPrompts = listOf(
    "冰箱里有鸡蛋、番茄、葱，能做什么菜？" to R.drawable.egg,
    "晚餐想做3道菜，2个人，30分钟内搞定" to R.drawable.timer,
    "鸡胸肉怎么做才不柴？有什么诀窍" to R.drawable.beef,
    "帮我设计一份减脂三餐食谱" to R.drawable.heart,
    "如何挑选新鲜的鱼？有什么技巧" to R.drawable.fish,
    "新手学做饭，从哪道菜开始最简单" to R.drawable.sparkles
)

@Composable
private fun QuickPromptsPanel(onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.sparkles),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Primary),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "试试这些问题",
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        quickPrompts.forEach { (prompt, iconRes) ->
            QuickPromptItem(text = prompt, iconRes = iconRes, onClick = { onSelect(prompt) })
        }
    }
}

@Composable
private fun QuickPromptItem(text: String, iconRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PrimaryLightest)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Primary),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            lineHeight = 18.sp
        )
        Image(
            painter = painterResource(id = R.drawable.chevron_right),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Primary),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ChatBubble(message: AssistantMessage) {
    val isUser = message.role == AssistantRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            AssistantAvatar()
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            when (message.type) {
                AssistantMessageType.RECIPE -> message.recipe?.let { RecipeCard(recipe = it) }
                AssistantMessageType.ANALYSIS -> message.analysis?.let { AnalysisCard(analysis = it) }
                else -> TextBubble(text = message.content, isUser = isUser)
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar()
        }
    }
}

@Composable
private fun TextBubble(text: String, isUser: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = if (isUser) Primary else SurfaceColor,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                )
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isUser) Color.White else TextPrimary,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun AssistantAvatar() {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(PrimaryLightest),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.chef_hat),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Primary),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun UserAvatar() {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Primary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.user),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun LoadingBubble() {
    Row {
        AssistantAvatar()
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(
                    color = SurfaceColor,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { index ->
                    val offset by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 900
                                0.2f at index * 150
                                1f at 300 + index * 150
                                0.2f at 600 + index * 150
                            }
                        ),
                        label = "loading_dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .graphicsLayer { alpha = offset }
                            .clip(CircleShape)
                            .background(TextTertiary)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Background)
                .border(1.dp, Border, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
                textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            color = TextTertiary
                        )
                    }
                    innerTextField()
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                singleLine = true,
                maxLines = 4
            )
            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = if (value.isNotBlank()) Primary else TextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenerateRecipeTab(
    isLoading: Boolean,
    recipe: GeneratedRecipe?,
    onGenerate: (String, String, String) -> Unit,
    onClear: () -> Unit
) {
    var ingredients by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("") }
    var dietary by remember { mutableStateOf("") }
    var styleExpanded by remember { mutableStateOf(false) }
    var dietaryExpanded by remember { mutableStateOf(false) }
    val styles = listOf("不限", "中式", "西式", "日式", "韩式", "川菜", "粤菜", "清淡", "香辣")
    val dietaries = listOf("不限", "素食", "低脂", "低糖", "无麸质")

    // 已选食材集合：用于「常用食材」chip 的高亮态（与输入框内容双向同步）
    val selectedSet = remember(ingredients) {
        ingredients.split(",", "，", "、").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }
    fun toggleIngredient(item: String) {
        val list = ingredients
            .split(",", "，", "、")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableList()
        if (!list.remove(item)) list.add(item)
        ingredients = list.joinToString(",")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        FormFieldCard(
            label = "可用食材",
            iconRes = R.drawable.salad,
            hint = "逗号分隔，也可点下方快速添加"
        ) {
            FormInputBox(
                value = ingredients,
                onValueChange = { ingredients = it },
                placeholder = "例如：番茄,鸡蛋,洋葱",
                minLines = 3
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "常用食材",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "点一下加入", fontSize = 11.sp, color = TextTertiary)
            Spacer(modifier = Modifier.weight(1f))
            if (ingredients.isNotBlank()) {
                Text(
                    text = "清空",
                    fontSize = 11.sp,
                    color = Primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { ingredients = "" }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalGap = 8.dp, verticalGap = 8.dp) {
            COMMON_INGREDIENTS.forEach { item ->
                val on = selectedSet.contains(item)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (on) Primary else SurfaceColor)
                        .border(
                            width = 1.dp,
                            color = if (on) Primary else BorderLight,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { toggleIngredient(item) }
                        .padding(horizontal = 13.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = item,
                        fontSize = 12.5.sp,
                        color = if (on) Color.White else TextSecondary,
                        fontWeight = if (on) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                FormFieldCard(label = "烹饪风格", iconRes = R.drawable.chef_hat) {
                    ExposedDropdownMenuBox(
                        expanded = styleExpanded,
                        onExpandedChange = { styleExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FormInputBox(
                            value = style,
                            onValueChange = {},
                            placeholder = "不限",
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = styleExpanded)
                            },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = styleExpanded,
                            onDismissRequest = { styleExpanded = false }
                        ) {
                            styles.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, fontSize = 14.sp) },
                                    onClick = {
                                        style = if (option == "不限") "" else option
                                        styleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                FormFieldCard(label = "饮食限制", iconRes = R.drawable.leaf) {
                    ExposedDropdownMenuBox(
                        expanded = dietaryExpanded,
                        onExpandedChange = { dietaryExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FormInputBox(
                            value = dietary,
                            onValueChange = {},
                            placeholder = "不限",
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dietaryExpanded)
                            },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = dietaryExpanded,
                            onDismissRequest = { dietaryExpanded = false }
                        ) {
                            dietaries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, fontSize = 14.sp) },
                                    onClick = {
                                        dietary = if (option == "不限") "" else option
                                        dietaryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        ActionButton(
            text = if (isLoading) "生成中..." else "生成食谱",
            enabled = ingredients.isNotBlank() && !isLoading,
            iconRes = R.drawable.sparkles,
            onClick = { onGenerate(ingredients, style, dietary) }
        )

        if (ingredients.isBlank() && !isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "填写或点选至少一种食材后即可生成",
                fontSize = 11.5.sp,
                color = TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (recipe != null) {
            Spacer(modifier = Modifier.height(20.dp))
            RecipeCard(recipe = recipe)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AnalyzeTab(
    isLoading: Boolean,
    analysis: IngredientAnalysis?,
    onAnalyze: (String) -> Unit,
    onClear: () -> Unit
) {
    var ingredients by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        FormFieldCard(
            label = "食材名称",
            iconRes = R.drawable.salad,
            hint = "逗号分隔多种食材"
        ) {
            FormInputBox(
                value = ingredients,
                onValueChange = { ingredients = it },
                placeholder = "例如：番茄,鸡蛋,洋葱",
                minLines = 3
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ActionButton(
            text = if (isLoading) "分析中..." else "分析食材",
            enabled = ingredients.isNotBlank() && !isLoading,
            iconRes = R.drawable.flask_round,
            onClick = { onAnalyze(ingredients) }
        )

        if (analysis != null) {
            Spacer(modifier = Modifier.height(20.dp))
            AnalysisCard(analysis = analysis)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TipsTab(
    isLoading: Boolean,
    tips: String?,
    onGetTips: (String) -> Unit,
    onClear: () -> Unit
) {
    var dishName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        FormFieldCard(
            label = "菜品名称",
            iconRes = R.drawable.soup,
            hint = "例如：红烧肉"
        ) {
            FormInputBox(
                value = dishName,
                onValueChange = { dishName = it },
                placeholder = "请输入菜品名称",
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ActionButton(
            text = if (isLoading) "获取中..." else "获取烹饪技巧",
            enabled = dishName.isNotBlank() && !isLoading,
            iconRes = R.drawable.info,
            onClick = { onGetTips(dishName) }
        )

        if (!tips.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryLightest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                                painter = painterResource(id = R.drawable.info),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Primary),
                                modifier = Modifier.size(20.dp)
                            )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "烹饪技巧",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tips,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * 主行动按钮。
 *
 * 之前只有一片纯 `BorderLight` 灰米色底 + 灰字，禁用态（首次进入必然是这个态，
 * 因为食材为空）看上去像一块"没渲染出来的方块"，既不像按钮也没有主 CTA 的分量。
 * 现在：常态是主色填充 + 白色粗体字 + 图标 + 轻微投影；禁用态改为浅橙底 + 半透明主色字，
 * 依然是"那个橙色按钮"，只是明显不可点，语义清楚也不掉观感。
 */
@Composable
private fun ActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    iconRes: Int? = null
) {
    val shape = RoundedCornerShape(14.dp)
    val container = if (enabled) Primary else softTint(PrimaryLightest, 0.18f)
    // 禁用态文字用主色 0.72 透明度：叠在浅橙底上对比度约 2:1，
    // 低于常态（白字 on 主色）但足够看清——0.45 时只有约 1.5:1，字几乎糊掉。
    val contentColor = if (enabled) Color.White else Primary.copy(alpha = 0.72f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = if (enabled) 6.dp else 0.dp, shape = shape)
            .clip(shape)
            .background(container)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(contentColor),
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun RecipeCard(recipe: GeneratedRecipe) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recipe.name.ifBlank { "AI生成食谱" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (recipe.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = recipe.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalGap = 8.dp,
                verticalGap = 8.dp
            ) {
                Tag(text = recipe.prepTime, bgColor = BackgroundWarm)
                Tag(text = recipe.cookTime, bgColor = BackgroundWarm)
                Tag(text = recipe.servings, bgColor = BackgroundWarm)
                Tag(
                    text = recipe.difficulty,
                    bgColor = when (recipe.difficulty) {
                        "简单" -> StateSuccessBg
                        "中等" -> StateWarningBg
                        "困难" -> StateErrorBg
                        else -> BackgroundWarm
                    },
                    textColor = when (recipe.difficulty) {
                        "简单" -> StateSuccess
                        "中等" -> StateWarning
                        "困难" -> com.example.kitchenpalapp.ui.theme.Error
                        else -> TextPrimary
                    }
                )
                if (recipe.calories.isNotBlank()) {
                    Tag(text = "${recipe.calories} 卡路里", bgColor = BackgroundWarm)
                }
            }

            if (recipe.ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "食材清单",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                recipe.ingredients.forEach { ingredient ->
                    IngredientRow(ingredient = ingredient)
                }
            }

            if (recipe.steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "烹饪步骤",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                recipe.steps.forEachIndexed { index, step ->
                    StepRow(index = index + 1, step = step)
                }
            }

            if (recipe.tips.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryLightest)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "小贴士",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recipe.tips,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: GeneratedIngredient) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = ingredient.name,
            fontSize = 14.sp,
            color = TextPrimary
        )
        Text(
            text = ingredient.quantity,
            fontSize = 14.sp,
            color = TextTertiary
        )
    }
}

@Composable
private fun StepRow(index: Int, step: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$index",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = step,
            fontSize = 14.sp,
            color = TextPrimary,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AnalysisCard(analysis: IngredientAnalysis) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AnalysisItem(
            title = "营养分析",
            content = analysis.nutrition,
            iconRes = R.drawable.heart,
            bgColor = StateInfoBg,
            iconColor = StateInfo
        )
        AnalysisItem(
            title = "风味特点",
            content = analysis.flavor,
            iconRes = R.drawable.flame,
            bgColor = StateSuccessBg,
            iconColor = StateSuccess
        )
        AnalysisItem(
            title = "推荐搭配",
            content = analysis.pairing,
            iconRes = R.drawable.share_2,
            bgColor = StateWarningBg,
            iconColor = StateWarning
        )
        AnalysisItem(
            title = "储存建议",
            content = analysis.storage,
            iconRes = R.drawable.download,
            bgColor = BackgroundWarm,
            iconColor = TextTertiary
        )
    }
}

@Composable
private fun AnalysisItem(
    title: String,
    content: String,
    iconRes: Int,
    bgColor: Color,
    iconColor: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(iconColor),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun Tag(text: String, bgColor: Color, textColor: Color = TextPrimary) {
    if (text.isBlank()) return
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FlowRow(
    horizontalGap: Dp = 0.dp,
    verticalGap: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content) { measurables, constraints ->
        val hGapPx = horizontalGap.roundToPx()
        val vGapPx = verticalGap.roundToPx()

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))

            if (currentRow.isNotEmpty() && currentRowWidth + hGapPx + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }

            currentRow.add(placeable)
            currentRowWidth += if (currentRow.size == 1) placeable.width else hGapPx + placeable.width
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        val width = rowWidths.maxOrNull()?.coerceIn(constraints.minWidth, constraints.maxWidth) ?: constraints.minWidth
        val height = rowHeights.sumOf { it } + (rowHeights.size - 1).coerceAtLeast(0) * vGapPx

        layout(width, height) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = 0
                row.forEachIndexed { index, placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + if (index < row.size - 1) hGapPx else 0
                }
                y += rowHeights[rowIndex] + if (rowIndex < rows.size - 1) vGapPx else 0
            }
        }
    }
}
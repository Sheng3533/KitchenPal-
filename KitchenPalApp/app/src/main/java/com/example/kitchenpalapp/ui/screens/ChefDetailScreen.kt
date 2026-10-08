package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.ChefData
import com.example.kitchenpalapp.data.KitchenPalRepository
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.CreamWhite
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.WarmGray
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

data class ChatMessage(val id: Long, val isUser: Boolean, val content: String, val timestamp: String)

private val IntroBg = Color(0xFFFDF7EF)
private val DividerColor = Color(0x14000000)
private val ChipBg = Color(0xFFFFF3E4)
private val OnlineGreen = Color(0xFF22C55E)
private val AvatarStart = Color(0xFFFFA05C)
private val AvatarEnd = Primary

/**
 * 顶栏渐变两端。取值贴着品牌色 Primary(#E8743B) 上下浮动：
 * 起始 #E8763A 与品牌色几乎一致，到 #C25A1E 收深。
 * 这样白色文字对比度约 3.0:1 → 4.4:1，达到大号粗体文字的 AA 线；
 * 早前用的 #FFB877 白字只有约 1.7:1，观感发灰发糊。
 */
private val HeaderStart = Color(0xFFE8763A)
private val HeaderEnd = Color(0xFFC25A1E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefDetailScreen(
    chefId: Long,
    onBack: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    var chef by remember { mutableStateOf<ChefData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(chefId, reloadKey) {
        isLoading = true
        loadFailed = false
        val result = KitchenPalRepository.getChefDetail(chefId)
        isLoading = false
        val data = result.data
        if (result.isSuccess && data != null) {
            chef = data
            messages.clear()
            messages.add(
                ChatMessage(
                    1L,
                    false,
                    "你好！我是${data.name}，${data.title}。很高兴为您解答烹饪方面的问题，请随时提问～",
                    "刚刚"
                )
            )
        } else {
            chef = null
            loadFailed = true
        }
    }

    // 新消息自动滚到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size + 1)
        }
    }

    val handleSendMessage: () -> Unit = {
        val text = inputMessage.trim()
        if (text.isNotEmpty() && !isSending) {
            messages.add(ChatMessage(System.currentTimeMillis(), true, text, "刚刚"))
            inputMessage = ""
            isSending = true
            coroutineScope.launch {
                val result = KitchenPalRepository.chatWithChef(chefId, text)
                isSending = false
                val reply = result.data?.get("reply")?.takeIf { it.isNotBlank() }
                    ?: "抱歉，我暂时没能组织好回答，请换种说法再问我一次。"
                messages.add(ChatMessage(System.currentTimeMillis(), false, reply, "刚刚"))
            }
        }
    }

    Scaffold(
        topBar = {
            ChefDetailHeader(
                chefName = chef?.name,
                chefTitle = chef?.title,
                onBack = onBack
            )
        },
        bottomBar = {
            if (chef != null) {
                MessageInputBar(
                    message = inputMessage,
                    onMessageChange = { inputMessage = it },
                    onSend = handleSendMessage,
                    isSending = isSending
                )
            }
        },
        containerColor = CreamWhite
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Primary) }
            }

            loadFailed -> {
                LoadFailedState(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onRetry = { reloadKey++ }
                )
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { chef?.let { ChefProfileCard(it) } }
                    item { ChatSectionHeader() }
                    items(messages, key = { it.id }) { message ->
                        ChatBubble(message = message, chefName = chef?.name ?: "大厨")
                    }
                }
            }
        }
    }
}

/**
 * 顶部渐变栏：暖橙渐变 + 白色返回按钮 + 厨师姓名/职称，底部圆角收口并带柔和阴影。
 *
 * ⚠️ 光晕必须用 drawBehind 画，不能写成子 Box：
 *    Box 的尺寸取「最大子项尺寸」，而 Modifier.offset 只改变绘制位置、
 *    不缩小子项被测量出的尺寸。之前两枚光晕是 150dp / 96dp 的子 Box，
 *    直接把整条顶栏顶到 150dp，正文下方凭空多出近 80dp 空白橙色。
 *    改用 drawBehind 后光晕纯视觉、零布局影响，顶栏回到 64dp 正常高度。
 */
@Composable
private fun ChefDetailHeader(
    chefName: String?,
    chefTitle: String?,
    onBack: () -> Unit
) {
    val headerShape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = headerShape, clip = false)
            .clip(headerShape)
            .background(Brush.linearGradient(listOf(HeaderStart, HeaderEnd)))
            .drawBehind {
                // 右上大光晕（偏出边界，只露出一角）
                drawSoftGlow(
                    center = Offset(size.width * 0.92f, -size.height * 0.30f),
                    radius = size.width * 0.58f,
                    alpha = 0.18f
                )
                // 左下小光晕，平衡构图
                drawSoftGlow(
                    center = Offset(-size.width * 0.04f, size.height * 1.20f),
                    radius = size.width * 0.34f,
                    alpha = 0.12f
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "返回",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // 显式给 lineHeight：默认行高（bodyLarge 的 24sp）会在 12~17sp 文字上下各留一大截
                // 空白，导致整块文字在导航栏里视觉偏上。收紧后上下留白才对称。
                Text(
                    text = chefName ?: "大厨详情",
                    color = Color.White,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = chefTitle?.takeIf { it.isNotBlank() } ?: "为你在线解答烹饪难题",
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 在绘制层画一枚「边缘自然消隐」的白色柔光。
 * 用径向渐变代替纯色填充，避免出现硬边圆形色块（看起来像污渍）。
 */
private fun DrawScope.drawSoftGlow(center: Offset, radius: Float, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

@Composable
private fun LoadFailedState(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "😕", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "大厨资料加载失败",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "请确认后端服务已启动，然后重试",
            fontSize = 13.sp,
            color = WarmGray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "重新加载", color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ChefProfileCard(chef: ChefData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头像
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(AvatarStart, AvatarEnd))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chef.name.take(1),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = chef.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (chef.status == 1) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(OnlineGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "在线",
                            color = OnlineGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = chef.title,
            fontSize = 13.sp,
            color = Primary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 数据行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ChefStatItem(
                value = "${chef.experienceYears}年",
                label = "从业年限",
                modifier = Modifier.weight(1f)
            )
            ChefStatItem(
                value = chef.specialty.ifBlank { "—" },
                label = "擅长菜系",
                modifier = Modifier.weight(1f)
            )
            ChefStatItem(
                value = chef.signatureDish.ifBlank { "—" },
                label = "招牌菜",
                modifier = Modifier.weight(1f)
            )
        }

        if (chef.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(IntroBg)
                    .padding(14.dp)
            ) {
                Text(
                    text = "个人简介",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = chef.description,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            }
        }

        val chips = listOfNotNull(
            chef.style.takeIf { it.isNotBlank() },
            chef.specialty.takeIf { it.isNotBlank() }.takeIf { it != chef.style }
        )
        if (chips.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEach { TagChip(it) }
            }
        }
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(DividerColor)
    )
}

@Composable
private fun ChefStatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(IntroBg)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = WarmGray
        )
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ChipBg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ChatSectionHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(3.dp, 14.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(Primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "向他提问",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, chefName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chefName.take(1),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 278.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isUser) 16.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (message.isUser) Primary else Surface)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = if (message.isUser) Color.White else TextPrimary,
                    modifier = Modifier.widthIn(max = 250.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.timestamp,
                fontSize = 10.sp,
                color = WarmGray
            )
        }
    }
}

@Composable
private fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    val canSend = message.trim().isNotEmpty() && !isSending
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            placeholder = { Text("输入消息…", color = WarmGray, fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFF3E3D0),
                focusedBorderColor = Primary,
                unfocusedContainerColor = Surface,
                focusedContainerColor = Surface
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (canSend || isSending) Primary else Color(0xFFE6D9CC))
                .clickable(enabled = canSend) { onSend() },
            contentAlignment = Alignment.Center
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.send),
                    contentDescription = "发送",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

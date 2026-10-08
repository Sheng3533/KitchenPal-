package com.example.kitchenpalapp.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.PrimaryLight
import com.example.kitchenpalapp.ui.theme.PrimaryLighter
import com.example.kitchenpalapp.ui.theme.PrimaryLightest
import com.example.kitchenpalapp.ui.theme.StateSuccess
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary

data class OnboardingPage(
    val title: String,
    val description: String,
    val iconRes: Int,
    val features: List<Pair<String, String>>
)

@Composable
fun OnboardingScreen(
    onStart: () -> Unit,
    onSkip: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "AI智能火候提醒",
            description = "实时监测烹饪温度，智能提醒火候调整，让每道菜都完美出锅",
            iconRes = R.drawable.flame,
            features = listOf(
                "拍照识食材" to "AI自动识别冰箱里的食材",
                "智能推荐" to "根据食材推荐合适菜谱",
                "火候守护" to "全程监控烹饪火候"
            )
        ),
        OnboardingPage(
            title = "海量精选菜谱",
            description = "汇集各地美食，从家常小炒到米其林大餐，应有尽有",
            iconRes = R.drawable.chef_hat,
            features = listOf(
                "精选菜谱" to "专业大厨精心研发",
                "详细步骤" to "图文并茂轻松上手",
                "视频教程" to "手把手教学更直观"
            )
        ),
        OnboardingPage(
            title = "健康饮食管理",
            description = "记录每日饮食，分析营养摄入，打造健康生活方式",
            iconRes = R.drawable.heart,
            features = listOf(
                "饮食记录" to "轻松记录每餐摄入",
                "营养分析" to "科学评估饮食健康",
                "饮食报告" to "每周汇总健康建议"
            )
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IllustrationCircle(page.iconRes)
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = page.title,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = page.description,
                    fontSize = 15.sp,
                    color = TextTertiary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                page.features.forEachIndexed { index, feature ->
                    FeatureCard(
                        iconRes = when (index) {
                            0 -> R.drawable.camera
                            1 -> R.drawable.sparkles
                            else -> R.drawable.flame
                        },
                        title = feature.first,
                        subtitle = feature.second
                    )
                    if (index < page.features.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentPage) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (index == currentPage) Primary else PrimaryLighter)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                    if (index < pages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "开始使用",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "跳过",
                fontSize = 14.sp,
                color = TextTertiary,
                modifier = Modifier
                    .clickable(onClick = onSkip)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun IllustrationCircle(iconRes: Int) {
    val transition = rememberInfiniteTransition(label = "onboarding_dots")

    val dot1Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    val dot4Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot4"
    )
    val dot5Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot5"
    )
    val dot6Y by transition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot6"
    )
    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(x = (-95).dp, y = (-85).dp + dot1Y.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(PrimaryLight.copy(alpha = 0.6f))
        )
        Box(
            modifier = Modifier
                .offset(x = 88.dp, y = (-78).dp + dot2Y.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(PrimaryLighter.copy(alpha = 0.7f))
        )
        Box(
            modifier = Modifier
                .offset(x = (-92).dp, y = 75.dp + dot3Y.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(PrimaryLighter.copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .offset(x = 90.dp, y = 82.dp + dot4Y.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(PrimaryLight.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .offset(x = (-55).dp, y = (-105).dp + dot5Y.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(PrimaryLighter.copy(alpha = 0.8f))
        )
        Box(
            modifier = Modifier
                .offset(x = 58.dp, y = (-102).dp + dot6Y.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(PrimaryLight.copy(alpha = 0.5f))
        )

        Canvas(
            modifier = Modifier.size(220.dp)
        ) {
            drawCircle(
                color = Primary.copy(alpha = 0.2f),
                radius = size.minDimension / 2f,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(20f, 14f),
                        phase = 0f
                    )
                )
            )
        }

        Box(
            modifier = Modifier
                .size(192.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Primary,
                            PrimaryLight,
                            PrimaryLighter
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

@Composable
private fun FeatureCard(
    iconRes: Int,
    title: String,
    subtitle: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryLightest),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(StateSuccess),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

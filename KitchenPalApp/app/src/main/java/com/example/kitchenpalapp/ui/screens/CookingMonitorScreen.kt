package com.example.kitchenpalapp.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.CookStep
import androidx.compose.ui.platform.LocalContext
import com.example.kitchenpalapp.data.AppPreferences
import com.example.kitchenpalapp.data.Recipe
import com.example.kitchenpalapp.data.SampleData
import com.example.kitchenpalapp.ui.theme.softTint
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.PrimaryLight
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.theme.StateSuccess
import com.example.kitchenpalapp.ui.theme.StateWarning
import com.example.kitchenpalapp.ui.theme.StateError
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import com.example.kitchenpalapp.ui.components.StepTtsState
import com.example.kitchenpalapp.ui.components.rememberStepTts
import kotlinx.coroutines.delay

private val DarkBg = Color(0xFF2D1F14)
private val DarkSurface = Color(0xFF3D2B1F)
private val DarkText = Color(0xFFFFF8F0)
private val DarkTextSecondary = Color(0xFFB07D55)
private val DarkTextTertiary = Color(0xFF8B5E3C)
private val OrangeGlow = Color(0xFFE8743B)

@Composable
fun CookingMonitorScreen(
    recipeId: String,
    onBack: () -> Unit,
    onComplete: (elapsedSeconds: Int) -> Unit,
    onResetClick: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val recipe = viewModel.getRecipeByIdStr(recipeId)
    val steps = recipe?.steps ?: emptyList()
    val totalSeconds = (recipe?.cookTimeMin ?: 30) * 60
    val tts = rememberStepTts()
    var currentStep by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var timerStarted by remember { mutableStateOf(false) }
    var resetKey by remember { mutableIntStateOf(0) }
    var currentStepTotal by remember { mutableIntStateOf(0) }
    var remainingSeconds by remember { mutableIntStateOf(0) }
    // 从进入烹饪页到完成的真实累计用时（暂停时不累计）
    var totalElapsedSeconds by remember { mutableIntStateOf(0) }
    // 火候提醒浮层：到「灵敏度」设定的间隔后弹出一次，提醒用户看火
    var heatRemindVisible by remember { mutableStateOf(false) }
    var heatRemindSecondsLeft by remember { mutableIntStateOf(0) }
    val monitorContext = LocalContext.current
    val heatRemindIntervalSec = remember(monitorContext) {
        AppPreferences(monitorContext).getHeatRemindIntervalSeconds()
    }
    val remindIntervalMs = heatRemindIntervalSec * 1000L

    // 累计整段烹饪的真实经过时间（暂停不计）
    LaunchedEffect(isPaused, isCompleted) {
        while (!isCompleted && !isPaused) {
            delay(1000)
            totalElapsedSeconds++
        }
    }

    // 每次切换步骤或重置时，按当前步骤 duration 重新初始化倒计时（无单步时长则回退到整道菜总时长）
    LaunchedEffect(resetKey, currentStep) {
        val step = steps.getOrNull(currentStep)
        // 进入/切换步骤时自动朗读当前步骤（无中文语音包则静默跳过）
        if (step != null) tts.autoSpeakStep(step, currentStep)
        val stepDuration = step?.durationSeconds ?: 0
        currentStepTotal = if (stepDuration > 0) stepDuration else totalSeconds
        remainingSeconds = currentStepTotal
        timerStarted = true
        while (!isCompleted && remainingSeconds > 0) {
            if (isPaused) {
                delay(500)
            } else {
                delay(1000)
                remainingSeconds--
            }
        }
        if (remainingSeconds <= 0 && !isCompleted) {
            isCompleted = true
            onComplete(totalElapsedSeconds)
        }
    }

    // 火候提醒：按「设置 → 火候灵敏度」的间隔周期性提醒看火（低 300s / 中等 180s / 高 90s）。
    // 暂停或已完成时不触发；提醒弹出 5 秒后自动收起。
    LaunchedEffect(resetKey, heatRemindIntervalSec) {
        var lastFiredAt = 0L
        while (!isCompleted) {
            delay(1000)
            if (isPaused) continue
            val now = System.currentTimeMillis()
            heatRemindSecondsLeft =
                ((remindIntervalMs - (now - lastFiredAt)) / 1000L).coerceAtLeast(0).toInt()
            if (now - lastFiredAt >= remindIntervalMs) {
                lastFiredAt = now
                heatRemindSecondsLeft = 5
                heatRemindVisible = true
                val cur = steps.getOrNull(currentStep)
                if (cur != null && !cur.heat.isNullOrBlank()) {
                    tts.speakStep(cur, currentStep)
                }
                delay(5000)
                heatRemindVisible = false
            }
        }
    }

    // TextToSpeech 引擎是异步初始化的：进入页面时它可能尚未 ready，上面的 LaunchedEffect 首次触发会落空。
    // 这里监听 ready，引擎一就绪就补播「当前步骤」（首次进入即第 1 步），之后切步骤由上面的 LaunchedEffect 负责。
    LaunchedEffect(tts.ready) {
        if (tts.ready) {
            val step = steps.getOrNull(currentStep)
            if (step != null) tts.autoSpeakStep(step, currentStep)
        }
    }

    val doReset: () -> Unit = {
        currentStep = 0
        isPaused = false
        isCompleted = false
        timerStarted = false
        resetKey++
        totalElapsedSeconds = 0
    }

    val elapsedSeconds = currentStepTotal - remainingSeconds
    val progress = if (currentStepTotal > 0) elapsedSeconds.toFloat() / currentStepTotal.toFloat() else 0f
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeStr = "%02d:%02d".format(minutes, seconds)
    val elapsedMin = elapsedSeconds / 60
    val elapsedSec = elapsedSeconds % 60
    val elapsedStr = "%02d:%02d".format(elapsedMin, elapsedSec)

    val step = steps.getOrNull(currentStep)
    val heatText = step?.heat
    // 「无需火候」「常温」等非加热步骤不弹提醒、仪表盘走「无需加热」态
    val noHeat = heatText.isNullOrBlank() || heatText.contains("无需") || heatText.contains("常温") || heatText.contains("凉")
    val heatLevel = if (noHeat) 0f else getHeatLevelFromTip(heatText)
    val showWarning = !noHeat

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(OrangeGlow.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(0.5f, -0.5f),
                        radius = 0.8f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            MonitorTopBar(onBack = onBack, onResetClick = doReset)

            // 到点弹出的火候提醒（频率由「设置 → 火候灵敏度」决定）
            if (heatRemindVisible && !noHeat) {
                HeatRemindBanner(
                    heat = heatText ?: "",
                    countdown = heatRemindSecondsLeft,
                    onDismiss = { heatRemindVisible = false }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (showWarning) {
                WarningBanner(tip = step?.heat ?: "")
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            HeatGauge(heatLevel = heatLevel, noHeat = noHeat)

            Spacer(modifier = Modifier.height(16.dp))

            TimerSection(
                timeStr = timeStr,
                elapsedStr = elapsedStr,
                progress = progress.coerceIn(0f, 1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (step != null) {
                CurrentStepCard(
                    stepIndex = currentStep,
                    totalSteps = steps.size,
                    step = step,
                    tts = tts,
                    onPrevStep = {
                        if (currentStep > 0) {
                            currentStep--
                        }
                    },
                    onNextStep = {
                        if (currentStep < steps.size - 1) {
                            currentStep++
                        } else {
                            isCompleted = true
                            onComplete(totalElapsedSeconds)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StepOverview(
                steps = steps,
                current = currentStep,
                onStepClick = { index -> currentStep = index }
            )

            Spacer(modifier = Modifier.weight(1f))

            BottomControls(
                isPaused = isPaused,
                onPauseToggle = { isPaused = !isPaused },
                onComplete = {
                    isCompleted = true
                    onComplete(totalElapsedSeconds)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/** 到点弹出的火候提醒条：显示当前火候与自动收起倒计时 */
@Composable
private fun HeatRemindBanner(
    heat: String,
    countdown: Int,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(OrangeGlow),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.flame),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "火候提醒",
                color = DarkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "当前「$heat」，记得看锅 · ${countdown}s 后收起",
                color = DarkTextSecondary,
                fontSize = 12.sp
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "×", color = Color.White, fontSize = 18.sp)
        }
    }
}

private fun getHeatLevelFromTip(tip: String?): Float {
    if (tip == null) return 0.5f
    return when {
        tip.contains("大火") -> 0.85f
        tip.contains("中火") -> 0.55f
        tip.contains("小火") -> 0.25f
        tip.contains("煮沸") -> 0.8f
        tip.contains("慢炖") || tip.contains("炖煮") -> 0.3f
        tip.contains("翻炒") -> 0.6f
        tip.contains("煎") -> 0.5f
        else -> 0.5f
    }
}

@Composable
private fun MonitorTopBar(onBack: () -> Unit, onResetClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = "返回",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "火候监控",
            color = DarkText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .clickable(onClick = onResetClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                    painter = painterResource(id = R.drawable.timer),
                    contentDescription = "重置",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(20.dp)
                )
        }
    }
}

@Composable
private fun WarningBanner(tip: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "warning_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9999.dp))
            .background(StateWarning.copy(alpha = 0.15f))
            .drawBehind {
                drawRoundRect(
                    color = StateWarning.copy(alpha = glowAlpha * 0.5f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(StateWarning.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flame),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(StateWarning),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "火候提醒：$tip",
                color = StateWarning,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HeatGauge(heatLevel: Float, noHeat: Boolean = false) {
    val clampedLevel = heatLevel.coerceIn(0f, 1f)
    val activeSweep = clampedLevel * 270f

    val heatLabel = when {
        noHeat -> "无需加热"
        clampedLevel < 0.3f -> "小火"
        clampedLevel < 0.6f -> "中火"
        else -> "大火"
    }
    val heatTemp = when {
        noHeat -> "常温备料"
        clampedLevel < 0.3f -> "约100°C"
        clampedLevel < 0.6f -> "约160°C"
        else -> "约220°C"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(256.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(220.dp)
        ) {
            val strokeWidth = 12.dp.toPx()
            val inset = strokeWidth / 2

            drawArc(
                color = DarkSurface,
                startAngle = -90f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val fullGradient = Brush.sweepGradient(
                colors = listOf(
                    StateError,
                    Primary,
                    StateWarning,
                    StateSuccess,
                    StateSuccess
                )
            )

            drawArc(
                brush = fullGradient,
                startAngle = -90f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val activeGradient = Brush.sweepGradient(
                colors = listOf(
                    StateError,
                    Primary,
                    StateWarning,
                    StateSuccess
                )
            )

            drawArc(
                brush = activeGradient,
                startAngle = -90f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val indicatorAngle = -90f + activeSweep
            val indicatorRadians = Math.toRadians(indicatorAngle.toDouble())
            val radius = (size.width / 2) - inset
            val centerX = size.width / 2
            val centerY = size.height / 2
            val dotX = centerX + radius * kotlin.math.cos(indicatorRadians).toFloat()
            val dotY = centerY + radius * kotlin.math.sin(indicatorRadians).toFloat()

            drawCircle(
                color = OrangeGlow,
                radius = 14.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = Offset(dotX, dotY)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = heatLabel,
                color = DarkText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = heatTemp,
                color = DarkTextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun TimerSection(
    timeStr: String,
    elapsedStr: String,
    progress: Float
) {
    Column {
        Text(
            text = "剩余时间",
            color = DarkTextTertiary,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = timeStr,
            color = DarkText,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(PrimaryLight, Primary)
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "已烹饪 $elapsedStr",
            color = DarkTextTertiary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CurrentStepCard(
    stepIndex: Int,
    totalSteps: Int,
    step: CookStep,
    tts: StepTtsState,
    onPrevStep: () -> Unit,
    onNextStep: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(softTint(Color(0xFFFBE8D6)))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "步骤 ${stepIndex + 1}/$totalSteps",
                    color = Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { tts.speakStep(step, stepIndex) },
                modifier = Modifier.size(32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_volume),
                    contentDescription = "朗读这一步",
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(18.dp)
                )
            }
            if (step.durationSeconds > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.clock),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Primary),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatStepDuration(step.durationSeconds),
                        color = Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        if (step.title.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = step.title,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp
            )
        }
        if (step.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.description,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
        if (!step.heat.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(softTint(Color(0xFFFFF4E0)))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flame),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xFFE8A33D)),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (step.heat.contains("无需") || step.heat.contains("常温")) {
                        "本步无需加热"
                    } else {
                        "建议火候：${step.heat}"
                    },
                    color = Color(0xFFE8A33D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (step.ingredients.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "本步用料",
                color = TextTertiary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                step.ingredients.forEach { name ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(softTint(Color(0xFFF3ECE5)))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = name,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
        if (!step.tip.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(softTint(Color(0xFFF7F2EC)))
                    .padding(12.dp)
            ) {
                Text(
                    text = "小贴士",
                    color = Primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.tip,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(softTint(Color(0xFFF0E6DC)))
                    .clickable(onClick = onPrevStep),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.chevron_down),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Primary),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "上一步",
                        color = Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PrimaryLight, Primary)
                        )
                    )
                    .clickable(onClick = onNextStep),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (stepIndex >= totalSteps - 1) "完成烹饪" else "下一步",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.chevron_down),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepOverview(
    steps: List<CookStep>,
    current: Int,
    onStepClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(14.dp)
    ) {
        Text(
            text = "全部步骤",
            color = DarkTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        steps.forEachIndexed { index, s ->
            val isCurrent = index == current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isCurrent) Color.White.copy(alpha = 0.14f) else Color.Transparent)
                    .clickable(onClick = { onStepClick(index) })
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                index < current -> Color(0xFF4CAF50)
                                isCurrent -> Primary
                                else -> Color.White.copy(alpha = 0.18f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < current) {
                        Image(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = s.title.ifBlank { "步骤 ${index + 1}" },
                    color = if (isCurrent) DarkText else DarkTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (s.durationSeconds > 0) {
                    Text(
                        text = formatStepDuration(s.durationSeconds),
                        color = DarkTextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

private fun formatStepDuration(seconds: Int): String {
    if (seconds <= 0) return ""
    val minutes = seconds / 60
    val secs = seconds % 60
    return when {
        minutes > 0 && secs > 0 -> "${minutes}分${secs}秒"
        minutes > 0 -> "${minutes}分钟"
        else -> "${secs}秒"
    }
}

@Composable
private fun BottomControls(
    isPaused: Boolean,
    onPauseToggle: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onPauseToggle),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.pause),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPaused) "继续计时" else "暂停计时",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(Color.Transparent)
                .border(1.dp, Color.White, RoundedCornerShape(9999.dp))
                .clickable(onClick = onComplete),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "完成烹饪",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
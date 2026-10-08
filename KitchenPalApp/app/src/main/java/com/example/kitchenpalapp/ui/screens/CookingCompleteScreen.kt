package com.example.kitchenpalapp.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Border
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.PrimaryLight
import com.example.kitchenpalapp.ui.theme.PrimaryLighter
import com.example.kitchenpalapp.ui.theme.PrimaryLightest
import com.example.kitchenpalapp.ui.theme.StateSuccess
import com.example.kitchenpalapp.ui.theme.StateWarning
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.data.Recipe
import com.example.kitchenpalapp.data.SampleData
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import kotlin.math.sin

private data class ConfettiDot(
    val xFrac: Float,
    val yFrac: Float,
    val sizeDp: Int,
    val color: Color
)

private val ConfettiDots = listOf(
    ConfettiDot(0.10f, 0.14f, 10, Primary),
    ConfettiDot(0.86f, 0.10f, 8, StateSuccess),
    ConfettiDot(0.93f, 0.30f, 12, PrimaryLight),
    ConfettiDot(0.06f, 0.34f, 9, StateWarning),
    ConfettiDot(0.14f, 0.60f, 11, PrimaryLighter),
    ConfettiDot(0.90f, 0.66f, 10, Primary),
    ConfettiDot(0.08f, 0.82f, 8, StateSuccess),
    ConfettiDot(0.88f, 0.86f, 11, PrimaryLight),
    ConfettiDot(0.50f, 0.06f, 9, StateWarning),
    ConfettiDot(0.28f, 0.93f, 10, Primary),
    ConfettiDot(0.72f, 0.95f, 8, StateSuccess)
)

@Composable
fun CookingCompleteScreen(
    recipeId: String,
    elapsedSeconds: Int = 0,
    onHome: () -> Unit,
    onMoreRecipes: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val recipe = viewModel.getRecipeByIdStr(recipeId)
    val recipeTitle = recipe?.title ?: "美食"
    var rating by remember { mutableStateOf(4) }
    var recorded by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }

    // 成品照选择：从相册挑一张 JPEG，走 /api/upload/image 上传后作为评论配图发布
    val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            uploading = true
            val numericId = recipeId.toLongOrNull()
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    android.widget.Toast.makeText(context, "读取图片失败", android.widget.Toast.LENGTH_SHORT).show()
                } else if (numericId == null) {
                    android.widget.Toast.makeText(context, "该食谱不支持上传", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.uploadCookingPhoto(numericId, bytes) { ok, msg ->
                        uploading = false
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                uploading = false
                android.widget.Toast.makeText(context, "上传失败：${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 点离开按钮时按最终星级落库（历史记录 + 用户统计），并防止重复记录
    val finishCooking: () -> Unit = {
        if (!recorded) {
            recorded = true
            viewModel.completeCooking(recipeId, rating, elapsedSeconds)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val baseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "base"
    )

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        ConfettiBackground(baseAnim = baseAnim)

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 340.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SuccessIcon()

            Text(
                text = "恭喜，烹饪完成！",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 20.dp)
            )

            Text(
                text = recipeTitle,
                color = TextSecondary,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
                    .height(1.dp)
                    .background(Border)
            )

            StatsRow(
                recipe = recipe,
                elapsedSeconds = elapsedSeconds
            )

            RatingSection(
                rating = rating,
                onRatingChange = { rating = it },
                modifier = Modifier.padding(top = 20.dp)
            )

            ShareSection(
                modifier = Modifier.padding(top = 20.dp),
                onShare = {
                    com.example.kitchenpalapp.utils.ShareHelper.shareRecipe(
                        context,
                        recipeTitle,
                        recipe?.subtitle ?: ""
                    )
                },
                onSaveImage = {
                    // 生成分享卡片并写入相册（原来这里是空回调，点了没反应）。
                    // 卡片上的三项数据全部取自真实菜谱字段，不编造。
                    val card = com.example.kitchenpalapp.utils.ShareCardRenderer.render(
                        recipeTitle = recipeTitle,
                        rating = rating,
                        metrics = listOf(
                            "${recipe?.cookTimeMin ?: 0}min" to "用时",
                            String.format(java.util.Locale.CHINA, "%.1f", recipe?.rating ?: 0f) to "评分",
                            "${recipe?.calories ?: 0}kcal" to "热量"
                        )
                    )
                    val ok = com.example.kitchenpalapp.utils.ShareHelper.saveBitmapToGallery(
                        context,
                        card,
                        "kitchenpal_${System.currentTimeMillis()}.png"
                    )
                    android.widget.Toast.makeText(
                        context,
                        if (ok) "已保存到相册" else "保存失败，请稍后重试",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                onUploadPhoto = {
                    if (viewModel.currentUser.value == null) {
                        android.widget.Toast.makeText(
                            context, "请先登录后再上传成品照", android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else if (uploading) {
                        android.widget.Toast.makeText(
                            context, "正在上传，请稍候…", android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        photoPicker.launch("image/*")
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedPillButton(
                    text = "再做一道",
                    onClick = { finishCooking(); onMoreRecipes() }
                )
                FilledPillButton(
                    text = "返回首页",
                    onClick = { finishCooking(); onHome() }
                )
            }
        }
    }
}

@Composable
private fun ConfettiBackground(baseAnim: Float) {
    val angle = baseAnim * 2f * Math.PI.toFloat()
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = this.maxWidth
        val maxHeight = this.maxHeight
        ConfettiDots.forEachIndexed { index, dot ->
            val v = sin(angle + index * 0.6f)
            val yOffset = v * 8f
            val scaleVal = 1f + v * 0.2f
            val alphaVal = 0.5f + (v + 1f) * 0.25f
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = maxWidth * dot.xFrac - (dot.sizeDp.dp / 2),
                        y = maxHeight * dot.yFrac - (dot.sizeDp.dp / 2) + yOffset.dp
                    )
                    .size(dot.sizeDp.dp)
                    .scale(scaleVal)
                    .alpha(alphaVal)
                    .clip(CircleShape)
                    .background(dot.color)
            )
        }
    }
}

@Composable
private fun SuccessIcon() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .shadow(elevation = 8.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(PrimaryLight, Primary))
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.check),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun StatsRow(
    recipe: Recipe?,
    elapsedSeconds: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val timeText = if (minutes > 0) {
            "%d:%02d".format(minutes, seconds)
        } else {
            "0:%02d".format(seconds)
        }
        StatItem(
            iconRes = R.drawable.clock,
            value = timeText,
            label = "总用时",
            modifier = Modifier.weight(1f)
        )
        StatItem(
            iconRes = R.drawable.check,
            value = "${recipe?.steps?.size ?: 0}步",
            label = "烹饪步骤",
            modifier = Modifier.weight(1f)
        )
        StatItem(
            iconRes = R.drawable.star,
            value = "${recipe?.calories ?: 0}千卡",
            label = "参考热量",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatItem(
    iconRes: Int,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryLightest),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Primary),
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = label,
            color = TextTertiary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun RatingSection(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "为这道菜打分",
            color = TextSecondary,
            fontSize = 14.sp
        )
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 1..5) {
                val filled = i <= rating
                Image(
                    painter = painterResource(
                        id = if (filled) R.drawable.star_filled else R.drawable.star
                    ),
                    contentDescription = "第${i}星",
                    colorFilter = ColorFilter.tint(if (filled) Primary else Border),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onRatingChange(i) }
                )
            }
        }
    }
}

@Composable
private fun ShareSection(
    modifier: Modifier = Modifier,
    onShare: () -> Unit = {},
    onSaveImage: () -> Unit = {},
    onUploadPhoto: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ShareButton(
            iconRes = R.drawable.message_square,
            label = "朋友圈",
            backgroundColor = Color(0xFF07C160),
            iconColor = Color.White,
            onClick = onShare
        )
        ShareButton(
            iconRes = R.drawable.message_square,
            label = "微信好友",
            backgroundColor = Color(0xFF5B8EC9),
            iconColor = Color.White,
            onClick = onShare
        )
        ShareButton(
            iconRes = R.drawable.download,
            label = "保存图片",
            backgroundColor = PrimaryLightest,
            iconColor = Primary,
            onClick = onSaveImage
        )
        ShareButton(
            iconRes = R.drawable.camera,
            label = "传成品照",
            backgroundColor = PrimaryLightest,
            iconColor = Primary,
            onClick = onUploadPhoto
        )
    }
}

@Composable
private fun ShareButton(
    iconRes: Int,
    label: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                colorFilter = ColorFilter.tint(iconColor),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun OutlinedPillButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Color.Transparent)
            .border(1.5.dp, Primary, CircleShape)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FilledPillButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Primary)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

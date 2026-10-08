package com.example.kitchenpalapp.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.DetectedIngredient
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.CreamWhite
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.WarmBlack
import com.example.kitchenpalapp.ui.theme.WarmGray
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import com.example.kitchenpalapp.ui.viewmodel.ScanState
import java.io.File

@Composable
fun ScanScreen(
    onClose: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onIngredientClick: (String) -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val scanState by viewModel.scanState.collectAsState()

    when (scanState) {
        ScanState.Idle -> ScanSourceView(
            onClose = onClose,
            viewModel = viewModel
        )
        ScanState.Scanning -> ScanningView(onClose = onClose)
        is ScanState.Result -> ScanResultView(
            ingredients = viewModel.getScanIngredients(),
            suggestions = viewModel.getScanSuggestedRecipes(),
            capturedBytes = viewModel.capturedImage.value,
            onClose = onClose,
            onRetake = { viewModel.resetScan() },
            onRecipeClick = onRecipeClick
        )
        is ScanState.Error -> ScanErrorView(
            onClose = onClose,
            onRetry = { viewModel.resetScan() }
        )
    }
}

/** 来源选择页：拍照 / 相册 */
@Composable
private fun ScanSourceView(
    onClose: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = cameraUri
        if (ok && uri != null) {
            viewModel.performScanFromUri(uri)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.performScanFromUri(it) }
    }

    /** 设备是否真的装有可处理拍照 Intent 的相机应用 */
    fun hasCameraApp(): Boolean {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return intent.resolveActivity(context.packageManager) != null
    }

    /** 真正发起拍照；全程 try/catch 防止 ActivityNotFoundException 等导致 App 闪退 */
    fun launchCameraInternal() {
        try {
            val dir = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(dir, "scan_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "无法打开相机，请改用相册选择", Toast.LENGTH_SHORT).show()
        }
    }

    // 运行时相机权限申请（Android 6+ 必须动态申请，否则部分机型/模拟器直接崩溃）
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraInternal()
        } else {
            Toast.makeText(context, "未授予相机权限，请改用相册选择", Toast.LENGTH_SHORT).show()
        }
    }

    /** 点击拍照：先检测设备相机，再走权限，最后才真正发起 */
    fun launchCamera() {
        if (!hasCameraApp()) {
            Toast.makeText(context, "设备无可用相机，请改用相册选择", Toast.LENGTH_SHORT).show()
            return
        }
        val perm = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED ->
                launchCameraInternal()
            else ->
                cameraPermissionLauncher.launch(perm)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ingredient_tomato),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))

        IconButton(
            onClick = onClose,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Image(
                painter = painterResource(id = R.drawable.x),
                contentDescription = "关闭",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.camera),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "AI 食材识别",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "拍一张食材照片，或从相册选择\nAI 自动识别并推荐菜谱",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Surface)
                    .border(4.dp, Primary.copy(alpha = 0.6f), CircleShape)
                    .clickable { launchCamera() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "拍照识别",
                    colorFilter = ColorFilter.tint(WarmBlack),
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(
                text = "拍照识别",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
            Box(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.image),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "从相册选择",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/** 识别中动画页 */
@Composable
private fun ScanningView(onClose: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_y"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ingredient_tomato),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        IconButton(
            onClick = onClose,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Image(
                painter = painterResource(id = R.drawable.x),
                contentDescription = "关闭",
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(28.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(320.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Primary)
                    .align(if (scanY < 0.5f) Alignment.TopCenter else Alignment.BottomCenter)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "识别食材中...",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "AI 正在分析你的食材，请稍候",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ScanResultView(
    ingredients: List<DetectedIngredient>,
    suggestions: List<com.example.kitchenpalapp.data.Recipe>,
    capturedBytes: ByteArray?,
    onClose: () -> Unit,
    onRetake: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    val capturedBitmap = remember(capturedBytes) {
        capturedBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }
    val firstSuggestion = suggestions.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamWhite)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            if (capturedBitmap != null) {
                Image(
                    bitmap = capturedBitmap.asImageBitmap(),
                    contentDescription = "拍摄的食材照片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ingredient_tomato),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            IconButton(
                onClick = onClose,
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.x),
                    contentDescription = "关闭",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
            ) {
                Text(
                    text = "识别成功",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Primary.copy(alpha = 0.9f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
                Text(
                    text = "检测到 ${ingredients.size} 种食材",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // 内容区可滚动：内容较多时底部操作栏不会被挤出屏幕
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "已识别食材",
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(ingredients) { ingredient ->
                    IngredientChip(ingredient = ingredient)
                }
            }

            Text(
                text = "AI 推荐",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (firstSuggestion != null) {
                com.example.kitchenpalapp.ui.components.RecipeCard(
                    recipe = firstSuggestion,
                    isFavorite = false,
                    onClick = { onRecipeClick(firstSuggestion.id) },
                    isHorizontal = true
                )
            } else {
                Text(
                    text = "暂无匹配的推荐菜谱",
                    color = WarmGray,
                    fontSize = 14.sp
                )
            }

        }

        // 底部固定操作栏：始终可见，不随内容滚动
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                Button(
                    onClick = onRetake,
                    colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = WarmGray),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重拍")
                }
                Button(
                    onClick = { firstSuggestion?.let { onRecipeClick(it.id) } },
                    enabled = firstSuggestion != null,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("查看食谱")
                }
        }
    }
}

@Composable
private fun IngredientChip(ingredient: DetectedIngredient) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Surface),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = ingredient.imageRes),
                contentDescription = ingredient.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(CircleShape)
            )
        }
        Text(
            text = ingredient.name,
            fontSize = 13.sp,
            color = WarmBlack,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = ingredient.weight,
            fontSize = 12.sp,
            color = WarmGray
        )
    }
}

@Composable
private fun ScanErrorView(onClose: () -> Unit, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(CreamWhite), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.x),
                contentDescription = null,
                colorFilter = ColorFilter.tint(WarmGray),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "未能识别食材",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "请检查网络后重新拍摄或选择图片",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(modifier = Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = Surface, contentColor = WarmGray)) {
                    Text("返回")
                }
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("重新选择")
                }
            }
        }
    }
}

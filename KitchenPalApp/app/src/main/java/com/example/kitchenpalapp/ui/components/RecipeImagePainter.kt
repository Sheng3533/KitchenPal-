package com.example.kitchenpalapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.kitchenpalapp.R

/**
 * 菜谱图片统一取图：
 * 1) 优先使用本地打包的 drawable（离线可用、必定显示，避免远程图床失效导致空白）；
 * 2) 本地没有对应图（未知菜名）且有远程 http URL 时再用 Coil 异步加载；
 * 3) 远程图未就绪/加载失败（如新菜配图尚未生成、网络异常）时，回退默认图，绝不白屏。
 */
@Composable
fun rememberRecipePainter(imageRes: Int?, imageUrl: String?): Painter = when {
    // 优先使用本地打包的 drawable：离线可用、必定显示，避免远程图床/AI 生成服务返回占位图
    imageRes != null && imageRes != 0 ->
        painterResource(id = imageRes)
    !imageUrl.isNullOrBlank() && imageUrl.startsWith("http") ->
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .placeholder(R.drawable.home_recipe_card)
                .error(R.drawable.home_recipe_card)
                .fallback(R.drawable.home_recipe_card)
                .build()
        )
    else -> painterResource(id = R.drawable.home_recipe_card)
}

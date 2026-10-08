package com.example.kitchenpalapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * 主题感知的「柔和着色底面」。
 *
 * 设计稿里很多地方用粉彩色（如分类图标的 0xFFFFF0F0）做浅底。
 * 这类颜色在深色模式下如果原样使用，会变成刺眼的亮色块。
 * 这里统一处理：浅色模式返回原色，深色模式返回低透明度的同色叠色，
 * 叠在深色页面底色上就变成柔和的同色系深色底。
 *
 * @param light 浅色模式下使用的原色
 * @param alphaInDark 深色模式下的不透明度，默认 0.20f
 */
@Composable
fun softTint(light: Color, alphaInDark: Float = 0.20f): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) light.copy(alpha = alphaInDark) else light
}

/**
 * 图标/胶囊的彩色底面。
 *
 * 浅色模式下做「加深但不发暗」处理：把粉彩色的饱和度按 [SATURATION_BOOST] 倍提升，
 * 同时**保持亮度 V 不变**——主色通道仍为 255，只压低其余通道，因此底色更饱满、
 * 色相更清晰，但整块不会变暗发灰（区别于"往黑/灰拉"的老做法）。
 * 深色模式下用稍高透明度叠色，保证在深色背景上可见。
 */
private const val SATURATION_BOOST = 2.4f

@Composable
fun iconBackground(light: Color): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) {
        light.copy(alpha = 0.30f)
    } else {
        boostSaturation(light, SATURATION_BOOST)
    }
}

/**
 * 提升颜色的饱和度并保持亮度（HSV 中的 V 不变），用于把近乎发白的粉彩底
 * 变成同色相、更饱满的粉彩底。灰阶（无彩度）颜色原样返回。
 */
fun boostSaturation(color: Color, factor: Float): Color {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    // 无彩度（灰阶）或全黑：无饱和度可提，直接返回
    if (max <= 0f || delta <= 0f) return color

    val v = max                                   // 亮度保持不变 → 不发暗
    val s = ((delta / max) * factor).coerceIn(0f, 1f)

    var h = when (max) {
        r -> 60f * (((g - b) / delta) % 6f)
        g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }
    if (h < 0f) h += 360f

    val c = v * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = v - c
    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f)
    )
}

/** 当前主题是否为深色（按背景亮度判断，跟随应用内深色模式设置而非系统设置）。 */
@Composable
fun isAppInDarkTheme(): Boolean =
    MaterialTheme.colorScheme.background.luminance() < 0.5f

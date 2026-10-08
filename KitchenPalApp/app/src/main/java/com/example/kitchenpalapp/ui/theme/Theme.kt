package com.example.kitchenpalapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * 注意：这里必须写「颜色字面量」。
 * Color.kt 里的中性色（Background / Surface / TextPrimary ...）已改成
 * @Composable 读取器，而 ColorScheme 是文件级常量、不在 Composable 上下文中，
 * 直接引用会编译失败。
 */

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE8743B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF5C49E),
    onPrimaryContainer = Color(0xFF9A4A20),
    inversePrimary = Color(0xFFF09B6E),

    secondary = Color(0xFF8B5E3C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5E3D3),
    onSecondaryContainer = Color(0xFF4A3628),

    tertiary = Color(0xFFF09B6E),
    onTertiary = Color(0xFF3D2B1F),

    background = Color(0xFFFEFAF5),
    onBackground = Color(0xFF3D2B1F),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF3D2B1F),
    surfaceVariant = Color(0xFFF5EDE4),
    onSurfaceVariant = Color(0xFF6B5744),
    surfaceTint = Color(0xFFE8743B),

    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFF2EAE0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFCF8),
    surfaceContainer = Color(0xFFFDF7EF),
    surfaceContainerHigh = Color(0xFFFBF4EB),
    surfaceContainerHighest = Color(0xFFFFFCF8),

    inverseSurface = Color(0xFF3D2B1F),
    inverseOnSurface = Color(0xFFFEFAF5),

    outline = Color(0xFFE8DDD0),
    outlineVariant = Color(0xFFF0E8DE),

    error = Color(0xFFD94B4B),
    onError = Color(0xFFFFFFFF),
    scrim = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF09B6E),
    onPrimary = Color(0xFF3A2013),
    primaryContainer = Color(0xFF8A4622),
    onPrimaryContainer = Color(0xFFFBDCC6),
    inversePrimary = Color(0xFFE8743B),

    secondary = Color(0xFFD4A87A),
    onSecondary = Color(0xFF3A2013),
    secondaryContainer = Color(0xFF5A4636),
    onSecondaryContainer = Color(0xFFF5E3D3),

    tertiary = Color(0xFFE8A33D),
    onTertiary = Color(0xFF3A2013),

    background = Color(0xFF241A14),
    onBackground = Color(0xFFF3EAE1),

    surface = Color(0xFF2E221B),
    onSurface = Color(0xFFF3EAE1),
    surfaceVariant = Color(0xFF3E2E24),
    onSurfaceVariant = Color(0xFFC8B6A6),
    surfaceTint = Color(0xFFF09B6E),

    surfaceBright = Color(0xFF54402F),
    surfaceDim = Color(0xFF1F1610),
    surfaceContainerLowest = Color(0xFF2A1F18),
    surfaceContainerLow = Color(0xFF33261E),
    surfaceContainer = Color(0xFF3E2E24),
    surfaceContainerHigh = Color(0xFF443227),
    surfaceContainerHighest = Color(0xFF4A382C),

    inverseSurface = Color(0xFFF3EAE1),
    inverseOnSurface = Color(0xFF2E221B),

    outline = Color(0xFF6E5A48),
    outlineVariant = Color(0xFF4E3D30),

    error = Color(0xFFE57373),
    onError = Color(0xFF3A1010),
    scrim = Color(0xFF000000)
)

@Composable
fun KitchenPalAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

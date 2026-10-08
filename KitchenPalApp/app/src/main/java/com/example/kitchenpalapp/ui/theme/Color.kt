package com.example.kitchenpalapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/* ==================================================================
 * 品牌色：冷暖统一，深浅色模式下保持一致（橙色是产品识别色）
 * ================================================================== */

val Primary = Color(0xFFE8743B)
val PrimaryLight = Color(0xFFF09B6E)
val PrimaryLighter = Color(0xFFF5C49E)
val PrimaryLightest = Color(0xFFFBE8D6)
val PrimaryDark = Color(0xFFC45E2A)
val PrimaryDarker = Color(0xFF9A4A20)

val Secondary = Color(0xFF8B5E3C)
val SecondaryLight = Color(0xFFB07D55)
val SecondaryLighter = Color(0xFFD4A87A)

/* 状态色：语义固定，不随主题变化 */
val StateSuccess = Color(0xFF4A9D5B)
val StateSuccessBg = Color(0xFFE8F5EA)
val StateWarning = Color(0xFFE8A33D)
val StateWarningBg = Color(0xFFFFF4E0)
val StateError = Color(0xFFD94B4B)
val StateErrorBg = Color(0xFFFDE8E8)
val StateInfo = Color(0xFF5B8EC9)
val StateInfoBg = Color(0xFFE8F0FA)

/* ==================================================================
 * 中性色（背景 / 卡片 / 文字 / 描边）
 *
 * 这些颜色必须随深浅色模式变化，因此做成 @Composable 读取器，
 * 直接代理 MaterialTheme.colorScheme。好处是：
 *   界面上所有 `color = TextPrimary`、`.background(Surface)` 的调用点
 *   无需任何改动，就能自动跟随深浅色模式切换。
 *
 * ⚠️ 只能在 @Composable 上下文（含 Compose 的默认参数）中使用；
 *    不要在文件级 val、remember {}、onClick {} 等非 Composable 处引用。
 *    Theme.kt 里构建 ColorScheme 时必须写颜色字面量，不能用这些读取器。
 * ================================================================== */

/** 页面底色 */
val Background: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

/** 页面底色（暖调，等同于 Background） */
val BackgroundWarm: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

/** 卡片 / 弹层表面色 */
val Surface: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface

/** 抬升表面（比 Surface 略亮/略深一档） */
val SurfaceRaised: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest

/** 内嵌容器 / 次级表面 */
val SurfaceOverlay: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

/** 常规描边 */
val Border: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

/** 浅描边 / 分隔线 */
val BorderLight: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant

/** 主文字 */
val TextPrimary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

/** 次级文字 */
val TextSecondary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

/** 弱化文字（辅助说明 / 占位） */
val TextTertiary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

/** 反色文字：始终为白，用于深色底/图片上的文字 */
val TextInverse = Color(0xFFFFFFFF)

/** 主色按钮上的文字：始终为白 */
val TextOnPrimary = Color(0xFFFFFFFF)

/* ==================================================================
 * 兼容别名（历史命名，保持既有调用点不变）
 * ================================================================== */

val WarmOrange = Primary
val WarmOrangeLight = PrimaryLight
val WarmOrangeLighter = PrimaryLighter
val WarmBrown = Secondary
val WarmBrownDark = SecondaryLight

val CreamWhite: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

val WarmBlack: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onBackground

val WarmGray: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

val WarmGrayLight: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

val WarmGrayLighter: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant

val Success = StateSuccess
val Info = StateInfo
val Warning = StateWarning
val Error = StateError

val OnPrimary = TextOnPrimary
val OnSecondary = TextOnPrimary

val OnBackground: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onBackground

val OnSurface: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

val Outline: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

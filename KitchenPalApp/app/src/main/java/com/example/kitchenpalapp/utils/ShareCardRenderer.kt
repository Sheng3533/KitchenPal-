package com.example.kitchenpalapp.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface

/**
 * 「烹饪完成」分享卡片生成器。
 *
 * 用纯 Canvas 绘制一张可直接保存/分享的图片（不依赖截图、不需要存储之外的任何权限）。
 * 尺寸 1080x1350，长菜名会自动缩放字号以适配卡片宽度。
 */
object ShareCardRenderer {

    private const val W = 1080
    private const val H = 1350

    private const val COLOR_TEXT = "#2B2118"
    private const val COLOR_BRAND = "#E8743B"
    private const val COLOR_SUB = "#8C7B6B"

    /**
     * @param recipeTitle 菜名
     * @param rating      用户本次给的星级（0-5）
     * @param metrics     底部三栏数据，元素为「值 to 标签」，全部由调用方传入真实数据
     */
    fun render(
        recipeTitle: String,
        rating: Int,
        metrics: List<Pair<String, String>>
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawBackground(canvas)
        drawCard(canvas)
        drawBadge(canvas)
        drawTexts(canvas, recipeTitle, rating, metrics)

        return bitmap
    }

    private fun drawBackground(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, W.toFloat(), H.toFloat(),
                Color.parseColor("#FFF7F1"),
                Color.parseColor("#FFE3CE"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), paint)
    }

    private fun drawCard(canvas: Canvas) {
        val rect = RectF(70f, 150f, (W - 70).toFloat(), (H - 150).toFloat())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(rect, 56f, 56f, paint)
    }

    private fun drawBadge(canvas: Canvas) {
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFEDE1")
        }
        canvas.drawCircle(W / 2f, 330f, 96f, ring)

        val disc = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(COLOR_BRAND)
        }
        canvas.drawCircle(W / 2f, 330f, 72f, disc)

        val check = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 14f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val path = android.graphics.Path().apply {
            moveTo(W / 2f - 30f, 332f)
            lineTo(W / 2f - 8f, 356f)
            lineTo(W / 2f + 34f, 304f)
        }
        canvas.drawPath(path, check)
    }

    private fun drawTexts(
        canvas: Canvas,
        recipeTitle: String,
        rating: Int,
        metrics: List<Pair<String, String>>
    ) {
        val center = W / 2f

        // 小标题
        val header = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(COLOR_SUB)
            textSize = 40f
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.12f
        }
        canvas.drawText("烹 饪 完 成", center, 500f, header)

        // 菜名（自适应宽度）
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(COLOR_TEXT)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = 88f
        }
        fitText(namePaint, recipeTitle, (W - 260).toFloat())
        canvas.drawText(recipeTitle, center, 640f, namePaint)

        // 星级
        val safeRating = rating.coerceIn(0, 5)
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F5A623")
            textSize = 72f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("★".repeat(safeRating) + "☆".repeat(5 - safeRating), center, 760f, starPaint)

        // 分割线
        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F0E6DC")
            strokeWidth = 2f
        }
        canvas.drawLine(180f, 836f, (W - 180).toFloat(), 836f, divider)

        // 三栏真实数据（按传入条数均匀分布）
        val shown = metrics.take(3)
        when (shown.size) {
            1 -> drawMetric(canvas, center, shown[0].second, shown[0].first)
            2 -> {
                drawMetric(canvas, center - 150f, shown[0].second, shown[0].first)
                drawMetric(canvas, center + 150f, shown[1].second, shown[1].first)
            }
            else -> shown.forEachIndexed { index, (value, label) ->
                drawMetric(canvas, center + (index - 1) * 270f, label, value)
            }
        }

        // 品牌落款
        val brand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(COLOR_BRAND)
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("KitchenPal · 厨房助手", center, (H - 240).toFloat(), brand)

        val hint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(COLOR_SUB)
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("记录每一次下厨的成就感", center, (H - 186).toFloat(), hint)
    }

    private fun drawMetric(canvas: Canvas, cx: Float, label: String, value: String) {
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(COLOR_TEXT)
            textSize = 56f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(COLOR_SUB)
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(value, cx, 940f, valuePaint)
        canvas.drawText(label, cx, 1000f, labelPaint)
    }

    /** 逐步缩小字号直到文本能放进 maxWidth，避免长菜名被裁掉。 */
    private fun fitText(paint: Paint, text: String, maxWidth: Float) {
        var size = paint.textSize
        while (size > 36f && paint.measureText(text) > maxWidth) {
            size -= 4f
            paint.textSize = size
        }
    }
}

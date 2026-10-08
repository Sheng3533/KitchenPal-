package com.example.kitchenpalapp.data

/**
 * 每日美食图片信息，对应后端 DailyImageService.DailyImageInfo。
 * url 为相对路径（如 /daily/2026-09-26.png），使用前需拼接 BASE_URL。
 * recipeId 为当天推荐的真实菜谱 id，点击「今日推荐」应跳转该菜谱详情。
 */
data class DailyImageData(
    val date: String? = null,
    val url: String? = null,
    val prompt: String? = null,
    val title: String? = null,
    val recipeId: Long? = null
)

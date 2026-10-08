package com.example.kitchenpalapp.data

data class Recipe(
    val id: String,
    val name: String = "",
    val title: String,
    val subtitle: String,
    val image: String = "",
    val imageRes: Int,
    val rating: Float,
    val cookingTime: Int = 0,
    val cookTimeMin: Int,
    val calories: Int,
    val difficulty: String,
    val category: String,
    val description: String = "",
    val author: String = "",
    val tags: List<String> = emptyList(),
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<CookStep> = emptyList(),
    val isFavorite: Boolean = false,
    val matchPercent: Int = 0
)

data class Ingredient(
    val name: String,
    val amount: String,
    val iconName: String = "leaf"
)

data class CookStep(
    val description: String = "",
    /** 火候，如「中火」「大火转小火」 */
    val heat: String? = null,
    /** 本步建议时长（秒），0 表示未提供 */
    val durationSeconds: Int = 0,
    /** 步骤标题，如「牛腩预处理」 */
    val title: String = "",
    /** 关键技巧 / 易错点 */
    val tip: String? = null,
    /** 本步用到的食材 */
    val ingredients: List<String> = emptyList()
)

/** 购物清单条目 */
data class ShoppingItem(
    val name: String,
    val amount: String = "",
    val checked: Boolean = false,
    val addedAt: Long = 0L
)

data class DetectedIngredient(
    val name: String,
    val confidence: Int,
    val weight: String,
    val imageRes: Int
)

data class CookingHistoryEntry(
    val recipeId: String,
    val recipeName: String,
    val date: String,
    val dayLabel: String,
    val time: String,
    val duration: String,
    val heatAdjustments: Int = 0,
    val score: Int,
    val stars: Int,
    val imageRes: Int?,
    val imageUrl: String? = null
)

data class NotificationItem(
    val id: String,
    val icon: String,
    val title: String,
    val description: String,
    val time: String,
    val isRead: Boolean,
    val type: Int = 1,
    val relatedId: Long = 0L
)

data class IngredientInfo(
    val name: String,
    val aliases: String,
    val season: String,
    val imageRes: Int,
    val calories: String,
    val water: String,
    val vitaminC: String,
    val nutrition: List<NutritionItem>,
    val storage: List<StorageMethod>,
    val goodPairings: List<String>,
    val badPairings: List<String>,
    val relatedRecipes: List<Recipe>
)

/**
 * 食材详情页的数据载体。
 * 与 [IngredientInfo] 的区别：这里的每个字段都由真实菜谱库统计得出，不含写死的示例内容。
 */
data class IngredientDetailData(
    val name: String,
    /** 用到该食材的菜谱数量 */
    val recipeCount: Int,
    /** 用到该食材最多的菜系/分类 */
    val topCategory: String,
    /** 封面图：取第一道用到它的菜谱图，没有则为 null */
    val coverImageUrl: String?,
    /** 相关菜谱的平均热量（千卡） */
    val avgCalories: Int,
    /** 常与它一起出现的其它食材，按共现次数降序 */
    val pairings: List<String>,
    /** 相关菜谱 */
    val recipes: List<Recipe>
)

data class NutritionItem(
    val name: String,
    val percent: Int,
    val color: Long
)

data class StorageMethod(
    val method: String,
    val duration: String,
    val icon: String,
    val bgColor: Long
)

data class DietReportData(
    val score: Int,
    val scoreDelta: Int,
    val nutrition: List<NutritionIntake>,
    val cookingStats: List<CookingStat>,
    val categoryDistribution: List<CategoryDist>,
    val suggestions: List<SuggestionItem>
)

data class NutritionIntake(
    val name: String,
    val current: String,
    val target: String,
    val percent: Int,
    val icon: String,
    val achieved: Boolean = false
)

data class CookingStat(
    val value: String,
    val unit: String,
    val label: String,
    val icon: String,
    val color: Long
)

data class CategoryDist(
    val name: String,
    val percent: Int,
    val color: Long
)

data class SuggestionItem(
    val title: String,
    val description: String
)

data class FaqItem(
    val question: String,
    val answer: String
)

data class QuickAction(
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Long,
    val bgColor: Long
)

data class SearchChannel(
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val gradientStart: Long,
    val gradientEnd: Long
)

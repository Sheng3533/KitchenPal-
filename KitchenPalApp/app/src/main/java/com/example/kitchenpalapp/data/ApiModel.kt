package com.example.kitchenpalapp.data

data class UserData(
    val id: Long = 0,
    val phone: String = "",
    val password: String = "",
    val nickname: String = "",
    val avatar: String? = null,
    val status: Int = 1,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val tastePreferences: String? = null,
    val cookingCount: Int = 0,
    val favoriteCount: Int = 0,
    val streakDays: Int = 0,
    val darkMode: Boolean = false,
    val cookingNotify: Boolean = true
)

data class LoginData(
    val token: String = "",
    val user: UserData = UserData()
)

data class RecipeData(
    val id: Long = 0,
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val rating: Float = 4.5f,
    val cookTimeMin: Int = 30,
    val calories: Int = 300,
    val difficulty: String = "简单",
    val category: String = "家常菜",
    val tags: String? = null,
    val matchPercent: Int? = null,
    val ingredientsJson: String? = null,
    val stepsJson: String? = null,
    val isHero: Boolean = false,
    val isUserCreated: Boolean = false
)

data class IngredientData(
    val name: String = "",
    val amount: String = "",
    val iconName: String = ""
)

data class CookStepData(
    val stepNumber: Int = 0,
    val title: String = "",              // 步骤标题
    val description: String = "",        // 详细说明
    val heat: String? = null,            // 火候
    val heatTip: String? = null,         // 兼容后端旧字段（火候）
    val durationSeconds: Int = 0,        // 本步时长（秒）
    val tip: String? = null,             // 关键技巧
    val ingredients: List<String>? = null // 本步食材
)

data class NotificationData(
    val id: Long = 0,
    val userId: Long = 0,
    val type: String = "",
    val title: String = "",
    val content: String = "",
    val icon: String? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null,
    val relatedId: Long? = null
)

data class CookingHistoryData(
    val id: Long = 0,
    val userId: Long = 0,
    val recipeId: Long = 0,
    val recipeTitle: String = "",
    val recipeImageUrl: String = "",
    val cookDate: String = "",
    val cookTime: String = "",
    val durationMin: Int = 0,
    val score: Int = 0,
    val rating: Int = 0,
    /** 后端返回的真实时间戳（ISO-8601，如 2026-09-26T12:30:00），用于按真实日期分组/筛选。 */
    val createdAt: String? = null
)

data class ScanResultData(
    val ingredients: List<DetectedIngredientData> = emptyList(),
    val suggestedRecipeIds: List<Long> = emptyList()
)

data class DetectedIngredientData(
    val name: String = "",
    val confidence: Int = 0,
    val weight: String = "",
    val imageUrl: String = ""
)

data class ChefData(
    val id: Long = 0,
    val name: String = "",
    val title: String = "",
    val specialty: String = "",
    val description: String = "",
    val avatar: String = "",
    val style: String = "",
    val experienceYears: Int = 0,
    val signatureDish: String = "",
    val status: Int = 1
)

data class ChatMessageData(
    val id: Long = 0,
    val chefId: Long = 0,
    val chefName: String = "",
    val message: String = "",
    val reply: String = "",
    val timestamp: String = ""
)

data class AssistantChatRequest(
    val message: String,
    val history: List<Map<String, String>> = emptyList(),
    val userContext: Map<String, Any> = emptyMap()
)

data class GenerateRecipeRequest(
    val ingredients: String,
    val style: String = "",
    val dietary: String = ""
)

data class AnalyzeIngredientsRequest(val ingredients: String)

data class CookingTipsRequest(val dishName: String)

data class AssistantMessage(
    val id: String = "",
    val role: AssistantRole = AssistantRole.ASSISTANT,
    val type: AssistantMessageType = AssistantMessageType.TEXT,
    val content: String = "",
    val recipe: GeneratedRecipe? = null,
    val analysis: IngredientAnalysis? = null
)

enum class AssistantRole {
    USER, ASSISTANT
}

enum class AssistantMessageType {
    TEXT, RECIPE, ANALYSIS
}

data class GeneratedRecipe(
    val name: String = "",
    val description: String = "",
    val prepTime: String = "",
    val cookTime: String = "",
    val servings: String = "",
    val difficulty: String = "",
    val calories: String = "",
    val ingredients: List<GeneratedIngredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val tips: String = ""
)

data class GeneratedIngredient(
    val name: String = "",
    val quantity: String = ""
)

data class IngredientAnalysis(
    val nutrition: String = "",
    val flavor: String = "",
    val pairing: String = "",
    val storage: String = ""
)

// ===== 评论 / 评分 =====

data class CommentData(
    val id: Long = 0,
    val recipeId: Long = 0,
    val userId: Long = 0,
    val userName: String = "",
    val userAvatar: String? = null,
    val content: String = "",
    val imageUrl: String? = null,
    val likes: Int = 0,
    val createdAt: String? = null
)

data class AddCommentRequest(
    val recipeId: Long,
    val content: String,
    val imageUrl: String? = null
)

data class RecipeRatingData(
    val id: Long = 0,
    val recipeId: Long = 0,
    val userId: Long = 0,
    val userName: String = "",
    val rating: Int = 0,
    val comment: String? = null,
    val createdAt: String? = null
)

data class RateRecipeRequest(
    val rating: Int,
    val comment: String? = null
)

// ===== 冰箱食材 =====

data class FridgeItemData(
    val id: Long = 0,
    val userId: Long = 0,
    val name: String = "",
    val category: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val expireDate: String? = null,
    val addedAt: String? = null
)

// ===== 买菜清单（云端） =====

data class CloudShoppingItem(
    val id: Long = 0,
    val name: String = "",
    val quantity: String? = null,
    val unit: String? = null,
    val category: String? = null,
    val checked: Boolean = false
)

package com.example.kitchenpalapp.data

import com.example.kitchenpalapp.network.ApiResponse
import com.example.kitchenpalapp.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull

object KitchenPalRepository {

    private val gson = Gson()

    suspend fun login(phone: String, password: String): ApiResponse<Map<String, Any>> {
        return safeCall { RetrofitClient.api.login(mapOf("phone" to phone, "password" to password)) }
    }

    suspend fun register(phone: String, password: String, nickname: String? = null): ApiResponse<UserData> {
        return safeCall { RetrofitClient.api.register(mapOf("phone" to phone, "password" to password, "nickname" to nickname)) }
    }

    suspend fun getUser(id: Long): ApiResponse<UserData> {
        return safeCall { RetrofitClient.api.getUser(id) }
    }

    suspend fun updateUser(id: Long, user: UserData): ApiResponse<UserData> {
        return safeCall { RetrofitClient.api.updateUser(id, user) }
    }

    suspend fun getRecipeList(category: String? = null, keyword: String? = null): ApiResponse<List<RecipeData>> {
        return safeCall { RetrofitClient.api.getRecipeList(category, keyword) }
    }

    suspend fun getRecipeDetail(id: Long): ApiResponse<RecipeData> {
        return safeCall { RetrofitClient.api.getRecipeDetail(id) }
    }

    suspend fun getHeroRecipes(): ApiResponse<List<RecipeData>> {
        return safeCall { RetrofitClient.api.getHeroRecipes() }
    }

    /** 发布用户自制菜谱（UGC） */
    suspend fun createRecipe(recipe: RecipeData): Result<RecipeData> = runCatching {
        val resp = RetrofitClient.api.createRecipe(recipe.copy(isUserCreated = true))
        if (resp.code == 200 && resp.data != null) resp.data else throw RuntimeException(resp.message)
    }

    suspend fun getCategories(): ApiResponse<List<String>> {
        return safeCall { RetrofitClient.api.getCategories() }
    }

    suspend fun getDailyImage(): ApiResponse<DailyImageData> {
        return safeCall { RetrofitClient.api.getDailyImage() }
    }

    suspend fun getFavoriteRecipes(userId: Long): ApiResponse<List<RecipeData>> {
        return safeCall { RetrofitClient.api.getFavoriteRecipes(userId) }
    }

    suspend fun getFavoriteIds(userId: Long): ApiResponse<Set<Long>> {
        return safeCall { RetrofitClient.api.getFavoriteIds(userId) }
    }

    suspend fun toggleFavorite(userId: Long, recipeId: Long): ApiResponse<String> {
        return safeCall { RetrofitClient.api.toggleFavorite(mapOf("userId" to userId, "recipeId" to recipeId)) }
    }

    suspend fun getHistory(userId: Long): ApiResponse<List<CookingHistoryData>> {
        return safeCall { RetrofitClient.api.getHistory(userId) }
    }

    suspend fun addHistory(history: CookingHistoryData): ApiResponse<CookingHistoryData> {
        return safeCall { RetrofitClient.api.addHistory(history) }
    }

    suspend fun getNotifications(userId: Long): ApiResponse<List<NotificationData>> {
        return safeCall { RetrofitClient.api.getNotifications(userId) }
    }

    suspend fun markAsRead(id: Long): ApiResponse<String> {
        return safeCall { RetrofitClient.api.markAsRead(id) }
    }

    suspend fun markAllAsRead(userId: Long): ApiResponse<String> {
        return safeCall { RetrofitClient.api.markAllAsRead(mapOf("userId" to userId)) }
    }

    suspend fun recognizeImage(imageBytes: ByteArray): ApiResponse<ScanResultData> {
        return safeCall {
            val requestBody = okhttp3.RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
            val part = okhttp3.MultipartBody.Part.createFormData("image", "photo.jpg", requestBody)
            RetrofitClient.api.recognize(part)
        }
    }

    suspend fun getChefList(): ApiResponse<List<ChefData>> {
        return safeCall { RetrofitClient.api.getChefList() }
    }

    suspend fun getChefDetail(id: Long): ApiResponse<ChefData> {
        return safeCall { RetrofitClient.api.getChefDetail(id) }
    }

    suspend fun chatWithChef(chefId: Long, message: String): ApiResponse<Map<String, String>> {
        return safeCall { RetrofitClient.api.chatWithChef(chefId, mapOf("message" to message)) }
    }

    suspend fun getAgreement(type: String): ApiResponse<Map<String, Any>> {
        return safeCall { RetrofitClient.api.getAgreement(type) }
    }

    suspend fun submitFeedback(title: String, content: String, contact: String, type: String): ApiResponse<Boolean> {
        return safeCall {
            RetrofitClient.api.submitFeedback(
                mapOf(
                    "title" to title,
                    "content" to content,
                    "contact" to contact,
                    "type" to type
                )
            )
        }
    }

    suspend fun checkVersion(): ApiResponse<Map<String, Any>> {
        return safeCall { RetrofitClient.api.checkVersion() }
    }

    suspend fun updatePreferences(userId: Long, preferences: Map<String, String>): ApiResponse<Boolean> {
        return safeCall { RetrofitClient.api.updatePreferences(userId, preferences) }
    }

    suspend fun updateSettings(userId: Long, settings: Map<String, Boolean>): ApiResponse<Boolean> {
        return safeCall { RetrofitClient.api.updateSettings(userId, settings) }
    }

    suspend fun getUserStats(userId: Long): ApiResponse<Map<String, Any>> {
        return safeCall { RetrofitClient.api.getUserStats(userId) }
    }

    suspend fun assistantChat(
        message: String,
        history: List<Map<String, String>> = emptyList(),
        userContext: Map<String, Any> = emptyMap()
    ): ApiResponse<Map<String, Any>> {
        val body = AssistantChatRequest(message, history, userContext)
        return safeCall { RetrofitClient.api.assistantChat(body) }
    }

    suspend fun generateRecipe(ingredients: String, style: String = "", dietary: String = ""): ApiResponse<Map<String, Any>> {
        return safeCall {
            RetrofitClient.api.generateRecipe(GenerateRecipeRequest(ingredients, style, dietary))
        }
    }

    suspend fun analyzeIngredients(ingredients: String): ApiResponse<Map<String, Any>> {
        return safeCall { RetrofitClient.api.analyzeIngredients(AnalyzeIngredientsRequest(ingredients)) }
    }

    suspend fun cookingTips(dishName: String): ApiResponse<Map<String, Any>> {
        return safeCall { RetrofitClient.api.cookingTips(CookingTipsRequest(dishName)) }
    }

    // ===== 评论 / 评分（返回 Result 便于 UI 直接判成败） =====

    suspend fun getComments(recipeId: Long): Result<List<CommentData>> = runCatching {
        val resp = RetrofitClient.api.getComments(recipeId)
        if (resp.code == 200) resp.data ?: emptyList() else throw RuntimeException(resp.message)
    }

    suspend fun addComment(recipeId: Long, content: String, imageUrl: String? = null): Result<CommentData> = runCatching {
        val resp = RetrofitClient.api.addComment(AddCommentRequest(recipeId, content, imageUrl))
        if (resp.code == 200 && resp.data != null) resp.data else throw RuntimeException(resp.message)
    }

    suspend fun deleteComment(id: Long): Result<Unit> = runCatching {
        val resp = RetrofitClient.api.deleteComment(id)
        if (resp.code == 200) Unit else throw RuntimeException(resp.message)
    }

    suspend fun rateRecipe(recipeId: Long, rating: Int, comment: String? = null): Result<Map<String, Any>> = runCatching {
        val resp = RetrofitClient.api.rateRecipe(recipeId, RateRecipeRequest(rating, comment))
        if (resp.code == 200) resp.data ?: emptyMap() else throw RuntimeException(resp.message)
    }

    suspend fun getRatings(recipeId: Long): Result<List<RecipeRatingData>> = runCatching {
        val resp = RetrofitClient.api.getRatings(recipeId)
        if (resp.code == 200) resp.data ?: emptyList() else throw RuntimeException(resp.message)
    }

    // ===== 冰箱食材 =====

    suspend fun getFridgeItems(userId: Long): Result<List<FridgeItemData>> = runCatching {
        val resp = RetrofitClient.api.getFridgeItems(userId)
        if (resp.code == 200) resp.data ?: emptyList() else throw RuntimeException(resp.message)
    }

    suspend fun getExpiringItems(userId: Long, days: Int = 3): Result<List<FridgeItemData>> = runCatching {
        val resp = RetrofitClient.api.getExpiringItems(userId, days)
        if (resp.code == 200) resp.data ?: emptyList() else throw RuntimeException(resp.message)
    }

    suspend fun addFridgeItem(item: FridgeItemData): Result<FridgeItemData> = runCatching {
        val resp = RetrofitClient.api.addFridgeItem(item)
        if (resp.code == 200 && resp.data != null) resp.data else throw RuntimeException(resp.message)
    }

    suspend fun deleteFridgeItem(id: Long): Result<Unit> = runCatching {
        val resp = RetrofitClient.api.deleteFridgeItem(id)
        if (resp.code == 200) Unit else throw RuntimeException(resp.message)
    }

    // ===== 买菜清单上云 =====

    suspend fun getShoppingItems(userId: Long): Result<List<CloudShoppingItem>> = runCatching {
        val resp = RetrofitClient.api.getShoppingItems(userId)
        if (resp.code == 200) resp.data ?: emptyList() else throw RuntimeException(resp.message)
    }

    suspend fun syncShopping(userId: Long, items: List<CloudShoppingItem>): Result<List<CloudShoppingItem>> = runCatching {
        val resp = RetrofitClient.api.syncShoppingItems(userId, items)
        if (resp.code == 200) resp.data ?: emptyList() else throw RuntimeException(resp.message)
    }

    // ===== 图片上传 =====

    /** 上传图片，返回服务端相对 URL（如 /uploads/cooking/xxx.jpg） */
    suspend fun uploadImage(bytes: ByteArray, type: String = "cooking"): Result<String> = runCatching {
        val body = okhttp3.RequestBody.create("image/jpeg".toMediaTypeOrNull(), bytes)
        val part = okhttp3.MultipartBody.Part.createFormData("file", "upload.jpg", body)
        val typeBody = okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), type)
        val resp = RetrofitClient.api.uploadImage(part, typeBody)
        if (resp.code == 200 && !resp.data.isNullOrBlank()) resp.data else throw RuntimeException(resp.message)
    }

    fun parseIngredients(json: String?): List<IngredientData> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val type = object : TypeToken<List<IngredientData>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseSteps(json: String?): List<CookStepData> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val type = object : TypeToken<List<CookStepData>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseTags(tags: String?): List<String> {
        if (tags.isNullOrEmpty()) return emptyList()
        return tags.split(",").map { it.trim() }
    }

    fun parseTastePreferences(prefs: String?): List<String> {
        if (prefs.isNullOrEmpty()) return emptyList()
        return prefs.split(",").map { it.trim() }
    }

    private suspend fun <T> safeCall(call: suspend () -> ApiResponse<T>): ApiResponse<T> {
        return try {
            call()
        } catch (e: Exception) {
            ApiResponse(500, e.message ?: "网络请求失败", null)
        }
    }
}

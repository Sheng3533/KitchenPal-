package com.example.kitchenpalapp.network

import com.example.kitchenpalapp.data.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {

    @POST("api/user/register")
    suspend fun register(
        @Body body: Map<String, String?>
    ): ApiResponse<UserData>

    @POST("api/user/login")
    suspend fun login(
        @Body body: Map<String, String>
    ): ApiResponse<Map<String, Any>>

    @GET("api/user/{id}")
    suspend fun getUser(@Path("id") id: Long): ApiResponse<UserData>

    @PUT("api/user/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body user: UserData
    ): ApiResponse<UserData>

    @GET("api/recipe/list")
    suspend fun getRecipeList(
        @Query("category") category: String? = null,
        @Query("keyword") keyword: String? = null
    ): ApiResponse<List<RecipeData>>

    @GET("api/recipe/{id}")
    suspend fun getRecipeDetail(@Path("id") id: Long): ApiResponse<RecipeData>

    @GET("api/recipe/hero")
    suspend fun getHeroRecipes(): ApiResponse<List<RecipeData>>

    @GET("api/recipe/categories")
    suspend fun getCategories(): ApiResponse<List<String>>

    @POST("api/recipe")
    suspend fun createRecipe(@Body body: RecipeData): ApiResponse<RecipeData>

    @GET("api/daily-image")
    suspend fun getDailyImage(): ApiResponse<DailyImageData>

    @GET("api/favorite/list/{userId}")
    suspend fun getFavoriteRecipes(@Path("userId") userId: Long): ApiResponse<List<RecipeData>>

    @GET("api/favorite/ids/{userId}")
    suspend fun getFavoriteIds(@Path("userId") userId: Long): ApiResponse<Set<Long>>

    @GET("api/favorite/check")
    suspend fun checkFavorite(
        @Query("userId") userId: Long,
        @Query("recipeId") recipeId: Long
    ): ApiResponse<Boolean>

    @POST("api/favorite/toggle")
    suspend fun toggleFavorite(
        @Body body: Map<String, Long>
    ): ApiResponse<String>

    @GET("api/history/{userId}")
    suspend fun getHistory(@Path("userId") userId: Long): ApiResponse<List<CookingHistoryData>>

    @POST("api/history/add")
    suspend fun addHistory(@Body history: CookingHistoryData): ApiResponse<CookingHistoryData>

    @GET("api/notification/{userId}")
    suspend fun getNotifications(@Path("userId") userId: Long): ApiResponse<List<NotificationData>>

    @GET("api/notification/unread/{userId}")
    suspend fun getUnreadCount(@Path("userId") userId: Long): ApiResponse<Long>

    @POST("api/notification/read/{id}")
    suspend fun markAsRead(@Path("id") id: Long): ApiResponse<String>

    @POST("api/notification/read-all")
    suspend fun markAllAsRead(@Body body: Map<String, Long>): ApiResponse<String>

    @Multipart
    @POST("api/scan/recognize")
    suspend fun recognize(@Part image: MultipartBody.Part): ApiResponse<ScanResultData>

    @GET("api/chef/list")
    suspend fun getChefList(): ApiResponse<List<ChefData>>

    @GET("api/chef/{id}")
    suspend fun getChefDetail(@Path("id") id: Long): ApiResponse<ChefData>

    @POST("api/chef/{id}/chat")
    suspend fun chatWithChef(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): ApiResponse<Map<String, String>>

    @GET("api/app/agreement")
    suspend fun getAgreement(@Query("type") type: String): ApiResponse<Map<String, Any>>

    @POST("api/feedback/submit")
    suspend fun submitFeedback(@Body body: Map<String, Any>): ApiResponse<Boolean>

    @GET("api/app/version/check")
    suspend fun checkVersion(): ApiResponse<Map<String, Any>>

    @PUT("api/user/{id}/preferences")
    suspend fun updatePreferences(@Path("id") id: Long, @Body body: Map<String, String>): ApiResponse<Boolean>

    @PUT("api/user/{id}/settings")
    suspend fun updateSettings(@Path("id") id: Long, @Body body: Map<String, Boolean>): ApiResponse<Boolean>

    @GET("api/stats/user/{userId}")
    suspend fun getUserStats(@Path("userId") userId: Long): ApiResponse<Map<String, Any>>

    @POST("api/assistant/chat")
    suspend fun assistantChat(@Body body: AssistantChatRequest): ApiResponse<Map<String, Any>>

    @POST("api/assistant/generate-recipe")
    suspend fun generateRecipe(@Body body: GenerateRecipeRequest): ApiResponse<Map<String, Any>>

    @POST("api/assistant/analyze-ingredients")
    suspend fun analyzeIngredients(@Body body: AnalyzeIngredientsRequest): ApiResponse<Map<String, Any>>

    @POST("api/assistant/cooking-tips")
    suspend fun cookingTips(@Body body: CookingTipsRequest): ApiResponse<Map<String, Any>>

    // ===== 评论 =====

    @GET("api/comment/recipe/{recipeId}")
    suspend fun getComments(@Path("recipeId") recipeId: Long): ApiResponse<List<CommentData>>

    @POST("api/comment")
    suspend fun addComment(@Body body: AddCommentRequest): ApiResponse<CommentData>

    @DELETE("api/comment/{id}")
    suspend fun deleteComment(@Path("id") id: Long): ApiResponse<String>

    // ===== 评分 =====

    @POST("api/recipe/{id}/rate")
    suspend fun rateRecipe(@Path("id") recipeId: Long, @Body body: RateRecipeRequest): ApiResponse<Map<String, Any>>

    @GET("api/recipe/{id}/ratings")
    suspend fun getRatings(@Path("id") recipeId: Long): ApiResponse<List<RecipeRatingData>>

    // ===== 冰箱食材 =====

    @GET("api/fridge/{userId}")
    suspend fun getFridgeItems(@Path("userId") userId: Long): ApiResponse<List<FridgeItemData>>

    @GET("api/fridge/{userId}/expiring")
    suspend fun getExpiringItems(
        @Path("userId") userId: Long,
        @Query("days") days: Int = 3
    ): ApiResponse<List<FridgeItemData>>

    @POST("api/fridge")
    suspend fun addFridgeItem(@Body body: FridgeItemData): ApiResponse<FridgeItemData>

    @PUT("api/fridge/{id}")
    suspend fun updateFridgeItem(@Path("id") id: Long, @Body body: FridgeItemData): ApiResponse<FridgeItemData>

    @DELETE("api/fridge/{id}")
    suspend fun deleteFridgeItem(@Path("id") id: Long): ApiResponse<String>

    // ===== 买菜清单（云端同步） =====

    @GET("api/shopping/{userId}")
    suspend fun getShoppingItems(@Path("userId") userId: Long): ApiResponse<List<CloudShoppingItem>>

    @POST("api/shopping/{userId}/sync")
    suspend fun syncShoppingItems(
        @Path("userId") userId: Long,
        @Body items: List<CloudShoppingItem>
    ): ApiResponse<List<CloudShoppingItem>>

    @DELETE("api/shopping/{userId}/clear")
    suspend fun clearShoppingItems(@Path("userId") userId: Long): ApiResponse<String>

    // ===== 图片上传 =====

    @Multipart
    @POST("api/upload/image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("type") type: okhttp3.RequestBody
    ): ApiResponse<String>
}

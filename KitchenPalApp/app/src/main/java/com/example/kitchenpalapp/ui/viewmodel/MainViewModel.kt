package com.example.kitchenpalapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kitchenpalapp.data.*
import com.example.kitchenpalapp.network.ApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val appPreferences = AppPreferences(application)

    // App 启动即装回用户自定义的服务器地址（真机联调换网络时无需重新编译）
    init {
        appPreferences.applySavedServerUrl()
    }

    private val _autoLoginChecked = MutableStateFlow(false)
    val autoLoginChecked: StateFlow<Boolean> = _autoLoginChecked

    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    /** 全量菜谱：任何筛选/搜索都不会覆盖它，供首页推荐、吃什么、收藏、历史等全局场景使用 */
    private val _recipes = MutableStateFlow<List<RecipeData>>(emptyList())
    val recipes: StateFlow<List<RecipeData>> = _recipes

    /** 当前分类/关键词筛选后的结果：仅发现页使用，避免污染全量列表 */
    private val _filteredRecipes = MutableStateFlow<List<RecipeData>>(emptyList())
    val filteredRecipes: StateFlow<List<RecipeData>> = _filteredRecipes

    private val _heroRecipes = MutableStateFlow<List<RecipeData>>(emptyList())
    val heroRecipes: StateFlow<List<RecipeData>> = _heroRecipes

    /** 今日美食图片相对路径（/daily/yyyy-MM-dd.png），由后端每天生成一次 */
    private val _dailyImageUrl = MutableStateFlow<String?>(null)
    val dailyImageUrl: StateFlow<String?> = _dailyImageUrl

    /** 今日美食图片对应的菜名，用于覆盖 Hero 卡片的标题 */
    private val _dailyImageTitle = MutableStateFlow<String?>(null)
    val dailyImageTitle: StateFlow<String?> = _dailyImageTitle

    /** 今日推荐对应的真实菜谱 id，点击「今日推荐」应跳转到该菜谱详情 */
    private val _dailyImageRecipeId = MutableStateFlow<Long?>(null)
    val dailyImageRecipeId: StateFlow<Long?> = _dailyImageRecipeId

    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState

    private val _capturedImage = MutableStateFlow<ByteArray?>(null)
    val capturedImage: StateFlow<ByteArray?> = _capturedImage

    private val _selectedCategory = MutableStateFlow("全部")
    val selectedCategory: StateFlow<String> = _selectedCategory

    /** 发现页排序方式：从本地偏好初始化，切换后持久化，重进 App 仍生效。 */
    private val _discoverSort = MutableStateFlow(appPreferences.getDiscoverSort())
    val discoverSort: StateFlow<String> = _discoverSort

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val _shoppingItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems

    // 冰箱食材（云端）
    private val _fridgeItems = MutableStateFlow<List<FridgeItemData>>(emptyList())
    val fridgeItems: StateFlow<List<FridgeItemData>> = _fridgeItems

    private val _expiringItems = MutableStateFlow<List<FridgeItemData>>(emptyList())
    val expiringItems: StateFlow<List<FridgeItemData>> = _expiringItems

    // 云端同步状态
    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    // 评论 / 评分
    private val _comments = MutableStateFlow<List<CommentData>>(emptyList())
    val comments: StateFlow<List<CommentData>> = _comments

    private val _recipeRatings = MutableStateFlow<List<RecipeRatingData>>(emptyList())
    val recipeRatings: StateFlow<List<RecipeRatingData>> = _recipeRatings

    private val _commentSubmitting = MutableStateFlow(false)
    val commentSubmitting: StateFlow<Boolean> = _commentSubmitting

    // 服务器地址（运行时配置）：初始值直接取已保存的自定义地址，保证设置页首帧就正确回显
    private val _serverUrl = MutableStateFlow(appPreferences.getServerUrl())
    val serverUrl: StateFlow<String> = _serverUrl

    /** 当前实际生效的服务器地址（用于设置页展示，自定义为空时显示编译期默认） */
    val effectiveServerUrl: String get() = com.example.kitchenpalapp.network.BaseUrlHolder.current

    /** 连接测试结果：null=未测，true=通，false=不通 */
    private val _connTestResult = MutableStateFlow<Boolean?>(null)
    val connTestResult: StateFlow<Boolean?> = _connTestResult

    private val _connTesting = MutableStateFlow(false)
    val connTesting: StateFlow<Boolean> = _connTesting

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notifications: StateFlow<List<NotificationData>> = _notifications

    private val _cookingHistory = MutableStateFlow<List<CookingHistoryData>>(emptyList())
    val cookingHistory: StateFlow<List<CookingHistoryData>> = _cookingHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /** 全局 Snackbar 展示后由 UI 调用，避免重复弹出同一条错误 */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /** 深色模式开关：持久化到本地并驱动根主题切换 */
    fun setDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
        appPreferences.setDarkMode(enabled)
    }

    private val _checkedSteps = MutableStateFlow<Map<String, Set<Int>>>(emptyMap())
    val checkedSteps: StateFlow<Map<String, Set<Int>>> = _checkedSteps

    private val _tastePreferences = MutableStateFlow<List<String>>(listOf("清淡", "咸鲜", "素食"))
    val tastePreferences: StateFlow<List<String>> = _tastePreferences

    private val _userStats = MutableStateFlow<Map<String, Any>>(emptyMap())
    val userStats: StateFlow<Map<String, Any>> = _userStats

    private val _appVersionInfo = MutableStateFlow<Map<String, Any>?>(null)
    val appVersionInfo: StateFlow<Map<String, Any>?> = _appVersionInfo

    private val _assistantMessages = MutableStateFlow<List<AssistantMessage>>(
        listOf(
            AssistantMessage(
                id = "welcome",
                role = AssistantRole.ASSISTANT,
                type = AssistantMessageType.TEXT,
                content = "你好！我是小厨，你的厨房管家。\n\n我能根据你手边的食材、口味和时间，帮你：\n• 决定做什么菜（直接说\"我家有 XX\"）\n• 给出具体步骤、火候、用量\n• 分析营养与搭配\n• 解答新手难题\n\n把你的情况告诉我吧～"
            )
        )
    )
    val assistantMessages: StateFlow<List<AssistantMessage>> = _assistantMessages

    private val _assistantLoading = MutableStateFlow(false)
    val assistantLoading: StateFlow<Boolean> = _assistantLoading

    private val _generatedRecipe = MutableStateFlow<GeneratedRecipe?>(null)
    val generatedRecipe: StateFlow<GeneratedRecipe?> = _generatedRecipe

    private val _ingredientAnalysis = MutableStateFlow<IngredientAnalysis?>(null)
    val ingredientAnalysis: StateFlow<IngredientAnalysis?> = _ingredientAnalysis

    private val _cookingTipsContent = MutableStateFlow<String?>(null)
    val cookingTipsContent: StateFlow<String?> = _cookingTipsContent

    private val _darkMode = MutableStateFlow(appPreferences.getDarkMode())
    val darkMode: StateFlow<Boolean> = _darkMode

    init {
        _searchHistory.value = appPreferences.getSearchHistory()
        _shoppingItems.value = appPreferences.getShoppingList()
        // 冷启动恢复登录 token，使 auth 拦截器在重启后仍能携带
        com.example.kitchenpalapp.network.TokenHolder.token = appPreferences.getToken()
        // 离线兜底：先用最近一次成功加载的数据填充，网络恢复后被刷新覆盖
        val cachedRecipes = appPreferences.getRecipeCache()
        val cachedHero = appPreferences.getHeroCache()
        if (cachedRecipes.isNotEmpty()) {
            _recipes.value = cachedRecipes
            _filteredRecipes.value = cachedRecipes
        }
        if (cachedHero.isNotEmpty()) _heroRecipes.value = cachedHero
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            checkAutoLogin()
            loadCategories()
            loadRecipes()
            loadHeroRecipes()
            loadDailyImage()
        }
    }

    private fun checkAutoLogin() {
        if (appPreferences.isAutoLoginValid()) {
            val savedToken = appPreferences.getToken()
            val savedUser = appPreferences.getUser()
            if (savedToken.isNotBlank() && savedUser != null) {
                _token.value = savedToken
                _currentUser.value = savedUser
                _isLoggedIn.value = true
                _tastePreferences.value = KitchenPalRepository.parseTastePreferences(savedUser.tastePreferences)
                viewModelScope.launch {
                    loadFavorites(savedUser.id)
                    loadNotifications(savedUser.id)
                    loadHistory(savedUser.id)
                }
            }
        }
        _autoLoginChecked.value = true
    }

    fun setUser(user: UserData) {
        _currentUser.value = user
        _isLoggedIn.value = _token.value.isNotBlank()
        _tastePreferences.value = KitchenPalRepository.parseTastePreferences(user.tastePreferences)
        viewModelScope.launch {
            loadFavorites(user.id)
            loadNotifications(user.id)
            loadHistory(user.id)
        }
    }

    fun setToken(token: String) {
        _token.value = token
        _isLoggedIn.value = _currentUser.value != null && token.isNotBlank()
    }

    fun clearUser() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _favoriteIds.value = emptySet()
        _notifications.value = emptyList()
        _cookingHistory.value = emptyList()
    }

    fun logout() {
        _currentUser.value = null
        _token.value = ""
        _isLoggedIn.value = false
        _favoriteIds.value = emptySet()
        _notifications.value = emptyList()
        _cookingHistory.value = emptyList()
        _searchHistory.value = emptyList()
        _checkedSteps.value = emptyMap()
        appPreferences.clearLoginData()
    }

    suspend fun login(phone: String, password: String): ApiResponse<Map<String, Any>> {
        _isLoading.value = true
        val result = KitchenPalRepository.login(phone, password)
        _isLoading.value = false
        if (result.isSuccess && result.data != null) {
            val token = result.data["token"] as? String ?: ""
            val userMap = result.data["user"] as? Map<*, *>
            _token.value = token
            var savedUser: UserData? = null
            if (userMap != null) {
                val gson = com.google.gson.Gson()
                val userJson = gson.toJson(userMap)
                savedUser = gson.fromJson(userJson, UserData::class.java)
                setUser(savedUser)
            }
            _isLoggedIn.value = token.isNotBlank() && _currentUser.value != null
            if (token.isNotBlank() && savedUser != null) {
                appPreferences.saveToken(token)
                appPreferences.saveUser(savedUser)
                appPreferences.saveLoginTime(System.currentTimeMillis())
            }
        } else {
            _errorMessage.value = result.message
        }
        return result
    }

    suspend fun register(phone: String, password: String, nickname: String): ApiResponse<UserData> {
        _isLoading.value = true
        val result = KitchenPalRepository.register(phone, password, nickname)
        _isLoading.value = false
        if (result.isSuccess && result.data != null) {
            _errorMessage.value = null
        } else {
            _errorMessage.value = result.message
        }
        return result
    }

    private suspend fun loadCategories() {
        val result = KitchenPalRepository.getCategories()
        if (result.isSuccess && result.data != null) {
            _categories.value = listOf("全部") + result.data
        } else {
            _categories.value = listOf("全部", "家常菜", "家常热菜", "汤煲")
        }
    }

    suspend fun loadRecipes(category: String? = null, keyword: String? = null) {
        _isLoading.value = true
        val result = KitchenPalRepository.getRecipeList(category, keyword)
        _isLoading.value = false
        if (result.isSuccess && result.data != null) {
            _filteredRecipes.value = result.data
            // 仅全量加载（无筛选条件）才刷新全量列表与缓存，
            // 避免分类/关键词筛选结果污染全量列表（曾导致"吃什么"只剩筛选后的几道菜）
            if (category == null && keyword == null) {
                _recipes.value = result.data
                appPreferences.saveRecipeCache(result.data)
            }
            _errorMessage.value = null
        } else {
            // 断网兜底：已有数据（缓存或上次结果）时静默使用，不弹错误
            if (_recipes.value.isEmpty()) {
                _errorMessage.value = result.message
            }
        }
    }

    private suspend fun loadHeroRecipes() {
        val result = KitchenPalRepository.getHeroRecipes()
        if (result.isSuccess && result.data != null) {
            _heroRecipes.value = result.data
            appPreferences.saveHeroCache(result.data)
        }
    }

    /** 拉取今日美食图片（后端幂等：当天已生成则直接返回，每天仅生成一次）。 */
    private fun loadDailyImage() {
        viewModelScope.launch {
            try {
                val result = KitchenPalRepository.getDailyImage()
                if (result.isSuccess && !result.data?.url.isNullOrBlank()) {
                    _dailyImageUrl.value = result.data!!.url
                    _dailyImageTitle.value = result.data?.title
                    _dailyImageRecipeId.value = result.data?.recipeId
                }
            } catch (_: Exception) {
                // 图片生成依赖外网，失败不影响首页主流程，保留默认图
            }
        }
    }

    private suspend fun loadFavorites(userId: Long) {
        val result = KitchenPalRepository.getFavoriteIds(userId)
        if (result.isSuccess && result.data != null) {
            _favoriteIds.value = result.data
        }
    }

    private suspend fun loadNotifications(userId: Long) {
        val result = KitchenPalRepository.getNotifications(userId)
        if (result.isSuccess && result.data != null) {
            _notifications.value = result.data
        }
    }

    private suspend fun loadHistory(userId: Long) {
        val result = KitchenPalRepository.getHistory(userId)
        if (result.isSuccess && result.data != null) {
            _cookingHistory.value = result.data
        }
    }

    fun loadHistory() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            loadHistory(user.id)
        }
    }

    /**
     * 烹饪完成后记录历史（POST /api/history/add），并刷新本地烹饪历史缓存。
     * 用真实烹饪耗时替换写死的 durationMin，用星级映射替换写死的 96 分。
     */
    fun recordCookingHistory(recipeId: String, rating: Int, elapsedSeconds: Int = 0) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val longId = recipeId.toLongOrNull()
            val data = longId?.let { getRecipeById(it) }
            val timeFmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.CHINA)
            val actualDurationMin = (elapsedSeconds / 60).coerceAtLeast(1)
            val scoreFromRating = rating.coerceIn(1, 5) * 20
            val history = CookingHistoryData(
                userId = user.id,
                recipeId = longId ?: 0L,
                recipeTitle = data?.title ?: "美食",
                recipeImageUrl = data?.imageUrl ?: "",
                cookDate = java.time.LocalDate.now().toString(),
                cookTime = timeFmt.format(java.util.Date()),
                durationMin = actualDurationMin,
                score = scoreFromRating,
                rating = rating
            )
            KitchenPalRepository.addHistory(history)
            loadHistory(user.id)
        }
    }

    /**
     * 烹饪完成落库：记录历史（含最终星级与实际用时）+ 更新用户统计（cookingCount/streakDays）。
     */
    fun completeCooking(recipeId: String, rating: Int, elapsedSeconds: Int = 0) {
        recordCookingHistory(recipeId, rating, elapsedSeconds)
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(
                cookingCount = user.cookingCount + 1,
                streakDays = user.streakDays + 1
            )
            val result = KitchenPalRepository.updateUser(user.id, updated)
            if (result.isSuccess && result.data != null) {
                _currentUser.value = result.data
                appPreferences.saveUser(result.data)
            } else {
                // 后端失败时本地兜底更新，保证界面统计不倒退
                _currentUser.value = updated
                appPreferences.saveUser(updated)
            }
        }
    }

    fun toggleFavorite(recipeId: Long) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = KitchenPalRepository.toggleFavorite(user.id, recipeId)
            if (result.isSuccess) {
                val current = _favoriteIds.value.toMutableSet()
                if (current.contains(recipeId)) {
                    current.remove(recipeId)
                } else {
                    current.add(recipeId)
                }
                _favoriteIds.value = current
            }
        }
    }

    fun isFavorite(recipeId: Long): Boolean = _favoriteIds.value.contains(recipeId)

    // ---------- 购物清单 ----------

    /** 把菜谱食材加入购物清单（同名去重，保留已勾选状态） */
    fun addToShoppingList(items: List<Ingredient>) {
        if (items.isEmpty()) return
        val existing = _shoppingItems.value.associateBy { it.name }
        val merged = existing.toMutableMap()
        val now = System.currentTimeMillis()
        items.forEach { ingredient ->
            val old = merged[ingredient.name]
            if (old == null) {
                merged[ingredient.name] = ShoppingItem(
                    name = ingredient.name,
                    amount = ingredient.amount,
                    addedAt = now
                )
            } else if (!old.checked && old.amount.isBlank() && ingredient.amount.isNotBlank()) {
                merged[ingredient.name] = old.copy(amount = ingredient.amount)
            }
        }
        _shoppingItems.value = merged.values.sortedBy { it.addedAt }
        appPreferences.saveShoppingList(_shoppingItems.value)
    }

    fun toggleShoppingItem(name: String) {
        _shoppingItems.value = _shoppingItems.value.map {
            if (it.name == name) it.copy(checked = !it.checked) else it
        }
        appPreferences.saveShoppingList(_shoppingItems.value)
    }

    fun removeShoppingItem(name: String) {
        _shoppingItems.value = _shoppingItems.value.filter { it.name != name }
        appPreferences.saveShoppingList(_shoppingItems.value)
    }

    fun clearCheckedShoppingItems() {
        _shoppingItems.value = _shoppingItems.value.filter { !it.checked }
        appPreferences.saveShoppingList(_shoppingItems.value)
    }

    fun clearShoppingList() {
        _shoppingItems.value = emptyList()
        appPreferences.saveShoppingList(emptyList())
    }

    // ---------- 买菜清单上云 ----------

    /** 把本地购物清单全量上传到云端（登录后可用） */
    fun syncShoppingToCloud() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _syncing.value = true
            try {
                val payload = _shoppingItems.value.map {
                    CloudShoppingItem(
                        name = it.name,
                        quantity = it.amount,
                        checked = it.checked
                    )
                }
                val result = KitchenPalRepository.syncShopping(user.id, payload)
                if (result.isSuccess) {
                    _errorMessage.value = "已同步到云端"
                } else {
                    _errorMessage.value = "同步失败：${result.exceptionOrNull()?.message ?: "网络异常"}"
                }
            } finally {
                _syncing.value = false
            }
        }
    }

    /** 从云端拉取购物清单并覆盖本地 */
    fun pullShoppingFromCloud() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _syncing.value = true
            try {
                val result = KitchenPalRepository.getShoppingItems(user.id)
                val data = result.getOrNull()
                if (data != null) {
                    val local = data.map {
                        ShoppingItem(
                            name = it.name,
                            amount = it.quantity ?: "",
                            checked = it.checked,
                            addedAt = System.currentTimeMillis()
                        )
                    }
                    _shoppingItems.value = local
                    appPreferences.saveShoppingList(local)
                    _errorMessage.value = "已从云端同步 ${local.size} 项"
                } else {
                    _errorMessage.value = "拉取失败：${result.exceptionOrNull()?.message ?: "网络异常"}"
                }
            } finally {
                _syncing.value = false
            }
        }
    }

    // ---------- 评论 / 评分 ----------

    fun loadComments(recipeId: Long) {
        viewModelScope.launch {
            KitchenPalRepository.getComments(recipeId).getOrNull()?.let {
                _comments.value = it
            }
        }
    }

    fun loadRatings(recipeId: Long) {
        viewModelScope.launch {
            KitchenPalRepository.getRatings(recipeId).getOrNull()?.let {
                _recipeRatings.value = it
            }
        }
    }

    fun submitComment(recipeId: Long, content: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _commentSubmitting.value = true
            try {
                val result = KitchenPalRepository.addComment(recipeId, content)
                if (result.isSuccess) {
                    loadComments(recipeId)
                    onResult(true, "评论已发布")
                } else {
                    onResult(false, "发布失败：${result.exceptionOrNull()?.message ?: "网络异常"}")
                }
            } finally {
                _commentSubmitting.value = false
            }
        }
    }

    fun removeComment(id: Long, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = KitchenPalRepository.deleteComment(id)
            if (result.isSuccess) {
                _comments.value = _comments.value.filter { it.id != id }
                onResult(true, "已删除")
            } else {
                onResult(false, "删除失败：${result.exceptionOrNull()?.message ?: "网络异常"}")
            }
        }
    }

    fun submitRating(recipeId: Long, rating: Int, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = KitchenPalRepository.rateRecipe(recipeId, rating)
            if (result.isSuccess) {
                onResult(true, "评分成功")
            } else {
                onResult(false, "评分失败：${result.exceptionOrNull()?.message ?: "网络异常"}")
            }
        }
    }

    /** 上传成品照：先上传图片拿 URL，再作为带图评论发布到该菜谱 */
    fun uploadCookingPhoto(recipeId: Long, bytes: ByteArray, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val up = KitchenPalRepository.uploadImage(bytes, "cooking")
            val url = up.getOrNull()
            if (url.isNullOrBlank()) {
                onResult(false, "图片上传失败：${up.exceptionOrNull()?.message ?: "网络异常"}")
                return@launch
            }
            val comment = KitchenPalRepository.addComment(recipeId, "分享了成品照 🍳", url)
            if (comment.isSuccess) {
                onResult(true, "成品照已发布")
            } else {
                onResult(false, "发布失败：${comment.exceptionOrNull()?.message ?: "网络异常"}")
            }
        }
    }

    /** 发布用户自制菜谱（UGC），成功后刷新菜谱列表 */
    fun publishRecipe(recipe: RecipeData, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = KitchenPalRepository.createRecipe(recipe)
            if (result.isSuccess) {
                loadRecipes()
                onResult(true, "发布成功")
            } else {
                onResult(false, "发布失败：${result.exceptionOrNull()?.message ?: "网络异常"}")
            }
        }
    }

    // ---------- 冰箱食材 ----------

    fun loadFridgeItems() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            KitchenPalRepository.getFridgeItems(user.id).getOrNull()?.let {
                _fridgeItems.value = it
            }
            KitchenPalRepository.getExpiringItems(user.id, 3).getOrNull()?.let {
                _expiringItems.value = it
            }
        }
    }

    fun addFridgeItem(item: FridgeItemData) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = KitchenPalRepository.addFridgeItem(item.copy(userId = user.id))
            if (result.isSuccess) {
                _errorMessage.value = "已加入冰箱"
                loadFridgeItems()
            } else {
                _errorMessage.value = "添加失败：${result.exceptionOrNull()?.message ?: "网络异常"}"
            }
        }
    }

    fun deleteFridgeItem(id: Long) {
        viewModelScope.launch {
            val result = KitchenPalRepository.deleteFridgeItem(id)
            if (result.isSuccess) {
                _fridgeItems.value = _fridgeItems.value.filter { it.id != id }
                _expiringItems.value = _expiringItems.value.filter { it.id != id }
            } else {
                _errorMessage.value = "删除失败：${result.exceptionOrNull()?.message ?: "网络异常"}"
            }
        }
    }

    fun getFavoriteRecipes(): List<RecipeData> {
        return _recipes.value.filter { _favoriteIds.value.contains(it.id) }
    }

    // ---------- 服务器地址（运行时网络配置） ----------

    /** 读取已保存的自定义地址（用于设置页输入框回显） */
    fun loadServerUrl() {
        _serverUrl.value = appPreferences.getServerUrl()
    }

    /**
     * 保存并立即切换服务器地址，成功后自动重试一次拉取菜谱验证是否可用。
     * @return 是否保存成功（地址非空即视为格式合法，实际连通性由 testConnection 判断）
     */
    fun saveServerUrl(url: String): Boolean {
        if (url.isBlank()) return false
        appPreferences.setServerUrl(url)
        _serverUrl.value = appPreferences.getServerUrl()
        _connTestResult.value = null
        return true
    }

    /** 清除自定义地址，回退到编译期默认 */
    fun resetServerUrl() {
        appPreferences.clearServerUrl()
        _serverUrl.value = ""
        _connTestResult.value = null
    }

    /** 用当前地址测一次连接：拉 /api/recipe/list，成功即视为通 */
    fun testConnection() {
        _connTesting.value = true
        _connTestResult.value = null
        viewModelScope.launch {
            val ok = KitchenPalRepository.getRecipeList().isSuccess
            _connTestResult.value = ok
            _connTesting.value = false
        }
    }

    /** 切换地址后重新加载基础数据，让界面立刻反映新服务器 */
    fun reloadAfterServerChange() {
        viewModelScope.launch {
            loadRecipes()
            loadCategories()
        }
    }

    fun startScan() {
        _scanState.value = ScanState.Scanning
    }

    /**
     * 从相机/相册的 Uri 读取图片（IO 线程统一转 JPEG）后识别。
     */
    fun performScanFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            val bytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.example.kitchenpalapp.utils.ImageUtils.uriToJpegBytes(getApplication(), uri)
            }
            if (bytes == null) {
                _scanState.value = ScanState.Error
            } else {
                performScan(bytes)
            }
        }
    }

    fun performScan(imageBytes: ByteArray) {
        viewModelScope.launch {
            _scanState.value = ScanState.Scanning
            _capturedImage.value = imageBytes
            val result = KitchenPalRepository.recognizeImage(imageBytes)
            _scanState.value = if (result.isSuccess && result.data != null) {
                ScanState.Result(result.data)
            } else {
                ScanState.Error
            }
        }
    }

    fun resetScan() {
        _scanState.value = ScanState.Idle
        _capturedImage.value = null
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        viewModelScope.launch {
            loadRecipes(category = if (category == "全部") null else category)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /** 保存发现页排序方式（内存 StateFlow + 本地持久化）。 */
    fun saveSortOption(name: String) {
        _discoverSort.value = name
        appPreferences.setDiscoverSort(name)
    }

    fun performSearch() {
        viewModelScope.launch {
            loadRecipes(keyword = _searchQuery.value.takeIf { it.isNotBlank() })
        }
    }

    fun filteredRecipesByTag(tag: String): List<RecipeData> {
        return _recipes.value.filter {
            it.title.contains(tag, ignoreCase = true) ||
            it.category.contains(tag, ignoreCase = true) ||
            it.tags?.contains(tag, ignoreCase = true) == true
        }
    }

    fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        _searchHistory.value = (listOf(query) + _searchHistory.value.filter { it != query }).take(10)
        appPreferences.saveSearchHistory(_searchHistory.value)
    }

    fun removeSearchHistory(query: String) {
        _searchHistory.value = _searchHistory.value.filter { it != query }
        appPreferences.saveSearchHistory(_searchHistory.value)
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
        appPreferences.saveSearchHistory(emptyList())
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            KitchenPalRepository.markAsRead(notificationId)
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
        }
    }

    fun markAllAsRead() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            KitchenPalRepository.markAllAsRead(user.id)
            _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        }
    }

    fun getCheckedSteps(recipeId: String): Set<Int> {
        return _checkedSteps.value[recipeId] ?: emptySet()
    }

    fun toggleStep(recipeId: String, stepIndex: Int) {
        val currentMap = _checkedSteps.value.toMutableMap()
        val currentSet = currentMap[recipeId]?.toMutableSet() ?: mutableSetOf()
        if (currentSet.contains(stepIndex)) {
            currentSet.remove(stepIndex)
        } else {
            currentSet.add(stepIndex)
        }
        currentMap[recipeId] = currentSet
        _checkedSteps.value = currentMap
    }

    fun updateTastePreferences(preferences: List<String>) {
        _tastePreferences.value = preferences
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(tastePreferences = preferences.joinToString(","))
            val result = KitchenPalRepository.updateUser(user.id, updatedUser)
            if (result.isSuccess && result.data != null) {
                _currentUser.value = result.data
            }
        }
    }

    fun getRecipeById(id: Long): RecipeData? {
        return _recipes.value.find { it.id == id }
    }

    fun getRecipeList(): List<Recipe> {
        return RecipeConverter.toRecipes(_recipes.value)
    }

    fun getHeroRecipeList(): List<Recipe> {
        return RecipeConverter.toRecipes(_heroRecipes.value)
    }

    fun getRecipeByIdStr(id: String): Recipe? {
        val longId = id.toLongOrNull() ?: return null
        return getRecipeById(longId)?.let { RecipeConverter.toRecipe(it) }
    }

    /**
     * 详情页专用解析：优先命中本地缓存（秒开），未命中时回源后端 `GET /api/recipe/{id}` 拉取，
     * 并回填本地缓存。这样无论从哪个入口进入（尤其是「每日推荐」按日期动态选中的菜谱），
     * 详情页都能拿到对应真实菜谱信息，不会因缓存缺失而显示「食谱未找到」。
     */
    suspend fun fetchRecipeDetail(id: String): Recipe? {
        val longId = id.toLongOrNull() ?: return null
        getRecipeById(longId)?.let { return RecipeConverter.toRecipe(it) }
        val result = KitchenPalRepository.getRecipeDetail(longId)
        val data = if (result.isSuccess) result.data else null
        if (data != null) {
            // 回填缓存，供其它页面复用；避免重复添加
            if (_recipes.value.none { it.id == data.id }) {
                _recipes.value = _recipes.value + data
            }
            return RecipeConverter.toRecipe(data)
        }
        return null
    }

    fun getFavoriteRecipeList(): List<Recipe> {
        val favorites = _recipes.value.filter { _favoriteIds.value.contains(it.id) }
        return RecipeConverter.toRecipes(favorites)
    }

    fun getScanIngredients(): List<DetectedIngredient> {
        val scanResult = (_scanState.value as? ScanState.Result)?.data ?: return emptyList()
        return RecipeConverter.toDetectedIngredients(scanResult.ingredients)
    }

    fun getScanSuggestedRecipes(): List<Recipe> {
        val scanResult = (_scanState.value as? ScanState.Result)?.data ?: return emptyList()
        val suggested = _recipes.value.filter { scanResult.suggestedRecipeIds.contains(it.id) }
        return RecipeConverter.toRecipes(suggested)
    }

    fun getCookingHistoryList(): List<CookingHistoryEntry> {
        return RecipeConverter.toCookingHistoryEntries(_cookingHistory.value)
    }

    /**
     * 含指定食材的真实菜谱（按各菜谱用料表的食材名匹配）。
     * 食材详情页的「相关食谱」用它，不再依赖写死的示例数据。
     */
    fun getRecipesByIngredient(name: String, limit: Int = 12): List<RecipeData> {
        val key = name.trim()
        if (key.isEmpty()) return emptyList()
        return _recipes.value.filter { data ->
            RecipeConverter.ingredientNames(data.ingredientsJson).any { it.matchesIngredient(key) }
        }.take(limit)
    }

    /**
     * 与指定食材在同一道菜里高频共现的其它食材，按出现次数降序。
     * 食材详情页的「常见搭配」用它——完全是真实菜谱里统计出来的，不是编的。
     */
    fun getIngredientPairings(name: String, limit: Int = 10): List<String> {
        val key = name.trim()
        if (key.isEmpty()) return emptyList()
        val counter = mutableMapOf<String, Int>()
        _recipes.value.forEach { data ->
            val names = RecipeConverter.ingredientNames(data.ingredientsJson)
            if (names.any { it.matchesIngredient(key) }) {
                names.filterNot { it.matchesIngredient(key) }
                    .distinct()
                    .forEach { counter[it] = (counter[it] ?: 0) + 1 }
            }
        }
        return counter.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(limit)
            .map { it.key }
    }

    /** 食材名匹配：完全相等，或一方包含另一方（如「番茄」↔「小番茄」「番茄酱」）。 */
    private fun String.matchesIngredient(key: String): Boolean =
        isNotBlank() && (this == key || this.contains(key) || key.contains(this))

    /**
     * 组装食材详情页所需的全部真实数据：用到它的菜谱、出现最多的分类、
     * 相关菜谱的平均热量、以及与之高频共现的搭配食材。
     * 全部由菜谱库统计得出，不含任何写死的示例内容。
     */
    fun buildIngredientDetail(name: String): IngredientDetailData {
        val matched = getRecipesByIngredient(name, limit = 50)
        val recipes = RecipeConverter.toRecipes(matched)
        val topCategory = matched
            .map { it.category }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            .orEmpty()
        val avgCalories = if (matched.isEmpty()) 0 else matched.sumOf { it.calories } / matched.size
        return IngredientDetailData(
            name = name,
            recipeCount = matched.size,
            topCategory = topCategory.ifBlank { "食材" },
            coverImageUrl = recipes.firstOrNull()?.image?.takeIf { it.isNotBlank() },
            avgCalories = avgCalories,
            pairings = getIngredientPairings(name, limit = 12),
            recipes = recipes
        )
    }

    fun getNotificationList(): List<NotificationItem> {
        return RecipeConverter.toNotificationItems(_notifications.value)
    }

    fun getFilteredRecipesByTag(tag: String): List<Recipe> {
        val filtered = filteredRecipesByTag(tag)
        return RecipeConverter.toRecipes(filtered)
    }

    fun submitFeedback(title: String, content: String, contact: String, type: String) {
        viewModelScope.launch {
            KitchenPalRepository.submitFeedback(title, content, contact, type)
        }
    }

    fun checkAppVersion() {
        viewModelScope.launch {
            val result = KitchenPalRepository.checkVersion()
            if (result.isSuccess && result.data != null) {
                _appVersionInfo.value = result.data
            }
        }
    }

    fun updatePreferences(userId: Long, preferences: Map<String, String>) {
        viewModelScope.launch {
            KitchenPalRepository.updatePreferences(userId, preferences)
        }
    }

    fun updateSettings(userId: Long, darkMode: Boolean, cookingNotify: Boolean) {
        viewModelScope.launch {
            KitchenPalRepository.updateSettings(
                userId,
                mapOf("darkMode" to darkMode, "cookingNotify" to cookingNotify)
            )
        }
    }

    fun loadUserStats(userId: Long) {
        viewModelScope.launch {
            val result = KitchenPalRepository.getUserStats(userId)
            if (result.isSuccess && result.data != null) {
                _userStats.value = result.data
            }
        }
    }

    fun sendAssistantMessage(message: String) {
        if (message.isBlank()) return
        val userMsg = AssistantMessage(
            id = System.currentTimeMillis().toString(),
            role = AssistantRole.USER,
            type = AssistantMessageType.TEXT,
            content = message
        )
        _assistantMessages.value = _assistantMessages.value + userMsg
        _assistantLoading.value = true

        viewModelScope.launch {
            try {
                val history = buildAssistantHistory()
                val userContext = buildAssistantUserContext()
                val result = KitchenPalRepository.assistantChat(message, history, userContext)
                if (result.isSuccess && result.data != null) {
                    val reply = result.data["reply"] as? String ?: ""
                    val intent = result.data["intent"] as? String ?: detectLocalIntent(message)

                    when (intent) {
                        "recipe" -> generateRecipeFromChat(message)
                        "analysis" -> analyzeIngredientsFromChat(message)
                        "tips" -> getCookingTipsFromChat(message)
                        else -> {
                            val assistantMsg = AssistantMessage(
                                id = (System.currentTimeMillis() + 1).toString(),
                                role = AssistantRole.ASSISTANT,
                                type = AssistantMessageType.TEXT,
                                content = reply
                            )
                            _assistantMessages.value = _assistantMessages.value + assistantMsg
                        }
                    }
                } else {
                    addAssistantErrorMessage(result.message ?: "抱歉，我暂时无法回答这个问题。")
                }
            } catch (e: Exception) {
                addAssistantErrorMessage("抱歉，网络出现错误：${e.message}")
            } finally {
                _assistantLoading.value = false
            }
        }
    }

    private fun buildAssistantHistory(): List<Map<String, String>> {
        return _assistantMessages.value
            .filter { it.type == AssistantMessageType.TEXT && it.id != "welcome" }
            .takeLast(10)
            .map { mapOf("role" to it.role.name.lowercase(), "content" to it.content) }
    }

    private fun buildAssistantUserContext(): Map<String, Any> {
        val ctx = HashMap<String, Any>()
        val user = _currentUser.value
        if (user != null) {
            if (user.nickname.isNotBlank()) ctx["nickname"] = user.nickname
            if (!user.tastePreferences.isNullOrBlank()) ctx["tastePreferences"] = user.tastePreferences
            ctx["skillLevel"] = inferSkillLevel(user.cookingCount)
            ctx["cookingCount"] = user.cookingCount
        }
        if (_tastePreferences.value.isNotEmpty()) {
            ctx["tastePreferences"] = _tastePreferences.value.joinToString("、")
        }
        return ctx
    }

    private fun inferSkillLevel(cookingCount: Int): String {
        return when {
            cookingCount < 5 -> "新手（建议简单易做的菜，步骤要详细）"
            cookingCount < 30 -> "初学者（可以尝试基础菜品）"
            cookingCount < 100 -> "熟练（可以尝试中等难度）"
            else -> "熟练（可以挑战高难度菜）"
        }
    }

    private fun detectLocalIntent(message: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("生成食谱") || lower.contains("帮我写个食谱") || lower.contains("做个菜")
                    || lower.contains("能做什么") || (lower.contains("怎么做") && !lower.contains("这道菜"))
                    || lower.contains("我家有") || lower.contains("手边有") -> "recipe"
            lower.contains("分析") || lower.contains("营养") || lower.contains("卡路里")
                    || lower.contains("热量") || lower.contains("搭配") -> "analysis"
            lower.contains("技巧") || lower.contains("小贴士") || lower.contains("注意事项")
                    || lower.contains("怎么做好吃") || lower.contains("怎么做得") -> "tips"
            else -> "chat"
        }
    }

    private fun getCookingTipsFromChat(message: String) {
        val dishName = extractDishName(message)
        if (dishName.isBlank()) {
            val assistantMsg = AssistantMessage(
                id = System.currentTimeMillis().toString(),
                role = AssistantRole.ASSISTANT,
                type = AssistantMessageType.TEXT,
                content = "请告诉我你想了解哪道菜的烹饪技巧，比如\"红烧肉怎么做好吃\"。"
            )
            _assistantMessages.value = _assistantMessages.value + assistantMsg
            return
        }
        viewModelScope.launch {
            _assistantLoading.value = true
            val result = KitchenPalRepository.cookingTips(dishName)
            _assistantLoading.value = false
            if (result.isSuccess && result.data != null) {
                val tips = result.data["tips"] as? String ?: "抱歉，没有获取到烹饪技巧。"
                val assistantMsg = AssistantMessage(
                    id = System.currentTimeMillis().toString(),
                    role = AssistantRole.ASSISTANT,
                    type = AssistantMessageType.TEXT,
                    content = "「$dishName」烹饪技巧：\n\n$tips"
                )
                _assistantMessages.value = _assistantMessages.value + assistantMsg
            } else {
                addAssistantErrorMessage("获取技巧失败：${result.message}")
            }
        }
    }

    private fun extractDishName(message: String): String {
        val cleaned = message
            .replace("怎么做", "")
            .replace("怎么做好吃", "")
            .replace("怎么做得", "")
            .replace("做法", "")
            .replace("技巧", "")
            .replace("小贴士", "")
            .replace("注意事项", "")
            .replace("?","")
            .replace("?","")
            .replace("？", "")
            .trim()
        return cleaned
    }

    private fun addAssistantErrorMessage(content: String) {
        val assistantMsg = AssistantMessage(
            id = System.currentTimeMillis().toString(),
            role = AssistantRole.ASSISTANT,
            type = AssistantMessageType.TEXT,
            content = content
        )
        _assistantMessages.value = _assistantMessages.value + assistantMsg
    }

    private fun generateRecipeFromChat(ingredients: String) {
        viewModelScope.launch {
            _assistantLoading.value = true
            val result = KitchenPalRepository.generateRecipe(ingredients)
            _assistantLoading.value = false
            if (result.isSuccess && result.data != null) {
                val recipeMap = result.data["recipe"] as? Map<*, *>
                val recipe = parseGeneratedRecipe(recipeMap)
                val assistantMsg = AssistantMessage(
                    id = System.currentTimeMillis().toString(),
                    role = AssistantRole.ASSISTANT,
                    type = AssistantMessageType.RECIPE,
                    recipe = recipe
                )
                _assistantMessages.value = _assistantMessages.value + assistantMsg
            } else {
                addAssistantErrorMessage("食谱生成失败：${result.message}")
            }
        }
    }

    private fun analyzeIngredientsFromChat(ingredients: String) {
        viewModelScope.launch {
            _assistantLoading.value = true
            val result = KitchenPalRepository.analyzeIngredients(ingredients)
            _assistantLoading.value = false
            if (result.isSuccess && result.data != null) {
                val analysisMap = result.data["analysis"] as? Map<*, *>
                val analysis = parseIngredientAnalysis(analysisMap)
                val assistantMsg = AssistantMessage(
                    id = System.currentTimeMillis().toString(),
                    role = AssistantRole.ASSISTANT,
                    type = AssistantMessageType.ANALYSIS,
                    analysis = analysis
                )
                _assistantMessages.value = _assistantMessages.value + assistantMsg
            } else {
                addAssistantErrorMessage("食材分析失败：${result.message}")
            }
        }
    }

    fun generateRecipe(ingredients: String, style: String = "", dietary: String = "") {
        _assistantLoading.value = true
        viewModelScope.launch {
            val result = KitchenPalRepository.generateRecipe(ingredients, style, dietary)
            _assistantLoading.value = false
            if (result.isSuccess && result.data != null) {
                val recipeMap = result.data["recipe"] as? Map<*, *>
                _generatedRecipe.value = parseGeneratedRecipe(recipeMap)
            } else {
                _errorMessage.value = result.message
            }
        }
    }

    fun analyzeIngredients(ingredients: String) {
        _assistantLoading.value = true
        viewModelScope.launch {
            val result = KitchenPalRepository.analyzeIngredients(ingredients)
            _assistantLoading.value = false
            if (result.isSuccess && result.data != null) {
                val analysisMap = result.data["analysis"] as? Map<*, *>
                _ingredientAnalysis.value = parseIngredientAnalysis(analysisMap)
            } else {
                _errorMessage.value = result.message
            }
        }
    }

    fun getCookingTips(dishName: String) {
        _assistantLoading.value = true
        viewModelScope.launch {
            val result = KitchenPalRepository.cookingTips(dishName)
            _assistantLoading.value = false
            if (result.isSuccess && result.data != null) {
                _cookingTipsContent.value = result.data["tips"] as? String
            } else {
                _errorMessage.value = result.message
            }
        }
    }

    fun clearAssistantMessages() {
        _assistantMessages.value = listOf(
            AssistantMessage(
                id = "welcome",
                role = AssistantRole.ASSISTANT,
                type = AssistantMessageType.TEXT,
                content = "你好！我是小厨，你的厨房管家。\n\n把你的情况告诉我，我来给你具体方案～"
            )
        )
    }

    fun clearGeneratedRecipe() {
        _generatedRecipe.value = null
    }

    fun clearIngredientAnalysis() {
        _ingredientAnalysis.value = null
    }

    fun clearCookingTips() {
        _cookingTipsContent.value = null
    }

    private fun parseGeneratedRecipe(map: Map<*, *>?): GeneratedRecipe {
        if (map == null) return GeneratedRecipe()
        val ingredients = (map["ingredients"] as? List<*>)?.mapNotNull { item ->
            (item as? Map<*, *>)?.let {
                GeneratedIngredient(
                    name = it["name"] as? String ?: "",
                    quantity = it["quantity"] as? String ?: ""
                )
            }
        } ?: emptyList()
        val steps = (map["steps"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        return GeneratedRecipe(
            name = map["name"] as? String ?: "",
            description = map["description"] as? String ?: "",
            prepTime = map["prepTime"] as? String ?: "",
            cookTime = map["cookTime"] as? String ?: "",
            servings = map["servings"] as? String ?: "",
            difficulty = map["difficulty"] as? String ?: "",
            calories = map["calories"]?.toString() ?: "",
            ingredients = ingredients,
            steps = steps,
            tips = map["tips"] as? String ?: ""
        )
    }

    private fun parseIngredientAnalysis(map: Map<*, *>?): IngredientAnalysis {
        if (map == null) return IngredientAnalysis()
        return IngredientAnalysis(
            nutrition = map["nutrition"] as? String ?: "",
            flavor = map["flavor"] as? String ?: "",
            pairing = map["pairing"] as? String ?: "",
            storage = map["storage"] as? String ?: ""
        )
    }

    companion object {
        /**
         * 根据烹饪历史的真实日期计算连续烹饪天数。
         * 规则：从最近一条记录开始向前数，日期连续（相差 1 天）则累计，遇到断档即止。
         * 这样比 UserData 里盲目 +1 的 streakDays 更能反映真实打卡情况。
         */
        fun calculateStreakDays(history: List<CookingHistoryData>): Int {
            val dates = history.mapNotNull { entry ->
                entry.createdAt?.takeIf { it.length >= 10 }?.let { raw ->
                    try {
                        java.time.LocalDate.parse(raw.substring(0, 10))
                    } catch (_: Exception) { null }
                }
            }.distinct().sortedDescending()
            if (dates.isEmpty()) return 0
            var streak = 1
            var current = dates.first()
            for (i in 1 until dates.size) {
                if (dates[i] == current.minusDays(1)) {
                    streak++
                    current = dates[i]
                } else {
                    break
                }
            }
            return streak
        }
    }
}

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class Result(val data: com.example.kitchenpalapp.data.ScanResultData) : ScanState()
    object Error : ScanState()
}

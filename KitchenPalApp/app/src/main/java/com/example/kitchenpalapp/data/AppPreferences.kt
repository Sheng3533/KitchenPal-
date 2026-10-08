package com.example.kitchenpalapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class AppPreferences(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("KitchenPalPrefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER = "user"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val KEY_SHOPPING_LIST = "shopping_list"
        private const val KEY_RECIPE_CACHE = "recipe_cache"
        private const val KEY_HERO_CACHE = "hero_cache"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_DISCOVER_SORT = "discover_sort"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_HEAT_SENSITIVITY = "heat_sensitivity"
        private const val AUTO_LOGIN_DURATION = 7 * 24 * 60 * 60 * 1000L
        private const val MAX_SEARCH_HISTORY = 10
    }

    fun saveToken(token: String) {
        preferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String {
        return preferences.getString(KEY_TOKEN, "") ?: ""
    }

    fun saveUser(user: UserData) {
        val userJson = gson.toJson(user)
        preferences.edit().putString(KEY_USER, userJson).apply()
    }

    fun getUser(): UserData? {
        val userJson = preferences.getString(KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, UserData::class.java)
        } else {
            null
        }
    }

    fun saveLoginTime(time: Long) {
        preferences.edit().putLong(KEY_LOGIN_TIME, time).apply()
    }

    fun getLoginTime(): Long {
        return preferences.getLong(KEY_LOGIN_TIME, 0)
    }

    fun isAutoLoginValid(): Boolean {
        val token = getToken()
        if (token.isBlank()) return false

        val loginTime = getLoginTime()
        if (loginTime == 0L) return false

        return System.currentTimeMillis() - loginTime < AUTO_LOGIN_DURATION
    }

    fun isLoggedIn(): Boolean {
        return getToken().isNotBlank() && getUser() != null
    }

    fun clearLoginData() {
        preferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER)
            .remove(KEY_LOGIN_TIME)
            .apply()
        com.example.kitchenpalapp.network.TokenHolder.token = ""
    }

    // ---------- 搜索历史（最多 10 条，最新在前） ----------

    fun getSearchHistory(): List<String> {
        val json = preferences.getString(KEY_SEARCH_HISTORY, null) ?: return emptyList()
        return try {
            val type = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, String::class.java).type
            gson.fromJson<List<String>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSearchHistory(history: List<String>) {
        preferences.edit()
            .putString(KEY_SEARCH_HISTORY, gson.toJson(history.take(MAX_SEARCH_HISTORY)))
            .apply()
    }

    // ---------- 购物清单 ----------
    fun getShoppingList(): List<ShoppingItem> {
        val json = preferences.getString(KEY_SHOPPING_LIST, null) ?: return emptyList()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<ShoppingItem>>() {}.type
            gson.fromJson<List<ShoppingItem>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveShoppingList(items: List<ShoppingItem>) {
        preferences.edit()
            .putString(KEY_SHOPPING_LIST, gson.toJson(items))
            .apply()
    }

    // ---------- 菜谱离线缓存（断网兜底，缓存最近一次成功加载的列表） ----------

    fun getRecipeCache(): List<RecipeData> {
        val json = preferences.getString(KEY_RECIPE_CACHE, null) ?: return emptyList()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<RecipeData>>() {}.type
            gson.fromJson<List<RecipeData>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveRecipeCache(items: List<RecipeData>) {
        preferences.edit().putString(KEY_RECIPE_CACHE, gson.toJson(items)).apply()
    }

    fun getHeroCache(): List<RecipeData> {
        val json = preferences.getString(KEY_HERO_CACHE, null) ?: return emptyList()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<RecipeData>>() {}.type
            gson.fromJson<List<RecipeData>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveHeroCache(items: List<RecipeData>) {
        preferences.edit().putString(KEY_HERO_CACHE, gson.toJson(items)).apply()
    }

    // ---------- 深色模式偏好（本地持久化，驱动 App 根主题） ----------

    fun getDarkMode(): Boolean {
        return preferences.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    // ---------- 发现页排序偏好（本地持久化，下次进入发现页仍沿用上次选择） ----------

    fun getDiscoverSort(): String {
        return preferences.getString(KEY_DISCOVER_SORT, "") ?: ""
    }

    fun setDiscoverSort(name: String) {
        preferences.edit().putString(KEY_DISCOVER_SORT, name).apply()
    }

    // ---------- 服务器地址（运行时可改，真机联调换网络时无需重新编译） ----------

    /** 用户自定义的服务器地址；未设置过时返回空串（由 BaseUrlHolder 回退到编译期默认值） */
    fun getServerUrl(): String {
        return preferences.getString(KEY_SERVER_URL, "") ?: ""
    }

    fun setServerUrl(url: String) {
        val normalized = com.example.kitchenpalapp.network.BaseUrlHolder.normalize(url)
        preferences.edit().putString(KEY_SERVER_URL, normalized).apply()
        com.example.kitchenpalapp.network.BaseUrlHolder.set(normalized)
    }

    /** 清除自定义地址，回到编译期默认（模拟器 10.0.2.2 / 编译时配置的局域网 IP） */
    fun clearServerUrl() {
        preferences.edit().remove(KEY_SERVER_URL).apply()
        com.example.kitchenpalapp.network.BaseUrlHolder.reset()
    }

    // ---------- 火候灵敏度（0=低 1=中等 2=高，默认中等） ----------

    /** 火候提醒间隔（秒），与灵敏度一一对应：由火候监控页消费，决定提醒频率 */
    fun getHeatRemindIntervalSeconds(): Int = arrayOf(300, 180, 90)[getHeatSensitivity()]

    fun getHeatSensitivity(): Int = preferences.getInt(KEY_HEAT_SENSITIVITY, 1)

    fun setHeatSensitivity(level: Int) {
        preferences.edit().putInt(KEY_HEAT_SENSITIVITY, level.coerceIn(0, 2)).apply()
    }

    /** App 启动时调用：把已保存的自定义地址装回 BaseUrlHolder */
    fun applySavedServerUrl() {
        val saved = getServerUrl()
        if (saved.isNotBlank()) {
            com.example.kitchenpalapp.network.BaseUrlHolder.set(saved)
        }
    }
}
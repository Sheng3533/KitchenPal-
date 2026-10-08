package com.example.kitchenpalapp.data

import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.network.RetrofitClient
import com.google.gson.Gson

object RecipeConverter {

    private val gson = Gson()

    fun toRecipe(data: RecipeData): Recipe {
        val ingredients = parseIngredients(data.ingredientsJson)
        val steps = parseSteps(data.stepsJson)
        val tags = parseTags(data.tags)

        return Recipe(
            id = data.id.toString(),
            title = data.title,
            subtitle = data.subtitle,
            image = fullImageUrl(data.imageUrl),
            imageRes = 0,
            rating = data.rating,
            cookTimeMin = data.cookTimeMin,
            calories = data.calories,
            difficulty = data.difficulty,
            category = data.category,
            tags = tags,
            ingredients = ingredients,
            steps = steps,
            isFavorite = false,
            matchPercent = data.matchPercent ?: 0
        )
    }

    fun toRecipes(dataList: List<RecipeData>): List<Recipe> {
        return dataList.map { toRecipe(it) }
    }

    /**
     * 图片统一走后端资源：后端返回相对路径（如 /images/1.jpg）时补全为绝对地址，便于 Coil 加载。
     * 与菜名一一对应的图片由后端 static/images 托管，彻底避免"名称与图片不一致"。
     */
    fun fullImageUrl(url: String?): String {
        val u = url?.trim().orEmpty()
        if (u.isEmpty()) return ""
        return if (u.startsWith("/")) RetrofitClient.BASE_URL.trimEnd('/') + u else u
    }

    private fun parseIngredients(json: String?): List<Ingredient> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson(json, Array<IngredientData>::class.java).map {
                Ingredient(it.name, it.amount, it.iconName)
            }.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** 只取食材名（供食材维度统计复用，如「哪些菜用到了番茄」）。 */
    fun ingredientNames(json: String?): List<String> =
        parseIngredients(json).map { it.name }

    private fun parseSteps(json: String?): List<CookStep> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson(json, Array<CookStepData>::class.java).map { s ->
                val hasDescription = s.description.isNotBlank()
                CookStep(
                    description = if (hasDescription) s.description else s.title,
                    heat = s.heat?.takeIf { it.isNotBlank() }
                        ?: s.heatTip?.takeIf { it.isNotBlank() },
                    durationSeconds = s.durationSeconds,
                    title = if (hasDescription) s.title else "",
                    tip = s.tip?.takeIf { it.isNotBlank() },
                    ingredients = s.ingredients ?: emptyList()
                )
            }.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseTags(tags: String?): List<String> {
        if (tags.isNullOrBlank()) return emptyList()
        return tags.split(",").map { it.trim() }
    }

    fun toDetectedIngredients(dataList: List<DetectedIngredientData>): List<DetectedIngredient> {
        return dataList.map {
            val imageRes = when (it.name) {
                "西红柿", "番茄" -> R.drawable.ingredient_tomato
                "洋葱", "葱" -> R.drawable.ingredient_onion
                "蒜", "大蒜" -> R.drawable.ingredient_garlic
                "鸡蛋", "蛋" -> R.drawable.egg
                "牛肉", "牛腩", "牛排", "猪肉", "里脊", "排骨" -> R.drawable.beef
                "鱼", "鲈鱼", "鲫鱼", "三文鱼" -> R.drawable.fish
                else -> R.drawable.ingredient_tomato
            }
            DetectedIngredient(it.name, it.confidence, it.weight, imageRes)
        }
    }

    /**
     * 把后端烹饪历史转成时间线条目。
     *
     * 日期 / 星期标签由后端真实时间 `createdAt` 推导，不再依赖后端写死的「今天/昨天」字面量，
     * 也不再只有「今天」的记录能显示——任何一天的记录都会归到自己的日期分组。
     * 结果按时间倒序排列，保证最新的在最上面。
     */
    fun toCookingHistoryEntries(dataList: List<CookingHistoryData>): List<CookingHistoryEntry> {
        val today = java.time.LocalDate.now()
        return dataList
            .sortedByDescending { it.createdAt ?: "" }
            .map { it ->
                val date = parseDate(it.createdAt)
                val dateLabel = when {
                    date != null -> "${date.monthValue}月${date.dayOfMonth}日"
                    it.cookDate.isNotBlank() -> it.cookDate
                    else -> "未知日期"
                }
                val dayLabel = when {
                    date == null -> if (it.cookDate == "今天" || it.cookDate == "昨天") it.cookDate else ""
                    date == today -> "今天"
                    date == today.minusDays(1) -> "昨天"
                    else -> "周" + WEEK_CN[date.dayOfWeek.value - 1]
                }
                CookingHistoryEntry(
                    recipeId = it.recipeId.toString(),
                    recipeName = it.recipeTitle,
                    date = dateLabel,
                    dayLabel = dayLabel,
                    time = it.cookTime,
                    duration = "${it.durationMin}分钟",
                    heatAdjustments = 0,
                    score = it.score,
                    stars = it.rating,
                    imageRes = 0,
                    imageUrl = fullImageUrl(it.recipeImageUrl).takeIf { url -> url.isNotBlank() }
                )
            }
    }

    /** 周一到周日的中文单字，索引 = LocalDate.dayOfWeek.value - 1 */
    private const val WEEK_CN = "一二三四五六日"

    /** 解析 ISO-8601 时间串（取前 10 位日期部分即可，兼容 2026-09-26T12:30 与 2026-09-26）。 */
    private fun parseDate(raw: String?): java.time.LocalDate? {
        if (raw.isNullOrBlank()) return null
        return try {
            java.time.LocalDate.parse(raw.substring(0, 10))
        } catch (e: Exception) {
            null
        }
    }

    fun toNotificationItems(dataList: List<NotificationData>): List<NotificationItem> {
        return dataList.map {
            NotificationItem(
                id = it.id.toString(),
                icon = it.icon ?: "bell",
                title = it.title,
                description = it.content,
                time = it.createdAt ?: "",
                isRead = it.isRead,
                type = when (it.type) {
                    "recipe" -> 0
                    "promo" -> 2
                    else -> 1
                },
                relatedId = it.relatedId ?: 0L
            )
        }
    }
}
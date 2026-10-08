package com.example.kitchenpalapp.data

import com.example.kitchenpalapp.R

object SampleData {

    val detectedIngredients = listOf(
        DetectedIngredient(name = "番茄", confidence = 98, weight = "约200g", imageRes = R.drawable.ingredient_tomato),
        DetectedIngredient(name = "洋葱", confidence = 95, weight = "约150g", imageRes = R.drawable.ingredient_onion),
        DetectedIngredient(name = "大蒜", confidence = 91, weight = "约30g", imageRes = R.drawable.ingredient_garlic)
    )

    val heroRecipe = Recipe(
        id = "tomato-beef-brisket",
        title = "番茄炖牛腩",
        subtitle = "",
        imageRes = R.drawable.home_recipe_card,
        rating = 4.8f,
        cookTimeMin = 45,
        calories = 380,
        difficulty = "中等",
        category = "家常热菜",
        tags = listOf("推荐"),
        ingredients = listOf(
            Ingredient(name = "牛腩", amount = "500g", iconName = "beef"),
            Ingredient(name = "番茄", amount = "3个", iconName = "tomato"),
            Ingredient(name = "洋葱", amount = "1个", iconName = "onion"),
            Ingredient(name = "大蒜", amount = "5瓣", iconName = "garlic"),
            Ingredient(name = "生姜", amount = "3片", iconName = "leaf"),
            Ingredient(name = "料酒", amount = "2勺", iconName = "flask"),
            Ingredient(name = "盐", amount = "适量", iconName = "flask"),
            Ingredient(name = "冰糖", amount = "15g", iconName = "flask")
        ),
        steps = listOf(
            CookStep("牛腩切块焯水，去除血沫杂质", "大火煮沸后转小火"),
            CookStep("番茄划十字，开水烫后去皮切块"),
            CookStep("热油爆香葱姜蒜，加牛腩翻炒上色", "中火翻炒3分钟"),
            CookStep("加番茄和热水，大火烧开转小火炖40分钟", "大火转小火是关键")
        )
    )

    val suggestedAfterScan = Recipe(
        id = "tomato-pasta",
        title = "番茄肉酱意面",
        subtitle = "利用现有食材，30分钟搞定",
        imageRes = R.drawable.recipe_suggestion,
        rating = 4.6f,
        cookTimeMin = 30,
        calories = 420,
        difficulty = "简单",
        category = "西式简餐",
        tags = listOf("食材匹配", "快手菜"),
        matchPercent = 92,
        ingredients = listOf(
            Ingredient(name = "番茄", amount = "2个", iconName = "tomato"),
            Ingredient(name = "洋葱", amount = "半个", iconName = "onion"),
            Ingredient(name = "大蒜", amount = "2瓣", iconName = "garlic"),
            Ingredient(name = "意面", amount = "120g", iconName = "wheat"),
            Ingredient(name = "肉末", amount = "100g", iconName = "beef")
        ),
        steps = listOf(
            CookStep("番茄顶部划十字，开水烫后去皮切丁"),
            CookStep("洋葱、大蒜切末，锅中热油炒香"),
            CookStep("加入肉末炒散，倒入番茄丁熬成酱汁"),
            CookStep("意面煮熟捞出，拌入酱汁即可")
        )
    )

    val recipes = listOf(
        heroRecipe,
        Recipe(
            id = "green-pepper-pork",
            title = "青椒肉丝",
            subtitle = "鲜香嫩滑，下饭首选",
            imageRes = R.drawable.recipe_card_1,
            rating = 4.7f,
            cookTimeMin = 15,
            calories = 280,
            difficulty = "简单",
            category = "家常菜",
            tags = listOf("快手", "高蛋白"),
            matchPercent = 85,
            ingredients = listOf(
                Ingredient(name = "猪里脊", amount = "200g", iconName = "beef"),
                Ingredient(name = "青椒", amount = "3个", iconName = "leaf"),
                Ingredient(name = "蒜", amount = "2瓣", iconName = "garlic")
            ),
            steps = listOf(
                CookStep("猪肉切丝，加生抽、淀粉腌制10分钟"),
                CookStep("青椒去籽切丝，蒜切末备用"),
                CookStep("热锅凉油滑炒肉丝至变色盛出"),
                CookStep("爆香蒜末，下青椒翻炒，再倒入肉丝炒匀"),
                CookStep("调入盐、生抽，翻炒均匀出锅")
            )
        ),
        Recipe(
            id = "herbal-chicken-soup",
            title = "山药药膳鸡汤",
            subtitle = "滋补暖身，慢火细炖",
            imageRes = R.drawable.recipe_card_2,
            rating = 4.9f,
            cookTimeMin = 90,
            calories = 320,
            difficulty = "中等",
            category = "汤煲",
            tags = listOf("养生", "温补"),
            matchPercent = 78,
            ingredients = listOf(
                Ingredient(name = "土鸡", amount = "半只", iconName = "beef"),
                Ingredient(name = "山药", amount = "200g", iconName = "leaf"),
                Ingredient(name = "枸杞", amount = "10g", iconName = "leaf"),
                Ingredient(name = "红枣", amount = "5颗", iconName = "leaf"),
                Ingredient(name = "姜片", amount = "3片", iconName = "leaf")
            ),
            steps = listOf(
                CookStep("鸡肉切块焯水洗净"),
                CookStep("砂锅中加足量清水，放入鸡肉和姜片"),
                CookStep("大火烧开后转小火炖煮60分钟"),
                CookStep("加入山药继续炖20分钟"),
                CookStep("出锅前加枸杞红枣，盐调味")
            )
        ),
        Recipe(
            id = "tomato-egg",
            title = "番茄炒蛋",
            subtitle = "国民家常菜，酸甜开胃",
            imageRes = R.drawable.recipe_tomato_egg,
            rating = 4.7f,
            cookTimeMin = 10,
            calories = 210,
            difficulty = "简单",
            category = "家常菜",
            tags = listOf("素食", "快手"),
            matchPercent = 88,
            ingredients = listOf(
                Ingredient(name = "番茄", amount = "2个", iconName = "tomato"),
                Ingredient(name = "鸡蛋", amount = "3个", iconName = "egg"),
                Ingredient(name = "葱", amount = "适量", iconName = "leaf")
            ),
            steps = listOf(
                CookStep("番茄切块，鸡蛋打散加少许盐"),
                CookStep("热锅热油倒入蛋液，炒熟盛出"),
                CookStep("锅中留底油炒番茄出汁"),
                CookStep("倒入鸡蛋翻炒均匀，撒葱花出锅")
            )
        ),
        Recipe(
            id = "garlic-broccoli",
            title = "蒜蓉西兰花",
            subtitle = "清爽低脂，健康首选",
            imageRes = R.drawable.recipe_broccoli,
            rating = 4.6f,
            cookTimeMin = 12,
            calories = 120,
            difficulty = "简单",
            category = "家常菜",
            tags = listOf("低脂", "健康"),
            matchPercent = 80,
            ingredients = listOf(
                Ingredient(name = "西兰花", amount = "300g", iconName = "leaf"),
                Ingredient(name = "大蒜", amount = "4瓣", iconName = "garlic"),
                Ingredient(name = "胡萝卜", amount = "半根", iconName = "leaf")
            ),
            steps = listOf(
                CookStep("西兰花切小朵，盐水浸泡后焯水"),
                CookStep("大蒜切末，胡萝卜切片"),
                CookStep("热锅爆香蒜末，下胡萝卜片翻炒"),
                CookStep("倒入西兰花，加盐和蚝油调味炒匀")
            )
        ),
        Recipe(
            id = "braised-ribs",
            title = "红烧排骨",
            subtitle = "酱香浓郁，骨肉分离",
            imageRes = R.drawable.recipe_ribs,
            rating = 4.9f,
            cookTimeMin = 60,
            calories = 480,
            difficulty = "中等",
            category = "家常热菜",
            tags = listOf("下饭", "聚会"),
            matchPercent = 70,
            ingredients = listOf(
                Ingredient(name = "猪肋排", amount = "500g", iconName = "beef"),
                Ingredient(name = "八角", amount = "2个", iconName = "flask"),
                Ingredient(name = "桂皮", amount = "1块", iconName = "flask"),
                Ingredient(name = "冰糖", amount = "20g", iconName = "flask")
            ),
            steps = listOf(
                CookStep("排骨剁块焯水洗净"),
                CookStep("炒糖色后倒入排骨翻炒"),
                CookStep("加入香料、生抽、老抽和开水"),
                CookStep("小火炖煮50分钟，大火收汁")
            )
        ),
        Recipe(
            id = "tomato-egg-soup",
            title = "番茄蛋汤",
            subtitle = "鲜美暖胃，十分钟上桌",
            imageRes = R.drawable.recipe_egg_soup,
            rating = 4.7f,
            cookTimeMin = 10,
            calories = 95,
            difficulty = "简单",
            category = "汤煲",
            tags = listOf("快手", "清淡"),
            matchPercent = 90,
            ingredients = listOf(
                Ingredient(name = "番茄", amount = "1个", iconName = "tomato"),
                Ingredient(name = "鸡蛋", amount = "1个", iconName = "egg"),
                Ingredient(name = "葱", amount = "适量", iconName = "leaf")
            ),
            steps = listOf(
                CookStep("番茄切块，鸡蛋打散"),
                CookStep("锅中加水烧开，放入番茄煮软"),
                CookStep("缓缓倒入蛋液形成蛋花"),
                CookStep("加盐调味，撒葱花出锅")
            )
        )
    )

    fun getRecipeById(id: String): Recipe? = recipes.find { it.id == id }

    val categories = listOf(
        "全部" to "restaurant",
        "家常菜" to "soup",
        "健康蔬菜" to "spa",
        "汤煲" to "local_dining",
        "快手简餐" to "timer",
        "烘焙甜品" to "cake"
    )

    val filterTags = listOf("全部", "快手菜", "家常菜", "汤煲", "烘焙", "甜品")
    val hotSearches = listOf("番茄炒蛋", "红烧排骨", "家常豆腐", "蒜蓉虾", "糖醋里脊", "清蒸鲈鱼", "宫保鸡丁", "酸辣汤")
    val searchHistory = listOf("牛肉炖土豆", "番茄意面", "西兰花炒虾仁")
    val searchChannels = listOf(
        SearchChannel("快手菜", "10分钟搞定", R.drawable.clock, 0xFFE8743B, 0xFFF09B6E),
        SearchChannel("家常菜", "家的味道", R.drawable.home, 0xFF8B5E3C, 0xFFB07D55),
        SearchChannel("养生汤", "滋补养生", R.drawable.heart, 0xFF4A9D5B, 0xFF6BBF7A),
        SearchChannel("烘焙", "甜蜜时光", R.drawable.cake_slice, 0xFFD97B93, 0xFFE8A0B3)
    )

    val cookingHistory = listOf(
        CookingHistoryEntry("tomato-beef-brisket", "番茄炖牛腩", "7月11日", "今天", "12:30", "45分钟", 3, 96, 5, R.drawable.home_recipe_card),
        CookingHistoryEntry("garlic-broccoli", "蒜蓉西兰花", "7月11日", "今天", "18:45", "12分钟", 0, 92, 4, R.drawable.recipe_broccoli),
        CookingHistoryEntry("green-pepper-pork", "青椒肉丝", "7月9日", "周三", "19:20", "15分钟", 0, 88, 4, R.drawable.recipe_card_1),
        CookingHistoryEntry("herbal-chicken-soup", "山药药膳鸡汤", "7月9日", "周三", "11:00", "90分钟", 5, 94, 5, R.drawable.recipe_card_2),
        CookingHistoryEntry("tomato-egg", "番茄炒蛋", "7月7日", "周一", "12:15", "10分钟", 0, 98, 5, R.drawable.recipe_tomato_egg)
    )

    val notifications = listOf(
        NotificationItem("1", "flame", "炖牛腩火候提醒", "已炖煮40分钟，建议检查火候并翻动牛腩", "10分钟前", false, type = 1, relatedId = 0L),
        NotificationItem("2", "sparkles", "新食谱推荐", "根据您最近烹饪记录，推荐3道新菜谱", "1小时前", false, type = 0, relatedId = 1L),
        NotificationItem("3", "chef_hat", "烹饪完成记录", "您上次制作的番茄炒蛋已自动记录", "3小时前", true, type = 1, relatedId = 0L),
        NotificationItem("4", "heart", "收藏食谱更新", "您收藏的\"红烧排骨\"新增了2个做法技巧", "昨天", true, type = 0, relatedId = 7L),
        NotificationItem("5", "sparkles", "每周烹饪报告已生成", "点击查看您本周的烹饪数据和营养分析", "昨天 18:00", true, type = 1, relatedId = 0L),
        NotificationItem("6", "bell", "系统更新通知", "v2.5版本已发布，新增AI火候预测功能", "昨天 10:00", true, type = 2, relatedId = 0L)
    )

    val ingredientInfo = IngredientInfo(
        name = "番茄",
        aliases = "西红柿 · 番茄",
        season = "夏季",
        imageRes = R.drawable.ingredient_tomato,
        calories = "18kcal/100g",
        water = "95%",
        vitaminC = "14mg",
        nutrition = listOf(
            NutritionItem("维生素C", 89, 0xFFE8743B),
            NutritionItem("膳食纤维", 62, 0xFF4A9D5B),
            NutritionItem("钾", 45, 0xFF5B8EC9),
            NutritionItem("番茄红素", 95, 0xFFD94B4B)
        ),
        storage = listOf(
            StorageMethod("冷藏", "7天", "snowflake", 0xFF5B8EC9),
            StorageMethod("冷冻", "3月", "snowflake", 0xFF5B8EC9),
            StorageMethod("常温", "3天", "sun", 0xFFE8A33D)
        ),
        goodPairings = listOf("牛腩", "鸡蛋", "洋葱"),
        badPairings = listOf("黄瓜"),
        relatedRecipes = listOf(
            recipes.find { it.id == "tomato-pasta" } ?: suggestedAfterScan,
            heroRecipe
        )
    )

    val dietReport = DietReportData(
        score = 85,
        scoreDelta = 3,
        nutrition = listOf(
            NutritionIntake("蛋白质", "62g", "80g", 78, "beef"),
            NutritionIntake("碳水化合物", "195g", "300g", 65, "wheat"),
            NutritionIntake("脂肪", "32g", "70g", 45, "droplets"),
            NutritionIntake("膳食纤维", "23g", "25g", 92, "salad", achieved = true)
        ),
        cookingStats = listOf(
            CookingStat("12", "次", "烹饪", "chef_hat", 0xFFE8743B),
            CookingStat("3", "道", "尝试新菜", "sparkles", 0xFFE8A33D),
            CookingStat("35", "min", "平均用时", "clock", 0xFF5B8EC9),
            CookingStat("91", "%", "火候完美率", "flame", 0xFFD94B4B)
        ),
        categoryDistribution = listOf(
            CategoryDist("家常菜", 42, 0xFFE8743B),
            CategoryDist("快手菜", 28, 0xFF8B5E3C),
            CategoryDist("汤煲", 18, 0xFF4A9D5B),
            CategoryDist("烘焙", 12, 0xFFD97B93)
        ),
        suggestions = listOf(
            SuggestionItem("增加蛋白质摄入", "建议在日常饮食中增加豆类、鸡蛋等高蛋白食材"),
            SuggestionItem("尝试更多汤煲", "推荐山药鸡汤，滋补养生，适合本周烹饪")
        )
    )

    val faqItems = listOf(
        FaqItem("如何使用AI食材识别？", "打开相机对准食材，AI会自动识别并推荐食谱。支持多食材同时识别，识别准确率高达95%以上。"),
        FaqItem("火候提醒准确吗？", "基于AI算法和传感器数据，准确率超过95%。系统会根据菜品类型和烹饪阶段智能调整提醒策略。"),
        FaqItem("如何收藏食谱？", "在食谱详情页点击右上角的收藏图标即可，收藏的食谱可在\"我的收藏\"中查看。"),
        FaqItem("支持哪些语言？", "目前支持中文、英语、日语、韩语等12种语言，可在设置中切换。"),
        FaqItem("如何导出烹饪记录？", "进入\"我的\"页面，点击烹饪记录，选择右上角导出按钮，支持PDF和Excel格式。")
    )

    val helpQuickActions = listOf(
        QuickAction("意见反馈", "提交建议", "message_square", 0xFFE8743B, 0xFFFBE8D6),
        QuickAction("联系客服", "在线咨询", "headset", 0xFF8B5E3C, 0xFFEDE3D6),
        QuickAction("使用教程", "视频指南", "play_circle", 0xFFB07D55, 0xFFF5EDE4)
    )
}

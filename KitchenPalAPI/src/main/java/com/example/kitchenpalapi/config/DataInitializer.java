package com.example.kitchenpalapi.config;

import com.example.kitchenpalapi.entity.*;
import com.example.kitchenpalapi.repository.*;
import com.example.kitchenpalapi.config.KnowledgeSeed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CookingHistoryRepository historyRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private KnowledgeRepository knowledgeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        initRecipes();
        initUser();
        initKnowledge();
    }

    private void initKnowledge() {
        if (knowledgeRepository.count() > 0) return;
        knowledgeRepository.saveAll(KnowledgeSeed.getDefaultEntries());
        System.out.println("=== 知识库数据初始化完成，共 " + knowledgeRepository.count() + " 条 ===");
    }

    private void initRecipes() throws JsonProcessingException {
        if (recipeRepository.count() > 0) return;

        List<Map<String, String>> ingredients1 = List.of(
                Map.of("name", "牛腩", "amount", "500g", "iconName", "beef"),
                Map.of("name", "番茄", "amount", "3个", "iconName", "tomato"),
                Map.of("name", "洋葱", "amount", "1个", "iconName", "onion"),
                Map.of("name", "大蒜", "amount", "5瓣", "iconName", "garlic"),
                Map.of("name", "生姜", "amount", "3片", "iconName", "leaf"),
                Map.of("name", "料酒", "amount", "2勺", "iconName", "flask"),
                Map.of("name", "盐", "amount", "适量", "iconName", "flask"),
                Map.of("name", "冰糖", "amount", "15g", "iconName", "flask")
        );

        List<Map<String, Object>> steps1 = List.of(
                step(1, "牛腩预处理", "牛腩切成 3cm 见方的块，冷水下锅，加 2 片姜和 1 勺料酒，大火煮沸后撇净浮沫，再煮 2 分钟捞出，用温水冲净沥干。", "大火", 300, "必须冷水下锅，血沫才能充分逼出；捞出后用温水冲洗，冷水会让肉质紧缩发柴。", "牛腩", "生姜", "料酒"),
                step(2, "番茄去皮切块", "番茄顶部划十字，放入开水中烫 30 秒，捞出过凉水后剥皮，一半切成小丁、一半切成滚刀块备用。", "无需明火", 120, "烫皮后马上过凉水，果皮一撕即落；留一半滚刀块，成品才有完整果肉口感。", "番茄"),
                step(3, "爆香底料", "炒锅放少量油，中火烧至五成热，下洋葱丝、姜片、蒜瓣炒出香味，约 1 分钟。", "中火", 90, "油温别太高，蒜瓣一旦变褐就会发苦，整锅汤都会带苦味。", "洋葱", "生姜", "大蒜"),
                step(4, "牛腩上色", "倒入沥干的牛腩块，中火翻炒 3 分钟至表面微黄，再下番茄丁继续炒 2 分钟，炒到番茄出沙、酱汁挂勺。", "中火", 300, "这一步是汤色红亮的关键，番茄一定要炒到「出沙」再加水。", "牛腩", "番茄"),
                step(5, "加水炖煮", "冲入没过食材 2cm 的热水，加冰糖和 1 勺生抽，大火烧开后转最小火，加盖炖 30 分钟。", "大火转小火", 1800, "务必加热水，冷水会让牛肉骤缩变硬；中途尽量不要开盖，热量流失会拖长时间。", "冰糖", "盐"),
                step(6, "收汁调味", "开盖转中火收汁约 4 分钟，汤汁剩三分之一时加盐调味，放入剩余滚刀块番茄翻匀，撒葱花出锅。", "中火", 240, "盐一定最后放，过早加盐会让牛肉久炖不烂。", "番茄", "盐", "葱")
        );

        Recipe r1 = createRecipe("番茄炖牛腩", "", "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=braised%20beef%20brisket%20with%20tomato%20in%20bowl%20chinese%20food&image_size=square",
                4.8f, 45, 380, "中等", "家常热菜", "推荐", 96, ingredients1, steps1, true);
        recipeRepository.save(r1);

        List<Map<String, String>> ingredients2 = List.of(
                Map.of("name", "猪里脊", "amount", "200g", "iconName", "beef"),
                Map.of("name", "青椒", "amount", "3个", "iconName", "leaf"),
                Map.of("name", "蒜", "amount", "2瓣", "iconName", "garlic")
        );
        List<Map<String, Object>> steps2 = List.of(
                step(1, "肉丝腌制", "猪里脊顺着纹路切成 3mm 粗的丝，加 1 勺生抽、1 勺淀粉和少许清水抓匀，封一层油，静置 10 分钟。", "无需火候", 600, "抓匀后封油能锁住水分，滑炒时才嫩；逆纹切丝口感更松软。", "猪里脊"),
                step(2, "青椒切配", "青椒去籽去筋，切成与肉丝等粗的丝；大蒜切末备用。", "无需火候", 180, "青椒筋最辣，去掉后口感更清爽；丝要切得均匀，受热才一致。", "青椒", "蒜"),
                step(3, "滑炒肉丝", "锅烧热后倒油，油温四成热时下肉丝，快速划散滑炒至变白，立刻盛出控油。", "中火", 120, "热锅凉油下肉丝不粘锅；变白就出锅，炒老就柴了。", "猪里脊"),
                step(4, "爆香炒制", "锅中留底油，下蒜末爆香，倒入青椒丝大火翻炒 1 分钟至断生，再倒回肉丝炒匀。", "大火", 120, "青椒要大火快炒，断生即停，久炒会发黄出水。", "青椒", "蒜"),
                step(5, "调味出锅", "沿锅边淋半勺生抽，加盐和少许糖提鲜，翻炒均匀后出锅。", "大火", 60, "沿锅边淋生抽能激出酱香；少许糖只提鲜吃不出甜味。", "盐")
        );
        Recipe r2 = createRecipe("青椒肉丝", "鲜香嫩滑，下饭首选", "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=stir%20fried%20shredded%20pork%20with%20green%20pepper%20chinese%20dish&image_size=square",
                4.7f, 15, 280, "简单", "家常菜", "快手,高蛋白", 85, ingredients2, steps2, false);
        recipeRepository.save(r2);

        List<Map<String, String>> ingredients3 = List.of(
                Map.of("name", "土鸡", "amount", "半只", "iconName", "beef"),
                Map.of("name", "山药", "amount", "200g", "iconName", "leaf"),
                Map.of("name", "枸杞", "amount", "10g", "iconName", "leaf"),
                Map.of("name", "红枣", "amount", "5颗", "iconName", "leaf"),
                Map.of("name", "姜片", "amount", "3片", "iconName", "leaf")
        );
        List<Map<String, Object>> steps3 = List.of(
                step(1, "鸡肉焯水", "土鸡斩块，冷水下锅，加姜片和 1 勺料酒，大火煮沸撇净浮沫，煮 3 分钟后捞出用温水洗净。", "大火", 300, "散养土鸡油脂少、腥味轻，焯水时间不用太长，避免鲜味流失。", "土鸡", "姜片"),
                step(2, "备料入锅", "山药去皮切滚刀块，泡在清水中防氧化；砂锅中加足量清水，放入鸡块和姜片。", "无需火候", 180, "山药削皮时戴手套，黏液接触皮肤会痒；水一次加够，中途加水会冲淡汤味。", "山药", "姜片"),
                step(3, "大火转小火炖", "大火烧开后撇去浮沫，转最小火加盖炖 50 分钟，保持汤面微微冒泡。", "大火转小火", 3000, "「大火烧开、小火慢炖」是汤清味浓的关键，全程不要大滚。", "土鸡"),
                step(4, "加山药同炖", "放入山药块继续小火炖 25 分钟，至山药能用筷子轻松扎透。", "小火", 1500, "山药后放，久炖会化在汤里；扎透即熟，别煮过头。", "山药"),
                step(5, "调味收尾", "出锅前 5 分钟放入枸杞和红枣，加盐调味，撇去表面浮油即可。", "小火", 300, "枸杞久煮会发酸，最后再放；盐也要最后加，早加会让鸡肉变紧。", "枸杞", "红枣", "盐")
        );
        Recipe r3 = createRecipe("山药药膳鸡汤", "滋补暖身，慢火细炖", "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20herbal%20chicken%20soup%20with%20yam%20in%20clay%20pot&image_size=square",
                4.9f, 90, 320, "中等", "汤煲", "养生,温补", 78, ingredients3, steps3, false);
        recipeRepository.save(r3);

        List<Map<String, String>> ingredients4 = List.of(
                Map.of("name", "番茄", "amount", "2个", "iconName", "tomato"),
                Map.of("name", "鸡蛋", "amount", "3个", "iconName", "egg"),
                Map.of("name", "葱", "amount", "适量", "iconName", "leaf")
        );
        List<Map<String, Object>> steps4 = List.of(
                step(1, "备料打蛋", "番茄去蒂切滚刀块；鸡蛋 3 个打散，加 2 勺清水和少许盐搅匀至起小泡。", "无需火候", 240, "蛋液加清水炒出来更蓬松嫩滑；想更香可滴几滴料酒去腥。", "番茄", "鸡蛋"),
                step(2, "滑炒鸡蛋", "热锅倒油，油温六成热倒入蛋液，待边缘凝固后从外向内推炒成大块，八成熟盛出。", "中火", 120, "油要够热蛋才蓬松；八成熟就出锅，回锅还要再炒一次。", "鸡蛋"),
                step(3, "炒出番茄汁", "锅中留底油，下番茄块中火翻炒 2 分钟，炒到番茄软塌出汁，加半勺糖和少许盐。", "中火", 150, "加一点糖能中和番茄的酸，味道更柔和；出汁后再回蛋，汤汁才挂得住。", "番茄"),
                step(4, "合炒出锅", "倒回鸡蛋块，大火快速翻炒 30 秒让蛋吸饱番茄汁，撒葱花出锅。", "大火", 90, "合炒时间要短，翻两下就出锅，鸡蛋才嫩。", "鸡蛋", "葱")
        );
        Recipe r4 = createRecipe("番茄炒蛋", "国民家常菜，酸甜开胃", "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=scrambled%20eggs%20with%20tomatoes%20chinese%20home%20style&image_size=square",
                4.7f, 10, 210, "简单", "家常菜", "素食,快手", 88, ingredients4, steps4, false);
        recipeRepository.save(r4);

        List<Map<String, String>> ingredients5 = List.of(
                Map.of("name", "西兰花", "amount", "300g", "iconName", "leaf"),
                Map.of("name", "大蒜", "amount", "4瓣", "iconName", "garlic"),
                Map.of("name", "胡萝卜", "amount", "半根", "iconName", "leaf")
        );
        List<Map<String, Object>> steps5 = List.of(
                step(1, "浸泡焯水", "西兰花切小朵，用淡盐水浸泡 5 分钟后冲洗干净；锅中水烧开加少许盐和油，下西兰花焯 60 秒捞出过凉水。", "大火", 360, "焯水加盐和油能保持翠绿；过凉水锁色，口感更脆。", "西兰花"),
                step(2, "切配备料", "大蒜切末，胡萝卜削皮切成薄片。", "无需火候", 180, "蒜末分两次下锅，一半爆香、一半出锅前撒，蒜香更足。", "大蒜", "胡萝卜"),
                step(3, "爆香蒜末", "热锅倒油，下蒜末小火煸至微黄出香。", "小火", 60, "蒜末小火慢煸才香，大火容易焦苦。", "大蒜"),
                step(4, "大火快炒", "转大火下胡萝卜片炒 30 秒，倒入西兰花，加盐和 1 勺蚝油快速翻炒均匀出锅。", "大火", 120, "焯过的西兰花已经半熟，回锅只需炒匀调味，久炒会变软发黄。", "西兰花", "胡萝卜", "盐")
        );
        Recipe r5 = createRecipe("蒜蓉西兰花", "清爽低脂，健康首选", "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=garlic%20broccoli%20stir%20fry%20healthy%20dish&image_size=square",
                4.6f, 12, 120, "简单", "家常菜", "低脂,健康", 80, ingredients5, steps5, false);
        recipeRepository.save(r5);

        List<Map<String, String>> ingredients6 = List.of(
                Map.of("name", "猪肋排", "amount", "500g", "iconName", "beef"),
                Map.of("name", "八角", "amount", "2个", "iconName", "flask"),
                Map.of("name", "桂皮", "amount", "1块", "iconName", "flask"),
                Map.of("name", "冰糖", "amount", "20g", "iconName", "flask")
        );
        List<Map<String, Object>> steps6 = List.of(
                step(1, "排骨焯水", "猪肋排剁成 5cm 段，冷水下锅，加姜片和料酒，大火煮沸撇沫，煮 3 分钟后捞出用温水冲净。", "大火", 360, "冷水下锅才能把血水逼出来；温水冲洗避免排骨遇冷收缩。", "猪肋排", "姜片"),
                step(2, "炒糖色", "锅中放少量油，下冰糖小火慢炒至融化变枣红色冒小泡，立刻倒入排骨翻炒裹匀。", "小火", 240, "糖色炒到枣红就要立刻下排骨，再炒几秒就会发苦。", "冰糖", "猪肋排"),
                step(3, "加料炖煮", "加八角、桂皮、姜片炒香，烹入生抽和老抽，冲入没过排骨的开水，大火烧开转小火加盖炖 40 分钟。", "大火转小火", 2400, "一定加开水，冷水会让肉变硬；水一次加够，中途别添水。", "八角", "桂皮"),
                step(4, "大火收汁", "开盖转大火收汁 5 分钟，汤汁浓稠裹住排骨即可关火装盘。", "大火", 300, "收汁时勤翻动防止糊底；汤汁挂勺就关火，收过头会发苦发咸。", "猪肋排")
        );
        Recipe r6 = createRecipe("红烧排骨", "酱香浓郁，骨肉分离", "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=braised%20pork%20ribs%20in%20brown%20sauce%20chinese&image_size=square",
                4.9f, 60, 480, "中等", "家常热菜", "下饭,聚会", 70, ingredients6, steps6, false);
        recipeRepository.save(r6);

        List<Map<String, String>> ingredients7 = List.of(
                Map.of("name", "番茄", "amount", "1个", "iconName", "tomato"),
                Map.of("name", "鸡蛋", "amount", "1个", "iconName", "egg"),
                Map.of("name", "葱", "amount", "适量", "iconName", "leaf")
        );
        List<Map<String, Object>> steps7 = List.of(
                step(1, "备料", "番茄去蒂切薄片；鸡蛋 1 个打散加少许盐；葱切葱花。", "无需火候", 120, "番茄切薄片更容易煮出汤汁，汤色更红亮。", "番茄", "鸡蛋"),
                step(2, "煮出番茄味", "锅中加 2 碗清水烧开，放入番茄片煮 3 分钟至软烂出汁。", "大火", 240, "先煮番茄把酸甜味煮进汤里，再淋蛋液味道才浓。", "番茄"),
                step(3, "淋蛋花", "保持汤面微沸，将蛋液沿筷子缓缓淋入，静置 5 秒再轻轻推动形成蛋花。", "小火", 90, "淋蛋液时汤不能大滚，否则蛋花会碎成渣；淋完别马上搅。", "鸡蛋"),
                step(4, "调味出锅", "加盐和几滴香油调味，撒葱花出锅。", "小火", 90, "香油最后点几滴即可，煮久了香气会散。", "盐", "葱")
        );
        Recipe r7 = createRecipe("番茄蛋汤", "鲜美暖胃，十分钟上桌", "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tomato%20egg%20drop%20soup%20chinese%20style&image_size=square",
                4.7f, 10, 95, "简单", "汤煲", "快手,清淡", 90, ingredients7, steps7, false);
        recipeRepository.save(r7);

        System.out.println("=== 食谱数据初始化完成 ===");
    }

    /** 构造一个「专业版」烹饪步骤：标题 + 详细操作 + 火候 + 时长 + 技巧 + 本步用料 */
    private Map<String, Object> step(int stepNumber, String title, String description, String heat,
                                     int durationSeconds, String tip, String... ingredients) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("stepNumber", stepNumber);
        m.put("title", title);
        m.put("description", description);
        m.put("heat", heat);
        m.put("durationSeconds", durationSeconds);
        m.put("tip", tip);
        m.put("ingredients", List.of(ingredients));
        return m;
    }

    private Recipe createRecipe(String title, String subtitle, String imageUrl,
                                 float rating, int cookTime, int calories, String difficulty,
                                 String category, String tags, int matchPercent,
                                 List<? extends Map<String, ?>> ingredients,
                                 List<? extends Map<String, ?>> steps, boolean isHero) throws JsonProcessingException {
        Recipe r = new Recipe();
        r.setTitle(title);
        r.setSubtitle(subtitle);
        r.setImageUrl(imageUrl);
        r.setRating(rating);
        r.setCookTimeMin(cookTime);
        r.setCalories(calories);
        r.setDifficulty(difficulty);
        r.setCategory(category);
        r.setTags(tags);
        r.setMatchPercent(matchPercent);
        r.setIngredientsJson(objectMapper.writeValueAsString(ingredients));
        r.setStepsJson(objectMapper.writeValueAsString(steps));
        r.setIsHero(isHero);
        return r;
    }

    private void initUser() {
        if (userRepository.existsByPhone("13800138000")) return;

        User user = new User();
        user.setPhone("13800138000");
        user.setPassword("123456");
        user.setNickname("美食探索家");
        user.setTastePreferences("清淡,咸鲜,素食");
        user.setCookingCount(128);
        user.setFavoriteCount(56);
        user.setStreakDays(12);
        user.setDarkMode(false);
        user.setCookingNotify(true);
        user = userRepository.save(user);

        List<Recipe> recipes = recipeRepository.findAll();
        if (!recipes.isEmpty()) {
            Favorite fav1 = new Favorite();
            fav1.setUserId(user.getId());
            fav1.setRecipeId(recipes.get(0).getId());
            favoriteRepository.save(fav1);

            Favorite fav2 = new Favorite();
            fav2.setUserId(user.getId());
            fav2.setRecipeId(recipes.get(1).getId());
            favoriteRepository.save(fav2);
        }

        String[] titles = {"炖牛腩火候提醒", "新食谱推荐", "烹饪完成记录", "收藏食谱更新", "每周烹饪报告已生成", "系统更新通知"};
        String[] types = {"cooking", "recipe", "cooking", "recipe", "system", "system"};
        Long[] relatedIds = {1L, 1L, null, 7L, null, null};
        String[] contents = {
                "已炖煮40分钟，建议检查火候并翻动牛腩",
                "根据您最近烹饪记录，推荐3道新菜谱",
                "您上次制作的番茄炒蛋已自动记录",
                "您收藏的\"红烧排骨\"新增了2个做法技巧",
                "点击查看您本周的烹饪数据和营养分析",
                "v2.5版本已发布，新增AI火候预测功能"
        };
        String[] icons = {"flame", "sparkles", "chef_hat", "heart", "sparkles", "bell"};
        boolean[] readFlags = {false, false, true, true, true, true};

        for (int i = 0; i < titles.length; i++) {
            Notification n = new Notification();
            n.setUserId(user.getId());
            n.setType(types[i]);
            n.setRelatedId(relatedIds[i]);
            n.setTitle(titles[i]);
            n.setContent(contents[i]);
            n.setIcon(icons[i]);
            n.setIsRead(readFlags[i]);
            notificationRepository.save(n);
        }

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("M月d日");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();

        CookingHistory h1 = new CookingHistory();
        h1.setUserId(user.getId());
        h1.setRecipeId(recipes.get(0).getId());
        h1.setRecipeTitle("番茄炖牛腩");
        h1.setRecipeImageUrl(recipes.get(0).getImageUrl());
        h1.setCookDate("今天");
        h1.setCookTime("12:30");
        h1.setDurationMin(45);
        h1.setScore(96);
        h1.setRating(5);
        historyRepository.save(h1);

        CookingHistory h2 = new CookingHistory();
        h2.setUserId(user.getId());
        h2.setRecipeId(recipes.get(4).getId());
        h2.setRecipeTitle("蒜蓉西兰花");
        h2.setRecipeImageUrl(recipes.get(4).getImageUrl());
        h2.setCookDate("今天");
        h2.setCookTime("18:45");
        h2.setDurationMin(12);
        h2.setScore(92);
        h2.setRating(4);
        historyRepository.save(h2);

        System.out.println("=== 用户数据初始化完成 ===");
        System.out.println("测试账号: 13800138000 / 123456");
    }
}

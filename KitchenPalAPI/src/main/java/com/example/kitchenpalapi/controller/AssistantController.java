package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.KnowledgeEntry;
import com.example.kitchenpalapi.service.KnowledgeService;
import com.example.kitchenpalapi.service.LlmClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
@CrossOrigin(origins = "*")
public class AssistantController {

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private LlmClient llmClient;

    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ApiResponse.error("请输入问题");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> history = (List<Map<String, Object>>) body.get("history");
        @SuppressWarnings("unchecked")
        Map<String, Object> userContext = (Map<String, Object>) body.get("userContext");

        try {
            // 知识库召回：所有问答默认带上相关片段，使回答有依据
            List<KnowledgeEntry> kb = knowledgeService.retrieve(message, 4);

            ArrayNode messages = llmClient.messages();
            messages.add(llmClient.text("system", buildSystemPrompt(userContext, kb)));

            if (history != null) {
                int startIndex = Math.max(0, history.size() - 10);
                for (int i = startIndex; i < history.size(); i++) {
                    Map<String, Object> h = history.get(i);
                    Object contentObj = h.get("content");
                    if (contentObj != null) {
                        messages.add(llmClient.text(String.valueOf(h.getOrDefault("role", "user")), contentObj.toString()));
                    }
                }
            }
            messages.add(llmClient.text("user", message));

            String content = llmClient.chat(messages, 1500, 0.7);
            if (content != null && !content.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("reply", content);
                result.put("intent", detectIntent(message, content));
                if (!kb.isEmpty()) {
                    List<Map<String, String>> sources = new ArrayList<>();
                    for (KnowledgeEntry e : kb) {
                        Map<String, String> s = new HashMap<>();
                        s.put("title", e.getTitle());
                        s.put("category", e.getCategory());
                        sources.add(s);
                    }
                    result.put("sources", sources);
                }
                return ApiResponse.success("回答成功", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fallbackReply = getSmartFallbackReply(message);
        Map<String, Object> result = new HashMap<>();
        result.put("reply", fallbackReply);
        result.put("intent", detectIntent(message, fallbackReply));
        return ApiResponse.success("回答成功", result);
    }

    @PostMapping("/ask")
    public ApiResponse<Map<String, Object>> ask(@RequestBody Map<String, Object> body) {
        String question = (String) body.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ApiResponse.error("请输入问题");
        }
        try {
            Map<String, Object> result = knowledgeService.askWithKnowledge(question);
            return ApiResponse.success("回答成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("answer", "抱歉，知识库暂时无法访问，请稍后再试。");
            fallback.put("sources", List.of());
            fallback.put("grounded", false);
            return ApiResponse.success("回答成功", fallback);
        }
    }

    private String buildSystemPrompt(Map<String, Object> userContext, List<KnowledgeEntry> kb) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个智能厨房助手，名叫小厨。");
        sb.append("你的职责是真正帮用户解决厨房问题，而不是泛泛而谈。\n\n");
        sb.append("【工作原则】\n");
        sb.append("1. 仔细理解用户的真实需求，给出可执行的答案（具体步骤、具体食材、具体时间）。\n");
        sb.append("2. 回答要简洁、结构化、实用。必要时使用列表或步骤编号。\n");
        sb.append("3. 根据用户提供的口味偏好、过敏信息、厨艺水平个性化回答。\n");
        sb.append("4. 当用户问\"能做什么\"时，主动询问或基于已有信息给出可执行方案。\n");
        sb.append("5. 当不确定时，承认不知道并引导用户提供更多信息。\n\n");

        if (kb != null && !kb.isEmpty()) {
            sb.append("【知识库参考】（请优先依据以下内容作答，不要与它冲突）\n");
            for (KnowledgeEntry e : kb) {
                sb.append("- [").append(e.getCategory()).append("] ").append(e.getContent()).append("\n");
            }
            sb.append("\n");
        }

        if (userContext != null && !userContext.isEmpty()) {
            sb.append("【用户信息】\n");
            Object nickname = userContext.get("nickname");
            if (nickname != null && !nickname.toString().isEmpty()) {
                sb.append("- 称呼：").append(nickname).append("\n");
            }
            Object tastes = userContext.get("tastePreferences");
            if (tastes != null && !tastes.toString().isEmpty()) {
                sb.append("- 口味偏好：").append(tastes).append("\n");
            }
            Object allergens = userContext.get("allergens");
            if (allergens != null && !allergens.toString().isEmpty()) {
                sb.append("- 过敏原：").append(allergens).append("\n");
            }
            Object skill = userContext.get("skillLevel");
            if (skill != null && !skill.toString().isEmpty()) {
                sb.append("- 厨艺水平：").append(skill).append("\n");
            }
            Object people = userContext.get("servings");
            if (people != null && !people.toString().isEmpty()) {
                sb.append("- 用餐人数：").append(people).append("人\n");
            }
            Object time = userContext.get("availableTime");
            if (time != null && !time.toString().isEmpty()) {
                sb.append("- 可用时间：").append(time).append("\n");
            }
            Object scenarios = userContext.get("scenarios");
            if (scenarios != null && !scenarios.toString().isEmpty()) {
                sb.append("- 使用场景：").append(scenarios).append("\n");
            }
            Object favorites = userContext.get("favoriteCategories");
            if (favorites != null && !favorites.toString().isEmpty()) {
                sb.append("- 常做菜系：").append(favorites).append("\n");
            }
            sb.append("\n");
        }

        sb.append("【回复风格】\n");
        sb.append("- 像朋友一样自然，称呼用户（如果有称呼）。\n");
        sb.append("- 不堆砌套话，直接给出答案。\n");
        sb.append("- 涉及具体操作时，给出用量、火候、时间等参数。\n");
        sb.append("- 涉及安全/过敏时主动提醒。\n");
        sb.append("\n【输出要求】直接给出回答，不要输出思考过程或解释性文字。/no_think\n");
        return sb.toString();
    }

    private String detectIntent(String userMessage, String assistantReply) {
        if (userMessage == null) return "chat";
        String lower = userMessage.toLowerCase();
        if (lower.contains("生成食谱") || lower.contains("帮我写个食谱") || lower.contains("做个菜")
                || lower.contains("做个菜") || (lower.contains("能做什么") && userMessage.contains("食材"))
                || (lower.contains("怎么做") && userMessage.length() > 4 && !userMessage.startsWith("这道菜"))) {
            return "recipe";
        }
        if (lower.contains("分析") || lower.contains("营养") || lower.contains("卡路里")
                || lower.contains("热量") || lower.contains("搭配")) {
            return "analysis";
        }
        if (lower.contains("技巧") || lower.contains("小贴士") || lower.contains("注意事项")
                || lower.contains("怎么做好吃") || lower.contains("怎么做得")) {
            return "tips";
        }
        return "chat";
    }

    /**
     * 宽松地把请求体字段转成字符串。
     * ingredients 既可能是 "鸡蛋,番茄" 字符串，也可能是 ["鸡蛋","番茄"] 数组，
     * 之前直接强转 (String) 会在数组入参时抛 ClassCastException 导致 500。
     */
    private String asText(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s;
        if (value instanceof java.util.Collection<?> c) {
            StringBuilder sb = new StringBuilder();
            for (Object o : c) {
                if (o == null) continue;
                String part = String.valueOf(o).trim();
                if (part.isEmpty()) continue;
                if (sb.length() > 0) sb.append(",");
                sb.append(part);
            }
            return sb.toString();
        }
        return String.valueOf(value);
    }

    @PostMapping("/generate-recipe")
    public ApiResponse<Map<String, Object>> generateRecipe(@RequestBody Map<String, Object> body) {
        String ingredients = asText(body.get("ingredients"));
        String style = asText(body.get("style"));
        String dietary = asText(body.get("dietary"));

        try {
            StringBuilder sys = new StringBuilder();
            sys.append("你是一个专业的美食创作助手。请生成真正能照做的食谱：\n");
            sys.append("① 每一步都要写清「火候/油温 + 具体用时（如中火2分钟、约40秒）+ 具体用量（如盐2克/半茶匙、食用油15毫升）」，禁止出现\"适量\"\"少许\"\"翻炒均匀\"等空话；\n");
            sys.append("② 步骤必须按操作顺序、可独立执行、可量化；\n");
            sys.append("③ 用量优先用克/毫升/茶匙等可量化的单位，时间用分钟或秒。\n");
            // 知识库接地：用相关烹饪知识约束生成，避免常识错误
            try {
                List<KnowledgeEntry> genKb = knowledgeService.retrieve(
                        (ingredients != null ? ingredients : "") + " " + (style != null ? style : ""), 4);
                if (genKb != null && !genKb.isEmpty()) {
                    sys.append("\n【可参考的烹饪知识库】（据此校正火候/时间/安全要点，不要与之冲突）：\n");
                    for (KnowledgeEntry e : genKb) {
                        sys.append("- [").append(e.getCategory()).append("] ").append(e.getContent()).append("\n");
                    }
                }
            } catch (Exception ignored) {}

            StringBuilder userContent = new StringBuilder();
            userContent.append("请根据以下信息生成一道完整的食谱：\n");
            if (ingredients != null && !ingredients.isEmpty()) {
                userContent.append("可用食材：").append(ingredients).append("\n");
            }
            if (style != null && !style.isEmpty()) {
                userContent.append("烹饪风格：").append(style).append("\n");
            }
            if (dietary != null && !dietary.isEmpty()) {
                userContent.append("饮食限制：").append(dietary).append("\n");
            }
            userContent.append("\n请以JSON格式返回，包含以下字段：\n");
            userContent.append("- name: 食谱名称\n");
            userContent.append("- description: 食谱描述\n");
            userContent.append("- prepTime: 准备时间\n");
            userContent.append("- cookTime: 烹饪时间\n");
            userContent.append("- servings: 份量\n");
            userContent.append("- difficulty: 难度（简单/中等/困难）\n");
            userContent.append("- calories: 卡路里\n");
            userContent.append("- ingredients: 数组，每个元素包含 name(食材名), quantity(用量，需具体如\"鸡蛋3个\"\"盐2克\")\n");
            userContent.append("- steps: 数组，每个元素是一个字符串，表示一步完整操作。每步必须同时包含：动作 + 火候/油温 + 具体用时 + 具体用量。\n");
            userContent.append("  示例：\"中火加热炒锅，倒入食用油15毫升，约10秒至六成热，倒入打散的鸡蛋液，用铲子快速划散约40秒至刚凝固，盛出备用。\"\n");
            userContent.append("- tips: 烹饪小贴士\n");
            userContent.append("\n【输出要求】直接输出符合上述结构的 JSON，不要包含 Markdown 代码块标记、不要包含解释性文字、不要输出思考过程。/no_think\n");

            ArrayNode messages = llmClient.messages();
            messages.add(llmClient.text("system", sys.toString()));
            messages.add(llmClient.text("user", userContent.toString()));

            String content = llmClient.chat(messages, 4000, 0.6);
            if (content != null) {
                JsonNode recipeJson = extractJsonObject(content);
                Map<String, Object> result = new HashMap<>();
                if (recipeJson != null) {
                    result.put("recipe", normalizeRecipe(recipeJson));
                } else {
                    result.put("recipe", null);
                    result.put("rawContent", content);
                }
                return ApiResponse.success("食谱生成成功", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("recipe", getFallbackRecipe(ingredients));
        return ApiResponse.success("食谱生成成功", result);
    }

    @PostMapping("/analyze-ingredients")
    public ApiResponse<Map<String, Object>> analyzeIngredients(@RequestBody Map<String, Object> body) {
        String ingredients = asText(body.get("ingredients"));
        
        try {
            ArrayNode messages = llmClient.messages();
            messages.add(llmClient.text("system", "你是一个专业的食材分析助手，请分析用户提供的食材组合并给出建议。"));
            messages.add(llmClient.text("user", "请分析以下食材：" + ingredients + "\n\n请以JSON格式返回，且每个字段的值都必须是【一段中文字符串】，不要使用嵌套对象或数组：\n" +
                    "- nutrition: 营养分析（字符串）\n" +
                    "- flavor: 风味特点（字符串）\n" +
                    "- pairing: 推荐搭配（字符串）\n" +
                    "- storage: 储存建议（字符串）\n" +
                    "示例：{\"nutrition\":\"番茄富含维生素C，鸡蛋提供优质蛋白\",\"flavor\":\"酸甜鲜香\",\"pairing\":\"适合搭配米饭或面条\",\"storage\":\"冷藏保存2天\"}\n" +
                    "直接输出JSON，不要包含Markdown代码块标记。/no_think"));

            String content = llmClient.chat(messages, 1500, 0.4);
            if (content != null) {
                JsonNode analysis = extractJsonObject(content);
                Map<String, Object> result = new HashMap<>();
                if (analysis != null) {
                    result.put("analysis", normalizeAnalysis(analysis));
                } else {
                    result.put("analysis", null);
                    result.put("rawContent", content);
                }
                return ApiResponse.success("分析成功", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("analysis", getFallbackAnalysis(ingredients));
        return ApiResponse.success("分析成功", result);
    }

    @PostMapping("/cooking-tips")
    public ApiResponse<Map<String, Object>> cookingTips(@RequestBody Map<String, Object> body) {
        String dishName = (String) body.get("dishName");

        try {
            ArrayNode messages = llmClient.messages();
            messages.add(llmClient.text("system", "你是一个专业的烹饪技巧指导专家，请提供实用的烹饪建议。"));
            messages.add(llmClient.text("user", "请提供制作「" + dishName + "」的烹饪技巧和注意事项。"));

            String content = llmClient.chat(messages, 1000, 0.5);
            if (content != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("tips", content);
                return ApiResponse.success("获取成功", result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tips", getFallbackTips(dishName));
        return ApiResponse.success("获取成功", result);
    }

    private String getSmartFallbackReply(String message) {
        if (message == null) {
            return "你好！我是小厨，可以帮你生成食谱、分析食材、解答烹饪问题。告诉我你想做什么吧～";
        }
        String lower = message.toLowerCase();
        if (lower.contains("能做什么") || lower.contains("做什么菜") || lower.contains("做什么好")) {
            return "你可以告诉我手边现有的食材（比如\"我家有鸡蛋、番茄、葱\"），或者告诉我你的口味和场景（比如\"晚餐想做清淡的\"），我立刻给你具体方案。";
        }
        if (lower.contains("怎么做") || lower.contains("做法") || lower.contains("步骤")) {
            return "请告诉我你想做哪道菜，比如\"番茄炒蛋\"、\"红烧肉\"，我会给你具体的步骤、用量、火候和时间。";
        }
        if (lower.contains("推荐") || lower.contains("食谱")) {
            return "好的！告诉我：①你手边有哪些食材；②几个人吃；③大概多少时间。我给你定制食谱。";
        }
        if (lower.contains("营养") || lower.contains("卡路里") || lower.contains("热量")) {
            return "请列出你想分析的食材（用逗号分隔），比如\"鸡胸肉,西兰花,糙米\"，我会从营养、风味、搭配、储存四个维度给你分析。";
        }
        if (lower.contains("技巧") || lower.contains("小贴士")) {
            return "告诉我菜名，比如\"红烧肉\"、\"酸辣土豆丝\"，我给你实用的烹饪技巧和避坑要点。";
        }
        if (lower.contains("保存") || lower.contains("储存") || lower.contains("保鲜")) {
            return "请告诉我具体食材和保存环境（常温/冷藏/冷冻），以及大概保存多久，我给你具体方案。";
        }
        return "你好！我是小厨，可以帮你：\n• 食材能做什么菜\n• 食谱步骤详解\n• 营养搭配分析\n• 烹饪技巧答疑\n\n请直接描述你的情况～";
    }

    private String getFallbackReply(String message) {
        return getSmartFallbackReply(message);
    }

    private Map<String, Object> getFallbackRecipe(String ingredients) {
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("name", ingredients != null && !ingredients.isEmpty() ? ingredients + "创意料理" : "美味家常菜");
        recipe.put("description", "一道用新鲜食材精心制作的美味佳肴");
        recipe.put("prepTime", "15分钟");
        recipe.put("cookTime", "30分钟");
        recipe.put("servings", "2人份");
        recipe.put("difficulty", "中等");
        recipe.put("calories", "350");
        
        List<Map<String, String>> ingredientList = new ArrayList<>();
        Map<String, String> ing1 = new HashMap<>();
        ing1.put("name", "主要食材");
        ing1.put("quantity", "适量");
        ingredientList.add(ing1);
        recipe.put("ingredients", ingredientList);
        
        List<String> steps = new ArrayList<>();
        steps.add("将食材洗净切好");
        steps.add("热锅冷油，爆香调料");
        steps.add("加入主料翻炒");
        steps.add("调味后出锅装盘");
        recipe.put("steps", steps);
        
        recipe.put("tips", "烹饪时注意火候，保持食材鲜嫩多汁");
        
        return recipe;
    }

    private Map<String, Object> getFallbackAnalysis(String ingredients) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("nutrition", "这些食材富含维生素和矿物质，营养均衡。");
        analysis.put("flavor", "口感丰富，味道鲜美。");
        analysis.put("pairing", "建议搭配适量蛋白质和主食。");
        analysis.put("storage", "建议冷藏保存，尽快食用。");
        return analysis;
    }

    private String getFallbackTips(String dishName) {
        return "制作「" + (dishName != null ? dishName : "这道菜") + "」的一些小贴士：\n" +
                "1. 食材提前准备好可以节省烹饪时间\n" +
                "2. 注意火候控制，避免烧焦\n" +
                "3. 调味时可以少量多次添加\n" +
                "4. 烹饪完成后静置几分钟让味道更融合";
    }

    private JsonNode extractJsonObject(String content) {
        if (content == null) return null;
        String c = content.trim();
        if (c.startsWith("```")) {
            int firstNewline = c.indexOf('\n');
            int lastFence = c.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                c = c.substring(firstNewline + 1, lastFence).trim();
            }
        }
        int s = c.indexOf('{');
        int e = c.lastIndexOf('}');
        if (s >= 0 && e > s) {
            return llmClient.readTree(c.substring(s, e + 1));
        }
        return null;
    }

    private Map<String, Object> normalizeRecipe(JsonNode node) {
        Map<String, Object> r = new HashMap<>();
        r.put("name", text(node, "name", "智能菜谱"));
        r.put("description", text(node, "description", ""));
        r.put("prepTime", text(node, "prepTime", ""));
        r.put("cookTime", text(node, "cookTime", ""));
        r.put("servings", text(node, "servings", ""));
        r.put("difficulty", text(node, "difficulty", ""));
        r.put("calories", text(node, "calories", ""));

        List<Map<String, String>> ings = new ArrayList<>();
        JsonNode ingsNode = node.get("ingredients");
        if (ingsNode != null && ingsNode.isArray()) {
            for (JsonNode it : ingsNode) {
                Map<String, String> m = new HashMap<>();
                if (it.isTextual()) {
                    m.put("name", it.asText());
                    m.put("quantity", "");
                } else {
                    m.put("name", text(it, "name", ""));
                    m.put("quantity", text(it, "quantity", ""));
                }
                ings.add(m);
            }
        }
        r.put("ingredients", ings);

        List<String> steps = new ArrayList<>();
        JsonNode stepsNode = node.get("steps");
        if (stepsNode != null && stepsNode.isArray()) {
            for (JsonNode it : stepsNode) {
                if (it.isTextual()) {
                    steps.add(it.asText());
                } else if (it.isObject()) {
                    String s = text(it, "instructions", null);
                    if (s == null) s = text(it, "step", null);
                    if (s == null) s = text(it, "text", null);
                    if (s == null) s = text(it, "description", null);
                    if (s == null) s = it.toString();
                    steps.add(s);
                } else {
                    steps.add(it.asText());
                }
            }
        }
        r.put("steps", steps);
        r.put("tips", text(node, "tips", ""));
        return r;
    }

    private Map<String, Object> normalizeAnalysis(JsonNode node) {
        Map<String, Object> a = new HashMap<>();
        a.put("nutrition", text(node, "nutrition", ""));
        a.put("flavor", text(node, "flavor", ""));
        a.put("pairing", text(node, "pairing", ""));
        a.put("storage", text(node, "storage", ""));
        return a;
    }

    private String text(JsonNode node, String field, String def) {
        if (node == null) return def;
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return def;
        if (v.isValueNode()) return v.asText();
        // 模型有时返回嵌套对象/数组，App 端字段是 String，需扁平化为可读文本
        return flatten(v);
    }

    /** 把任意 JsonNode 扁平化为可读字符串（对象：key：value，数组：用；连接）。 */
    private String flatten(JsonNode node) {
        if (node == null || node.isNull()) return "";
        if (node.isValueNode()) return node.asText();
        List<String> parts = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode it : node) parts.add(flatten(it));
            return String.join("；", parts);
        }
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> e = it.next();
            parts.add(e.getKey() + "：" + flatten(e.getValue()));
        }
        return String.join("，", parts);
    }
}

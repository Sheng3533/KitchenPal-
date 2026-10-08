package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.dto.ScanResult;
import com.example.kitchenpalapi.entity.Recipe;
import com.example.kitchenpalapi.service.QwenService;
import com.example.kitchenpalapi.service.RecipeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/scan")
@CrossOrigin(origins = "*")
public class ScanController {

    private static final Logger log = LoggerFactory.getLogger(ScanController.class);

    /** 送模型前把图片压到最大边 768px：CPU 推理耗时约为原图的 1/2.5，识别质量实测不变。 */
    private static final int VISION_MAX_DIM = 768;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private QwenService qwenService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/recognize")
    public ApiResponse<ScanResult> recognize(@RequestParam("image") MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return ApiResponse.error("请上传食材照片");
        }

        // 识别失败时明确报错（App 会提示重试），不再用写死的示例食材冒充「识别成功」
        List<ScanResult.DetectedIngredient> ingredients = recognizeWithAI(image);
        if (ingredients == null || ingredients.isEmpty()) {
            return ApiResponse.error("识别失败，请换一张更清晰的照片重试");
        }

        ScanResult result = new ScanResult();
        result.setIngredients(ingredients);

        List<String> ingredientNames = ingredients.stream()
                .map(ScanResult.DetectedIngredient::getName)
                .toList();
        List<Long> recipeIds = findMatchingRecipes(ingredientNames);
        result.setSuggestedRecipeIds(recipeIds);

        return ApiResponse.success("识别成功", result);
    }

    /** 识别成功返回食材列表；模型不可用 / 超时 / 返回无法解析时返回 null（由上层报错）。 */
    private List<ScanResult.DetectedIngredient> recognizeWithAI(MultipartFile image) {
        try {
            byte[] bytes = downscale(image.getBytes());
            String base64Image = Base64.getEncoder().encodeToString(bytes);
            String aiResponse = qwenService.recognizeImage(base64Image);

            if (aiResponse != null && !aiResponse.isBlank()) {
                // ① 宽容 JSON 解析：小模型常把 JSON 包在 ```json 围栏里，裸 readTree 会直接失败
                JsonNode root = parseJsonLoose(aiResponse);
                if (root != null) {
                    JsonNode ingredientsNode = root.get("ingredients");
                    if (ingredientsNode != null && ingredientsNode.isArray()) {
                        List<ScanResult.DetectedIngredient> ingredients = new ArrayList<>();
                        for (JsonNode node : ingredientsNode) {
                            ScanResult.DetectedIngredient ingredient = new ScanResult.DetectedIngredient();
                            ingredient.setName(node.has("name") ? node.get("name").asText() : "未知食材");
                            ingredient.setConfidence(node.has("confidence") ? node.get("confidence").asInt(80) : 80);
                            ingredient.setWeight(node.has("weight") ? node.get("weight").asText() : "未知重量");
                            ingredient.setImageUrl("");
                            ingredients.add(ingredient);
                        }
                        if (!ingredients.isEmpty()) {
                            return ingredients;
                        }
                    }
                }
                // ② 再退一步：模型有时直接回「山药、红枣、枸杞」这种纯文本
                List<ScanResult.DetectedIngredient> plain = parsePlainText(aiResponse);
                if (!plain.isEmpty()) {
                    return plain;
                }
                log.warn("AI 识别返回无法解析。原始返回前 200 字: {}",
                        aiResponse.length() > 200 ? aiResponse.substring(0, 200) : aiResponse);
            } else {
                log.warn("AI 识别无返回（模型未配置 / 超时 / 服务不可用）");
            }
        } catch (Exception e) {
            log.warn("AI 识别调用失败: {}", e.toString());
        }
        return null;
    }

    /**
     * 把图片等比缩放到最大边 {@link #VISION_MAX_DIM}px 的 JPEG。
     * CPU 推理耗时与像素数近似正相关，缩图可把单次识别从 ~70s 压到 ~30s；
     * 读不出的格式（如部分 HEIC）或缩放失败时原样返回，保证功能不倒退。
     */
    private byte[] downscale(byte[] original) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(original));
            if (src == null) return original;
            int w = src.getWidth();
            int h = src.getHeight();
            int longer = Math.max(w, h);
            if (longer <= VISION_MAX_DIM) return original;

            double scale = (double) VISION_MAX_DIM / longer;
            int nw = Math.max(1, (int) Math.round(w * scale));
            int nh = Math.max(1, (int) Math.round(h * scale));

            BufferedImage dst = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, nw, nh, null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(dst, "jpg", out);
            byte[] shrunk = out.toByteArray();
            if (shrunk.length > 0) {
                log.info("扫描图片降采样 {}x{} -> {}x{} ({}KB -> {}KB)",
                        w, h, nw, nh, original.length / 1024, shrunk.length / 1024);
                return shrunk;
            }
        } catch (Exception e) {
            log.warn("图片降采样失败，改用原图: {}", e.getMessage());
        }
        return original;
    }

    /** 宽容解析：先剥掉 ```json 代码块围栏，再退化为截取最外层大括号；仍失败返回 null。 */
    private JsonNode parseJsonLoose(String text) {
        if (text == null) return null;
        String s = text.trim();
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            if (nl > 0) s = s.substring(nl + 1);
            int fence = s.lastIndexOf("```");
            if (fence >= 0) s = s.substring(0, fence);
            s = s.trim();
        }
        try {
            return objectMapper.readTree(s);
        } catch (Exception ignored) {
            // 继续退化处理
        }
        int a = s.indexOf('{');
        int b = s.lastIndexOf('}');
        if (a >= 0 && b > a) {
            try {
                return objectMapper.readTree(s.substring(a, b + 1));
            } catch (Exception ignored) {
                // 返回 null，交给上层走纯文本/兜底
            }
        }
        return null;
    }

    /** 纯文本降级解析：模型直接回「山药、红枣、枸杞」时按分隔符拆词；不足 2 个词视为无效。 */
    private List<ScanResult.DetectedIngredient> parsePlainText(String text) {
        List<ScanResult.DetectedIngredient> list = new ArrayList<>();
        if (text == null) return list;
        String firstLine = text.trim().split("\\r?\\n")[0];
        for (String p : firstLine.split("[、,，;；/|]+")) {
            String name = p.replaceAll("[\\s\"'`*\\[\\]{}:：]", "").trim();
            if (name.length() >= 1 && name.length() <= 8
                    && !name.matches(".*(图|以下|食材|识别|结果|名称|列表|包含|分析).*")) {
                ScanResult.DetectedIngredient ingredient = new ScanResult.DetectedIngredient();
                ingredient.setName(name);
                ingredient.setConfidence(85);
                ingredient.setWeight("未知重量");
                ingredient.setImageUrl("");
                list.add(ingredient);
            }
        }
        return list.size() >= 2 ? list : new ArrayList<>();
    }

    private List<Long> findMatchingRecipes(List<String> ingredientNames) {
        List<Recipe> allRecipes = recipeService.getAllRecipes();
        List<Long> recipeIds = new ArrayList<>();

        for (Recipe r : allRecipes) {
            for (String name : ingredientNames) {
                if (r.getTitle().contains(name) || r.getIngredientsJson().contains(name)) {
                    if (!recipeIds.contains(r.getId())) {
                        recipeIds.add(r.getId());
                        if (recipeIds.size() >= 3) break;
                    }
                }
            }
            if (recipeIds.size() >= 3) break;
        }

        if (recipeIds.isEmpty() && !allRecipes.isEmpty()) {
            recipeIds.add(allRecipes.get(0).getId());
        }

        return recipeIds;
    }
}

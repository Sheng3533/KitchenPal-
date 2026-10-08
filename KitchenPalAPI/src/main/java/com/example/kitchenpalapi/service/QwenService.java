package com.example.kitchenpalapi.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 视觉识别 + 食谱推荐。
 *
 * <p>LLM 调用统一收敛到 {@link LlmClient}；这里只保留「视觉识别需要独立超时」这一特有逻辑：
 * 纯文本模型遇到图片请求会长时间挂起，因此视觉调用走单独的 RestTemplate（连接 10s / 读 300s）。
 *
 * <p>读超时给到 300s 的原因：本机 CPU 跑 2B 视觉模型，一张 768px 菜图约需 30s，
 * 而早期 90s 的上限太小——图片稍大就会超时，被上层误判为「识别失败」并返回示例数据。
 */
@Service
public class QwenService {

    /** 视觉识别专用：给一个明确上限，避免纯文本模型遇到图片请求时长时间挂起。 */
    private final RestTemplate visionRestTemplate;
    private final String visionModel;
    private final LlmClient llmClient;

    @Autowired
    public QwenService(String llmVisionModel, LlmClient llmClient) {
        this.visionModel = llmVisionModel;
        this.llmClient = llmClient;

        SimpleClientHttpRequestFactory visionFactory = new SimpleClientHttpRequestFactory();
        visionFactory.setConnectTimeout(10000);
        visionFactory.setReadTimeout(300000);
        this.visionRestTemplate = new RestTemplate(visionFactory);
    }

    public String recognizeImage(String base64Image) {
        // 未配置视觉模型时，用纯文本模型识别图片必然失败且响应极慢，
        // 直接返回 null 让上层走兜底食材，避免用户干等 1~2 分钟。
        if (visionModel == null || visionModel.trim().isEmpty()) {
            return null;
        }

        ArrayNode messages = llmClient.messages();

        // system prompt 限定输出格式
        messages.add(llmClient.text("system",
                "你是一个专业的食材识别助手。请分析用户上传的图片，识别图片中所有可见食材。\n" +
                "必须按以下 JSON 格式返回，不要包含任何解释：\n" +
                "{\n" +
                "  \"ingredients\": [\n" +
                "    {\"name\": \"食材名称\", \"confidence\": 0-100, \"weight\": \"预估重量\"}\n" +
                "  ],\n" +
                "  \"suggestedRecipes\": [\"食谱名1\", \"食谱名2\"]\n" +
                "}\n" +
                "示例：{\"ingredients\":[{\"name\":\"番茄\",\"confidence\":98,\"weight\":\"约200g\"}],\"suggestedRecipes\":[\"番茄炒蛋\",\"番茄牛腩\"]}"));

        // Ollama 原生 /api/chat 的多模态格式：content 是普通文本字符串，
        // 图片放在同一条消息的 images 数组里（纯 base64，不带 data:image/...;base64, 前缀）。
        // 注意：不要用 OpenAI 的 content:[{type:"image_url"}] 数组格式——Ollama 原生端点不认，图片会被丢弃。
        ObjectNode userMsg = llmClient.text("user", "请识别这张图片中的食材，并返回 JSON 结果。");
        ArrayNode images = llmClient.newArray();
        images.add(stripDataUrlPrefix(base64Image));
        userMsg.set("images", images);
        messages.add(userMsg);

        return llmClient.chat(visionModel.trim(), messages, 1000, 0.1, visionRestTemplate);
    }

    /** 兼容传入 data:image/jpeg;base64,xxx 形式：Ollama 只接受逗号之后的纯 base64。 */
    private String stripDataUrlPrefix(String base64) {
        if (base64 == null) return "";
        int idx = base64.indexOf("base64,");
        return idx >= 0 ? base64.substring(idx + 7) : base64.trim();
    }

    public String recommendRecipes(List<String> ingredients) {
        ArrayNode messages = llmClient.messages();
        messages.add(llmClient.text("user", "你是一个专业的美食推荐助手。"));
        messages.add(llmClient.text("user", "已识别的食材：" + String.join("、", ingredients)));
        messages.add(llmClient.text("user", "请基于这些食材推荐3-5道食谱。请以JSON格式返回：\n" +
                "- recipes: 数组，每个元素包含 name(食谱名称), matchScore(匹配度0-100), reason(推荐理由)\n" +
                "示例：{\"recipes\":[{\"name\":\"番茄炒蛋\",\"matchScore\":95,\"reason\":\"使用番茄和鸡蛋制作\"}]}"));

        return llmClient.chat(messages, 1000, 0.3);
    }
}

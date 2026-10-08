package com.example.kitchenpalapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 统一的 Ollama 原生对话客户端。
 *
 * <p>改造背景：项目原先在 8 处（AssistantController×4、QwenService×2、KnowledgeService×1、
 * ChefController×1）各自手写同一段「拼 {@code /api/chat} 请求体 → 设 {@code think:false} →
 * POST → 取 {@code root.message.content}」的样板代码，一处要改就得改 8 遍，异常处理与日志
 * 也各不相同。这里收敛为单一入口，后续调整 LLM 参数/端点只需改这一个文件。
 *
 * <p>约定（与改造前保持完全一致，避免行为变化）：
 * <ul>
 *   <li>走 Ollama 原生端点 {@code {base-url 去掉 /v1}/api/chat}；</li>
 *   <li>请求体固定带 {@code think:false} / {@code stream:false}，防止 qwen3.x 默认 thinking 占空 content；</li>
 *   <li>响应取 {@code root.message.content}；任何异常都返回 {@code null}，由调用方走各自的兜底文案。</li>
 * </ul>
 */
@Service
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger("LlmClient");

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String defaultModel;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    @Autowired
    public LlmClient(RestTemplate restTemplate,
                     String llmBaseUrl,
                     String llmModel,
                     String llmApiKey,
                     ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.baseUrl = llmBaseUrl;
        this.defaultModel = llmModel;
        this.apiKey = llmApiKey;
        this.objectMapper = objectMapper;
    }

    /** 配置的默认对话模型 */
    public String defaultModel() {
        return defaultModel;
    }

    /** Ollama 原生对话端点（由 base-url 去掉 /v1 得到） */
    public String endpoint() {
        return baseUrl.replace("/v1", "") + "/api/chat";
    }

    // ---------- 消息构造 ----------

    /** 新建 messages 数组 */
    public ArrayNode messages() {
        return objectMapper.createArrayNode();
    }

    /** 构造一条文本消息 */
    public ObjectNode text(String role, String content) {
        ObjectNode m = objectMapper.createObjectNode();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    // ---------- 暴露给调用方的 JSON 工具（避免各处重复注入 ObjectMapper） ----------

    /** 新建数组节点（如 Ollama 多模态消息的 {@code images} 数组） */
    public ArrayNode newArray() {
        return objectMapper.createArrayNode();
    }

    /** 解析 JSON 文本为 JsonNode（失败返回 null） */
    public JsonNode readTree(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------- 对话调用 ----------

    /** 用默认模型对话，返回 content；失败返回 null */
    public String chat(ArrayNode messages, int maxTokens, double temperature) {
        return chat(defaultModel, messages, maxTokens, temperature, restTemplate);
    }

    /** 指定模型对话，返回 content；失败返回 null */
    public String chat(String model, ArrayNode messages, int maxTokens, double temperature) {
        return chat(model, messages, maxTokens, temperature, restTemplate);
    }

    /**
     * 指定模型 + 指定 RestTemplate 对话（视觉识别传入短超时实例），返回 content；失败返回 null。
     */
    public String chat(String model, ArrayNode messages, int maxTokens, double temperature, RestTemplate rt) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("think", false);
            body.put("stream", false);
            body.put("max_tokens", maxTokens);
            body.put("temperature", temperature);
            body.set("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = rt.exchange(endpoint(), HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode messageNode = root.get("message");
                if (messageNode != null && messageNode.get("content") != null) {
                    return messageNode.get("content").asText();
                }
            }
        } catch (Exception e) {
            log.warn("LLM 调用失败 (model={}, endpoint={}): {}", model, endpoint(), e.getMessage());
        }
        return null;
    }
}

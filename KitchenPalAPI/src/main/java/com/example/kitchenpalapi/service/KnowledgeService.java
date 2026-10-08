package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.KnowledgeEntry;
import com.example.kitchenpalapi.repository.KnowledgeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地知识库检索 + RAG 生成服务。
 *
 * 召回策略：
 *  1) 默认「关键词召回」（中文二元组重叠 + 关键词加权），无需任何额外模型，1.5B 本机 Ollama 即可跑；
 *  2) 可选「向量召回」：当配置了 llm.embed-model（如 nomic-embed-text）时启用，
 *     通过 Ollama /api/embed 计算向量做余弦相似度；嵌入计算失败自动降级为关键词召回。
 *
 * 生成：把 Top-K 片段拼进 prompt，交给本地 Ollama 生成「有依据」的回答，并回传引用来源。
 */
@Service
public class KnowledgeService {

    private final KnowledgeRepository knowledgeRepository;
    private final RestTemplate restTemplate;
    private final String llmBaseUrl;
    private final String llmApiKey;
    private final String llmEmbedModel;
    private final ObjectMapper objectMapper;
    private final LlmClient llmClient;

    /** 向量召回用的条目向量缓存：id -> 向量 */
    private final Map<Long, float[]> entryEmbeddingCache = new ConcurrentHashMap<>();

    @Autowired
    public KnowledgeService(KnowledgeRepository knowledgeRepository,
                            RestTemplate restTemplate,
                            String llmBaseUrl,
                            String llmApiKey,
                            String llmEmbedModel,
                            ObjectMapper objectMapper,
                            LlmClient llmClient) {
        this.knowledgeRepository = knowledgeRepository;
        this.restTemplate = restTemplate;
        this.llmBaseUrl = llmBaseUrl;
        this.llmApiKey = llmApiKey;
        this.llmEmbedModel = llmEmbedModel;
        this.objectMapper = objectMapper;
        this.llmClient = llmClient;
    }

    /** 检索 Top-K 相关条目 */
    public List<KnowledgeEntry> retrieve(String query, int topK) {
        List<KnowledgeEntry> all = knowledgeRepository.findAll();
        if (all.isEmpty()) return List.of();

        List<KnowledgeEntry> result;
        if (llmEmbedModel != null && !llmEmbedModel.isBlank()) {
            result = retrieveByVector(query, all, topK); // 可能为 null（降级）
        } else {
            result = null;
        }
        if (result == null) {
            result = retrieveByKeyword(query, all, topK);
        }
        return result;
    }

    /** 关键词召回：中文二元组重叠 + 关键词字段加权 */
    private List<KnowledgeEntry> retrieveByKeyword(String query, List<KnowledgeEntry> all, int topK) {
        String q = normalize(query);
        Set<String> qBigrams = bigrams(q);

        List<Scored> scored = new ArrayList<>();
        for (KnowledgeEntry e : all) {
            String text = normalize(e.getTitle() + " " + e.getContent());
            Set<String> eBigrams = bigrams(text);
            int overlap = 0;
            for (String bg : qBigrams) {
                if (eBigrams.contains(bg)) overlap++;
            }
            int boost = 0;
            if (e.getKeywords() != null) {
                for (String kw : e.getKeywords().split("[,，、;；\\s]+")) {
                    if (!kw.isBlank() && q.contains(normalize(kw))) boost += 5;
                }
            }
            int score = overlap + boost;
            if (score > 0) scored.add(new Scored(e, score));
        }
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        return scored.stream().limit(topK).map(s -> s.entry).toList();
    }

    /** 向量召回：依赖 llm.embed-model，失败返回 null 触发降级 */
    private List<KnowledgeEntry> retrieveByVector(String query, List<KnowledgeEntry> all, int topK) {
        float[] qVec = embed(query);
        if (qVec == null) return null;
        List<Scored> scored = new ArrayList<>();
        for (KnowledgeEntry e : all) {
            float[] ev = entryEmbeddingCache.computeIfAbsent(e.getId(), id -> embed(e.getTitle() + " " + e.getContent()));
            if (ev == null) continue;
            float sim = cosine(qVec, ev);
            if (sim > 0) scored.add(new Scored(e, Math.round(sim * 100)));
        }
        if (scored.isEmpty()) return null;
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        return scored.stream().limit(topK).map(s -> s.entry).toList();
    }

    /** RAG：检索 + 本地 Ollama 生成带来源回答 */
    public Map<String, Object> askWithKnowledge(String question) {
        List<KnowledgeEntry> kb = retrieve(question, 5);
        Map<String, Object> result = new HashMap<>();

        if (kb.isEmpty()) {
            // 知识库无命中：直接让本地模型凭常识回答
            String answer = generate(question, "");
            result.put("answer", answer);
            result.put("sources", List.of());
            result.put("grounded", false);
            return result;
        }

        StringBuilder ctx = new StringBuilder();
        List<Map<String, String>> sources = new ArrayList<>();
        for (KnowledgeEntry e : kb) {
            ctx.append("- [").append(e.getCategory()).append("] ").append(e.getContent()).append("\n");
            Map<String, String> s = new HashMap<>();
            s.put("title", e.getTitle());
            s.put("category", e.getCategory());
            sources.add(s);
        }
        String answer = generate(question, ctx.toString());
        result.put("answer", answer);
        result.put("sources", sources);
        result.put("grounded", true);
        return result;
    }

    private String generate(String question, String context) {
        ArrayNode messages = llmClient.messages();
        messages.add(llmClient.text("system",
                "你是一个严谨的厨房知识助手「小厨」。\n" +
                "作答规则：\n" +
                "1. 仅依据下方【知识库】内容回答，不要编造知识库之外的具体数据。\n" +
                "2. 若知识库内容不足以回答，坦诚说明并给出安全、通用的常识建议。\n" +
                "3. 回答结构化、可操作；涉及用量/火候/时间必须给出具体数值（克/毫升/分钟），禁止\"适量\"\"少许\"等空话。\n" +
                "4. 若用户问某道菜的做法或步骤，用带编号的详细步骤作答，每步必须含：动作 + 火候/油温 + 具体用时 + 具体用量。\n" +
                "5. 涉及食品安全务必主动提醒。\n" +
                (context.isBlank() ? "" : "【知识库】\n" + context) +
                "\n\n【输出要求】直接给出回答，不要输出思考过程或解释性文字。/no_think"));
        messages.add(llmClient.text("user", question));

        String content = llmClient.chat(messages, 2000, 0.5);
        if (content != null) {
            return content;
        }
        return "抱歉，我暂时无法基于知识库回答这个问题，请稍后再试。";
    }

    /** 调用 Ollama /api/embed 计算文本向量；失败返回 null */
    private float[] embed(String text) {
        if (llmEmbedModel == null || llmEmbedModel.isBlank()) return null;
        try {
            ObjectNode rb = objectMapper.createObjectNode();
            rb.put("model", llmEmbedModel);
            ArrayNode input = objectMapper.createArrayNode();
            input.add(text);
            rb.set("input", input);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (llmApiKey != null && !llmApiKey.isBlank()) {
                headers.set("Authorization", "Bearer " + llmApiKey);
            }
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(rb), headers);

            // llmBaseUrl 形如 http://localhost:11434/v1 -> 嵌向量端点为 http://localhost:11434/api/embed
            String url = llmBaseUrl.replaceAll("/v1/?$", "") + "/api/embed";
            JsonNode root = objectMapper.readTree(restTemplate.postForEntity(url, request, String.class).getBody());
            JsonNode embeddings = root.get("embeddings");
            if (embeddings != null && embeddings.isArray() && embeddings.get(0).isArray()) {
                JsonNode vec = embeddings.get(0);
                float[] out = new float[vec.size()];
                for (int i = 0; i < vec.size(); i++) out[i] = (float) vec.get(i).asDouble();
                return out;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private float cosine(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0 || a.length != b.length) return 0f;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) return 0f;
        return (float) (dot / (Math.sqrt(na) * Math.sqrt(nb)));
    }

    /** 归一化：去空白与标点，仅保留中日韩汉字与字母数字 */
    private String normalize(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || (c >= 0x4E00 && c <= 0x9FFF)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 中文二元组（相邻字符对），用于近似 token 重叠 */
    private Set<String> bigrams(String s) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i + 1 < s.length(); i++) {
            set.add(s.substring(i, i + 2));
        }
        if (s.length() == 1) set.add(s);
        return set;
    }

    private static class Scored {
        final KnowledgeEntry entry;
        final int score;
        Scored(KnowledgeEntry entry, int score) {
            this.entry = entry;
            this.score = score;
        }
    }
}

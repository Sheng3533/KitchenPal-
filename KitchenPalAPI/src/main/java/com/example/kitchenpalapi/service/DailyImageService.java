package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.Recipe;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 每日美食图片生成服务。
 *
 * 每天从数据库菜谱中按日期（Asia/Shanghai）选一道真实菜谱，用 Pollinations 免费文生图接口
 * （flux 模型，免 API key、直连可达）生成一张与之对应的美食图片，保存到服务器本地目录
 * （image.save-dir，默认 static/images），并通过 /daily/** 静态资源对外提供访问。
 *
 * 图片按日期命名（yyyy-MM-dd.png），跨天自动累积、永不覆盖、永不删除，历史图片越攒越多。
 *
 * 图片、标题、点击跳转的菜谱三者完全统一：当天首次确定选菜后，会把「日期 → 标题 / recipeId / prompt」
 * 固化到 saveDir/meta/{date}.json。之后无论菜谱库如何增删（例如菜谱总数从 36 扩到 110 会让
 * date.hashCode() % size() 的取模结果漂移），当天读到的都是固化值，图片/标题/点击不会错位。
 *
 * 生成具有幂等性与容错：同一天只生成一次；生成失败（网络抖动 / 图床限流）仅记 WARN 且不固化
 * 元数据，下次访问自动重试，不会被"失败状态"锁死。
 */
@Service
public class DailyImageService {

    private static final Logger log = LoggerFactory.getLogger(DailyImageService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RecipeService recipeService;

    @Value("${image.base-url}")
    private String baseUrl;

    @Value("${image.model:qwen-image-3.0}")
    private String model;

    @Value("${image.save-dir}")
    private String saveDir;

    @Value("${image.enabled:true}")
    private boolean enabled;

    @Value("${image.api-key:}")
    private String apiKey;

    public DailyImageService(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /** 返回当天图片信息；首访即触发，保证 App 一打开就有图。 */
    public synchronized DailyImageInfo ensureToday() {
        String date = today();
        File dir = new File(saveDir);
        dir.mkdirs();
        File meta = metaFile(dir, date);

        // 当天已固化过选菜：直接返回，图片/标题/点击三者永不因菜谱库变化而错位
        DailyImageInfo frozen = readMeta(meta, date);
        if (frozen != null) {
            return frozen;
        }

        Recipe recipe = pickRecipe(date);
        if (recipe == null) {
            return fallbackInfo(date);
        }

        // 优先复用该菜谱的真实配图（/images/{id}.jpg），避免 AI 文生图不可控地生成人物、人脸等内容。
        // 菜谱图是 classpath 静态资源，打包后位于 jar 内，不能直接用 File 判断存在性；
        // 目前菜谱库 1-155 均已配图，直接复用即可。
        DailyImageInfo info = new DailyImageInfo(
                date,
                "/images/" + recipe.getId() + ".jpg",
                buildPrompt(recipe.getTitle()),
                recipe.getTitle(),
                recipe.getId());
        writeMeta(meta, info);
        log.info("每日推荐已复用菜谱原图 date={} recipeId={} title={}", date, recipe.getId(), recipe.getTitle());
        return info;
    }

    /** 当天选菜结果的固化文件：saveDir/meta/{date}.json */
    private File metaFile(File dir, String date) {
        return new File(new File(dir, "meta"), date + ".json");
    }

    /** 读取固化元数据；不存在或损坏时返回 null，调用方回退到重新选菜。 */
    private DailyImageInfo readMeta(File meta, String date) {
        if (!meta.exists()) {
            return null;
        }
        try {
            JsonNode n = objectMapper.readTree(meta);
            String title = n.path("title").asText(null);
            if (title == null || title.isBlank()) {
                return null;
            }
            Long recipeId = n.hasNonNull("recipeId") ? n.get("recipeId").asLong() : null;
            String prompt = n.path("prompt").asText(null);
            String url = n.path("url").asText(null);
            if (url == null || url.isBlank()) {
                // 历史元数据未存 url 时的兜底：仍回退到每日 AI 图路径
                url = "/daily/" + date + ".png";
            }
            return new DailyImageInfo(date, url, prompt, title, recipeId);
        } catch (Exception e) {
            log.warn("读取每日图片元数据失败，回退到重新选菜 date={}", date, e);
            return null;
        }
    }

    /** 写入选菜元数据（原子性不重要，损坏时 readMeta 会容错回退）。 */
    private void writeMeta(File meta, DailyImageInfo info) {
        try {
            meta.getParentFile().mkdirs();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("date", info.date);
            node.put("url", info.url);
            node.put("title", info.title);
            if (info.recipeId != null) {
                node.put("recipeId", info.recipeId);
            } else {
                node.putNull("recipeId");
            }
            if (info.prompt != null) {
                node.put("prompt", info.prompt);
            } else {
                node.putNull("prompt");
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(meta, node);
            log.info("每日图片选菜已固化 date={} title={} recipeId={}", info.date, info.title, info.recipeId);
        } catch (Exception e) {
            log.warn("写入每日图片元数据失败 date={}", info.date, e);
        }
    }

    /** 每天 00:05 自动预热当天图片（无网或关闭时静默跳过，下次访问懒加载补生成）。 */
    @Scheduled(cron = "0 5 0 * * ?")
    public void scheduledGenerate() {
        if (!enabled) {
            return;
        }
        DailyImageInfo info = ensureToday();
        log.info("每日美食图片定时生成完成 date={} recipeId={} url={}", info.date, info.recipeId, info.url);
    }

    private void generate(String date, Recipe recipe) {
        if (!enabled) {
            return;
        }
        if (recipe == null) {
            log.warn("每日美食图片生成被跳过：数据库暂无菜谱可选。");
            return;
        }
        try {
            String prompt = buildPrompt(recipe.getTitle());
            // 用日期 hash 作种子：同一天结果稳定、不同天各不相同
            int seed = Math.floorMod(date.hashCode(), 1_000_000_000);
            String imageUrl = buildPollinationsUrl(prompt, seed);
            File dir = new File(saveDir);
            dir.mkdirs();
            File file = new File(dir, date + ".png");
            download(imageUrl, file);
            // 下载产物校验：图床偶发返回错误页而非图片，过小则视为失败并删除，保留重试机会
            if (file.length() < 5120) {
                boolean deleted = file.delete();
                log.warn("每日美食图片生成异常 date={}：下载内容非有效图片（{}B），已删除重试 deleted={}", date, file.length(), deleted);
                return;
            }
            log.info("每日美食图片已生成 date={} recipe={} size={} path={}", date, recipe.getTitle(), file.length(), file.getAbsolutePath());
        } catch (Exception e) {
            log.warn("每日美食图片生成异常 date={}：{}", date, e.getMessage());
        }
    }

    /**
     * 构造 Pollinations 文生图 URL（flux 模型，免 key）。返回的就是图片本身，直接下载即可。
     */
    private String buildPollinationsUrl(String prompt, int seed) throws Exception {
        String p = URLEncoder.encode(prompt, StandardCharsets.UTF_8);
        return "https://image.pollinations.ai/prompt/" + p
                + "?width=768&height=768&nologo=true&model=flux&seed=" + seed + "&referrer=kitchenpal";
    }

    /**
     * 从 URL 下载图片字节到本地文件。强制直连（Proxy.NO_PROXY），避免任何系统/透明代理干扰导致超时。
     */
    private void download(String imageUrl, File file) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection(Proxy.NO_PROXY);
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        int code = conn.getResponseCode();
        if (code != 200) {
            conn.disconnect();
            throw new RuntimeException("下载图片失败 httpCode=" + code);
        }
        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
        }
        conn.disconnect();
    }

    /** 基于日期字符串 hash 从数据库真实菜谱中选一道，保证当天固定、每天不同。 */
    private Recipe pickRecipe(String date) {
        List<Recipe> all = recipeService.getAllRecipes();
        if (all == null || all.isEmpty()) {
            return null;
        }
        int idx = Math.floorMod(date.hashCode(), all.size());
        return all.get(idx);
    }

    /** 数据库无菜谱时的兜底信息。 */
    private DailyImageInfo fallbackInfo(String date) {
        String title = "家常美食";
        return new DailyImageInfo(date, null, buildPrompt(title), title, null);
    }

    private String buildPrompt(String food) {
        return food + " 美食 高清摄影 俯拍 暖色调 中式家常菜 白瓷盘 精致摆盘 食欲感 背景虚化 无人物 无面部 无人体 无手 只有食物和餐具";
    }

    private String today() {
        return LocalDate.now(ZONE).format(DATE_FMT);
    }

    /** 每日图片对外信息。 */
    public static class DailyImageInfo {
        public String date;
        public String url;
        public String prompt;
        public String title;
        public Long recipeId;

        public DailyImageInfo(String date, String url, String prompt, String title, Long recipeId) {
            this.date = date;
            this.url = url;
            this.prompt = prompt;
            this.title = title;
            this.recipeId = recipeId;
        }
    }
}

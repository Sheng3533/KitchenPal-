package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.CookingHistory;
import com.example.kitchenpalapi.repository.CookingHistoryRepository;
import com.example.kitchenpalapi.repository.NotificationRepository;
import com.example.kitchenpalapi.repository.RecipeRepository;
import com.example.kitchenpalapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 管理端数据看板聚合统计：一次请求返回 Dashboard 全部指标。
 */
@Service
public class StatsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private CookingHistoryRepository cookingHistoryRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public Map<String, Object> getOverview() {
        Map<String, Object> result = new LinkedHashMap<>();

        // KPI 基础指标
        result.put("totalUsers", userRepository.count());
        result.put("totalRecipes", recipeRepository.count());

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        result.put("todayCooking", cookingHistoryRepository.countByCreatedAtBetween(todayStart, todayEnd));
        result.put("pendingNotifications", notificationRepository.countByIsReadFalse());

        // 近 7 天烹饪趋势（含今天，按 createdAt 分组）
        List<CookingHistory> recent = cookingHistoryRepository.findByCreatedAtBetween(todayStart.minusDays(6), todayEnd);
        Map<LocalDate, Long> byDay = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            byDay.put(todayStart.minusDays(i).toLocalDate(), 0L);
        }
        for (CookingHistory h : recent) {
            if (h.getCreatedAt() != null) {
                byDay.merge(h.getCreatedAt().toLocalDate(), 1L, Long::sum);
            }
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> e : byDay.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", e.getKey().toString());
            item.put("label", e.getKey().getMonthValue() + "/" + e.getKey().getDayOfMonth());
            item.put("count", e.getValue());
            trend.add(item);
        }
        result.put("weeklyTrend", trend);

        // 食谱分类占比
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        recipeRepository.findAll().forEach(r -> {
            String cat = (r.getCategory() == null || r.getCategory().isBlank()) ? "其他" : r.getCategory();
            categoryMap.merge(cat, 1L, Long::sum);
        });
        result.put("categoryDist", categoryMap);

        // 热门食谱 TOP10（按评分）
        List<Map<String, Object>> top = new ArrayList<>();
        recipeRepository.findAll().stream()
                .sorted(Comparator.comparing((com.example.kitchenpalapi.entity.Recipe r) ->
                        r.getRating() == null ? 0f : r.getRating()).reversed())
                .limit(10)
                .forEach(r -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("title", r.getTitle());
                    item.put("rating", r.getRating());
                    top.add(item);
                });
        result.put("topRecipes", top);

        return result;
    }
}

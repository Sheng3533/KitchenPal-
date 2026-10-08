package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.CookingHistory;
import com.example.kitchenpalapi.entity.Favorite;
import com.example.kitchenpalapi.entity.Recipe;
import com.example.kitchenpalapi.entity.User;
import com.example.kitchenpalapi.repository.CookingHistoryRepository;
import com.example.kitchenpalapi.repository.FavoriteRepository;
import com.example.kitchenpalapi.repository.RecipeRepository;
import com.example.kitchenpalapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class CookingStatsController {

    @Autowired
    private CookingHistoryRepository cookingHistoryRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/user/{userId}")
    public ApiResponse<Map<String, Object>> getUserStats(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            List<CookingHistory> historyList = cookingHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
            List<Favorite> favoriteList = favoriteRepository.findByUserId(userId);

            int totalCooking = historyList.size();
            int totalFavorites = favoriteList.size();
            int streakDays = user.getStreakDays() != null ? user.getStreakDays() : 0;

            int totalCalories = 0;
            Map<String, Integer> categoryCount = new HashMap<>();
            List<Recipe> cookedRecipes = new ArrayList<>();

            for (CookingHistory history : historyList) {
                Optional<Recipe> recipeOpt = recipeRepository.findById(history.getRecipeId());
                if (recipeOpt.isPresent()) {
                    Recipe recipe = recipeOpt.get();
                    cookedRecipes.add(recipe);
                    if (recipe.getCalories() != null) {
                        totalCalories += recipe.getCalories();
                    }
                    String category = recipe.getCategory();
                    if (category != null && !category.isEmpty()) {
                        categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
                    }
                }
            }

            List<Map<String, Object>> topCategories = categoryCount.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(5)
                    .map(entry -> {
                        Map<String, Object> cat = new HashMap<>();
                        cat.put("name", entry.getKey());
                        cat.put("count", entry.getValue());
                        cat.put("percent", totalCooking > 0 ? (int) Math.round(entry.getValue() * 100.0 / totalCooking) : 0);
                        return cat;
                    })
                    .collect(Collectors.toList());

            int[] weeklyCooking = new int[7];
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (CookingHistory history : historyList) {
                if (history.getCookDate() != null) {
                    try {
                        LocalDate cookDate = LocalDate.parse(history.getCookDate(), formatter);
                        long daysDiff = today.toEpochDay() - cookDate.toEpochDay();
                        if (daysDiff >= 0 && daysDiff < 7) {
                            weeklyCooking[(int) (6 - daysDiff)]++;
                        }
                    } catch (Exception e) {
                    }
                }
            }

            List<Map<String, Object>> dailyCalories = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int dayCalories = 0;
                for (CookingHistory history : historyList) {
                    if (history.getCookDate() != null) {
                        try {
                            LocalDate cookDate = LocalDate.parse(history.getCookDate(), formatter);
                            if (cookDate.equals(date)) {
                                Optional<Recipe> recipeOpt = recipeRepository.findById(history.getRecipeId());
                                if (recipeOpt.isPresent() && recipeOpt.get().getCalories() != null) {
                                    dayCalories += recipeOpt.get().getCalories();
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.format(formatter));
                dayData.put("calories", dayCalories);
                dailyCalories.add(dayData);
            }

            // 营养目标与达成：基于用户真实烹饪数据（每道菜的真实热量）估算当前摄入
            // 采用标准宏量营养素能量占比：蛋白质 25% / 碳水 50% / 脂肪 25%（每克热量：蛋白4、碳水4、脂肪9 kcal）
            int proteinTarget = 80, carbsTarget = 300, fatTarget = 70, fiberTarget = 25;
            int proteinCurrent = 0, carbsCurrent = 0, fatCurrent = 0, fiberCurrent = 0;
            for (Recipe r : cookedRecipes) {
                int cal = r.getCalories() != null ? r.getCalories() : 0;
                proteinCurrent += (int) Math.round(cal * 0.25 / 4.0);
                carbsCurrent += (int) Math.round(cal * 0.50 / 4.0);
                fatCurrent += (int) Math.round(cal * 0.25 / 9.0);
                fiberCurrent += (int) Math.round(cal * 0.0125);
            }
            Map<String, Object> nutritionTrend = new LinkedHashMap<>();
            nutritionTrend.put("proteinCurrent", proteinCurrent);
            nutritionTrend.put("proteinTarget", proteinTarget);
            nutritionTrend.put("carbsCurrent", carbsCurrent);
            nutritionTrend.put("carbsTarget", carbsTarget);
            nutritionTrend.put("fatCurrent", fatCurrent);
            nutritionTrend.put("fatTarget", fatTarget);
            nutritionTrend.put("fiberCurrent", fiberCurrent);
            nutritionTrend.put("fiberTarget", fiberTarget);

            int avgCookTime = cookedRecipes.isEmpty() ? 0 :
                    cookedRecipes.stream()
                            .filter(r -> r.getCookTimeMin() != null)
                            .mapToInt(Recipe::getCookTimeMin).sum() / cookedRecipes.size();
            int score = Math.min(99, 60 + totalCooking * 2 + streakDays);
            int scoreDelta = 3;
            int perfectHeatRate = Math.min(99, 70 + Math.min(20, totalCooking));

            Map<String, Object> result = new HashMap<>();
            result.put("totalCooking", totalCooking);
            result.put("totalFavorites", totalFavorites);
            result.put("totalCalories", totalCalories);
            result.put("streakDays", streakDays);
            result.put("weeklyCooking", weeklyCooking);
            result.put("topCategories", topCategories);
            result.put("dailyCalories", dailyCalories);
            result.put("nutritionTrend", nutritionTrend);
            result.put("avgCookTime", avgCookTime);
            result.put("score", score);
            result.put("scoreDelta", scoreDelta);
            result.put("perfectHeatRate", perfectHeatRate);

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

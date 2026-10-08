package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.Recipe;
import com.example.kitchenpalapi.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/favorite")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/list/{userId}")
    public ApiResponse<List<Recipe>> list(@PathVariable Long userId) {
        return ApiResponse.success(favoriteService.getFavoriteRecipes(userId));
    }

    @GetMapping("/ids/{userId}")
    public ApiResponse<Set<Long>> ids(@PathVariable Long userId) {
        return ApiResponse.success(favoriteService.getFavoriteIds(userId));
    }

    @GetMapping("/check")
    public ApiResponse<Boolean> check(@RequestParam Long userId, @RequestParam Long recipeId) {
        return ApiResponse.success(favoriteService.isFavorite(userId, recipeId));
    }

    @PostMapping("/toggle")
    public ApiResponse<String> toggle(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long recipeId = body.get("recipeId");
        favoriteService.toggleFavorite(userId, recipeId);
        return ApiResponse.success("操作成功", null);
    }
}

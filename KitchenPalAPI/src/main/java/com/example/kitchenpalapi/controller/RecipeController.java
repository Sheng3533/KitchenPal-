package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.Recipe;
import com.example.kitchenpalapi.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@CrossOrigin(origins = "*")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping("/list")
    public ApiResponse<List<Recipe>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        List<Recipe> recipes;
        if (keyword != null && !keyword.isEmpty()) {
            recipes = recipeService.searchRecipes(keyword);
        } else if (category != null && !category.isEmpty()) {
            recipes = recipeService.getRecipesByCategory(category);
        } else {
            recipes = recipeService.getAllRecipes();
        }
        return ApiResponse.success(recipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Recipe>> detail(@PathVariable Long id) {
        try {
            Recipe recipe = recipeService.getRecipeById(id);
            return ResponseEntity.ok(ApiResponse.success(recipe));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @GetMapping("/hero")
    public ApiResponse<List<Recipe>> hero() {
        return ApiResponse.success(recipeService.getHeroRecipes());
    }

    @GetMapping("/categories")
    public ApiResponse<List<String>> categories() {
        return ApiResponse.success(recipeService.getCategories());
    }

    @PostMapping
    public ApiResponse<Recipe> create(@RequestBody Recipe recipe) {
        // 新增时清掉客户端占位 id，避免 JPA 走 merge 当成更新
        recipe.setId(null);
        Recipe saved = recipeService.saveRecipe(recipe);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<Recipe> update(@PathVariable Long id, @RequestBody Recipe recipe) {
        recipe.setId(id);
        Recipe updated = recipeService.saveRecipe(recipe);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/hero")
    public ApiResponse<Recipe> toggleHero(@PathVariable Long id) {
        Recipe recipe = recipeService.getRecipeById(id);
        recipe.setIsHero(!recipe.getIsHero());
        Recipe updated = recipeService.saveRecipe(recipe);
        return ApiResponse.success(updated);
    }
}

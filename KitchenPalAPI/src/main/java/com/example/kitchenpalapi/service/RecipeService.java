package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.Recipe;
import com.example.kitchenpalapi.entity.RecipeRating;
import com.example.kitchenpalapi.repository.RecipeRatingRepository;
import com.example.kitchenpalapi.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeRatingRepository recipeRatingRepository;

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Recipe getRecipeById(Long id) {
        return recipeRepository.findById(id).orElseThrow(() -> new RuntimeException("食谱不存在"));
    }

    public List<Recipe> getRecipesByCategory(String category) {
        if (category == null || category.equals("全部")) {
            return recipeRepository.findAll();
        }
        return recipeRepository.findByCategory(category);
    }

    public List<Recipe> searchRecipes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return recipeRepository.findAll();
        }
        return recipeRepository.searchByKeyword(keyword.trim());
    }

    public List<Recipe> getHeroRecipes() {
        return recipeRepository.findHeroRecipes();
    }

    public List<String> getCategories() {
        return recipeRepository.findAllCategories();
    }

    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new RuntimeException("食谱不存在");
        }
        recipeRepository.deleteById(id);
    }

    /**
     * 用户对菜谱评分（1~5 星）。同一用户对同一菜谱只保留最新一条，
     * 随后重算 Recipe.rating 平均分与 ratingCount 并落库。
     */
    public Recipe rateRecipe(Long recipeId, Long userId, String userName, int rating, String comment) {
        if (rating < 1 || rating > 5) throw new RuntimeException("评分需在 1~5 之间");
        Recipe recipe = getRecipeById(recipeId);
        Optional<RecipeRating> existing = recipeRatingRepository.findByRecipeIdAndUserId(recipeId, userId);
        RecipeRating rr = existing.orElse(new RecipeRating());
        rr.setRecipeId(recipeId);
        rr.setUserId(userId);
        rr.setUserName(userName);
        rr.setRating(rating);
        rr.setComment(comment);
        recipeRatingRepository.save(rr);

        List<RecipeRating> all = recipeRatingRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId);
        double avg = all.stream().mapToInt(RecipeRating::getRating).average().orElse(recipe.getRating());
        recipe.setRating(Math.round(avg * 10) / 10f);
        recipe.setRatingCount(all.size());
        return recipeRepository.save(recipe);
    }

    public List<RecipeRating> getRatings(Long recipeId) {
        return recipeRatingRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId);
    }
}

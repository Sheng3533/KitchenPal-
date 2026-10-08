package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.Favorite;
import com.example.kitchenpalapi.entity.Recipe;
import com.example.kitchenpalapi.entity.User;
import com.example.kitchenpalapi.repository.FavoriteRepository;
import com.example.kitchenpalapi.repository.RecipeRepository;
import com.example.kitchenpalapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Recipe> getFavoriteRecipes(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        List<Long> recipeIds = favorites.stream().map(Favorite::getRecipeId).collect(Collectors.toList());
        if (recipeIds.isEmpty()) {
            return new ArrayList<>();
        }
        return recipeRepository.findAllById(recipeIds);
    }

    public Set<Long> getFavoriteIds(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream().map(Favorite::getRecipeId).collect(Collectors.toSet());
    }

    public boolean isFavorite(Long userId, Long recipeId) {
        return favoriteRepository.existsByUserIdAndRecipeId(userId, recipeId);
    }

    @Transactional
    public void toggleFavorite(Long userId, Long recipeId) {
        if (favoriteRepository.existsByUserIdAndRecipeId(userId, recipeId)) {
            favoriteRepository.deleteByUserIdAndRecipeId(userId, recipeId);
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setRecipeId(recipeId);
            favoriteRepository.save(fav);
        }
        updateFavoriteCount(userId);
    }

    private void updateFavoriteCount(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            long count = favoriteRepository.findByUserId(userId).size();
            user.setFavoriteCount((int) count);
            userRepository.save(user);
        }
    }
}

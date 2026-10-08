package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.RecipeRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRatingRepository extends JpaRepository<RecipeRating, Long> {
    List<RecipeRating> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);
    Optional<RecipeRating> findByRecipeIdAndUserId(Long recipeId, Long userId);
}

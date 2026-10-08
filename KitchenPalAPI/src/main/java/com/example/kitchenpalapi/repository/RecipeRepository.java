package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByCategory(String category);

    @Query("SELECT r FROM Recipe r WHERE r.isHero = true")
    List<Recipe> findHeroRecipes();

    @Query("SELECT r FROM Recipe r WHERE " +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.subtitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Recipe> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT r.category FROM Recipe r ORDER BY r.category")
    List<String> findAllCategories();
}

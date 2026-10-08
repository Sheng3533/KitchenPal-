package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);
    long countByRecipeId(Long recipeId);
    void deleteByRecipeIdAndUserId(Long recipeId, Long userId);
}

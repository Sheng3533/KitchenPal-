package com.example.kitchenpalapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户对菜谱的评分（1~5 星，可带一句话评价）。
 * 每个用户对每道菜仅保留最新一条（user_id + recipe_id 唯一），
 * 后端据此重算 Recipe.rating 平均分与 ratingCount。
 */
@Data
@Entity
@Table(name = "recipe_ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"recipe_id", "user_id"})
})
public class RecipeRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(nullable = false)
    private Integer rating; // 1~5

    @Column(length = 500)
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.example.kitchenpalapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜谱评论：用户对某道菜谱的图文评价。
 * 与 RecipeRating 不同——这里是自由评论（可多条），评分是聚合到 Recipe.rating。
 */
@Data
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_avatar")
    private String userAvatar;

    @Column(length = 1000, nullable = false)
    private String content;

    /** 用户晒图（成品照）URL，可空 */
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "likes")
    private Integer likes = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

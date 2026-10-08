package com.example.kitchenpalapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cooking_history")
public class CookingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long recipeId;

    private String recipeTitle;

    private String recipeImageUrl;

    private String cookDate;

    private String cookTime;

    private Integer durationMin;

    private Integer score;

    private Integer rating;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

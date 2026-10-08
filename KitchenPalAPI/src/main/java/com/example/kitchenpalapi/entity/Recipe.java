package com.example.kitchenpalapi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    private String imageUrl;

    private Float rating = 4.5f;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "is_user_created")
    private Boolean isUserCreated = false;

    private Integer cookTimeMin = 30;

    private Integer calories = 300;

    private String difficulty = "简单";

    private String category = "家常菜";

    @Column(length = 2000)
    private String tags;

    private Integer matchPercent;

    @Column(length = 3000)
    private String ingredientsJson;

    @Column(length = 5000)
    private String stepsJson;

    private Boolean isHero = false;
}

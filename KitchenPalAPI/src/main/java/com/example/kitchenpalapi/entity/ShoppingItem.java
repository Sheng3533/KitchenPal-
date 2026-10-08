package com.example.kitchenpalapi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "shopping_items")
@Data
public class ShoppingItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 32)
    private String quantity;

    @Column(length = 16)
    private String unit;

    @Column(length = 32)
    private String category;

    @Column(nullable = false)
    private Boolean checked = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

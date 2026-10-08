package com.example.kitchenpalapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String phone;

    private String nickname;

    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    private String avatar;

    private String tastePreferences;

    private Integer cookingCount = 0;

    private Integer favoriteCount = 0;

    private Integer streakDays = 0;

    private Boolean darkMode = false;

    private Boolean cookingNotify = true;

    @Column(name = "status")
    private Integer status = 1;  // 1=正常, 0=禁用

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

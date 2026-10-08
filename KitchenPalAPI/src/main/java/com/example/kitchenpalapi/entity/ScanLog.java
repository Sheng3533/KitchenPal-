package com.example.kitchenpalapi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_logs")
public class ScanLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "detected_ingredients", columnDefinition = "TEXT")
    private String detectedIngredients;

    @Column(name = "suggested_recipe_ids", length = 500)
    private String suggestedRecipeIds;

    @Column(name = "scan_time")
    private LocalDateTime scanTime;

    @Column(name = "status", length = 20)
    private String status;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDetectedIngredients() { return detectedIngredients; }
    public void setDetectedIngredients(String detectedIngredients) { this.detectedIngredients = detectedIngredients; }
    public String getSuggestedRecipeIds() { return suggestedRecipeIds; }
    public void setSuggestedRecipeIds(String suggestedRecipeIds) { this.suggestedRecipeIds = suggestedRecipeIds; }
    public LocalDateTime getScanTime() { return scanTime; }
    public void setScanTime(LocalDateTime scanTime) { this.scanTime = scanTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

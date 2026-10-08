package com.example.kitchenpalapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * 冰箱食材：用户登记家中现有食材与保质期，用于临期提醒、智能推荐「清冰箱菜谱」。
 */
@Data
@Entity
@Table(name = "fridge_items")
public class FridgeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    private String category; // 蔬菜/肉蛋/调料/主食...

    private String quantity;

    private String unit; // 个/克/袋...

    @Column(name = "expire_date")
    private LocalDate expireDate; // 保质期；为空表示不限

    @Column(name = "added_at")
    private LocalDate addedAt;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) addedAt = LocalDate.now();
    }
}

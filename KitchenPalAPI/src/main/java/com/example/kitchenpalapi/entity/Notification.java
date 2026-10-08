package com.example.kitchenpalapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String type;

    private String title;

    @Column(length = 500)
    private String content;

    private String icon;

    private Boolean isRead = false;

    private LocalDateTime createdAt;

    /** 关联对象 ID（如食谱 ID），用于点击通知跳转到相关页面 */
    private Long relatedId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

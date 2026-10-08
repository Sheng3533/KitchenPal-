package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.Notification;
import com.example.kitchenpalapi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{userId}")
    public ApiResponse<List<Notification>> list(@PathVariable Long userId) {
        return ApiResponse.success(notificationService.getNotifications(userId));
    }

    @GetMapping("/unread/{userId}")
    public ApiResponse<Long> unreadCount(@PathVariable Long userId) {
        return ApiResponse.success(notificationService.getUnreadCount(userId));
    }

    @PostMapping("/read/{id}")
    public ApiResponse<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResponse.success("已标记为已读", null);
    }

    @PostMapping("/read-all")
    public ApiResponse<String> markAllAsRead(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        notificationService.markAllAsRead(userId);
        return ApiResponse.success("全部已读", null);
    }

    @GetMapping("/list")
    public ApiResponse<List<Notification>> listAll() {
        return ApiResponse.success(notificationService.getAllNotifications());
    }

    @PostMapping("/send")
    public ApiResponse<Notification> send(@RequestBody Notification notification) {
        Notification saved = notificationService.sendNotification(notification);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<Notification> update(@PathVariable Long id, @RequestBody Notification notification) {
        notification.setId(id);
        Notification updated = notificationService.sendNotification(notification);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ApiResponse.success(null);
    }
}

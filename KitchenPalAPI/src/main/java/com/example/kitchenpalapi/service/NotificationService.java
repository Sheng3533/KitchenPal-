package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.Notification;
import com.example.kitchenpalapi.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long id) {
        Notification notif = notificationRepository.findById(id).orElse(null);
        if (notif != null) {
            notif.setIsRead(true);
            notificationRepository.save(notif);
        }
    }

    public void markAllAsRead(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            if (!n.getIsRead()) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        }
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Notification sendNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    /** 自动生成：用户完成一道菜谱的烹饪后，推送一条鼓励通知（App 内轮询拉取，无需 FCM） */
    public void notifyCookingComplete(Long userId, String recipeTitle, Long recipeId, Integer streakDays) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType("cooking");
        n.setIcon("🍳");
        n.setTitle("烹饪完成");
        String streak = (streakDays != null && streakDays > 0) ? String.format("，连续下厨 %d 天", streakDays) : "";
        n.setContent("你完成了《" + (recipeTitle != null ? recipeTitle : "美食") + "》" + streak + "，继续加油！");
        n.setRelatedId(recipeId);
        n.setIsRead(false);
        notificationRepository.save(n);
    }

    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("通知不存在");
        }
        notificationRepository.deleteById(id);
    }
}

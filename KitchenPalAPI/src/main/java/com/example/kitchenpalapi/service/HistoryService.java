package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.CookingHistory;
import com.example.kitchenpalapi.repository.CookingHistoryRepository;
import com.example.kitchenpalapi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    @Autowired
    private CookingHistoryRepository historyRepository;

    @Autowired
    private NotificationService notificationService;

    public List<CookingHistory> getHistory(Long userId) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public CookingHistory addHistory(CookingHistory history) {
        CookingHistory saved = historyRepository.save(history);
        // 自动生成「烹饪完成」通知（App 内轮询拉取，无需 FCM）
        notificationService.notifyCookingComplete(
                saved.getUserId(), saved.getRecipeTitle(), saved.getRecipeId(), null);
        return saved;
    }

    public List<CookingHistory> getAllHistory() {
        return historyRepository.findAll();
    }

    public void deleteHistory(Long id) {
        if (!historyRepository.existsById(id)) {
            throw new RuntimeException("烹饪记录不存在");
        }
        historyRepository.deleteById(id);
    }
}

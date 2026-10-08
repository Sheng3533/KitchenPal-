package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.FridgeItem;
import com.example.kitchenpalapi.repository.FridgeItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FridgeService {

    @Autowired
    private FridgeItemRepository fridgeItemRepository;

    public List<FridgeItem> list(Long userId) {
        return fridgeItemRepository.findByUserIdOrderByAddedAtDesc(userId);
    }

    /** 临期（expireDate 不为空且 <= today + days）的食材 */
    public List<FridgeItem> expiringSoon(Long userId, int days) {
        LocalDate limit = LocalDate.now().plusDays(days);
        return fridgeItemRepository.findByUserIdOrderByAddedAtDesc(userId).stream()
                .filter(i -> i.getExpireDate() != null && !i.getExpireDate().isAfter(limit))
                .toList();
    }

    public FridgeItem add(FridgeItem item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new RuntimeException("食材名称不能为空");
        }
        // 新增时必须清掉客户端可能带上的占位 id（如 0），否则 JPA 会走 merge 当成更新而报错
        item.setId(null);
        return fridgeItemRepository.save(item);
    }

    public FridgeItem update(Long id, Long userId, FridgeItem patch) {
        FridgeItem item = fridgeItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食材不存在"));
        if (!item.getUserId().equals(userId)) throw new RuntimeException("只能修改自己的食材");
        if (patch.getName() != null) item.setName(patch.getName());
        if (patch.getCategory() != null) item.setCategory(patch.getCategory());
        if (patch.getQuantity() != null) item.setQuantity(patch.getQuantity());
        if (patch.getUnit() != null) item.setUnit(patch.getUnit());
        if (patch.getExpireDate() != null) item.setExpireDate(patch.getExpireDate());
        return fridgeItemRepository.save(item);
    }

    public void delete(Long id, Long userId) {
        FridgeItem item = fridgeItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食材不存在"));
        if (!item.getUserId().equals(userId)) throw new RuntimeException("只能删除自己的食材");
        fridgeItemRepository.delete(item);
    }
}

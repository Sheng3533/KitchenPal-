package com.example.kitchenpalapi.service;

import com.example.kitchenpalapi.entity.ShoppingItem;
import com.example.kitchenpalapi.repository.ShoppingItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingService {

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    public List<ShoppingItem> list(Long userId) {
        return shoppingItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public ShoppingItem add(Long userId, ShoppingItem item) {
        item.setId(null);
        item.setUserId(userId);
        if (item.getChecked() == null) item.setChecked(false);
        if (item.getCreatedAt() == null) item.setCreatedAt(LocalDateTime.now());
        return shoppingItemRepository.save(item);
    }

    public ShoppingItem update(Long id, Long userId, ShoppingItem patch) {
        ShoppingItem existing = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("购物项不存在"));
        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该购物项");
        }
        if (patch.getName() != null) existing.setName(patch.getName());
        if (patch.getQuantity() != null) existing.setQuantity(patch.getQuantity());
        if (patch.getUnit() != null) existing.setUnit(patch.getUnit());
        if (patch.getCategory() != null) existing.setCategory(patch.getCategory());
        if (patch.getChecked() != null) existing.setChecked(patch.getChecked());
        return shoppingItemRepository.save(existing);
    }

    public void delete(Long id, Long userId) {
        ShoppingItem existing = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("购物项不存在"));
        if (!existing.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该购物项");
        }
        shoppingItemRepository.delete(existing);
    }

    @Transactional
    public void clear(Long userId) {
        shoppingItemRepository.deleteByUserId(userId);
    }

    /**
     * 设备 → 云端全量同步：先用当前设备列表整体替换服务端数据。
     * 用于 App 离线编辑后一键上云，保证两端一致。
     */
    @Transactional
    public List<ShoppingItem> replaceAll(Long userId, List<ShoppingItem> items) {
        shoppingItemRepository.deleteByUserId(userId);
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        for (ShoppingItem it : items) {
            it.setId(null);
            it.setUserId(userId);
            if (it.getChecked() == null) it.setChecked(false);
            if (it.getCreatedAt() == null) it.setCreatedAt(LocalDateTime.now());
        }
        return shoppingItemRepository.saveAll(items);
    }
}

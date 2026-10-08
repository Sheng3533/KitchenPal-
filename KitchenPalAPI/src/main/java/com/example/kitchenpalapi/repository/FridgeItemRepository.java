package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.FridgeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FridgeItemRepository extends JpaRepository<FridgeItem, Long> {
    List<FridgeItem> findByUserIdOrderByAddedAtDesc(Long userId);
    void deleteByUserIdAndId(Long userId, Long id);
}

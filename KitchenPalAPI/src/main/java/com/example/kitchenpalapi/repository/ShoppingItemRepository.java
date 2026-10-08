package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
    List<ShoppingItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);

    int countByUserIdAndCheckedIsFalse(Long userId);
}

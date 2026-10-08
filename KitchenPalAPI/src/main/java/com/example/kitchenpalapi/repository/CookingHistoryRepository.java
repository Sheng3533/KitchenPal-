package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.CookingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CookingHistoryRepository extends JpaRepository<CookingHistory, Long> {
    List<CookingHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<CookingHistory> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.KnowledgeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeRepository extends JpaRepository<KnowledgeEntry, Long> {
    List<KnowledgeEntry> findByCategory(String category);

    long countByCategory(String category);
}

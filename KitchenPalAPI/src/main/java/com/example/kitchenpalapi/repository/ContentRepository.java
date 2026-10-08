package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByStatus(Integer status);
    List<Content> findByType(String type);
}

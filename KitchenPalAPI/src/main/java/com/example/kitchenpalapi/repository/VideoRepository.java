package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByStatus(Integer status);
    List<Video> findByCategory(String category);
}

package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.ScanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScanLogRepository extends JpaRepository<ScanLog, Long> {
}

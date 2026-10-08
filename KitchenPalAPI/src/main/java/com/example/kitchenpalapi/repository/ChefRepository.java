package com.example.kitchenpalapi.repository;

import com.example.kitchenpalapi.entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChefRepository extends JpaRepository<Chef, Long> {
}

package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.CookingHistory;
import com.example.kitchenpalapi.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @GetMapping("/{userId}")
    public ApiResponse<List<CookingHistory>> list(@PathVariable Long userId) {
        return ApiResponse.success(historyService.getHistory(userId));
    }

    @PostMapping("/add")
    public ApiResponse<CookingHistory> add(@RequestBody CookingHistory history) {
        CookingHistory saved = historyService.addHistory(history);
        return ApiResponse.success("添加成功", saved);
    }

    @GetMapping("/list")
    public ApiResponse<List<CookingHistory>> listAll() {
        return ApiResponse.success(historyService.getAllHistory());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        historyService.deleteHistory(id);
        return ApiResponse.success(null);
    }
}

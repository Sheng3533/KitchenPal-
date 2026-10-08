package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.ScanLog;
import com.example.kitchenpalapi.repository.ScanLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scan/logs")
@CrossOrigin(origins = "*")
public class ScanLogController {

    @Autowired
    private ScanLogRepository scanLogRepository;

    @GetMapping
    public ApiResponse<List<ScanLog>> list() {
        return ApiResponse.success(scanLogRepository.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ScanLog> detail(@PathVariable Long id) {
        return scanLogRepository.findById(id)
                .map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.error("日志不存在"));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!scanLogRepository.existsById(id)) {
            return ApiResponse.error("日志不存在");
        }
        scanLogRepository.deleteById(id);
        return ApiResponse.success(null);
    }
}

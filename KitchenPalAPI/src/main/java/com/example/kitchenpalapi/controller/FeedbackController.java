package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.Feedback;
import com.example.kitchenpalapi.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @PostMapping("/submit")
    public ApiResponse<Boolean> submit(@RequestBody Feedback feedback) {
        try {
            if (feedback.getStatus() == null) {
                feedback.setStatus(0);
            }
            feedbackRepository.save(feedback);
            return ApiResponse.success("提交成功", true);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Feedback> feedbackPage = feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
            Map<String, Object> result = new HashMap<>();
            result.put("list", feedbackPage.getContent());
            result.put("total", feedbackPage.getTotalElements());
            result.put("page", page);
            result.put("size", size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Feedback> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            Feedback feedback = feedbackRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("反馈不存在"));
            Integer status = body.get("status");
            feedback.setStatus(status);
            Feedback updated = feedbackRepository.save(feedback);
            return ApiResponse.success("状态更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            feedbackRepository.deleteById(id);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

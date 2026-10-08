package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.FridgeItem;
import com.example.kitchenpalapi.service.FridgeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fridge")
@CrossOrigin(origins = "*")
public class FridgeController {

    @Autowired
    private FridgeService fridgeService;

    @GetMapping("/{userId}")
    public ApiResponse<List<FridgeItem>> list(@PathVariable Long userId) {
        return ApiResponse.success(fridgeService.list(userId));
    }

    @GetMapping("/{userId}/expiring")
    public ApiResponse<List<FridgeItem>> expiring(@PathVariable Long userId,
                                                  @RequestParam(defaultValue = "3") int days) {
        return ApiResponse.success(fridgeService.expiringSoon(userId, days));
    }

    @PostMapping
    public ApiResponse<FridgeItem> add(@RequestBody FridgeItem item, HttpServletRequest request) {
        Long uid = (Long) request.getAttribute("userId");
        if (uid == null) return ApiResponse.error(401, "未登录");
        item.setUserId(uid);
        return ApiResponse.success("添加成功", fridgeService.add(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<FridgeItem> update(@PathVariable Long id, @RequestBody FridgeItem patch,
                                          HttpServletRequest request) {
        Long uid = (Long) request.getAttribute("userId");
        if (uid == null) return ApiResponse.error(401, "未登录");
        return ApiResponse.success("更新成功", fridgeService.update(id, uid, patch));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long uid = (Long) request.getAttribute("userId");
        if (uid == null) return ApiResponse.error(401, "未登录");
        fridgeService.delete(id, uid);
        return ApiResponse.success(null);
    }
}

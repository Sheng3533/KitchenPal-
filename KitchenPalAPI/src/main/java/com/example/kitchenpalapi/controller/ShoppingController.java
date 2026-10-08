package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.ShoppingItem;
import com.example.kitchenpalapi.service.ShoppingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping")
@CrossOrigin(origins = "*")
public class ShoppingController {

    @Autowired
    private ShoppingService shoppingService;

    private Long uid(HttpServletRequest request) {
        Long uid = (Long) request.getAttribute("userId");
        if (uid == null) throw new RuntimeException("未登录");
        return uid;
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<ShoppingItem>> list(@PathVariable Long userId) {
        return ApiResponse.success(shoppingService.list(userId));
    }

    @PostMapping("/{userId}/item")
    public ApiResponse<ShoppingItem> add(@PathVariable Long userId, @RequestBody ShoppingItem item,
                                         HttpServletRequest request) {
        Long uid = uid(request);
        return ApiResponse.success("添加成功", shoppingService.add(uid, item));
    }

    @PutMapping("/item/{id}")
    public ApiResponse<ShoppingItem> update(@PathVariable Long id, @RequestBody ShoppingItem patch,
                                           HttpServletRequest request) {
        Long uid = uid(request);
        return ApiResponse.success("更新成功", shoppingService.update(id, uid, patch));
    }

    @DeleteMapping("/item/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long uid = uid(request);
        shoppingService.delete(id, uid);
        return ApiResponse.success(null);
    }

    @PostMapping("/{userId}/sync")
    public ApiResponse<List<ShoppingItem>> sync(@PathVariable Long userId, @RequestBody List<ShoppingItem> items,
                                                HttpServletRequest request) {
        Long uid = uid(request);
        return ApiResponse.success("同步成功", shoppingService.replaceAll(uid, items));
    }

    @DeleteMapping("/{userId}/clear")
    public ApiResponse<Void> clear(@PathVariable Long userId, HttpServletRequest request) {
        Long uid = uid(request);
        shoppingService.clear(uid);
        return ApiResponse.success(null);
    }
}

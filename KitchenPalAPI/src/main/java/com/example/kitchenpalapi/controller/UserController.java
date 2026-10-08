package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.User;
import com.example.kitchenpalapi.service.UserService;
import com.example.kitchenpalapi.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        try {
            String phone = body.get("phone");
            String password = body.get("password");
            String nickname = body.get("nickname");
            User user = userService.register(phone, password, nickname);
            Map<String, Object> result = new HashMap<>();
            result.put("token", jwtUtil.generateToken(user.getId(), user.getPhone()));
            result.put("user", user);
            return ApiResponse.success("注册成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        try {
            String phone = body.get("phone");
            String password = body.get("password");
            User user = userService.login(phone, password);
            Map<String, Object> result = new HashMap<>();
            result.put("token", jwtUtil.generateToken(user.getId(), user.getPhone()));
            result.put("user", user);
            return ApiResponse.success("登录成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 找回密码（演示版：无短信/邮箱验证码，仅按手机号直接重置）。
     * 生产环境应叠加验证码或安全问题校验后再放行。
     */
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String phone = body.get("phone");
            String newPassword = body.get("newPassword");
            if (phone == null || newPassword == null || newPassword.length() < 6) {
                return ApiResponse.error("手机号或新密码不合法");
            }
            userService.resetPassword(phone, newPassword);
            return ApiResponse.success("密码重置成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updated = userService.updateUser(id, user);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<List<User>> list() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<User> toggleStatus(@PathVariable Long id) {
        User user = userService.getUserById(id);
        // 切换状态（假设 User 实体有 status 字段，1=正常, 0=禁用）
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        User updated = userService.updateUser(id, user);
        return ApiResponse.success(updated);
    }

    @PostMapping("/change-password")
    public ApiResponse<Map<String, Object>> changePassword(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String currentPassword = body.get("currentPassword").toString();
            String newPassword = body.get("newPassword").toString();
            boolean success = userService.changePassword(userId, currentPassword, newPassword);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            return ApiResponse.success("密码修改成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/profile")
    public ApiResponse<User> getProfile(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/preferences")
    public ApiResponse<User> updatePreferences(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            User user = userService.getUserById(id);
            String tastePreferences = body.get("tastePreferences");
            user.setTastePreferences(tastePreferences);
            User updated = userService.updateUser(id, user);
            return ApiResponse.success("口味偏好更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/settings")
    public ApiResponse<User> updateSettings(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            User user = userService.getUserById(id);
            if (body.containsKey("darkMode")) {
                user.setDarkMode((Boolean) body.get("darkMode"));
            }
            if (body.containsKey("cookingNotify")) {
                user.setCookingNotify((Boolean) body.get("cookingNotify"));
            }
            User updated = userService.updateUser(id, user);
            return ApiResponse.success("设置更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app")
@CrossOrigin(origins = "*")
public class AppVersionController {

    @GetMapping("/version/check")
    public ApiResponse<Map<String, Object>> checkVersion() {
        Map<String, Object> result = new HashMap<>();
        result.put("version", "1.0.0");
        result.put("updateUrl", "");
        result.put("hasUpdate", false);
        result.put("changelog", "");
        return ApiResponse.success(result);
    }

    @GetMapping("/agreement")
    public ApiResponse<Map<String, String>> getAgreement(@RequestParam String type) {
        Map<String, String> result = new HashMap<>();
        if ("privacy".equals(type)) {
            result.put("title", "隐私政策");
            result.put("content", "隐私政策内容\n\n我们非常重视您的个人信息和隐私保护。在您使用本应用服务前，请仔细阅读本隐私政策。\n\n1. 信息收集\n我们可能会收集您的设备信息、使用记录等，以提供更好的服务。\n\n2. 信息使用\n我们使用收集的信息来改进产品体验、提供个性化服务。\n\n3. 信息安全\n我们采取多种安全措施保护您的信息安全。\n\n4. 联系我们\n如有任何疑问，请通过应用内反馈联系我们。");
        } else if ("terms".equals(type)) {
            result.put("title", "用户协议");
            result.put("content", "用户协议\n\n欢迎使用 KitchenPal 应用！\n\n一、服务内容\n本应用为您提供菜谱推荐、烹饪记录、营养分析等服务。\n\n二、用户注册\n您需要注册账号才能使用部分功能，请确保提供真实信息。\n\n三、用户行为规范\n请遵守法律法规，不得发布违法违规内容。\n\n四、知识产权\n本应用内容受知识产权法保护。\n\n五、协议修改\n我们保留修改协议的权利，修改后的协议将在应用内公布。");
        } else {
            result.put("title", "");
            result.put("content", "");
        }
        return ApiResponse.success(result);
    }
}

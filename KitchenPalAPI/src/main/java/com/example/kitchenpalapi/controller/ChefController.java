package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.Chef;
import com.example.kitchenpalapi.repository.ChefRepository;
import com.example.kitchenpalapi.service.LlmClient;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chef")
@CrossOrigin(origins = "*")
public class ChefController {

    @Autowired
    private ChefRepository chefRepository;

    @Autowired
    private LlmClient llmClient;

    @GetMapping("/list")
    public ApiResponse<List<Chef>> list() {
        List<Chef> chefs = chefRepository.findAll();
        return ApiResponse.success(chefs);
    }

    @GetMapping("/{id}")
    public ApiResponse<Chef> getById(@PathVariable Long id) {
        Optional<Chef> chef = chefRepository.findById(id);
        if (chef.isPresent()) {
            return ApiResponse.success(chef.get());
        }
        return ApiResponse.error(404, "大厨不存在");
    }

    @PostMapping
    public ApiResponse<Chef> create(@RequestBody Chef chef) {
        Chef saved = chefRepository.save(chef);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<Chef> update(@PathVariable Long id, @RequestBody Chef chef) {
        Optional<Chef> existing = chefRepository.findById(id);
        if (!existing.isPresent()) {
            return ApiResponse.error(404, "大厨不存在");
        }
        chef.setId(id);
        Chef updated = chefRepository.save(chef);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!chefRepository.existsById(id)) {
            return ApiResponse.error(404, "大厨不存在");
        }
        chefRepository.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/{id}/chat")
    public ApiResponse<Map<String, Object>> chat(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ApiResponse.error("请输入问题");
        }

        Optional<Chef> chefOpt = chefRepository.findById(id);
        if (!chefOpt.isPresent()) {
            return ApiResponse.error(404, "大厨不存在");
        }

        Chef chef = chefOpt.get();

        String systemPrompt = String.format(
                "你是%s，一位%s，擅长%s。你的烹饪风格是%s，从业%d年，招牌菜是%s。请以这位大厨的身份和口吻回答用户的问题。",
                chef.getName(),
                chef.getTitle(),
                chef.getSpecialty(),
                chef.getStyle(),
                chef.getExperienceYears(),
                chef.getSignatureDish()
        );

        ArrayNode messages = llmClient.messages();
        messages.add(llmClient.text("system", systemPrompt));
        messages.add(llmClient.text("user", message));

        String content = llmClient.chat(messages, 2000, 0.7);
        if (content != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("reply", content);
            return ApiResponse.success("回答成功", result);
        }

        String fallbackReply = String.format("你好！我是%s，%s。很高兴与你交流关于烹饪的话题，请继续问我吧！",
                chef.getName(), chef.getTitle());
        Map<String, Object> result = new HashMap<>();
        result.put("reply", fallbackReply);
        return ApiResponse.success("回答成功", result);
    }
}

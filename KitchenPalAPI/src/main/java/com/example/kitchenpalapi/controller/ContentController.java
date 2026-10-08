package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.Content;
import com.example.kitchenpalapi.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/content")
@CrossOrigin(origins = "*")
public class ContentController {

    @Autowired
    private ContentRepository contentRepository;

    @GetMapping("/list")
    public ApiResponse<List<Content>> list() {
        List<Content> contents = contentRepository.findAll();
        return ApiResponse.success(contents);
    }

    @GetMapping("/{id}")
    public ApiResponse<Content> getById(@PathVariable Long id) {
        Optional<Content> content = contentRepository.findById(id);
        if (content.isPresent()) {
            return ApiResponse.success(content.get());
        }
        return ApiResponse.error(404, "内容不存在");
    }

    @GetMapping("/published")
    public ApiResponse<List<Content>> published() {
        List<Content> contents = contentRepository.findByStatus(1);
        return ApiResponse.success(contents);
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<Content>> getByType(@PathVariable String type) {
        List<Content> contents = contentRepository.findByType(type);
        return ApiResponse.success(contents);
    }

    @PostMapping
    public ApiResponse<Content> create(@RequestBody Content content) {
        content.setCreateTime(LocalDateTime.now().toString());
        content.setUpdateTime(LocalDateTime.now().toString());
        Content saved = contentRepository.save(content);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<Content> update(@PathVariable Long id, @RequestBody Content content) {
        Optional<Content> existing = contentRepository.findById(id);
        if (!existing.isPresent()) {
            return ApiResponse.error(404, "内容不存在");
        }
        content.setId(id);
        content.setUpdateTime(LocalDateTime.now().toString());
        if (content.getCreateTime() == null) {
            content.setCreateTime(existing.get().getCreateTime());
        }
        Content updated = contentRepository.save(content);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!contentRepository.existsById(id)) {
            return ApiResponse.error(404, "内容不存在");
        }
        contentRepository.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}

package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.config.KnowledgeSeed;
import com.example.kitchenpalapi.entity.KnowledgeEntry;
import com.example.kitchenpalapi.repository.KnowledgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin(origins = "*")
public class KnowledgeController {

    @Autowired
    private KnowledgeRepository knowledgeRepository;

    @GetMapping
    public ApiResponse<List<KnowledgeEntry>> list(@RequestParam(required = false) String category) {
        List<KnowledgeEntry> data = (category == null || category.isBlank())
                ? knowledgeRepository.findAll()
                : knowledgeRepository.findByCategory(category);
        return ApiResponse.success("查询成功", data);
    }

    @PostMapping
    public ApiResponse<KnowledgeEntry> add(@RequestBody KnowledgeEntry entry) {
        if (entry.getTitle() == null || entry.getTitle().isBlank()
                || entry.getContent() == null || entry.getContent().isBlank()) {
            return ApiResponse.error("标题和正文不能为空");
        }
        if (entry.getCategory() == null || entry.getCategory().isBlank()) {
            entry.setCategory("其他");
        }
        KnowledgeEntry saved = knowledgeRepository.save(entry);
        return ApiResponse.success("添加成功", saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeEntry> update(@PathVariable Long id, @RequestBody KnowledgeEntry entry) {
        return knowledgeRepository.findById(id).map(existing -> {
            if (entry.getCategory() != null) existing.setCategory(entry.getCategory());
            if (entry.getTitle() != null) existing.setTitle(entry.getTitle());
            if (entry.getContent() != null) existing.setContent(entry.getContent());
            if (entry.getKeywords() != null) existing.setKeywords(entry.getKeywords());
            if (entry.getSource() != null) existing.setSource(entry.getSource());
            return ApiResponse.success("更新成功", knowledgeRepository.save(existing));
        }).orElse(ApiResponse.error("条目不存在"));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!knowledgeRepository.existsById(id)) {
            return ApiResponse.error("条目不存在");
        }
        knowledgeRepository.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/seed")
    public ApiResponse<Map<String, Object>> reseed() {
        knowledgeRepository.deleteAll();
        knowledgeRepository.saveAll(KnowledgeSeed.getDefaultEntries());
        long count = knowledgeRepository.count();
        return ApiResponse.success("知识库已重新播种", Map.of("count", count));
    }
}

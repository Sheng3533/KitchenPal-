package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.entity.Video;
import com.example.kitchenpalapi.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoController {

    @Autowired
    private VideoRepository videoRepository;

    @GetMapping("/list")
    public ApiResponse<List<Video>> list() {
        List<Video> videos = videoRepository.findAll();
        return ApiResponse.success(videos);
    }

    @GetMapping("/{id}")
    public ApiResponse<Video> getById(@PathVariable Long id) {
        Optional<Video> video = videoRepository.findById(id);
        if (video.isPresent()) {
            return ApiResponse.success(video.get());
        }
        return ApiResponse.error(404, "视频不存在");
    }

    @GetMapping("/published")
    public ApiResponse<List<Video>> published() {
        List<Video> videos = videoRepository.findByStatus(1);
        return ApiResponse.success(videos);
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<Video>> getByCategory(@PathVariable String category) {
        List<Video> videos = videoRepository.findByCategory(category);
        return ApiResponse.success(videos);
    }

    @PostMapping
    public ApiResponse<Video> create(@RequestBody Video video) {
        video.setCreateTime(LocalDateTime.now().toString());
        Video saved = videoRepository.save(video);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<Video> update(@PathVariable Long id, @RequestBody Video video) {
        Optional<Video> existing = videoRepository.findById(id);
        if (!existing.isPresent()) {
            return ApiResponse.error(404, "视频不存在");
        }
        video.setId(id);
        if (video.getCreateTime() == null) {
            video.setCreateTime(existing.get().getCreateTime());
        }
        Video updated = videoRepository.save(video);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!videoRepository.existsById(id)) {
            return ApiResponse.error(404, "视频不存在");
        }
        videoRepository.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}

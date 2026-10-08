package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import com.example.kitchenpalapi.service.DailyImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-image")
public class DailyImageController {

    @Autowired
    private DailyImageService dailyImageService;

    /** 返回当天美食图片信息；当天尚未生成时自动生成一次（每天仅一次）。 */
    @GetMapping
    public ApiResponse<DailyImageService.DailyImageInfo> today() {
        return ApiResponse.success(dailyImageService.ensureToday());
    }
}

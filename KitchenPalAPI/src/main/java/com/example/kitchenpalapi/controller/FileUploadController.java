package com.example.kitchenpalapi.controller;

import com.example.kitchenpalapi.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 通用文件上传：成品照 / 头像等。保存到 image.upload-dir（外部目录，运行时可写），
 * 通过 /uploads/** 静态资源暴露。需登录（JwtAuthFilter 已拦截无 token 请求）。
 */
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Value("${image.upload-dir:d:/project/KitchenPal/KitchenPalAPI/static/uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "type", defaultValue = "cooking") String type,
                                      HttpServletRequest request) {
        Long uid = (Long) request.getAttribute("userId");
        if (uid == null) return ApiResponse.error(401, "未登录");
        if (file == null || file.isEmpty()) return ApiResponse.error("文件为空");
        try {
            String ext = "";
            String orig = file.getOriginalFilename();
            if (orig != null && orig.contains(".")) {
                ext = orig.substring(orig.lastIndexOf('.')).toLowerCase();
                if (!ext.matches("\\.(jpg|jpeg|png|webp|gif)$")) ext = ".jpg";
            }
            String dir = uploadDir + File.separator + type;
            Files.createDirectories(Path.of(dir));
            String filename = type + "_" + uid + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
            file.transferTo(new File(dir + File.separator + filename));
            String url = "/uploads/" + type + "/" + filename;
            return ApiResponse.success("上传成功", url);
        } catch (Exception e) {
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }
}

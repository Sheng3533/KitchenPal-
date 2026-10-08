package com.example.kitchenpalapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将每日生成的图片目录映射为静态资源：
 * /daily/2026-09-26.png -> file:{image.save-dir}/2026-09-26.png
 * image.save-dir 默认指向项目根目录 static/images，按日期命名、跨天累积、不覆盖。
 * 存在外部目录（非 jar 内），保证运行时可写。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${image.save-dir}")
    private String dailyDir;

    @Value("${image.upload-dir:d:/project/KitchenPal/KitchenPalAPI/static/uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + dailyDir;
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/daily/**")
                .addResourceLocations(location);

        String uploadLocation = "file:" + uploadDir;
        if (!uploadLocation.endsWith("/")) uploadLocation = uploadLocation + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}

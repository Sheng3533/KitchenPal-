package com.example.kitchenpalapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc-openapi 配置：访问 http://localhost:18080/swagger-ui.html 查看全部接口。
 * 仅用于开发/演示，生产可加开关关闭。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kitchenPalOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("KitchenPal API")
                .description("厨房助手后端接口文档（菜谱 / 用户 / 收藏 / 历史 / 通知 / 评论 / AI 助手）")
                .version("1.0.0")
                .contact(new Contact().name("KitchenPal").email("dev@kitchenpal.local")));
    }
}

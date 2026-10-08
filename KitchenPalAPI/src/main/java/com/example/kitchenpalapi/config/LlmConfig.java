package com.example.kitchenpalapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LlmConfig {

    @Value("${llm.api-key:your-api-key}")
    private String apiKey;

    @Value("${llm.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${llm.model:qwen3.7-plus}")
    private String model;

    @Value("${llm.vision-model:}")
    private String visionModel;

    @Value("${llm.embed-model:}")
    private String embedModel;

    @Bean
    public RestTemplate llmRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(600000);
        return new RestTemplate(factory);
    }

    @Bean
    public String llmApiKey() {
        return apiKey;
    }

    @Bean
    public String llmBaseUrl() {
        return baseUrl;
    }

    @Bean
    public String llmModel() {
        return model;
    }

    @Bean
    public String llmVisionModel() {
        return visionModel;
    }

    @Bean
    public String llmEmbedModel() {
        return embedModel;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
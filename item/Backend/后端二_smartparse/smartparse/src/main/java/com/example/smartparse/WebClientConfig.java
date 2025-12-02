package com.example.smartparse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url.summarize}")
    private String apiUrl;

    /**
     * 【核心修改】直接将配置好的 WebClient 实例注册为 Bean。
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
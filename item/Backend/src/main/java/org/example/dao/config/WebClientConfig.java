package org.example.dao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ai.deepseek.api-url}")
    private String deepSeekApiUrl;

    @Value("${ai.deepseek.api-key}")
    private String deepSeekApiKey;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(deepSeekApiUrl)
                .defaultHeaders(headers -> {
                    headers.setBearerAuth(deepSeekApiKey);
                    headers.set("Content-Type", "application/json");
                })
                .build();
    }
}
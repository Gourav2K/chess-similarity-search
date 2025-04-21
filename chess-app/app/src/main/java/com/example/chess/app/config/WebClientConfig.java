package com.example.chess.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${llm.service.base.url}")
    String llmServiceBaseURL;

    @Bean
    public WebClient llmServiceWebClient() {
        return WebClient.builder()
                .baseUrl(llmServiceBaseURL)
                .build();
    }
}

package com.example.chess.app.service;

import com.example.chess.app.dto.request.SimilarityResult;
import com.example.chess.app.model.LLMRequest;
import com.example.chess.app.model.LLMResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class LLMClient {

    private final WebClient llmServiceWebClient;

    public LLMClient(WebClient llmServiceWebClient) {
        this.llmServiceWebClient = llmServiceWebClient;
    }

    public Mono<LLMResponse> analyzeStrategy(List<LLMRequest.GameSummaryRequest> positions) {
        LLMRequest request = LLMRequest.builder().positions(positions).build();
        return llmServiceWebClient.post()
                .uri("/analyze-strategy")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LLMResponse.class);
    }
}


package com.example.chess.app.controller;

import com.example.chess.app.dto.request.SimilarityRequest;
import com.example.chess.app.dto.request.SimilarityResult;
import com.example.chess.app.model.LLMResponse;
import com.example.chess.app.service.PositionMatchingService;
import com.example.chess.app.service.PositionService;
import com.example.chess.app.service.SummaryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Controller
public class ChessPositionSearchResolver {

    private final PositionMatchingService matchingService;
    private final PositionService positionService;
    private final SummaryService summaryService;

    public ChessPositionSearchResolver(PositionMatchingService matchingService, PositionService positionService, SummaryService summaryService) {
        this.matchingService = matchingService;
        this.positionService = positionService;
        this.summaryService = summaryService;
    }

    @QueryMapping
    public Flux<SimilarityResult> findSimilarPositionsByFen(
            @Argument String fen,
            @Argument(name = "request") SimilarityRequest requestDTO) {

        return positionService.convertFenToPosition(fen)
                .flatMapMany(position -> {
                    requestDTO.toDomain();
                    return matchingService.findSimilarPositions(position, requestDTO);
                });
    }

    @QueryMapping
    public Mono<LLMResponse> generateSummaryForPositions(
            @Argument List<UUID> positionIds,
            @Argument String side
    ) {
        return summaryService.generateLLMSummaryFromPositionIds(positionIds, side);
    }

}
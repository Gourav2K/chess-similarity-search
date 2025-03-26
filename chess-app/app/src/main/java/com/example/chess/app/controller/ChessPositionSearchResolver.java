package com.example.chess.app.controller;

import com.example.chess.app.dto.request.SimilarityRequest;
import com.example.chess.app.dto.request.SimilarityResult;
import com.example.chess.app.service.PositionMatchingService;
import com.example.chess.app.service.PositionService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class ChessPositionSearchResolver {

    private final PositionMatchingService matchingService;
    private final PositionService positionService;

    public ChessPositionSearchResolver(PositionMatchingService matchingService, PositionService positionService) {
        this.matchingService = matchingService;
        this.positionService = positionService;
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

}
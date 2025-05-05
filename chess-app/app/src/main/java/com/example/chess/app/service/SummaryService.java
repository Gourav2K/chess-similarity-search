package com.example.chess.app.service;

import com.example.chess.app.dto.request.SimilarityResult;
import com.example.chess.app.model.Game;
import com.example.chess.app.model.LLMRequest;
import com.example.chess.app.model.LLMResponse;
import com.example.chess.app.model.Position;
import com.example.chess.app.repository.GameRepository;
import com.example.chess.app.repository.PositionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class SummaryService {
    private final PositionRepository positionRepository;
    private final GameRepository gameRepository;
    private final LLMClient llmClient;

    public SummaryService(PositionRepository positionRepository, GameRepository gameRepository, LLMClient llmClient) {
        this.positionRepository = positionRepository;
        this.gameRepository = gameRepository;
        this.llmClient = llmClient;
    }

    public Mono<LLMResponse> generateLLMSummaryFromPositionIds(List<UUID> positionIds, String side) {
        return positionRepository.findAllById(positionIds)
                .collectList()
                .flatMap(positions -> {
                    List<String> gameIds = positions.stream()
                            .map(Position::getGameId)
                            .toList();

                    return gameRepository.findAllById(gameIds)
                            .collectList()
                            .flatMap(games -> {
                                List<SimilarityResult> results = positions.stream()
                                        .map(pos -> {
                                            Game game = games.stream()
                                                    .filter(g -> g.getId().equals(pos.getGameId()))
                                                    .findFirst()
                                                    .orElseThrow(() -> new IllegalStateException("Game not found"));

                                            return SimilarityResult.builder()
                                                    .game(game)
                                                    .position(pos)
                                                    .build();
                                        })
                                        .toList();

                                return generateLLMSummary(results, side);
                            });
                });
    }


    public Mono<LLMResponse> generateLLMSummary(List<SimilarityResult> results, String side) {
        List<LLMRequest.GameSummaryRequest> requestList = results.stream()
                .map(result -> LLMRequest.GameSummaryRequest.builder()
                        .gameId(result.getGame().getId())
                        .fen(result.getPosition().getFen())
                        .moves(extractRemainingPgn(result.getPosition().getFen(), result.getGame().getPgn()))
                        .side(side)
                        .build())
                .toList();

        return llmClient.analyzeStrategy(requestList);
    }

    private String extractRemainingPgn(String fen, String fullPgn) {
        String[] fenParts = fen.split(" ");
        if (fenParts.length < 6) return "";

        String sideToMove = fenParts[1];
        int fullMoveNumber = Integer.parseInt(fenParts[5]);
        String movePrefix = fullMoveNumber + ".";

        int index = fullPgn.indexOf(movePrefix);
        if (index == -1) return "";

        String remaining = fullPgn.substring(index).trim();

        if ("b".equals(sideToMove)) {
            // Skip the move number and white’s move
            // E.g., "21. Qh6 O-O-O" → remove "21. Qh6"
            String[] tokens = remaining.split("\\s+", 3);
            if (tokens.length >= 3) {
                return tokens[2]; // skip "21." and White's move
            } else {
                return ""; // Not enough moves to process
            }
        }

        return remaining;
    }
}

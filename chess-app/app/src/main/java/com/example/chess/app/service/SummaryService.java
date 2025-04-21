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
                        .moves(extractRemainingPgn(result.getPosition().getMoveNumber(), result.getGame().getPgn()))  // You’ll need to make sure `moves` exists on Game or Position
                        .side(side)
                        .build())
                .toList();

        return llmClient.analyzeStrategy(requestList);
    }

    private String extractRemainingPgn(int moveNumber, String fullPgn) {
        int fullMove = (int) Math.floor(moveNumber / 2.0);
        String movePrefix = fullMove + ".";

        int index = fullPgn.indexOf(movePrefix);

        if (index == -1) {
            // Couldn’t find the move number in PGN
            return "";
        }

        return fullPgn.substring(index).trim();
    }
}

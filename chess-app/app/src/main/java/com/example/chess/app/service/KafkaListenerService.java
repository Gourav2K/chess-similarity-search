package com.example.chess.app.service;

import com.example.chess.app.dto.BitboardsDTO;
import com.example.chess.app.dto.GameDTO;
import com.example.chess.app.dto.GameMetadataDTO;
import com.example.chess.app.dto.PositionDTO;
import com.example.chess.app.model.Game;
import com.example.chess.app.model.Position;
import com.example.chess.app.repository.GameRepository;
import com.example.chess.app.repository.PositionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaListenerService {

    private final ObjectMapper objectMapper;
    private final GameRepository gameRepository;
    private final PositionRepository positionRepository;

    @KafkaListener(topics = "${kafka.topics.chess-games}", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeChessGames(List<String> messages) {
        log.info("Received batch of {} chess game messages", messages.size());

        // Convert messages to GameDTO objects
        List<GameDTO> gameDataList = messages.stream()
                .map(this::parseGameData)
                .filter(gameData -> gameData != null)  // Filter out parsing failures
                .toList();

        // Process all games as a single reactive flow
        Flux.fromIterable(gameDataList)
                .flatMap(this::processGameData)
                .collectList()
                .doOnSuccess(result -> log.info("Processed batch of {} messages", messages.size()))
                .doOnError(e -> log.error("Error processing batch: {}", e.getMessage()))
                .subscribe();  // Trigger execution without blocking
    }

    private GameDTO parseGameData(String message) {
        try {
            return objectMapper.readValue(message, GameDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing message: {}", e.getMessage());
            return null;
        }
    }

    private Mono<Void> processGameData(GameDTO gameData) {
        // Generate a unique game ID if not provided
        String gameId = gameData.getGameMetadata().getGameId();
        if (gameId == null || gameId.isEmpty()) {
            gameId = UUID.randomUUID().toString();
            gameData.getGameMetadata().setGameId(gameId);
        }

        final String finalGameId = gameId;

        // Create Game entity
        Game game = mapToGame(gameData.getGameMetadata());
        game.setIsNew(true);

        // Create Position entities
        List<Position> positions = mapToPositions(gameData.getPositions(), finalGameId);

        // Save game first, then save all positions, returning a single Mono that completes when all operations complete
        return gameRepository.save(game)
                .then(Flux.fromIterable(positions)
                        .flatMap(positionRepository::save)
                        .then())
                .doOnSuccess(v -> log.debug("Successfully processed game {}", finalGameId))
                .doOnError(e -> log.error("Error processing game {}: {}", finalGameId, e.getMessage()));
    }

    private Game mapToGame(GameMetadataDTO metadata) {
        return Game.builder()
                .id(metadata.getGameId())
                .result(metadata.getResult())
                .whiteElo(metadata.getWhiteElo())
                .blackElo(metadata.getBlackElo())
                .gameType(metadata.getGameType())
                .date(metadata.getDate())
                .whiteName(metadata.getWhiteName())
                .blackName(metadata.getBlackName())
                .eco(metadata.getEco())
                .timeControl(metadata.getTimeControl())
                .opening(metadata.getOpening())
                .site(metadata.getSite())
                .build();
    }

    private List<Position> mapToPositions(List<PositionDTO> positionDTOs, String gameId) {
        List<Position> positions = new ArrayList<>();

        for (PositionDTO dto : positionDTOs) {

            Position position = Position.builder()
                    .id(UUID.randomUUID())
                    .gameId(gameId)
                    .moveNumber(dto.getMoveNumber())
                    .whiteKing(dto.getWhiteKing())
                    .blackKing(dto.getBlackKing())
                    .whiteQueens(dto.getWhiteQueens())
                    .whiteRooks(dto.getWhiteRooks())
                    .whiteBishops(dto.getWhiteBishops())
                    .whiteKnights(dto.getWhiteKnights())
                    .blackQueens(dto.getBlackQueens())
                    .blackRooks(dto.getBlackRooks())
                    .blackBishops(dto.getBlackBishops())
                    .blackKnights(dto.getBlackKnights())
                    .whitePawns(dto.getWhitePawns())
                    .blackPawns(dto.getBlackPawns())
                    .sideToMove(dto.getSideToMove())
                    .castlingRights(dto.getCastlingRights())
                    .enPassantSquare(dto.getEnPassantSquare())
                    .halfMoveClock(dto.getHalfmoveClock())
                    .fullMoveNumber(dto.getFullmoveNumber())
                    .fen(dto.getFen())
                    .isNew(true)
                    .build();

            positions.add(position);
        }

        return positions;
    }
}
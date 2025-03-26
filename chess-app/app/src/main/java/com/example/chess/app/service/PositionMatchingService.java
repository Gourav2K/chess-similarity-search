package com.example.chess.app.service;

import com.example.chess.app.dto.request.SimilarityRequest;
import com.example.chess.app.dto.request.SimilarityResult;
import com.example.chess.app.model.Game;
import com.example.chess.app.model.Position;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Service
public class PositionMatchingService {

    private final R2dbcEntityTemplate template;

    public PositionMatchingService(R2dbcEntityTemplate template) {
        this.template = template;
    }

    /**
     * Find similar positions based on the requested pieces and filters
     */
    public Flux<SimilarityResult> findSimilarPositions(Position position, SimilarityRequest request) {
        StringBuilder query = new StringBuilder();

        // Start SELECT clause
        query.append("""
        SELECT p.id AS position_id, p.game_id, p.move_number""");

        // Build similarity score dynamically based on pieceTypes
        buildSimilarityScoreClause(query, position, request);

        // FROM and WHERE clause
        query.append(" FROM positions p WHERE 1=1 ");

        // Optionally filter by matching piece presence (bitwise for pawns, LIKE or equality for others)
        addSearchFilters(query, position, request);

        // ORDER BY score
        query.append(" ORDER BY similarity_score DESC LIMIT :limit ");

        // Prepare query
        DatabaseClient.GenericExecuteSpec spec = template.getDatabaseClient()
                .sql(query.toString());

        // Bind parameters
        spec = spec.bind("limit", request.getLimit());

        // Bind piece-specific values (like whitePawns, etc.)
        spec = bindFilterValues(spec, position, request);

        // Execute and fetch
        return spec.map((row, metadata) -> {
                    SimilarityResult result = new SimilarityResult();
                    result.setPositionId(row.get("position_id", UUID.class));
                    result.setGameId(row.get("game_id", String.class));
                    result.setMoveNumber(row.get("move_number", Integer.class));
                    result.setSimilarityScore(row.get("similarity_score", Double.class));
                    return result;
                })
                .all()
                .concatMap(result -> enrichWithPositionAndGame(result));
    }

    private void buildSimilarityScoreClause(StringBuilder query, Position position, SimilarityRequest request) {
        List<String> scoreParts = new ArrayList<>();

        for (String pieceType : request.getPieceTypes()) {
            switch (pieceType) {
                case "whitePawn" -> scoreParts.add(similarityForBitboard("white_pawns", position.getWhitePawns()));
                case "blackPawn" -> scoreParts.add(similarityForBitboard("black_pawns", position.getBlackPawns()));
                case "whiteKing" -> scoreParts.add(equalityScore("white_king", position.getWhiteKing()));
                case "blackKing" -> scoreParts.add(equalityScore("black_king", position.getBlackKing()));
                // Could use setRegexOverlapScore or setOverlapScore
                default -> scoreParts.add(setRegexOverlapScore(pieceTypeToColumn(pieceType), getPieceValue(position, pieceType)));
            }
        }

        query.append(", (")
                .append(String.join(" + ", scoreParts.isEmpty() ? List.of("0.0") : scoreParts))
                .append(") / ")
                .append(scoreParts.size() > 0 ? scoreParts.size() : 1)
                .append(" AS similarity_score");
    }

    private void addSearchFilters(StringBuilder query, Position position, SimilarityRequest request) {
        for (String pieceType : request.getPieceTypes()) {
            switch (pieceType) {
                case "whitePawn" -> query.append(" AND (p.white_pawns & :whitePawns) > 0 ");
                case "blackPawn" -> query.append(" AND (p.black_pawns & :blackPawns) > 0 ");
                default -> {
                    String col = pieceTypeToColumn(pieceType);
                    if (getPieceValue(position, pieceType) != null && !getPieceValue(position, pieceType).isEmpty()) {
                        query.append(" AND p.").append(col).append(" IS NOT NULL ");
                    }
                }
            }
        }
    }

    private DatabaseClient.GenericExecuteSpec bindFilterValues(DatabaseClient.GenericExecuteSpec spec, Position position, SimilarityRequest request) {
        for (String pieceType : request.getPieceTypes()) {
            switch (pieceType) {
                case "whitePawn" -> spec = spec.bind("whitePawns", position.getWhitePawns());
                case "blackPawn" -> spec = spec.bind("blackPawns", position.getBlackPawns());
            }
        }
        return spec;
    }

    private Mono<SimilarityResult> enrichWithPositionAndGame(SimilarityResult result) {
        return template.select(Position.class)
                .matching(query(where("id").is(result.getPositionId())))
                .one()
                .doOnNext(result::setPosition)
                .then(template.select(Game.class)
                        .matching(query(where("id").is(result.getGameId())))
                        .one()
                        .doOnNext(result::setGame)
                )
                .thenReturn(result);
    }

    private String pieceTypeToColumn(String pieceType) {
        return switch (pieceType) {
            case "whiteQueen" -> "white_queens";
            case "whiteRook" -> "white_rooks";
            case "whiteBishop" -> "white_bishops";
            case "whiteKnight" -> "white_knights";
            case "blackQueen" -> "black_queens";
            case "blackRook" -> "black_rooks";
            case "blackBishop" -> "black_bishops";
            case "blackKnight" -> "black_knights";
            default -> "";
        };
    }

    private String getPieceValue(Position position, String pieceType) {
        return switch (pieceType) {
            case "whiteQueen" -> position.getWhiteQueens();
            case "whiteRook" -> position.getWhiteRooks();
            case "whiteBishop" -> position.getWhiteBishops();
            case "whiteKnight" -> position.getWhiteKnights();
            case "blackQueen" -> position.getBlackQueens();
            case "blackRook" -> position.getBlackRooks();
            case "blackBishop" -> position.getBlackBishops();
            case "blackKnight" -> position.getBlackKnights();
            default -> "";
        };
    }

    private String similarityForBitboard(String column, long bitboard) {
        return String.format(
                "CASE WHEN BIT_COUNT(%s | %d) = 0 THEN 0.0 ELSE BIT_COUNT(%s & %d)::numeric / BIT_COUNT(%s | %d) END",
                column, bitboard, column, bitboard, column, bitboard
        );
    }

    private String equalityScore(String column, Integer value) {
        return String.format("CASE WHEN %s = %d THEN 1.0 ELSE 0.0 END", column, value);
    }

    private String setOverlapScore(String column, String valuesCsv) {
        if (valuesCsv == null || valuesCsv.isBlank()) {
            return "0.0";
        }

        // Convert "1,8" → ['1', '8']
        String[] squares = valuesCsv.split(",");
        int countA = squares.length;
        String listOfValues = Arrays.stream(squares)
                .map(s -> "'" + s.trim() + "'")
                .collect(Collectors.joining(", "));

        return String.format("""
        CASE
            WHEN ARRAY_LENGTH(STRING_TO_ARRAY('%s', ','), 1) = 0 THEN 0.0
            ELSE (
                SELECT COUNT(*) FROM UNNEST(STRING_TO_ARRAY(%s, ',')) db_val
                WHERE db_val IN (%s)
            )::numeric / %d
        END
        """, valuesCsv, column, listOfValues, countA);
    }

    private String setRegexOverlapScore(String column, String valuesCsv) {
        if (valuesCsv == null || valuesCsv.isBlank()) {
            return "0.0";
        }

        String[] squares = valuesCsv.split(",");
        int count = squares.length;

        List<String> caseStatements = Arrays.stream(squares)
                .map(String::trim)
                .map(sq -> String.format("CASE WHEN p.%s ~ '(^|,)%s(,|$)' THEN 1 ELSE 0 END", column, sq))
                .toList();

        return String.format("(%s)::numeric / %d", String.join(" + ", caseStatements), count);
    }
}

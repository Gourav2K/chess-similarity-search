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

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Service
public class PositionMatchingService {

    private final R2dbcEntityTemplate template;

    public PositionMatchingService(R2dbcEntityTemplate template) {
        this.template = template;
    }

    public Flux<SimilarityResult> findSimilarPositions(Position position, SimilarityRequest request) {
        List<String> filters = buildPrefilterConditions(position, request);
        List<String> scores = buildSimilarityScoreClauses(position, request);

        String sql = assembleSimilaritySQL(filters, scores);

        DatabaseClient.GenericExecuteSpec spec = template.getDatabaseClient().sql(sql);
        spec = bindFilterValues(spec, position, request);
        spec = spec.bind("minElo", request.getMinElo());
        spec = spec.bind("maxElo", request.getMaxElo());
        spec = spec.bind("limit", request.getLimit()*10);

        return spec.map((row, metadata) -> {
                    SimilarityResult result = new SimilarityResult();
                    result.setPositionId(row.get("position_id", UUID.class));
                    result.setGameId(row.get("game_id", String.class));
                    result.setMoveNumber(row.get("move_number", Integer.class));
                    result.setSimilarityScore(row.get("similarity_score", Double.class));
                    return result;
                })
                .all()
                .collectList()
                .flatMapMany(results -> {
                    Set<String> seenGames = new HashSet<>();
                    List<SimilarityResult> uniqueResults = new ArrayList<>();

                    for (SimilarityResult result : results) {
                        if (seenGames.add(result.getGameId())) {
                            uniqueResults.add(result);
                        }
                        if (uniqueResults.size() >= request.getLimit()) break;
                    }

                    return Flux.fromIterable(uniqueResults);
                })
                .concatMap(this::enrichWithPositionAndGame);
    }

    private List<String> buildPrefilterConditions(Position position,SimilarityRequest request) {
        List<String> filters = new ArrayList<>();
        filters.add("g.white_elo BETWEEN :minElo AND :maxElo");
        filters.add("g.black_elo BETWEEN :minElo AND :maxElo");
        for (String pieceType : request.getPieceTypes()) {
            switch (pieceType) {
                case "whitePawn" -> filters.add("white_pawns & :whitePawns > 0");
                case "blackPawn" -> filters.add("black_pawns & :blackPawns > 0");
                case "whiteKing" -> {
                    if (position.getWhiteKing() != null)
                        filters.add("white_king = :whiteKing");
                }
                case "blackKing" -> {
                    if (position.getBlackKing() != null)
                        filters.add("black_king = :blackKing");
                }
                default -> {
                    List<Integer> squares = getPieceValue(position, pieceType);
                    if (squares != null && !squares.isEmpty()) {
                        filters.add(pieceTypeToColumn(pieceType) + " && :" + pieceType);
                    }
                }
            }
        }
        return filters;
    }

    private List<String> buildSimilarityScoreClauses(Position position, SimilarityRequest request) {
        List<String> scores = new ArrayList<>();
        for (String pieceType : request.getPieceTypes()) {
            switch (pieceType) {
                case "whitePawn" -> scores.add(similarityForBitboard("white_pawns", position.getWhitePawns()));
                case "blackPawn" -> scores.add(similarityForBitboard("black_pawns", position.getBlackPawns()));
                case "whiteKing" -> {
                    if (position.getWhiteKing() != null)
                        scores.add(equalityScore("white_king", position.getWhiteKing()));
                }
                case "blackKing" -> {
                    if (position.getBlackKing() != null)
                        scores.add(equalityScore("black_king", position.getBlackKing()));
                }
                default -> {
                    List<Integer> squares = getPieceValue(position, pieceType);
                    if (squares != null && !squares.isEmpty()) {
                        scores.add(arrayOverlapScore(pieceTypeToColumn(pieceType), pieceType));
                    }
                }
            }
        }
        return scores;
    }

    private String assembleSimilaritySQL(List<String> filters, List<String> scores) {
        String filterClause = String.join(" AND ", filters);

        if (scores.isEmpty()) {
            return String.format("""
            WITH filtered_positions AS (
                SELECT p.id
                FROM positions p
                JOIN games g ON g.id = p.game_id
                WHERE %s
                LIMIT 50000
            )
            SELECT 
                p.id AS position_id,
                p.game_id,
                p.move_number,
                0.0 AS similarity_score
            FROM positions p
            JOIN filtered_positions f ON p.id = f.id
            ORDER BY similarity_score DESC
            LIMIT :limit
        """, filterClause);
        }

        String scoreClause = "(" + String.join(" + ", scores) + ") / " + scores.size();

        return String.format("""
        WITH filtered_positions AS (
            SELECT p.id
            FROM positions p
            JOIN games g ON g.id = p.game_id
            WHERE %s
            LIMIT 50000
        )
        SELECT 
            p.id AS position_id,
            p.game_id,
            p.move_number,
            %s AS similarity_score
        FROM positions p
        JOIN filtered_positions f ON p.id = f.id
        ORDER BY similarity_score DESC
        LIMIT :limit
    """, filterClause, scoreClause);
    }

    private DatabaseClient.GenericExecuteSpec bindFilterValues(DatabaseClient.GenericExecuteSpec spec, Position position, SimilarityRequest request) {
        for (String pieceType : request.getPieceTypes()) {
            switch (pieceType) {
                case "whitePawn" -> {
                    if (position.getWhitePawns() != null)
                        spec = spec.bind("whitePawns", position.getWhitePawns());
                }
                case "blackPawn" -> {
                    if (position.getBlackPawns() != null)
                        spec = spec.bind("blackPawns", position.getBlackPawns());
                }
                case "whiteKing" -> {
                    if (position.getWhiteKing() != null)
                        spec = spec.bind("whiteKing", position.getWhiteKing());
                }
                case "blackKing" -> {
                    if (position.getBlackKing() != null)
                        spec = spec.bind("blackKing", position.getBlackKing());
                }
                case "whiteQueen" -> {
                    if (position.getWhiteQueens() != null && !position.getWhiteQueens().isEmpty())
                        spec = spec.bind("whiteQueen", position.getWhiteQueens().toArray(new Integer[0]));
                }
                case "whiteRook" -> {
                    if (position.getWhiteRooks() != null && !position.getWhiteRooks().isEmpty())
                        spec = spec.bind("whiteRook", position.getWhiteRooks().toArray(new Integer[0]));
                }
                case "whiteBishop" -> {
                    if (position.getWhiteBishops() != null && !position.getWhiteBishops().isEmpty())
                        spec = spec.bind("whiteBishop", position.getWhiteBishops().toArray(new Integer[0]));
                }
                case "whiteKnight" -> {
                    if (position.getWhiteKnights() != null && !position.getWhiteKnights().isEmpty())
                        spec = spec.bind("whiteKnight", position.getWhiteKnights().toArray(new Integer[0]));
                }
                case "blackQueen" -> {
                    if (position.getBlackQueens() != null && !position.getBlackQueens().isEmpty())
                        spec = spec.bind("blackQueen", position.getBlackQueens().toArray(new Integer[0]));
                }
                case "blackRook" -> {
                    if (position.getBlackRooks() != null && !position.getBlackRooks().isEmpty())
                        spec = spec.bind("blackRook", position.getBlackRooks().toArray(new Integer[0]));
                }
                case "blackBishop" -> {
                    if (position.getBlackBishops() != null && !position.getBlackBishops().isEmpty())
                        spec = spec.bind("blackBishop", position.getBlackBishops().toArray(new Integer[0]));
                }
                case "blackKnight" -> {
                    if (position.getBlackKnights() != null && !position.getBlackKnights().isEmpty())
                        spec = spec.bind("blackKnight", position.getBlackKnights().toArray(new Integer[0]));
                }
            }
        }
        return spec;
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
            default -> throw new IllegalArgumentException("Unknown piece type: " + pieceType);
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

    private String arrayOverlapScore(String column, String bindParamName) {
        return String.format("""
        (
            SELECT CARDINALITY(
                ARRAY(
                    SELECT UNNEST(p.%s)
                    INTERSECT
                    SELECT UNNEST(:%s)
                )
            )::float / GREATEST(CARDINALITY(:%s), 1)
        )
        """, column, bindParamName, bindParamName);
    }

    private Mono<SimilarityResult> enrichWithPositionAndGame(SimilarityResult result) {
        return template.select(Position.class)
                .matching(query(where("id").is(result.getPositionId())))
                .one()
                .doOnNext(result::setPosition)
                .then(template.select(Game.class)
                        .matching(query(where("id").is(result.getGameId())))
                        .one()
                        .doOnNext(result::setGame))
                .thenReturn(result);
    }

    private List<Integer> getPieceValue(Position position, String pieceType) {
        return switch (pieceType) {
            case "whiteQueen" -> position.getWhiteQueens();
            case "whiteRook" -> position.getWhiteRooks();
            case "whiteBishop" -> position.getWhiteBishops();
            case "whiteKnight" -> position.getWhiteKnights();
            case "blackQueen" -> position.getBlackQueens();
            case "blackRook" -> position.getBlackRooks();
            case "blackBishop" -> position.getBlackBishops();
            case "blackKnight" -> position.getBlackKnights();
            default -> List.of();
        };
    }
}

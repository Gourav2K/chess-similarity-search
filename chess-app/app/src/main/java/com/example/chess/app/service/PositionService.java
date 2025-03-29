package com.example.chess.app.service;

import com.example.chess.app.model.Position;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class PositionService {

    /**
     * Convert a FEN string to our Chess Position object
     */
    public Mono<Position> convertFenToPosition(String fen) {
        return Mono.fromCallable(() -> {
            Position position = new Position();

            // Parse FEN string
            String[] fenParts = fen.split(" ");
            if (fenParts.length < 6) {
                throw new IllegalArgumentException("Invalid FEN string: " + fen);
            }

            String piecePlacement = fenParts[0];
            String activeColor = fenParts[1];
            String castlingAvailability = fenParts[2];
            String enPassantTarget = fenParts[3];
            String halfmoveClock = fenParts[4];
            String fullmoveNumber = fenParts[5];

            // Parse piece placement
            parsePiecePlacement(piecePlacement, position);

            // Set side to move
            position.setSideToMove(activeColor);

            // Parse castling rights
            int castlingRights = 0;
            if (castlingAvailability.contains("K")) castlingRights |= 1;
            if (castlingAvailability.contains("Q")) castlingRights |= 2;
            if (castlingAvailability.contains("k")) castlingRights |= 4;
            if (castlingAvailability.contains("q")) castlingRights |= 8;
            position.setCastlingRights(castlingRights);

            // Parse en passant target square
            if ("-".equals(enPassantTarget)) {
                position.setEnPassantSquare(0);
            } else {
                position.setEnPassantSquare(algebraicToSquareIndex(enPassantTarget));
            }

            // Set fullmove number
            position.setFullMoveNumber(Integer.parseInt(fullmoveNumber));

            return position;
        });
    }

    /**
     * Parse the piece placement part of a FEN string into our position representation
     */
    private void parsePiecePlacement(String piecePlacement, Position position) {
        // Initialize empty position
        position.setWhiteKing(null);
        position.setBlackKing(null);
        position.setWhiteQueens(List.of());
        position.setWhiteRooks(List.of());
        position.setWhiteBishops(List.of());
        position.setWhiteKnights(List.of());
        position.setBlackQueens(List.of());
        position.setBlackRooks(List.of());
        position.setBlackBishops(List.of());
        position.setBlackKnights(List.of());
        position.setWhitePawns(0L);
        position.setBlackPawns(0L);

        // Temporary lists to collect piece positions
        List<Integer> whiteQueens = new ArrayList<>();
        List<Integer> whiteRooks = new ArrayList<>();
        List<Integer> whiteBishops = new ArrayList<>();
        List<Integer> whiteKnights = new ArrayList<>();
        List<Integer> blackQueens = new ArrayList<>();
        List<Integer> blackRooks = new ArrayList<>();
        List<Integer> blackBishops = new ArrayList<>();
        List<Integer> blackKnights = new ArrayList<>();

        // Parse the FEN piece placement
        String[] ranks = piecePlacement.split("/");
        for (int rank = 0; rank < 8; rank++) {
            int file = 0;
            for (int i = 0; i < ranks[7-rank].length(); i++) {
                char c = ranks[7-rank].charAt(i);
                if (Character.isDigit(c)) {
                    file += Character.getNumericValue(c);
                } else {
                    int square = rank * 8 + file;

                    switch (c) {
                        case 'K':
                            position.setWhiteKing(square);
                            break;
                        case 'k':
                            position.setBlackKing(square);
                            break;
                        case 'Q':
                            whiteQueens.add(square);
                            break;
                        case 'q':
                            blackQueens.add(square);
                            break;
                        case 'R':
                            whiteRooks.add(square);
                            break;
                        case 'r':
                            blackRooks.add(square);
                            break;
                        case 'B':
                            whiteBishops.add(square);
                            break;
                        case 'b':
                            blackBishops.add(square);
                            break;
                        case 'N':
                            whiteKnights.add(square);
                            break;
                        case 'n':
                            blackKnights.add(square);
                            break;
                        case 'P':
                            position.setWhitePawns(position.getWhitePawns() | (1L << square));
                            break;
                        case 'p':
                            position.setBlackPawns(position.getBlackPawns() | (1L << square));
                            break;
                    }

                    file++;
                }
            }
        }

        // Convert lists to comma-separated strings
        position.setWhiteQueens(whiteQueens);
        position.setWhiteRooks(whiteRooks);
        position.setWhiteBishops(whiteBishops);
        position.setWhiteKnights(whiteKnights);
        position.setBlackQueens(blackQueens);
        position.setBlackRooks(blackRooks);
        position.setBlackBishops(blackBishops);
        position.setBlackKnights(blackKnights);
    }

    /**
     * Convert algebraic notation (e.g., "e4") to square index (0-63)
     */
    private int algebraicToSquareIndex(String algebraic) {
        if (algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }

        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);

        int file = fileChar - 'a';
        int rank = rankChar - '1';

        return rank * 8 + file;
    }
}

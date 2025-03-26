package com.example.chess.app.dto;

import lombok.Data;

@Data
public class PositionDTO {
    private Integer moveNumber;
    private String sideToMove;
    private Integer castlingRights;
    private Integer enPassantSquare;
    private Integer halfmoveClock;
    private Integer fullmoveNumber;
    private String fen;

    // pieces
    private Long whitePawns;
    private String whiteKnights;
    private String whiteBishops;
    private String whiteRooks;
    private String whiteQueens;
    private Integer whiteKing;
    private Long blackPawns;
    private String blackKnights;
    private String blackBishops;
    private String blackRooks;
    private String blackQueens;
    private Integer blackKing;
}

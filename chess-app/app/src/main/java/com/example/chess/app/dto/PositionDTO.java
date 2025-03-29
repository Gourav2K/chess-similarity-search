package com.example.chess.app.dto;

import lombok.Data;

import java.util.List;

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
    private List<Integer> whiteKnights;
    private List<Integer> whiteBishops;
    private List<Integer> whiteRooks;
    private List<Integer> whiteQueens;
    private Integer whiteKing;
    private Long blackPawns;
    private List<Integer> blackKnights;
    private List<Integer> blackBishops;
    private List<Integer> blackRooks;
    private List<Integer> blackQueens;
    private Integer blackKing;
}

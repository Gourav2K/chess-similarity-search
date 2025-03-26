package com.example.chess.app.dto;

import lombok.Data;

@Data
public class BitboardsDTO {
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

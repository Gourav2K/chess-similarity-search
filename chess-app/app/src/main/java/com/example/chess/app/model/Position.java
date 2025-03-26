package com.example.chess.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("positions")
public class Position implements Persistable<UUID> {
    @Id
    private UUID id;
    private String gameId;
    private Integer moveNumber;

    // Single-piece locations
    private Integer whiteKing;
    private Integer blackKing;

    // Multi-piece locations as comma-separated square indices
    private String whiteQueens;
    private String whiteRooks;
    private String whiteBishops;
    private String whiteKnights;

    private String blackQueens;
    private String blackRooks;
    private String blackBishops;
    private String blackKnights;

    // Bitboards for pawns
    private Long whitePawns;
    private Long blackPawns;

    // Additional position information
    private String sideToMove;
    private Integer castlingRights;
    private Integer enPassantSquare;
    private Integer halfMoveClock;
    private Integer fullMoveNumber;
    private String fen;

    @Transient
    private boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew;
    }
}

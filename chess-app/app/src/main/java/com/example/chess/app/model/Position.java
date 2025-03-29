package com.example.chess.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
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

    // Multi-piece locations as arrays
    private List<Integer> whiteQueens;
    private List<Integer> whiteRooks;
    private List<Integer> whiteBishops;
    private List<Integer> whiteKnights;

    private List<Integer> blackQueens;
    private List<Integer> blackRooks;
    private List<Integer> blackBishops;
    private List<Integer> blackKnights;

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

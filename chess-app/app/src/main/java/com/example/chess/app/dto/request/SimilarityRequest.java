package com.example.chess.app.dto.request;


import com.example.chess.app.dto.enums.Color;
import com.example.chess.app.dto.enums.PieceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class SimilarityRequest {

    // The color (white or black) for which user wants to match piece structures
    private Color color;

    // List of selected pieces (PAWN, KNIGHT, BISHOP, etc.)
    private Set<PieceType> selectedPieces;

    // Max number of similar results to return
    private Integer limit = 20;

    // List of pieces for searching - like whiteBishop, blackPawn, etc
    @JsonIgnore
    private Set<String> pieceTypes;

    public void toDomain() {
        Set<String> pieceTypes = selectedPieces.stream()
                .map(piece -> color.name().toLowerCase() + piece.name().charAt(0) + piece.name().substring(1).toLowerCase())
                .collect(Collectors.toSet());

        this.setPieceTypes(pieceTypes);

    }
}
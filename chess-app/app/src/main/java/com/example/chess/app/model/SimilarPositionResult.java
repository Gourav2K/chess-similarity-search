package com.example.chess.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarPositionResult {
    private Position position;
    private Game game;
    private Double similarityScore;
}

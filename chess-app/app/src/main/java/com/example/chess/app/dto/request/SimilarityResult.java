package com.example.chess.app.dto.request;

import com.example.chess.app.model.Game;
import com.example.chess.app.model.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Result object for position similarity search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityResult {
    private UUID positionId;
    private String gameId;
    private Integer moveNumber;
    private Double similarityScore;

    // Could include the full position data or just a link to it
    private Position position;
    private Game game;
}

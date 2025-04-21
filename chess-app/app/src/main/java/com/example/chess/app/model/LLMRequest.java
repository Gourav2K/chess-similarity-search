package com.example.chess.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequest {
    private List<GameSummaryRequest> positions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameSummaryRequest {
        private String gameId;
        private String fen;
        private String moves;
        private String side;
    }
}

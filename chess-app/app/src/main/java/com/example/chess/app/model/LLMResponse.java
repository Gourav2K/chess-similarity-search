package com.example.chess.app.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LLMResponse {
    private String aggregatedSummary;
    private List<PerGameSummary> perGameSummaries;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PerGameSummary {
        private String gameId;
        private String summary;
    }
}
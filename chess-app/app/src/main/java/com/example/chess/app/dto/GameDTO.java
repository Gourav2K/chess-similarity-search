package com.example.chess.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class GameDTO {
    private GameMetadataDTO gameMetadata;
    private List<PositionDTO> positions;
}


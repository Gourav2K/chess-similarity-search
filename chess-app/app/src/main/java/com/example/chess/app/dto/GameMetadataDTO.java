package com.example.chess.app.dto;

import lombok.Data;

@Data
public class GameMetadataDTO {
    private String gameId;
    private String result;
    private Integer whiteElo;
    private Integer blackElo;
    private String gameType;
    private String date;
    private String whiteName;
    private String blackName;
    private String eco;
    private String timeControl;
    private String opening;
    private String site;
}

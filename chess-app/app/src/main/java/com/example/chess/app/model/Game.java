package com.example.chess.app.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("games")
public class Game implements Persistable<String> {
    @Id
    private String id;
    private String result;
    private Integer whiteElo;
    private Integer blackElo;
    private String gameType;
    private String date;
    private String whiteName;
    private String blackName;
    private String eco;
    private String timeControl;
    private String site;
    private String opening;
    private String pgn;

    @Transient
    private Boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
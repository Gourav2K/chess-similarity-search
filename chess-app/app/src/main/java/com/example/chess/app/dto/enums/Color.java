package com.example.chess.app.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Color {
    WHITE,
    BLACK;

    @JsonCreator
    public static Color fromString(String value) {
        return Color.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}

package dev.tanguy.game.jeopardy.common.domain.model.id;

import java.util.Objects;

public record ClueId(String value) {
    public ClueId {
        Objects.requireNonNull(value, "ClueId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ClueId cannot be blank");
        }
    }

    public static ClueId of(String raw) {
        return new ClueId(raw);
    }
}
package dev.tanguy.game.jeopardy.common.domain.model.id;

import java.util.Objects;
import java.util.UUID;

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

    public static ClueId generate() {
        return new ClueId(UUID.randomUUID().toString());
    }
}
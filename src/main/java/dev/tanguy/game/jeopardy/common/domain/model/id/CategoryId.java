package dev.tanguy.game.jeopardy.common.domain.model.id;

import java.util.Objects;
import java.util.UUID;

public record CategoryId(String value) {
    public CategoryId {
        Objects.requireNonNull(value, "CategoryId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("CategoryId cannot be blank");
        }
    }

    public static CategoryId of(String raw) {
        return new CategoryId(raw);
    }

    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID().toString());
    }
}
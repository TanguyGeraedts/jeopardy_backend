package dev.tanguy.game.jeopardy.common.domain.model.id;

import java.util.Objects;
import java.util.UUID;

public record PlayerId(UUID value) {
    public PlayerId {
        Objects.requireNonNull(value, "PlayerId cannot be null");
    }

    public static PlayerId generate() {
        return new PlayerId(UUID.randomUUID());
    }

    public static PlayerId of(String raw) {
        return new PlayerId(UUID.fromString(raw));
    }
}
package dev.tanguy.game.jeopardy.common.domain.model.id;

import java.util.Objects;
import java.util.UUID;

public record GameSessionId(UUID value) {
    public GameSessionId {
        Objects.requireNonNull(value, "GameSessionId cannot be null");
    }

    public static GameSessionId generate() {
        return new GameSessionId(UUID.randomUUID());
    }

    public static GameSessionId of(String raw) {
        return new GameSessionId(UUID.fromString(raw));
    }
}
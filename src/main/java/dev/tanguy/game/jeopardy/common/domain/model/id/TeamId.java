package dev.tanguy.game.jeopardy.common.domain.model.id;

import java.util.Objects;
import java.util.UUID;

public record TeamId(UUID value) {
    public TeamId {
        Objects.requireNonNull(value, "TeamId cannot be null");
    }

    public static TeamId generate() {
        return new TeamId(UUID.randomUUID());
    }

    public static TeamId of(String raw) {
        return new TeamId(UUID.fromString(raw));
    }
}
package dev.tanguy.game.jeopardy.common.domain.model.id;

import java.util.Objects;
import java.util.UUID;

public record OwnerId(UUID value) {

    public OwnerId {
        Objects.requireNonNull(value, "OwnerId value cannot be null");
    }

    public static OwnerId generate() {
        return new OwnerId(UUID.randomUUID());
    }

    public static OwnerId of(String id) {
        return new OwnerId(UUID.fromString(id));
    }
}
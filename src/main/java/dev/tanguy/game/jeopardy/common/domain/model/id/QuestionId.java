package dev.tanguy.game.jeopardy.common.domain.model.id;

import java.util.Objects;
import java.util.UUID;

public record QuestionId(String value) {
    public QuestionId {
        Objects.requireNonNull(value, "QuestionId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("QuestionId cannot be blank");
        }
    }

    public static QuestionId of(String raw) {
        return new QuestionId(raw);
    }

    public static QuestionId generate() {
        return new QuestionId(UUID.randomUUID().toString());
    }
}
package dev.tanguy.game.jeopardy.common.domain.model.id;

import java.util.Objects;
import java.util.UUID;

public record QuizId(String value) {
    public QuizId {
        Objects.requireNonNull(value, "QuizId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("QuizId cannot be blank");
        }
    }

    public static QuizId of(String raw) {
        return new QuizId(raw);
    }

    public static QuizId generate() {
        return new QuizId(UUID.randomUUID().toString());
    }
}
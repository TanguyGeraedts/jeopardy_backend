package dev.tanguy.game.jeopardy.creator.port.in.category;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

import java.util.Objects;

public record AddCategoryCommand(
        QuizId quizId,
        OwnerId requesterId,
        String name
) {
    public AddCategoryCommand {
        Objects.requireNonNull(quizId, "quizId cannot be null");
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
    }
}
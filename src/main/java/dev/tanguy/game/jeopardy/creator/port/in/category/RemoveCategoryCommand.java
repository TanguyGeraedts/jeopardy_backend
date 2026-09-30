package dev.tanguy.game.jeopardy.creator.port.in.category;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

import java.util.Objects;

public record RemoveCategoryCommand(
        QuizId quizId,
        CategoryId categoryId,
        OwnerId requesterId
) {
    public RemoveCategoryCommand {
        Objects.requireNonNull(quizId, "quizId cannot be null");
        Objects.requireNonNull(categoryId, "categoryId cannot be null");
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
    }
}
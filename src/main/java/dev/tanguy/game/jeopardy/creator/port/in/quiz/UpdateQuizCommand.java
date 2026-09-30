package dev.tanguy.game.jeopardy.creator.port.in.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

import java.util.Objects;

public record UpdateQuizCommand(
        QuizId quizId,
        OwnerId requesterId,
        String name
) {
    public UpdateQuizCommand {
        Objects.requireNonNull(quizId, "quizId cannot be null");
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
    }
}
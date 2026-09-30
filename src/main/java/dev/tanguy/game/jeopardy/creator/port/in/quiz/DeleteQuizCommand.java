package dev.tanguy.game.jeopardy.creator.port.in.quiz;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

import java.util.Objects;

public record DeleteQuizCommand(
        QuizId quizId,
        OwnerId requesterId
) {
    public DeleteQuizCommand {
        Objects.requireNonNull(quizId, "quizId cannot be null");
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
    }
}
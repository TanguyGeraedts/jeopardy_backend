package dev.tanguy.game.jeopardy.creator.port.in.question;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

import java.util.Objects;

public record RemoveQuestionCommand(
        QuizId quizId,
        CategoryId categoryId,
        QuestionId questionId,
        OwnerId requesterId
) {
    public RemoveQuestionCommand {
        Objects.requireNonNull(quizId, "quizId cannot be null");
        Objects.requireNonNull(categoryId, "categoryId cannot be null");
        Objects.requireNonNull(questionId, "questionId cannot be null");
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
    }
}
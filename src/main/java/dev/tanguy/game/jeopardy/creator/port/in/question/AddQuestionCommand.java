package dev.tanguy.game.jeopardy.creator.port.in.question;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.domain.model.AnswerType;

import java.util.Objects;

public record AddQuestionCommand(
        QuizId quizId,
        CategoryId categoryId,
        OwnerId requesterId,
        int points,
        String questionText,
        String answerText,
        AnswerType answerType,
        String mediaUrl,
        boolean dailyDouble
) {
    public AddQuestionCommand {
        Objects.requireNonNull(quizId, "quizId cannot be null");
        Objects.requireNonNull(categoryId, "categoryId cannot be null");
        Objects.requireNonNull(requesterId, "requesterId cannot be null");
        Objects.requireNonNull(answerType, "answerType cannot be null");
    }
}
package dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper;

import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuestionRequest;
import dev.tanguy.game.jeopardy.creator.port.in.question.AddQuestionCommand;
import dev.tanguy.game.jeopardy.creator.port.in.question.RemoveQuestionCommand;
import dev.tanguy.game.jeopardy.creator.port.in.question.UpdateQuestionCommand;

import java.util.UUID;

public final class QuestionWebMapper {

    private QuestionWebMapper() {}

    public static AddQuestionCommand toAddCommand(
            UUID quizId, UUID categoryId, QuestionRequest request, OwnerId requesterId) {
        return new AddQuestionCommand(
                QuizId.of(quizId.toString()),
                CategoryId.of(categoryId.toString()),
                requesterId,
                request.points(), // validated @NotNull by @Valid
                request.questionText(),
                request.answerText(),
                request.answerType(),
                request.mediaUrl(),
                Boolean.TRUE.equals(request.dailyDouble()));
    }

    public static UpdateQuestionCommand toUpdateCommand(
            UUID quizId, UUID categoryId, UUID questionId, QuestionRequest request, OwnerId requesterId) {
        return new UpdateQuestionCommand(
                QuizId.of(quizId.toString()),
                CategoryId.of(categoryId.toString()),
                QuestionId.of(questionId.toString()),
                requesterId,
                request.points(), // validated @NotNull by @Valid
                request.questionText(),
                request.answerText(),
                request.answerType(),
                request.mediaUrl(),
                Boolean.TRUE.equals(request.dailyDouble()));
    }

    public static RemoveQuestionCommand toRemoveCommand(
            UUID quizId, UUID categoryId, UUID questionId, OwnerId requesterId) {
        return new RemoveQuestionCommand(
                QuizId.of(quizId.toString()),
                CategoryId.of(categoryId.toString()),
                QuestionId.of(questionId.toString()),
                requesterId);
    }
}
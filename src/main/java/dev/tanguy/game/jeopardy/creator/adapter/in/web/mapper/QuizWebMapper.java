package dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.CreateQuizRequest;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse.CategoryResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse.QuestionResponse;
import dev.tanguy.game.jeopardy.creator.domain.model.Category;
import dev.tanguy.game.jeopardy.creator.domain.model.Question;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.CreateQuizCommand;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizQuery;

import java.util.UUID;

public final class QuizWebMapper {

    private QuizWebMapper() {}

    public static CreateQuizCommand toCommand(CreateQuizRequest request, OwnerId ownerId) {
        return new CreateQuizCommand(ownerId, request.name());
    }

    public static GetQuizQuery toQuery(UUID quizId, OwnerId requesterId) {
        return new GetQuizQuery(QuizId.of(quizId.toString()), requesterId);
    }

    public static QuizResponse toResponse(Quiz quiz) {
        return new QuizResponse(
                UUID.fromString(quiz.getId().value()),
                quiz.getName(),
                quiz.getCategories().stream().map(QuizWebMapper::toCategoryResponse).toList()
        );
    }

    private static CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                UUID.fromString(category.getId().value()),
                category.getName(),
                category.getQuestions().stream().map(QuizWebMapper::toQuestionResponse).toList()
        );
    }

    private static QuestionResponse toQuestionResponse(Question question) {
        return new QuestionResponse(
                UUID.fromString(question.getId().value()),
                question.getPoints(),
                question.getQuestionText(),
                question.getAnswerText(),
                question.getAnswerType().name(),
                question.getMediaUrl(),
                question.isDailyDouble()
        );
    }
}
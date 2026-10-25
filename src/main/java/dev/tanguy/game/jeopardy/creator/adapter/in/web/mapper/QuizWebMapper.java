package dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.CreateQuizRequest;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.DiscoverQuizResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.DiscoverQuizResponse.CategoryShape;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse.CategoryResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse.QuestionResponse;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.UpdateQuizRequest;
import dev.tanguy.game.jeopardy.creator.domain.model.Category;
import dev.tanguy.game.jeopardy.creator.domain.model.Question;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.CreateQuizCommand;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.DeleteQuizCommand;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizQuery;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.GetQuizzesByOwnerQuery;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.UpdateQuizCommand;

import java.util.List;
import java.util.UUID;

public final class QuizWebMapper {

    private QuizWebMapper() {}

    public static CreateQuizCommand toCommand(CreateQuizRequest request, OwnerId ownerId) {
        return new CreateQuizCommand(ownerId, request.name());
    }

    public static UpdateQuizCommand toCommand(UUID quizId, UpdateQuizRequest request, OwnerId requesterId) {
        return new UpdateQuizCommand(QuizId.of(quizId.toString()), requesterId, request.name());
    }

    public static DeleteQuizCommand toDeleteCommand(UUID quizId, OwnerId requesterId) {
        return new DeleteQuizCommand(QuizId.of(quizId.toString()), requesterId);
    }

    public static GetQuizQuery toQuery(UUID quizId, OwnerId requesterId) {
        return new GetQuizQuery(QuizId.of(quizId.toString()), requesterId);
    }

    public static GetQuizzesByOwnerQuery toQuery(OwnerId ownerId) {
        return new GetQuizzesByOwnerQuery(ownerId);
    }

    public static QuizResponse toResponse(Quiz quiz) {
        return new QuizResponse(
                UUID.fromString(quiz.getId().value()),
                quiz.getName(),
                quiz.getCategories().stream().map(QuizWebMapper::toCategoryResponse).toList(),
                quiz.isPublished(),
                quiz.getPublishedAt(),
                quiz.getAuthorName(),
                topicNames(quiz),
                quiz.getTimesPlayed(),
                quiz.getUpdatedAt()
        );
    }

    /** Board shape only: no questions or answers. */
    public static DiscoverQuizResponse toDiscoverResponse(Quiz quiz, OwnerId requesterId) {
        return new DiscoverQuizResponse(
                UUID.fromString(quiz.getId().value()),
                quiz.getName(),
                quiz.getAuthorName(),
                topicNames(quiz),
                quiz.getTimesPlayed(),
                quiz.getPublishedAt(),
                quiz.isPublished(),
                quiz.getOwnerId().equals(requesterId),
                quiz.questionCount(),
                quiz.getCategories().stream()
                        .map(c -> new CategoryShape(c.getName(), c.getQuestions().size()))
                        .toList()
        );
    }

    /** In the enum's order, so the list is stable between requests. */
    private static List<String> topicNames(Quiz quiz) {
        return quiz.getTopics().stream().sorted().map(Enum::name).toList();
    }

    public static CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                UUID.fromString(category.getId().value()),
                category.getName(),
                category.getQuestions().stream().map(QuizWebMapper::toQuestionResponse).toList()
        );
    }

    public static QuestionResponse toQuestionResponse(Question question) {
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
package dev.tanguy.game.jeopardy.creator.adapter.in.web.mapper;

import dev.tanguy.game.jeopardy.common.domain.model.id.OwnerId;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.CreateQuizRequest;
import dev.tanguy.game.jeopardy.creator.adapter.in.web.dto.QuizResponse;
import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;
import dev.tanguy.game.jeopardy.creator.port.in.quiz.CreateQuizCommand;

import java.util.UUID;

public final class QuizWebMapper {

    private QuizWebMapper() {}

    public static CreateQuizCommand toCommand(CreateQuizRequest request, OwnerId ownerId) {
        return new CreateQuizCommand(ownerId, request.name());
    }

    public static QuizResponse toResponse(Quiz quiz) {
        return new QuizResponse(UUID.fromString(quiz.getId().value()), quiz.getName());
    }
}
package dev.tanguy.game.jeopardy.creator.port.in.quiz;

import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;

import java.util.List;

public interface GetQuizzesByOwnerUseCase {
    List<Quiz> getMyQuizzes(GetQuizzesByOwnerQuery query);
}

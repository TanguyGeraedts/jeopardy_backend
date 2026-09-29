package dev.tanguy.game.jeopardy.creator.port.in.quiz;

import dev.tanguy.game.jeopardy.creator.domain.model.Quiz;

public interface GetQuizUseCase {

    Quiz getQuiz(GetQuizQuery query);
}
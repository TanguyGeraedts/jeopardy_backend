package dev.tanguy.game.jeopardy.creator.api;

import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

/** For other modules: a game was hosted with this quiz (counts towards "times played" and Discover's Popular). */
public interface QuizPlayRecorder {

    void recordPlay(QuizId quizId);
}

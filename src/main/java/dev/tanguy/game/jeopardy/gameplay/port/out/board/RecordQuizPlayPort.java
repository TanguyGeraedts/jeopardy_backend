package dev.tanguy.game.jeopardy.gameplay.port.out.board;

import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

public interface RecordQuizPlayPort {

    /** A game session was created from this quiz. Best effort: never fails the game. */
    void recordPlay(QuizId quizId);
}

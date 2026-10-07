package dev.tanguy.game.jeopardy.gameplay.domain.exception.session;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

public class EmptyBoardException extends DomainConflictException {

    public EmptyBoardException(QuizId quizId) {
        super("Quiz " + quizId.value() + " has no questions, add at least one before starting a game.");
    }
}
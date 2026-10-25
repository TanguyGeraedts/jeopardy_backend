package dev.tanguy.game.jeopardy.creator.domain.event.quiz;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

public class TooManyTopicsException extends DomainConflictException {

    public TooManyTopicsException(QuizId quizId, int max) {
        super("Quiz with ID " + quizId + " can have at most " + max + " topics.");
    }
}

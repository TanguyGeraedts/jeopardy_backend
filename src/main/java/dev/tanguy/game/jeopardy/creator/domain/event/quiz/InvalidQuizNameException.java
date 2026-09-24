package dev.tanguy.game.jeopardy.creator.domain.event.quiz;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

public class InvalidQuizNameException extends DomainConflictException {

    public InvalidQuizNameException(QuizId quizId) {
        super("Quiz with ID " + quizId + " must have a non-blank name.");
    }

    public InvalidQuizNameException(QuizId quizId, String name) {
        super("Quiz with ID " + quizId + " has an invalid name: '" + name + "'");
    }
}
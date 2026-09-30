package dev.tanguy.game.jeopardy.creator.domain.event.quiz;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

public class DuplicateCategoryNameException extends DomainConflictException {

    public DuplicateCategoryNameException(QuizId quizId, String name) {
        super("Quiz with ID " + quizId + " already contains a category named '" + name + "'.");
    }
}
package dev.tanguy.game.jeopardy.creator.domain.event.quiz;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuizId;

public class QuizNotPublishableException extends DomainConflictException {

    public QuizNotPublishableException(QuizId quizId, String reason) {
        super("Quiz with ID " + quizId + " can't be published: " + reason);
    }
}

package dev.tanguy.game.jeopardy.creator.domain.event.question;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;

public class InvalidPointsException extends DomainConflictException {

    public InvalidPointsException(QuestionId questionId, int points) {
        super("Question with ID " + questionId + " has invalid points: " + points);
    }

    public InvalidPointsException(QuestionId questionId) {
        super("Question with ID " + questionId + " has invalid points.");
    }
}
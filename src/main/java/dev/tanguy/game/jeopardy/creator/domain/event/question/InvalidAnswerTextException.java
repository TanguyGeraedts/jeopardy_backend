package dev.tanguy.game.jeopardy.creator.domain.event.question;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;

public class InvalidAnswerTextException extends DomainConflictException {

    public InvalidAnswerTextException(QuestionId questionId) {
        super("Question with ID " + questionId + " must have a non-blank answer text.");
    }

    public InvalidAnswerTextException(QuestionId questionId, String text) {
        super("Question with ID " + questionId + " has invalid answer text: '" + text + "'");
    }
}
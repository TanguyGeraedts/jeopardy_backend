package dev.tanguy.game.jeopardy.creator.domain.event.question;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;

public class InvalidQuestionTextException extends DomainConflictException {

    public InvalidQuestionTextException(QuestionId questionId) {
        super("Question with ID " + questionId + " must have a non-blank question text.");
    }

    public InvalidQuestionTextException(QuestionId questionId, String text) {
        super("Question with ID " + questionId + " has invalid text: '" + text + "'");
    }
}
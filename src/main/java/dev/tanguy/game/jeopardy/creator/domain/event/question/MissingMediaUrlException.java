package dev.tanguy.game.jeopardy.creator.domain.event.question;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;
import dev.tanguy.game.jeopardy.creator.domain.model.AnswerType;

public class MissingMediaUrlException extends DomainConflictException {

    public MissingMediaUrlException(QuestionId questionId, AnswerType answerType) {
        super("Question with ID " + questionId + " requires a media URL for answer type: " + answerType);
    }

    public MissingMediaUrlException(QuestionId questionId) {
        super("Question with ID " + questionId + " requires a valid media URL.");
    }
}
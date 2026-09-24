package dev.tanguy.game.jeopardy.creator.domain.event.category;

import dev.tanguy.game.jeopardy.common.domain.exception.DomainConflictException;
import dev.tanguy.game.jeopardy.common.domain.model.id.CategoryId;
import dev.tanguy.game.jeopardy.common.domain.model.id.QuestionId;

public class QuestionNotFoundException extends DomainConflictException {

    public QuestionNotFoundException(CategoryId categoryId, QuestionId questionId) {
        super("Question with ID " + questionId + " was not found in Category " + categoryId);
    }
}